/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NoCostArgument;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.CostCancellableTask;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.services.WarmupManager;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.source.LocatedSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * DO NOT IMPLEMENT THIS DIRECTLY.
 */
public abstract class AbstractCommand<T extends CommandSource> implements CommandExecutor {

    private final boolean isAsync = this.getClass().getAnnotation(RunAsync.class) != null;

    // A period separated list of parent commands, starting with the root. Period terminateed.
    final String commandPath;

    // Null until set, then should be considered immutable.
    private Set<Class<? extends AbstractCommand>> moduleCommands = null;

    private final Map<UUID, Instant> cooldownStore = Maps.newHashMap();
    protected CommandPermissionHandler permissions;
    protected String[] aliases;
    private final Class<T> sourceType;
    private boolean bypassWarmup;
    private boolean generateWarmupAnyway;
    private boolean bypassCooldown;
    private boolean bypassCost;
    private String configSection;
    private boolean generateDefaults;
    private CommandSpec cs = null;
    private final List<String> afkArgs = Lists.newArrayList();

    @Inject protected Nucleus plugin;
    @Inject private CoreConfigAdapter cca;
    @Inject private WarmupManager warmupService;

    @SuppressWarnings("all")
    private Optional<AFKHandler> afkHandler = null;

    @SuppressWarnings("all")
    private Optional<AFKConfigAdapter> aca = null;

    @SuppressWarnings("unchecked")
    AbstractCommand() {
        // I hate type erasure - it leads to a hack like this. Admittedly, I
        // could've just created a subclass that does
        // the same thing, but I like to beat the system! :)
        //
        // This code reflectively looks for methods called "executeCommand",
        // which is defined in this class. However,
        // due to type erasure, if a generic type is specified, there are two
        // "executeCommand" methods, one that satisfies
        // this abstract class (with the CommandSource argument) and a second
        // method that fulfils the generic type T.
        //
        // Thus, we need to check that there is a method called executeCommand
        // that has a more restrictive argument than
        // "CommandSource" in order to check for the generic type.
        //
        // This allows us to then have code that filters out non-Players by
        // simply specifying the generic type "Player".
        //
        // The provided stream filters out the standard executeCommand method,
        // and checks to see if there is a second that makes
        // use of the generic parameter.
        Optional<Method> me = Arrays.asList(getClass().getMethods()).stream()
                .filter(x -> x.getName().equals("executeCommand") && x.getParameterTypes().length == 2
                        && x.getParameterTypes()[1].isAssignableFrom(CommandContext.class) && !x.getParameterTypes()[0].equals(CommandSource.class))
                .findFirst();

        // If there is a second executeCommand method, then we know that's the
        // type that we need and we can do our source
        // checks against it accordingly.
        if (me.isPresent()) {
            sourceType = (Class<T>) (me.get().getParameterTypes()[0]);
        } else {
            sourceType = (Class<T>) CommandSource.class;
        }

        this.commandPath = getSubcommandOf();
    }

    private String getSubcommandOf() {
        StringBuilder sb = new StringBuilder();
        getSubcommandOf(this.getClass(), sb);
        return sb.toString();
    }

    private void getSubcommandOf(Class<? extends AbstractCommand> c, StringBuilder sb) {
        // Get subcommand alias, if any.
        RegisterCommand rc = c.getAnnotation(RegisterCommand.class);
        if (!Modifier.isAbstract( rc.subcommandOf().getModifiers()) && rc.subcommandOf() != this.getClass()) {
            getSubcommandOf(rc.subcommandOf(), sb);
        }

        sb.append(rc.value()[0]).append(".");
    }

    public final void setModuleCommands(Set<Class<? extends AbstractCommand>> moduleCommands) {
        Preconditions.checkState(this.moduleCommands == null);
        this.moduleCommands = moduleCommands;
    }

    public final void postInit() {
        // Checks.
        Preconditions.checkNotNull(this.getAliases());
        Preconditions.checkArgument(this.getAliases().length > 0);

        // For these flags, we simply need to get whether the annotation was
        // declared. If they were not, we simply get back
        // a null - so the check is based around that.
        NoWarmup w = this.getClass().getAnnotation(NoWarmup.class);
        bypassWarmup = w != null;
        generateWarmupAnyway = !bypassWarmup || w.generateConfigEntry();

        bypassCooldown = this.getClass().getAnnotation(NoCooldown.class) != null;
        bypassCost = this.getClass().getAnnotation(NoCost.class) != null;

        // The Permissions annotation provides the backbone of the permissions
        // system for the commands.
        // The standard permission is based on the getAliases command,
        // specifically, the first argument in the
        // returned array, such that the permission it will generated if this
        // annotation is defined is:
        //
        // quickstart.(primaryalias).base
        //
        // Adding a "root" and/or "sub" string will generate:
        //
        // quickstart.(root).(primaryalias).(sub).base
        //
        // For warmup, cooldown and cost exemption, replace base with:
        //
        // exempt.(cooldown|warmup|cost)
        //
        // By default, the permission "quickstart.admin" also gets permission to
        // run and bypass all warmups,
        // cooldowns and costs, but this can be turned off in the annotation.
        permissions = new CommandPermissionHandler(this, plugin);

        ConfigCommandAlias cca = this.getClass().getAnnotation(ConfigCommandAlias.class);
        configSection = cca == null ? getAliases()[0].toLowerCase() : cca.value().toLowerCase();
        generateDefaults = cca == null || cca.generate();

        NotifyIfAFK n = this.getClass().getAnnotation(NotifyIfAFK.class);
        if (n != null) {
            this.afkArgs.addAll(Arrays.asList(n.value()));
        }

        permissionsToRegister().forEach((k, v) -> permissions.registerPermssion(k, v));
        permissionSuffixesToRegister().forEach((k, v) -> permissions.registerPermssionSuffix(k, v));
    }

    // Abstract functions - for implementation.

    /**
     * Returns a {@link CommandSpec} that allows this command to be registered.
     *
     * @return The {@link CommandSpec}
     */
    public abstract CommandSpec createSpec();

    /**
     * Gets the aliases for the command. The first alias will be the primary
     * alias within Nucleus.
     *
     * @return An array of aliases.
     */
    public String[] getAliases() {
        if (aliases == null) {
            RegisterCommand rc = getClass().getAnnotation(RegisterCommand.class);
            if (rc != null) {
                aliases = rc.value();
            }
        }

        return Arrays.copyOf(aliases, aliases.length);
    }

    /**
     * Functionally similar to
     * {@link CommandExecutor#execute(CommandSource, CommandContext)}, this
     * contains logic that actually executes the command.
     *
     * <p> Note that the {@link CommandResult} is important here. A success is
     * treated differently to a non-success! </p>
     *
     * @param src The executor of the command.
     * @param args The arguments for the command.
     * @return The {@link CommandResult}
     * @throws Exception If thrown, {@link TextMessageException#getText()} or
     *         {@link Exception#getMessage()} will be sent to the user.
     */
    public abstract CommandResult executeCommand(T src, CommandContext args) throws Exception;

    // -------------------------------------
    // Metadata
    // -------------------------------------

    /**
     * Contains extra permission suffixes that are to be registered in the
     * permission service.
     *
     * @return A map containing the extra permission suffixes to register.
     */
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return Maps.newHashMap();
    }

    /**
     * Contains extra permissions that are to be registered in the permission
     * service.
     *
     * @return A map containing the extra permissions to register.
     */
    protected Map<String, PermissionInformation> permissionsToRegister() {
        return Maps.newHashMap();
    }

    public final CommandPermissionHandler getPermissionHandler() {
        return permissions;
    }

    public String getCommandConfigAlias() {
        if (configSection == null) {
            return getAliases()[0];
        }

        return configSection;
    }

    public CommandSpec getSpec() {
        if (cs != null) {
            return cs;
        }

        try {
            return createSpec();
        } catch (Exception e) {
            return null;
        }
    }

    public final boolean mergeDefaults() {
        return generateDefaults;
    }

    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode n = SimpleCommentedConfigurationNode.root();
        n.getNode("enabled").setComment(Util.getMessageWithFormat("config.enabled")).setValue(true);

        if (!bypassCooldown) {
            n.getNode("cooldown").setComment(Util.getMessageWithFormat("config.cooldown")).setValue(0);
        }

        if (!bypassWarmup || generateWarmupAnyway) {
            n.getNode("warmup").setComment(Util.getMessageWithFormat("config.warmup")).setValue(0);
        }

        if (!bypassCost) {
            n.getNode("cost").setComment(Util.getMessageWithFormat("config.cost")).setValue(0);
        }

        return n;
    }

    // -------------------------------------
    // Command Execution
    // -------------------------------------

    /**
     * Runs any checks that need to occur before the warmup, cooldown and cost
     * checks.
     *
     * @param source The source of the command.
     * @param args The arguments.
     * @return Whether to continue or not.
     *
     * @throws Exception Thrown if there is a problem that means the command
     *         cannot continue.
     */
    protected ContinueMode preProcessChecks(T source, CommandContext args) throws Exception {
        return ContinueMode.CONTINUE;
    }

    @Override
    @NonnullByDefault
    public final CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        // If they don't match ANY permission, throw 'em.
        // This should have been done if the standard base has been used, but in case it hasn't, here it is!
        if (!permissions.testBase(source)) {
            throw new CommandPermissionException();
        }

        // If the implementing class has defined a generic parameter, then check
        // the source type.
        if (!checkSourceType(source)) {
            return CommandResult.empty();
        }

        // Cast as required.
        @SuppressWarnings("unchecked")
        T src = (T) source;

        try {
            ContinueMode mode = preProcessChecks(src, args);
            if (!mode.cont) {
                return mode.returnType;
            }
        } catch (Exception e) {
            // If it doesn't, just tell the user something went wrong.
            src.sendMessage(Text.builder().append(Util.getTextMessageWithFormat("command.error")).build());

            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            return CommandResult.empty();
        }

        if (src instanceof Player) {
            ContinueMode cm = runChecks((Player) src, args);

            if (!cm.cont) {
                return cm.returnType;
            }
        }

        // If we're running async...
        if (isAsync) {
            // Create an executor that runs the command async.
            plugin.getLogger().debug("Running " + this.getClass().getName() + " in async mode.");
            Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> startExecute(src, args));

            // Tell Sponge we're done.
            return CommandResult.success();
        }

        // Run the command sync.
        return startExecute(src, args);
    }

    private CommandResult startExecute(T src, CommandContext args) {
        CommandResult cr;
        try {
            checkAfk(src, args);

            // Execute the command in the specific executor.
            cr = executeCommand(src, args);
        } catch (TextMessageException e) {
            // If the exception contains a text object, render it like so...
            Text t = e.getText();
            src.sendMessage((t == null) ? Util.getTextMessageWithFormat("command.error") : t);

            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            cr = CommandResult.empty();
        } catch (Exception e) {
            // If it doesn't, just tell the user something went wrong.
            src.sendMessage(Util.getTextMessageWithFormat("command.error"));

            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            cr = CommandResult.empty();
        }

        if (src instanceof Player) {
            // If the player is subject to cooling down, apply the cooldown.
            final Player p = (Player) src;

            if (cr.getSuccessCount().orElse(0) > 0) {
                setCooldown(p);
            } else {
                // For the tests, keep this here so we can skip the hard to test
                // code below.
                final double cost = getCost(p, args);
                if (cost > 0) {
                    Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> plugin.getEconHelper().depositInPlayer(p, cost));
                }
            }
        }

        return cr;
    }

    // -------------------------------------
    // Source Type
    // -------------------------------------
    private boolean checkSourceType(CommandSource source) {
        if (sourceType.equals(Player.class) && !(source instanceof Player)) {
            source.sendMessage(Util.getTextMessageWithFormat("command.playeronly"));
            return false;
        } else if (sourceType.equals(ConsoleSource.class) && !(source instanceof Player)) {
            source.sendMessage(Util.getTextMessageWithFormat("command.consoleonly"));
            return false;
        } else if (sourceType.equals(CommandBlockSource.class) && !(source instanceof CommandBlockSource)) {
            source.sendMessage(Util.getTextMessageWithFormat("command.commandblockonly"));
            return false;
        }

        return true;
    }

    // -------------------------------------
    // Player Checks
    // -------------------------------------
    private ContinueMode runChecks(Player src, CommandContext args) {
        // Cooldown, cost, warmup.
        ContinueMode m = checkCooldown(src);
        if (!m.cont) {
            return m;
        }

        m = applyCost(src, args);
        if (!m.cont) {
            return m;
        }

        return setupWarmup(src, args);
    }

    /**
     * Gets the world properties from the specified argument.
     *
     * @param src The {@link CommandSource} executing the command.
     * @param argument The key of the argument to get the world properties from.
     * @param args The {@link CommandContext} with the data
     * @return An {@link Optional} containing the {@link WorldProperties}, if appropriate.
     */
    protected final Optional<WorldProperties> getWorldProperties(CommandSource src, String argument, CommandContext args) {
        Optional<WorldProperties> pr = args.getOne(argument);
        if (pr.isPresent()) {
            return pr;
        }

        // Actually, we just care about where we are.
        if (src instanceof LocatedSource) {
            return Optional.of(((LocatedSource) src).getWorld().getProperties());
        }

        return Optional.empty();
    }

    protected final WorldProperties getWorldPropertiesOrDefault(CommandSource src, String argument, CommandContext args) {
        Optional<WorldProperties> pr = getWorldProperties(src, argument, args);
        if (pr.isPresent()) {
            return pr.get();
        }

        src.sendMessage(Util.getTextMessageWithFormat("args.worldproperties.default"));
        return Sponge.getServer().getDefaultWorld().get();
    }

    protected <U extends User> Optional<U> getUser(Class<U> clazz, CommandSource src, String argument, CommandContext args) {
        Optional<U> opl = args.getOne(argument);
        U pl;
        if (opl.isPresent()) {
            pl = opl.get();
        } else if (clazz.isInstance(src)) {
            pl = clazz.cast(src);
        } else {
            src.sendMessage(Util.getTextMessageWithFormat("command.playeronly"));
            return Optional.empty();
        }

        return Optional.of(pl);
    }

    // -------------------------------------
    // Warmups
    // -------------------------------------
    protected int getWarmup(final Player src) {
        if (permissions.testWarmupExempt(src)) {
            return 0;
        }

        // Get the warmup time.
        return plugin.getCommandsConfig().getCommandNode(configSection).getNode("warmup").getInt();
    }

    @SuppressWarnings("unchecked")
    private ContinueMode setupWarmup(final Player src, CommandContext args) {
        if (bypassWarmup) {
            return ContinueMode.CONTINUE;
        }

        // Get the warmup time.
        int warmupTime = getWarmup(src);
        if (warmupTime <= 0) {
            return ContinueMode.CONTINUE;
        }

        // We create a task that executes the command at a later time. Because
        // we already know we have permission,
        // we can skip those checks.
        Task.Builder tb = Sponge.getScheduler().createTaskBuilder().delay(warmupTime, TimeUnit.SECONDS)
                .execute(new CostCancellableTask(plugin, src, getCost(src, args)) {

                    @Override
                    public void accept(Task task) {
                        src.sendMessage(Util.getTextMessageWithFormat("warmup.end"));
                        warmupService.removeWarmup(src.getUniqueId());
                        startExecute((T) src, args);
                    }
                }).name("Command Warmup - " + src.getName());

        // Run an async command async, of course!
        if (isAsync) {
            tb.async();
        }

        // Add the warmup to the service so we can cancel it if we need to.
        warmupService.addWarmup(src.getUniqueId(), tb.submit(plugin));

        // Tell the user we're warming up.
        src.sendMessage(Util.getTextMessageWithFormat("warmup.start", Util.getTimeStringFromSeconds(warmupTime)));

        // Sponge should think the command was run successfully.
        return ContinueMode.STOP_SUCCESS;
    }

    // -------------------------------------
    // Cooldowns
    // -------------------------------------
    private ContinueMode checkCooldown(Player src) {
        // Remove any expired cooldowns.
        cleanCooldowns();

        // If they are still in there, then tell them they are still cooling
        // down.
        if (!bypassCooldown && !permissions.testCooldownExempt(src) && cooldownStore.containsKey(src.getUniqueId())) {
            Instant l = cooldownStore.get(src.getUniqueId());
            src.sendMessage(Util.getTextMessageWithFormat("cooldown.message",
                    Util.getTimeStringFromSeconds(l.until(Instant.now(), ChronoUnit.SECONDS))));
            return ContinueMode.STOP;
        }

        return ContinueMode.CONTINUE;
    }

    private void setCooldown(Player src) {
        if (!permissions.testCooldownExempt(src)) {
            // Get the cooldown time.
            int cooldownTime = plugin.getCommandsConfig().getCommandNode(configSection).getNode("cooldown").getInt();
            if (cooldownTime > 0) {
                // If there is a cooldown, add the cooldown to the list, with
                // the end time as an Instant.
                cooldownStore.put(src.getUniqueId(), Instant.now().plus(cooldownTime, ChronoUnit.SECONDS));
            }
        }
    }

    private void cleanCooldowns() {
        // If the cooldown end is in the past, remove it.
        cooldownStore.entrySet().removeIf(k -> k.getValue().isBefore(Instant.now()));
    }

    // -------------------------------------
    // Costs
    // -------------------------------------
    /**
     * Applies a cost to the user, if required.
     *
     * @param src The {@link CommandSource}
     * @param args The {@link CommandContext}
     * @return Whether to continue with the command.
     */
    private ContinueMode applyCost(Player src, CommandContext args) {
        double cost = getCost(src, args);
        if (cost == 0.) {
            return ContinueMode.CONTINUE;
        }

        if (!plugin.getEconHelper().withdrawFromPlayer(src, cost)) {
            return ContinueMode.STOP;
        }

        return ContinueMode.CONTINUE;
    }

    /**
     * Gets the cost for this command, or zero if the player does not have to
     * pay.
     *
     * @param src The {@link CommandSource}
     * @param args The {@link CommandContext}
     * @return The cost.
     */
    protected double getCost(Player src, @Nullable CommandContext args) {
        boolean noCost = args != null && !args.<Boolean>getOne(NoCostArgument.NO_COST_ARGUMENT).orElse(false);

        // If the player or command itself is exempt, return a zero.
        if (bypassCost || noCost || permissions.testCostExempt(src)) {
            return 0.;
        }

        // Return the cost if positive, else, zero.
        double cost = plugin.getCommandsConfig().getCommandNode(configSection).getNode("cost").getDouble(0.);
        if (cost <= 0.) {
            return 0.;
        }

        return cost;
    }

    // -------------------------------------
    // AFK
    // -------------------------------------
    protected boolean isAfk(Player player) {
        if (afkHandler == null) {
            afkHandler = plugin.getInternalServiceManager().getService(AFKHandler.class);
        }

        return afkHandler.isPresent() && afkHandler.get().getAFKData(player).isAFK();
    }

    protected boolean alertOnAfk() {
        try {
            getAfkConfigAdapter();
        } catch (NoModuleException | IncorrectAdapterTypeException e) {
            return false;
        }

        return aca.isPresent() && aca.get().getNodeOrDefault().isAlertSenderOnAfk();
    }

    private void checkAfk(T src, CommandContext args) {
        try {
            // AFK checks can be done async and at the time.
            if (!afkArgs.isEmpty() && alertOnAfk()) {
                afkArgs.forEach(s ->
                        args.getAll(s).stream()
                                .filter(x -> Player.class.isAssignableFrom(x.getClass())).map(x -> (Player) x).filter(this::isAfk)
                                .forEach(p -> sendAfkMessage(src, p)));
            }
        } catch (Exception e) {
            // Swallow!
        }
    }

    private void getAfkConfigAdapter() throws NoModuleException, IncorrectAdapterTypeException {
        if (aca == null) {
            aca = Optional.of(plugin.getModuleContainer().getConfigAdapterForModule("afk", AFKConfigAdapter.class));
        }
    }

    protected void sendAfkMessage(CommandSource src, Player player) {
        try {
            getAfkConfigAdapter();
        } catch (NoModuleException | IncorrectAdapterTypeException e) {
            return;
        }

        aca.ifPresent(a -> {
            String onCommand = a.getNodeOrDefault().getMessages().getOnCommand();
            if (!onCommand.trim().isEmpty()) {
                src.sendMessage(plugin.getChatUtil().getPlayerMessageFromTemplate(onCommand, player, true));
            }
        });
    }

    // -------------------------------------
    // Child Commands
    // -------------------------------------
    final Map<List<String>, CommandCallable> createChildCommands() {
        if (this.moduleCommands == null) {
            return Maps.newHashMap();
        }

        Set<Class<? extends AbstractCommand>> scb = moduleCommands.stream().filter(x -> {
                        RegisterCommand r = x.getAnnotation(RegisterCommand.class);
                        // Only commands that are subcommands of this.
                        return r != null && r.subcommandOf().equals(this.getClass());
                    }).collect(Collectors.toSet());

        if (scb.isEmpty()) {
            return Maps.newHashMap();
        }

        return createChildCommands(scb);
    }

    private Map<List<String>, CommandCallable> createChildCommands(Collection<Class<? extends AbstractCommand>> bases) {
        if (this.moduleCommands == null) {
            return Maps.newHashMap();
        }

        Map<List<String>, CommandCallable> map = Maps.newHashMap();
        final Set<Class<? extends AbstractCommand>> s = this.moduleCommands;

        bases.forEach(cb -> {
            AbstractCommand c = null;
            try {
                c = plugin.getInjector().getInstance(cb);
                c.setModuleCommands(s);
                c.postInit();
                map.put(Arrays.asList(c.getAliases()), c.createSpec());
            } catch (Exception e) {
                plugin.getLogger().error(Util.getMessageWithFormat("command.child.notloaded", cb.getName()));

                if (cca.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }
            }
        });

        return map;
    }

    protected enum ContinueMode {
        /**
         * Continue executing the command.
         */
        CONTINUE(true, null),

        /**
         * Stop executing, but mark as success.
         */
        STOP_SUCCESS(false, CommandResult.success()),

        /**
         * Stop executing, mark as empty.
         */
        STOP(false, CommandResult.empty());

        final boolean cont;
        final CommandResult returnType;

        ContinueMode(boolean cont, CommandResult returnType) {
            this.cont = cont;
            this.returnType = returnType;
        }
    }
}
