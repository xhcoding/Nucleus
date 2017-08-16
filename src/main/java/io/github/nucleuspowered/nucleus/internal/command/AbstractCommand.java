/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoModifiersArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.util.NucleusProcessing;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.CostCancellableTask;
import io.github.nucleuspowered.nucleus.internal.TimingsDummy;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresEconomy;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RedirectModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCommandPrefix;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoHelpSubcommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoTimings;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.modules.core.config.WarmupConfig;
import io.github.nucleuspowered.nucleus.util.Action;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The basis for any command.
 */
@NonnullByDefault
public abstract class AbstractCommand<T extends CommandSource> implements CommandCallable {

    /**
     * An argument key to denote that the current operation is for a tab completion and
     * so the argument should react accordingly. This is specifically useful for an
     * argument that will accept partial completion of names.
     */
    public static final String COMPLETION_ARG = "comp";
    private static final InputTokenizer tokeniser = InputTokenizer.quotedStrings(false);

    public static final String BYPASS_COOLDOWN = "nucleus:bypass_cooldown";

    private final boolean isAsync = this.getClass().getAnnotation(RunAsync.class) != null;

    private Timing commandTimings = TimingsDummy.DUMMY;
    // A period separated list of parent commands, starting with the prefix. Period terminated.
    private final String commandPath;

    // Null until set, then should be considered immutable.
    @Nullable private Set<Class<? extends AbstractCommand<?>>> moduleCommands = null;

    private final Map<UUID, Instant> cooldownStore = Maps.newHashMap();

    protected final CommandPermissionHandler permissions;
    protected final String[] aliases;
    private final String[] forcedAliases;
    private final Class<T> sourceType;
    private final boolean bypassWarmup;
    private final boolean generateWarmupAnyway;
    private final boolean bypassCooldown;
    private final boolean bypassCost;
    private final boolean requiresEconomy;
    private final String configSection;
    private final boolean isRoot;

    private CommandElement argumentParser = GenericArguments.none();

    private final SimpleDispatcher dispatcher = new SimpleDispatcher(SimpleDispatcher.FIRST_DISAMBIGUATOR);

    private final UsageCommand usageCommand = new UsageCommand();

    protected final Nucleus plugin;
    private final boolean hasExecutor;

    @Nullable private CommandBuilder builder;
    @Nullable private String module;
    @Nullable private String moduleId;

    private final String warmupKey;
    private final String cooldownKey;
    private final String costKey;

    private final Predicate<CommandSource> sourceTypePredicate;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @Nullable private Optional<Text> desc;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @Nullable private Optional<Text> extended;

    @SuppressWarnings("unchecked")
    public AbstractCommand() {
        // I hate type erasure - it leads to a hack like this. Admittedly, I
        // could've just created a subclass that does
        // the same thing, but I like to beat the system! :)
        //
        // See http://stackoverflow.com/a/18709327
        Type type = getClass().getGenericSuperclass();

        while (!(type instanceof ParameterizedType) ||
                (((ParameterizedType) type).getRawType() != AbstractCommand.class &&
                ((ParameterizedType) type).getRawType() != AbstractCommand.class)) {
            if (type instanceof ParameterizedType) {
                type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class<?>) type).getGenericSuperclass();
            }
        }

        this.sourceType = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
        if (this.sourceType.getClass().isAssignableFrom(CommandSource.class)) {
            this.sourceTypePredicate = x -> true;
        } else {
            this.sourceTypePredicate = this.sourceType::isInstance;
        }

        this.commandPath = getSubcommandOf();

        // Now, if this is
        RegisterCommand rc = this.getClass().getAnnotation(RegisterCommand.class);

        this.isRoot = rc == null || rc.subcommandOf().equals(AbstractCommand.class);
        this.hasExecutor = rc != null && rc.hasExecutor();

        List<String> force = rc == null ? Lists.newArrayList() : Lists.newArrayList(rc.rootAliasRegister());

        List<String> a = rc == null ? Lists.newArrayList() : Lists.newArrayList(rc.value());
        if (!this.getClass().isAnnotationPresent(NoCommandPrefix.class)
            && !a.isEmpty() && this.isRoot) { // Testing might return a zero length.

            final String nPrimary = "n" + a.get(0).toLowerCase();
            if (!a.contains(nPrimary) && !force.contains(nPrimary)) {
                force.add(nPrimary);
            }
        }

        this.aliases = a.toArray(new String[a.size()]);
        this.forcedAliases = force.toArray(new String[force.size()]);

        // The Permissions annotation provides the backbone of the permissions
        // system for the commands.
        // The standard permission is based on the getAliases command,
        // specifically, the first argument in the
        // returned array, such that the permission it will generated if this
        // annotation is defined is:
        //
        // nucleus.(primaryalias).base
        //
        // Adding a "prefix" and/or "suffix" string will generate:
        //
        // nucleus.(prefix).(primaryalias).(suffix).base
        //
        // For warmup, cooldown and cost exemption, replace base with:
        //
        // exempt.(cooldown|warmup|cost)
        this.permissions = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(this.getClass());

        if (this.getClass().isAnnotationPresent(NoModifiers.class)) {
            this.bypassWarmup = true;
            this.generateWarmupAnyway = false;
            this.bypassCooldown = true;
            this.bypassCost = true;
        } else {
            // For these flags, we simply need to get whether the annotation was
            // declared. If they were not, we simply get back
            // a null - so the check is based around that.
            NoWarmup w = this.getClass().getAnnotation(NoWarmup.class);
            this.bypassWarmup = w != null;
            this.generateWarmupAnyway = !this.bypassWarmup || w.generateConfigEntry();

            this.bypassCooldown = this.getClass().getAnnotation(NoCooldown.class) != null;
            this.bypassCost = this.getClass().getAnnotation(NoCost.class) != null;
        }

        RedirectModifiers cca = this.getClass().getAnnotation(RedirectModifiers.class);
        String configSect;
        if (this.commandPath.isEmpty() || !this.commandPath.contains(".")) {
            configSect = "";
        } else {
            configSect = this.commandPath.replaceAll("\\.[^.]+$", ".");
        }

        this.configSection = configSect + (cca == null ? getAliases()[0].toLowerCase() : cca.value().toLowerCase());

        this.warmupKey = "nucleus." + configSection + ".warmup";
        this.cooldownKey = "nucleus." + configSection + ".cooldown";
        this.costKey = "nucleus." + configSection + ".cost";

        this.requiresEconomy = this.getClass().isAnnotationPresent(RequiresEconomy.class);

        // Timings
        if (!this.getClass().isAnnotationPresent(NoTimings.class)) {
            try {
                this.commandTimings =
                        Timings.of(Nucleus.getNucleus(), "Command - /" + (this.commandPath.replace(".", " ")));
            } catch (Throwable e) {
                if (Nucleus.getNucleus().isDebugMode()) {
                    e.printStackTrace();
                }

                this.commandTimings = TimingsDummy.DUMMY;
            }
        }

        // Injector
        this.plugin = Nucleus.getNucleus();
    }

    /**
     * The command will only load if this condition is true. Happens after injections.
     *
     * @return <code>true</code> if the command can be loaded.
     */
    public boolean canLoad() {
        return true;
    }

    private String getSubcommandOf() {
        StringBuilder sb = new StringBuilder();
        getSubcommandOf(this.getClass(), sb, false);
        return sb.toString();
    }

    private void getSubcommandOf(Class<? extends AbstractCommand> c, StringBuilder sb, boolean appendPeriod) {
        // Get subcommand alias, if any.
        RegisterCommand rc = c.getAnnotation(RegisterCommand.class);
        if (!Modifier.isAbstract( rc.subcommandOf().getModifiers()) && rc.subcommandOf() != this.getClass()) {
            getSubcommandOf(rc.subcommandOf(), sb, true);
        }

        sb.append(rc.value()[0]);
        if (appendPeriod) {
            sb.append(".");
        }
    }

    final void setModuleCommands(Set<Class<? extends AbstractCommand<?>>> moduleCommands) {
        Preconditions.checkState(this.moduleCommands == null);
        this.moduleCommands = moduleCommands;
    }

    final void setCommandBuilder(CommandBuilder builder) {
        this.builder = builder;
    }

    public final void postInit() {
        // Checks.
        Preconditions.checkNotNull(this.getAliases());
        Preconditions.checkArgument(this.getAliases().length > 0);

        this.argumentParser = GenericArguments.seq(getArguments());
        createChildCommands();

        afterPostInit();

        permissionsToRegister().forEach(permissions::registerPermission);
        permissionSuffixesToRegister().forEach(permissions::registerPermissionSuffix);
    }

    /**
     * Runs after postInit has completed.
     */
    protected void afterPostInit() {}

    // ----------------------------------------------------------------------
    // CommandCallable Interface
    // ----------------------------------------------------------------------
    @Override public CommandResult process(CommandSource source, String arguments) throws CommandException {
        // Create the arguments
        CommandArgs args = new CommandArgs(arguments, tokeniser.tokenize(arguments, false));

        return process(source, this.commandPath.replace(".", " "), arguments, args);
    }

    public CommandResult process(CommandSource source, String command, String arguments, CommandArgs args) throws CommandException {
        // Phase one: child command processing. Keep track of all thrown arguments.
        List<Tuple<String, CommandException>> thrown = Lists.newArrayList();

        CommandContext context;
        T castedSource;

        try {
            // If we have a child command to execute, then we execute it.
            if (args.hasNext() && this.dispatcher.containsAlias(args.peek())) {
                Object state = args.getState();
                String next = args.next();
                try {
                    // If this works, then we're A-OK.
                    CommandCallable callable = this.dispatcher.get(next.toLowerCase()).get().getCallable();
                    if (callable instanceof AbstractCommand) {
                        return ((AbstractCommand) callable).process(source, command + " " + next, arguments, args);
                    }

                    return callable.process(source, arguments);
                } catch (NucleusCommandException e) {
                    // Didn't work out. Let's move on.
                    thrown.addAll(e.getExceptions());
                } catch (CommandException e) {
                    // If the Exception is _not_ of right type, wrap it and add it. This shouldn't happen though.
                    thrown.add(Tuple.of(command + " " + next, e));
                } finally {
                    args.setState(state);
                }
            }

            // Phase one: test for what is required
            if (requiresEconomy && !plugin.getEconHelper().economyServiceExists()) {
                source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.economyrequired"));
                return CommandResult.empty();
            }

            // Phase two: source type - test to see if the person in question can execute this command.
            castedSource = checkSourceType(source);

            // Phase three - test the permission.
            if (!testPermissionOnSubject(castedSource)) {
                throw new CommandPermissionException();
            }

            if (!this.hasExecutor) {
                if (thrown.isEmpty()) {
                    // OK, we just process the usage command instead.
                    return this.usageCommand.process(source, "", args.nextIfPresent().map(String::toLowerCase).orElse(null));
                } else {
                    throw new NucleusCommandException(thrown);
                }
            }

            // Phase four - create the context and parse the arguments.
            context = new CommandContext();
            this.argumentParser.parse(source, args, context);
            if (args.hasNext()) {
                thrown.add(Tuple.of(command, new NucleusArgumentParseException(
                    Text.of(TextColors.RED, "Too many arguments"),
                    args.getRaw(),
                    args.getRawPosition(),
                    Text.of(getSimpleUsage(source)),
                    getChildrenUsage(source).orElse(null),
                    true)));
                throw new NucleusCommandException(thrown);
            }
        } catch (NucleusCommandException nce) {
            throw nce;
        } catch (ArgumentParseException ape) {
            // get the command to get the usage/subs from.
            thrown.add(Tuple.of(command, NucleusArgumentParseException.from(ape, Text.of(getSimpleUsage(source)),
                getChildrenUsage(source).orElse(null))));
            throw new NucleusCommandException(thrown);
        } catch (CommandException ex) {
            thrown.add(Tuple.of(command, ex)); // Errors at this point are expected, so we'll run with it - no need for debug mode checks.
            throw new NucleusCommandException(thrown);
        } catch (Throwable throwable) {
            String m;
            if (throwable.getMessage() == null) {
                m = "null";
            } else {
                m = throwable.getMessage();
            }

            thrown.add(
                Tuple.of(command, new CommandException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exception.unexpected", m), throwable)));

            if (plugin.isDebugMode()) {
                throwable.printStackTrace();
            }

            throw new NucleusCommandException(thrown);
        }

        try {
            commandTimings.startTimingIfSync();
            ContinueMode mode = preProcessChecks(castedSource, context);
            if (!mode.cont) {
                return mode.returnType;
            }

            if (castedSource instanceof Player) {
                @SuppressWarnings("unchecked")
                ContinueMode cm = runChecks((Player) castedSource, context);

                if (!cm.cont) {
                    return cm.returnType;
                }
            }

            // If we're running async...
            if (isAsync) {
                // Create an executor that runs the command async.
                plugin.getLogger().debug("Running " + this.getClass().getName() + " in async mode.");
                Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> onExecute(castedSource, context));

                // Tell Sponge we're done.
                return CommandResult.success();
            }

            return onExecute(castedSource, context);
        } finally {
            commandTimings.stopTimingIfSync();
        }
    }

    private CommandResult onExecute(T source, CommandContext context) {
        // Phase five - let's execute! As we got this far,
        try {
            return startExecute(source, context);
        } catch (TextMessageException ex) {
            if (plugin.isDebugMode()) {
                ex.printStackTrace();
            }

            source.sendMessage(
                    plugin.getMessageProvider().getTextMessageWithTextFormat("command.exception.unexpected", ex.getText()));
            return CommandResult.empty();
        } catch (Throwable throwable) {
            if (plugin.isDebugMode()) {
                throwable.printStackTrace();
            }

            String m;
            if (throwable.getMessage() == null) {
                m = "null";
            } else {
                m = throwable.getMessage();
            }

            source.sendMessage(
                plugin.getMessageProvider().getTextMessageWithFormat("command.exception.unexpected", m));

            return CommandResult.empty();
        }
    }

    private CommandResult startExecute(T src, CommandContext args) throws Exception {
        CommandResult cr;
        boolean isSuccess = false;
        try {
            // Any pre-processing steps
            if (args.hasAny(NucleusProcessing.PRE_RUN_KEY)) {
                args.<Action>getAll(NucleusProcessing.PRE_RUN_KEY).forEach(Action::action);
            }

            // Execute the command in the specific executor.
            cr = executeCommand(src, args);

            isSuccess = cr.getSuccessCount().orElse(0) > 0 ;
            if (args.hasAny(NucleusProcessing.SUCCESS_KEY)) {
                args.<Action>getAll(NucleusProcessing.SUCCESS_KEY).forEach(Action::action);
            }
        } catch (ReturnMessageException e) {
            Text t = e.getText();
            src.sendMessage((t == null) ? NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.error") : t);
            cr = CommandResult.empty();
        } finally {
            if (src instanceof Player) {
                // If the subject is subject to cooling down, apply the cooldown.
                @SuppressWarnings("unchecked")
                final Player p = (Player) src;

                if (isSuccess) {
                    setCooldown(p, args);
                } else {
                    // For the tests, keep this here so we can skip the hard to test
                    // code below.
                    final double cost = getCost(p, args);
                    if (cost > 0) {
                        Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> plugin.getEconHelper().depositInPlayer(p, cost));
                    }
                }
            }
        }

        return cr;
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition)
            throws CommandException {
        List<SingleArg> singleArgs = Lists.newArrayList(tokeniser.tokenize(arguments, false));
        // If we end with a space - then we add another argument.
        if (arguments.isEmpty() || arguments.endsWith(" ")) {
            singleArgs.add(new SingleArg("", arguments.length() - 1, arguments.length() - 1));
        }

        final CommandArgs args = new CommandArgs(arguments, singleArgs);

        final List<String> options = Lists.newArrayList();
        CommandContext context = new CommandContext();
        context.putArg(COMPLETION_ARG, true); // We don't care for the value.

        // Subcommand
        Object state = args.getState();
        options.addAll(this.dispatcher.getSuggestions(source, arguments, targetPosition));
        args.setState(state);

        options.addAll(this.argumentParser.complete(source, args, context));
        return options.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Checks to see if this command can be run by this {@link CommandSource}
     *
     * @param source The {@link CommandSource} to test.
     * @return The result of the check.
     */
    @Override public boolean testPermission(CommandSource source) {
        return testPermissionOnSubject(source);
    }

    /**
     * Checks to see if this command can be run by this {@link Subject}
     *
     * @param source The {@link Subject} to test.
     * @return The result of the check.
     */
    private boolean testPermissionOnSubject(Subject source) {
        return this.permissions.isPassthrough() || this.permissions.testBase(source);
    }

    /**
     * Gets the short description of the command.
     *
     * @param source The {@link CommandSource} to display it to.
     * @return The description, if it exists.
     */
    @Override public Optional<Text> getShortDescription(CommandSource source) {
        if (this.desc == null) {
            this.desc = Optional.of(Text.of(getDescription()));
        }

        return this.desc;
    }

    /**
     * Gets the full help that this command would offer.
     *
     * @param source The {@link CommandSource}
     * @return The help, if it exists.
     */
    @Override public Optional<Text> getHelp(CommandSource source) {
        if (desc == null) {
            desc = Optional.of(Text.of(getDescription()));
        }

        if (extended == null) {
            String r = getExtendedDescription();
            if (r.isEmpty()) {
                extended = desc;
            } else {
                extended = desc.map(text -> Optional.of(Text.of(text, Text.NEW_LINE, Util.SPACE, Text.NEW_LINE, Text.of(r))))
                        .orElseGet(() -> Optional.of(Text.of(r)));
            }
        }

        return extended;
    }

    /**
     * Gets the usage that would be displayed to the {@link CommandSource}. This will include both arguments for this command and subcommands.
     *
     * @param source The {@link CommandSource}
     * @return The {@link Text} containing the usage.
     */
    @Override public Text getUsage(CommandSource source) {
        return Text.of(getUsageString(source));
    }

    /**
     * Gets the aliases for the command. The first alias will be the primary
     * alias within NucleusPlugin.
     *
     * @return An array of aliases.
     */
    public String[] getAliases() {
        return Arrays.copyOf(this.aliases, this.aliases.length);
    }

    /**
     * Gets any aliases that need to be forced.
     *
     * @return An array of aliases.
     */
    public String[] getRootCommandAliases() {
        return Arrays.copyOf(forcedAliases, forcedAliases.length);
    }

    /**
     * Functionally similar to
     * {@link CommandExecutor#execute(CommandSource, CommandContext)}, this
     * contains logic that actually executes the command.
     *
     * <p> Note that the {@link CommandResult} is important here. A success is
     * treated differently to a non-success! </p>
     *
     * @param src The executor of the command
     * @param args The arguments for the command.
     * @return The {@link CommandResult}
     * @throws Exception If thrown, {@link TextMessageException#getText()} or
     *         {@link Exception#getMessage()} will be sent to the user.
     */
    protected abstract CommandResult executeCommand(T src, CommandContext args) throws Exception;

    // -------------------------------------
    // Metadata
    // -------------------------------------

    public String getCommandPath() {
        return commandPath;
    }

    /**
     * Gets the description for the command.
     *
     * @return The description.
     */
    public String getDescription() {
        return getFromCommandKey("desc");
    }

    /**
     * Gets the description for the command.
     *
     * @return The description.
     */
    public String getExtendedDescription() {
        return getFromCommandKey("extended");
    }

    private String getFromCommandKey(String type) {
        String key = String.format("%s.%s", this.commandPath, type);
        try {
            return NucleusPlugin.getNucleus().getCommandMessageProvider().getMessageWithFormat(key);
        } catch (Exception e) {
            if (Nucleus.getNucleus().isDebugMode()) {
                Nucleus.getNucleus().getLogger().debug("Could not get command resource key " + key);
            }

            return "";
        }
    }

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

    /**
     * The permission handler for this command.
     *
     * @return The {@link CommandPermissionHandler}
     */
    public final CommandPermissionHandler getPermissionHandler() {
        return this.permissions;
    }

    /**
     * Gets the <em>full</em> usage string, including subcommands.
     *
     * @param source The {@link CommandSource} to get the usage string for.
     * @return The string.
     */
    public final String getUsageString(CommandSource source) {
        final StringBuilder builder = new StringBuilder("/")
                .append(getCommandPath().replaceAll("\\.", " "))
                .append(" ");

        this.dispatcher.getPrimaryAliases().stream().map(x -> this.dispatcher.get(x, source).orElse(null))
                .filter(x -> x != null && x.getCallable().testPermission(source))
                .forEach(x -> builder.append(x.getPrimaryAlias()).append("|"));

        return builder.append(this.argumentParser.getUsage(source).toPlain().replaceAll("\\?\\|", "")).toString();
    }

    /**
     * Gets the usage string without subcommands.
     *
     * @param source The {@link CommandSource} to get the usage for.
     * @return The usage.
     */
    public final String getSimpleUsage(CommandSource source) {
        return "/" + getCommandPath().replaceAll("\\.", " ") + " " + this.argumentParser.getUsage(source).toPlain();
    }

    /**
     * Gets the subcommands.
     *
     * @param source The {@link CommandSource} to get the subcommands for.
     * @return The subcommands, or {@link Optional#empty()} if there aren't any.
     */
    public final Optional<Text> getChildrenUsage(CommandSource source) {
        Set<String> primary = Sets.newHashSet(this.dispatcher.getPrimaryAliases());
        primary.removeIf(x -> x.equalsIgnoreCase("?") || x.equalsIgnoreCase("help"));
        if (primary.isEmpty()) {
            return Optional.empty();
        }

        List<Text> s = primary.stream()
                .filter(x -> this.dispatcher.get(x).get().getCallable().testPermission(source))
                .map(x -> {
                    String toSuggest = "/" + getCommandPath().replaceAll("\\.", " ") + " " + x;
                    return Text.builder(x)
                            .onClick(TextActions.suggestCommand(toSuggest))
                            .onHover(TextActions.showText(Nucleus.getNucleus()
                                    .getMessageProvider().getTextMessageWithFormat("command.usage.suggest", toSuggest)))
                            .onShiftClick(TextActions.insertText(toSuggest + " ?"))
                            .color(TextColors.AQUA)
                            .build();
                }).collect(Collectors.toList());

        if (s.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Text.joinWith(Text.of(", "), s));
    }

    /**
     * Gets the arguments of the command.
     *
     * @return The arguments of the command.
     */
    public CommandElement[] getArguments() {
        return new CommandElement[]{};
    }

    final CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode n = SimpleCommentedConfigurationNode.root();
        String aliasComment = NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.aliases");

        if (this.isRoot) {
            n.getNode("enabled").setComment(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.enabled")).setValue(true);

            String[] al = this.getAliases();
            for (int i = 1; i < al.length; i++) {
                // All but the first command are aliases.
                n.getNode("aliases").getNode(al[i]).setValue(true);
            }
        }

        if (configSection.equalsIgnoreCase(aliases[0].toLowerCase())) {
            if (!bypassCooldown) {
                n.getNode("cooldown").setComment(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.cooldown")).setValue(0);
            }

            if (!bypassWarmup || generateWarmupAnyway) {
                n.getNode("warmup").setComment(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.warmup")).setValue(0);
            }

            if (!bypassCost) {
                n.getNode("cost").setComment(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.cost")).setValue(0);
            }
        }

        for (String alias : this.getRootCommandAliases()) {
            n.getNode("aliases").getNode(alias).setValue(true);
        }

        if (n.getNode("aliases").getValue() == null) {
            n.removeChild("aliases");
        } else {
            n.getNode("aliases").setComment(aliasComment);
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
     */
    protected ContinueMode preProcessChecks(T source, CommandContext args) {
        return ContinueMode.CONTINUE;
    }

    // -------------------------------------
    // Source Type
    // -------------------------------------
    @SuppressWarnings("unchecked")
    private T checkSourceType(CommandSource source) throws CommandException {
        if (sourceTypePredicate.test(source)) {
            // Yep, we're OK.
            return (T) source;
        }

        if (sourceType.equals(Player.class) && !(source instanceof Player)) {
            throw getExceptionFromKey("command.playeronly");
        } else if (sourceType.equals(ConsoleSource.class) && !(source instanceof ConsoleSource)) {
            throw getExceptionFromKey("command.consoleonly");
        } else if (sourceType.equals(CommandBlockSource.class) && !(source instanceof CommandBlockSource)) {
            throw getExceptionFromKey("command.commandblockonly");
        }

        throw getExceptionFromKey("command.unknownsource");
    }

    private CommandException getExceptionFromKey(String key, String... subs) {
        return new CommandException(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat(key, subs));
    }

    // -------------------------------------
    // Player Checks
    // -------------------------------------
    private ContinueMode runChecks(Player src, CommandContext args) {
        // Cooldown, cost, warmup.
        ContinueMode m = checkCooldown(src, args);
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
        if (src instanceof Locatable) {
            return Optional.of(((Locatable) src).getWorld().getProperties());
        }

        return Optional.empty();
    }

    protected final WorldProperties getWorldPropertiesOrDefault(CommandSource src, String argument, CommandContext args) {
        Optional<WorldProperties> pr = getWorldProperties(src, argument, args);
        if (pr.isPresent()) {
            return pr.get();
        }

        src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("args.worldproperties.default"));
        return Sponge.getServer().getDefaultWorld().get();
    }

    @Nonnull
    protected final WorldProperties getWorldFromUserOrArgs(CommandSource src, String argument, CommandContext args) throws ReturnMessageException {
        Optional<WorldProperties> owp = args.getOne(argument);
        if (owp.isPresent()) {
            return owp.get();
        } else {
            if (src instanceof Locatable) {
                return ((Locatable) src).getWorld().getProperties();
            } else {
                throw ReturnMessageException.fromKey("command.noworldconsole");
            }
        }
    }

    protected final <U extends User> U getUserFromArgs(Class<U> clazz, CommandSource src, String argument, CommandContext args) throws ReturnMessageException {
        return getUserFromArgs(clazz, src, argument, args, "command.playeronly");
    }

    /**
     * Gets a {@link User} from the specified argument, or if one does not exist, attempts to use the
     * subject currently running the command, if there is one.
     *
     * @param clazz The {@link Class} of the object we're expecting, which might be a {@link User} or {@link Player}
     * @param src The {@link CommandSource} running the command.
     * @param argument The name of the argument to check.
     * @param args The {@link CommandContext} that would contain the arguement result.
     * @param failKey The translation key to use if the method fails to produce a user.
     * @param <U> The type of object we're looking for.
     * @throws ReturnMessageException if no user was found.
     *
     * @return An object of type {@code clazz}.
     */
    protected final <U extends User> U getUserFromArgs(Class<U> clazz, CommandSource src, String argument, CommandContext args, String failKey) throws ReturnMessageException {
        Optional<U> opl = args.getOne(argument);
        if (opl.isPresent()) {
            return opl.get();
        } else if (clazz.isInstance(src)) {
            return clazz.cast(src);
        } else {
            throw new ReturnMessageException(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat(failKey));
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    protected final CatalogType getCatalogTypeFromHandOrArgs(CommandSource src, String argument, CommandContext args) throws ReturnMessageException {
        Optional<CatalogType> catalogTypeOptional = args.getOne(argument);
        if (catalogTypeOptional.isPresent()) {
            CatalogType type = catalogTypeOptional.get();
            if (type instanceof ItemType) {
                // Try to get the block state, if possible.
                Optional<BlockState> state = ItemStack.of((ItemType)type, 1).get(Keys.ITEM_BLOCKSTATE);
                if (state.isPresent()) {
                    return state.get();
                }
            }

            return type;
        } else {
            // If subject, get the item in hand, otherwise, we can't continue.
            if (src instanceof Player) {
                return Util.getTypeFromItemInHand((Player)src)
                    .orElseThrow(() -> new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.noneinhand")));
            }

            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.noitemconsole"));
        }
    }

    // -------------------------------------
    // Warmups
    // -------------------------------------
    protected int getWarmup(final Player src) {
        if (permissions.testWarmupExempt(src)) {
            return 0;
        }

        // Get the warmup time.
        return Util.getPositiveIntOptionFromSubject(src, warmupKey)
            .orElseGet(() -> this.plugin.getCommandsConfig().getCommandNode(configSection).getNode("warmup").getInt());
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
                        src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("warmup.end"));
                        plugin.getWarmupManager().removeWarmup(src.getUniqueId());
                        onExecute((T) src, args);
                    }
                }).name("Command Warmup - " + src.getName());

        // Run an async command async, of course!
        if (isAsync) {
            tb.async();
        }

        // Add the warmup to the service so we can cancel it if we need to.
        plugin.getWarmupManager().addWarmup(src.getUniqueId(), tb.submit(plugin));

        // Tell the user we're warming up.
        src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("warmup.start",
                Util.getTimeStringFromSeconds(warmupTime)));

        WarmupConfig wc = Nucleus.getNucleus().getWarmupConfig();
        if (wc.isOnMove() && wc.isOnCommand()) {
            src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("warmup.both"));
        } else if (wc.isOnMove()) {
            src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("warmup.onMove"));
        } else if (wc.isOnCommand()) {
            src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("warmup.onCommand"));
        }

        // Sponge should think the command was run successfully.
        return ContinueMode.STOP_SUCCESS;
    }

    // -------------------------------------
    // Cooldowns
    // -------------------------------------
    private ContinueMode checkCooldown(Player src, CommandContext args) {
        // Remove any expired cooldowns.
        cleanCooldowns();

        // If they are still in there, then tell them they are still cooling
        // down.
        if (!bypassCooldown && !args.hasAny(NoModifiersArgument.NO_COOLDOWN_ARGUMENT) &&
            !permissions.testCooldownExempt(src) && cooldownStore.containsKey(src.getUniqueId())) {

            Instant l = cooldownStore.get(src.getUniqueId());
            src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cooldown.message",
                    Util.getTimeStringFromSeconds(l.until(Instant.now(), ChronoUnit.SECONDS))));
            return ContinueMode.STOP;
        }

        return ContinueMode.CONTINUE;
    }

    private void setCooldown(Player src, CommandContext args) {
        if (!args.hasAny(NoModifiersArgument.NO_COOLDOWN_ARGUMENT) && !permissions.testCooldownExempt(src)) {
            // Get the cooldown time.
            int cooldownTime = Util.getPositiveIntOptionFromSubject(src, cooldownKey)
                .orElseGet(() -> plugin.getCommandsConfig().getCommandNode(configSection).getNode("cooldown").getInt());
            if (cooldownTime > 0) {
                // If there is a cooldown, add the cooldown to the list, with
                // the end time as an Instant.
                cooldownStore.put(src.getUniqueId(), Instant.now().plus(cooldownTime, ChronoUnit.SECONDS));
            }
        }
    }

    protected void removeCooldown(UUID uuid) {
        cooldownStore.remove(uuid);
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
     * Gets the cost for this command, or zero if the subject does not have to
     * pay.
     *
     * @param src The {@link CommandSource}
     * @param args The {@link CommandContext}
     * @return The cost.
     */
    protected double getCost(CommandSource src, @Nullable CommandContext args) {
        if (src instanceof Player) {
            boolean noCost = args != null && args.<Boolean>getOne(NoModifiersArgument.NO_COST_ARGUMENT).orElse(false);

            // If the subject or command itself is exempt, return a zero.
            if (bypassCost || noCost || permissions.testCostExempt(src)) {
                return 0.;
            }

            // Return the cost if positive, else, zero.
            double cost = Util.getDoubleOptionFromSubject(src, costKey)
                .orElseGet(() -> plugin.getCommandsConfig().getCommandNode(configSection).getNode("cost").getDouble(0.));
            if (cost <= 0.) {
                return 0.;
            }

            return cost;
        }

        return 0.;
    }

    // -------------------------------------
    // Child Commands
    // -------------------------------------
    private void createChildCommands() {
        Set<Class<? extends AbstractCommand<?>>> bases = null;
        if (this.moduleCommands != null) {
            bases = moduleCommands.stream().filter(x -> {
                RegisterCommand r = x.getAnnotation(RegisterCommand.class);
                // Only commands that are subcommands of this.
                return r != null && r.subcommandOf().equals(this.getClass());
            }).collect(Collectors.toSet());
        }

        if (bases != null) {
            bases.forEach(cb -> {
                try {
                    builder.buildCommand(cb, false).ifPresent(x -> this.dispatcher.register(x, Arrays.asList(x.getAliases())));
                } catch (Exception e) {
                    plugin.getLogger().error(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("command.child.notloaded", cb.getName()));

                    if (Nucleus.getNucleus().isDebugMode()) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (!this.getClass().isAnnotationPresent(NoHelpSubcommand.class)) {
            this.dispatcher.register(usageCommand, "?", "help");
        }
    }

    void setModuleName(String id, String module) {
        if (this.module == null) {
            this.moduleId = id;
            this.module = module;
        }
    }

    @NonnullByDefault
    private class UsageCommand implements CommandCallable, CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            return process(src, "");
        }

        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            return process(source, arguments, null);
        }

        public CommandResult process(CommandSource source, String arguments, @Nullable String previous) throws CommandException {
            if (!testPermission(source)) {
                source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.nopermission"));
                return CommandResult.empty();
            }

            Nucleus plugin = Nucleus.getNucleus();
            AbstractCommand<?> parent = AbstractCommand.this;

            String command = getCommandPath().replaceAll("\\.", " ");

            // Header
            Text header = plugin.getMessageProvider().getTextMessageWithFormat("command.usage.header", command);

            List<Text> textMessages = Lists.newArrayList();

            if (previous != null) {
                textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.noexist", previous));
                textMessages.add(Util.SPACE);
            }

            if (parent.sourceType == Player.class) {
                textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.playeronly"));
            }

            textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.module", module, moduleId));

            String desc = getDescription();
            if (!desc.isEmpty()) {
                textMessages.add(Util.SPACE);
                textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.summary"));
                textMessages.add(Text.of(desc));
            }

            String ext = getExtendedDescription();
            if (!ext.isEmpty()) {
                textMessages.add(Util.SPACE);
                textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.description"));
                String[] split = ext.split("(\\r|\\n|\\r\\n)");
                for (String s : split) {
                    textMessages.add(Text.of(s));
                }
            }

            if (hasExecutor) {
                textMessages.add(Util.SPACE);
                textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.usage"));
                textMessages.add(Text.of(TextColors.WHITE, AbstractCommand.this.getSimpleUsage(source)));
            }

            getChildrenUsage(source).ifPresent(x -> {
                textMessages.add(Util.SPACE);
                textMessages.add(plugin.getMessageProvider().getTextMessageWithFormat("command.usage.subcommand"));
                textMessages.add(Text.of(TextColors.WHITE, x));
            });

            PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
            PaginationList.Builder builder = ps.builder().title(header).contents(textMessages);
            if (!(source instanceof Player)) {
                builder.linesPerPage(-1);
            }

            builder.sendTo(source);
            return CommandResult.success();
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
            return Lists.newArrayList();
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return AbstractCommand.this.permissions.testBase(source);
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.EMPTY;
        }
    }

    @NonnullByDefault
    public abstract static class SimpleTargetOtherPlayer extends AbstractCommand<CommandSource> {

        protected final String playerKey = "player";

        public CommandElement[] additionalArguments() {
            return new CommandElement[] {};
        }

        @Override public final CommandElement[] getArguments() {
            return ArrayUtils.addAll(new CommandElement[] {
                GenericArguments.optionalWeak(
                    GenericArguments.requiringPermission(
                        new NoModifiersArgument<>(
                            SelectorWrapperArgument.nicknameSelector(Text.of(playerKey), NicknameArgument.UnderlyingType.PLAYER),
                            NoModifiersArgument.PLAYER_NOT_CALLER_PREDICATE
                        ),
                        permissions.getOthers()
                    )
                )
            }, additionalArguments());
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            Player target = this.getUserFromArgs(Player.class, src, playerKey, args);
            return executeWithPlayer(src, target, args, src instanceof Player && ((Player) src).getUniqueId().equals(target.getUniqueId()));
        }

        protected abstract CommandResult executeWithPlayer(CommandSource source, Player target, CommandContext args, final boolean isSelf)
                throws Exception;
    }
}
