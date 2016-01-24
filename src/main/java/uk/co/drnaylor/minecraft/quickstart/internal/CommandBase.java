package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarmupManagerService;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class CommandBase<T extends CommandSource> implements CommandExecutor {

    private final boolean isAsync = this.getClass().getAnnotation(RunAsync.class) != null;
    private final Set<String> additionalPermissions;
    private final Set<String> cooldown;
    private final Set<String> warmup;
    private final Set<String> cost;

    private final Map<UUID, Long> cooldownStore = Maps.newHashMap();
    private QuickStartWarmupManagerService warmupService = null;
    private final Class<T> sourceType;
    private final boolean bypassWarmup;
    private final boolean bypassCooldown;
    private final boolean bypassCost;

    @Inject protected QuickStart plugin;

    @SuppressWarnings("unchecked")
    protected CommandBase() {
        // I hate type erasure...
        Optional<Method> me = Arrays.asList(getClass().getMethods()).stream().filter(x -> x.getName().equals("executeCommand") &&
                x.getParameterTypes().length == 2 &&
                x.getParameterTypes()[1].isAssignableFrom(CommandContext.class) &&
                !x.getParameterTypes()[0].equals(CommandSource.class)).findFirst();

        if (me.isPresent()) {
            sourceType = (Class<T>) (me.get().getParameterTypes()[0]);
        } else {
            sourceType = (Class<T>) CommandSource.class;
        }

        bypassWarmup = this.getClass().getAnnotation(NoWarmup.class) != null;
        bypassCooldown = this.getClass().getAnnotation(NoCooldown.class) != null;
        bypassCost = this.getClass().getAnnotation(NoCost.class) != null;

        // Additional permissions
        Permissions op = this.getClass().getAnnotation(Permissions.class);
        additionalPermissions = Sets.newHashSet();
        cooldown = Sets.newHashSet();
        warmup = Sets.newHashSet();
        cost = Sets.newHashSet();

        if (op != null) {
            Collections.addAll(additionalPermissions, op.value());
            Collections.addAll(cooldown, op.cooldownExempt());
            Collections.addAll(warmup, op.warmupExempt());
            Collections.addAll(cost, op.costExempt());

            StringBuilder perm = new StringBuilder(QuickStart.PERMISSIONS_PREFIX);
            if (!op.root().isEmpty()) {
                perm.append(op.root()).append(".");
            }

            perm.append(getAliases()[0]).append(".");

            if (!op.sub().isEmpty()) {
                perm.append(op.sub()).append(".");
            }

            String defaultroot = perm.toString();
            if (op.useDefault()) {
                additionalPermissions.add(defaultroot + "base");
            }

            if (op.useDefaultCooldownExempt()) {
                cooldown.add(defaultroot + "exempt.cooldown");
            }

            if (op.useDefaultWarmupExempt()) {
                warmup.add(defaultroot + "exempt.warmup");
            }

            if (op.useDefaultCostExempt()) {
                warmup.add(defaultroot + "exempt.cost");
            }

            if (op.includeAdmin()) {
                additionalPermissions.add(QuickStart.PERMISSIONS_ADMIN);
                cooldown.add(QuickStart.PERMISSIONS_ADMIN);
                warmup.add(QuickStart.PERMISSIONS_ADMIN);
                cost.add(QuickStart.PERMISSIONS_ADMIN);
            }
        }
    }

    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode n = SimpleCommentedConfigurationNode.root();
        if (!bypassCooldown) {
            n.getNode("cooldown").setComment(Util.messageBundle.getString("config.cooldown")).setValue(0);
        }

        if (!bypassWarmup) {
            n.getNode("warmup").setComment(Util.messageBundle.getString("config.warmup")).setValue(0);
        }

        if (!bypassCost) {
            n.getNode("cost").setComment(Util.messageBundle.getString("config.cost")).setValue(0);
        }

        return n;
    }

    public abstract CommandSpec createSpec();

    public abstract String[] getAliases();

    public abstract CommandResult executeCommand(T src, CommandContext args) throws Exception;

    @Override
    @NonnullByDefault
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!checkSourceType(source)) {
            return CommandResult.empty();
        }

        @SuppressWarnings("unchecked") T src = (T)source;

        // If they don't match ANY permission, throw 'em.
        if (!additionalPermissions.isEmpty() && !additionalPermissions.stream().anyMatch(src::hasPermission)) {
            throw new CommandPermissionException();
        }

        if ((src instanceof Player) && !cooldown.stream().anyMatch(src::hasPermission)) {
            cleanCooldowns();
            if (cooldownStore.containsKey(((Player) src).getUniqueId())) {
                long l = (cooldownStore.get(((Player) src).getUniqueId()) / 1000) + 1;
                src.sendMessage(Text.builder(MessageFormat.format(Util.messageBundle.getString("cooldown.message"), Util.getTimeStringFromSeconds(l)))
                        .color(TextColors.YELLOW).build());
                return CommandResult.empty();
            }
        }

        // Do we apply a warmup?
        int getWarmup = applyWarmup(src);
        if (getWarmup > 0) {
            // We know that the source is a player
            final Player p = (Player)src;
            if (warmupService == null) {
                warmupService = Sponge.getServiceManager().provideUnchecked(QuickStartWarmupManagerService.class);
            }

            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().delay(getWarmup, TimeUnit.SECONDS).execute(t -> {
                src.sendMessage(Text.builder(Util.messageBundle.getString("warmup.end")).color(TextColors.YELLOW).build());
                warmupService.removeWarmup(p.getUniqueId());

                // Do we charge the player?
                Double cost = applyCost(src);
                if (cost == null) {
                    return;
                }

                startExecute(src, args);
            }).name("Command Warmup - " + src.getName());

            if (isAsync) {
                tb.async();
            }

            warmupService.addWarmup(p.getUniqueId(), tb.submit(plugin));
            src.sendMessage(Text.builder(MessageFormat.format(Util.messageBundle.getString("warmup.start"), Util.getTimeStringFromSeconds(getWarmup)))
                    .color(TextColors.YELLOW).build());
            return CommandResult.success();
        } else {

            // Do we charge the player?
            Double cost = applyCost(src);
            if (cost == null) {
                return CommandResult.empty();
            }

            if (isAsync) {
                plugin.getLogger().debug("Running " + this.getClass().getName() + " in async mode.");
                Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> startExecute(src, args));

                return CommandResult.success();
            }

            return startExecute(src, args);
        }
    }

    public final Set<String> getCommandPermissions() {
        return ImmutableSet.copyOf(additionalPermissions);
    }

    public final Set<String> getWarmupExemptPermissions() {
        return ImmutableSet.copyOf(warmup);
    }

    public final Set<String> getCooldownExemptPermissions() {
        return ImmutableSet.copyOf(cooldown);
    }

    public final Set<String> getCostExemptPermissions() {
        return ImmutableSet.copyOf(cost);
    }

    private int applyWarmup(CommandSource src) {
        if (bypassWarmup || !(src instanceof Player) || warmup.isEmpty() || warmup.stream().anyMatch(src::hasPermission)) {
            return 0;
        }

        int warmupTime = plugin.getConfig(CommandsConfig.class).get().getCommandNode(getAliases()[0]).getNode("warmup").getInt();
        if (warmupTime <= 0) {
            return 0;
        }

        return warmupTime;
    }

    /**
     * Applies a cost to the user, if required.
     *
     * @param src The {@link CommandSource}
     * @return <code>null</code> if there is a problem, or a non-negative value containing the amount charged.
     */
    private Double applyCost(CommandSource src) {
        double cost = getCost(src);
        if (cost == 0.) {
            return 0.;
        }

        // We know we have a player.
        final Player p = (Player)src;

        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> a = es.getAccount(p.getUniqueId());
            if (!a.isPresent()) {
                src.sendMessage(Text.builder(Util.messageBundle.getString("cost.noaccount"))
                        .color(TextColors.YELLOW).build());
                return null;
            }

            TransactionResult tr = a.get().withdraw(es.getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.of(this));
            if (tr.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
                src.sendMessage(Text.builder(MessageFormat.format(Util.messageBundle.getString("cost.nofunds"), es.getDefaultCurrency().format(BigDecimal.valueOf(cost)).toPlain()))
                        .color(TextColors.YELLOW).build());
                return null;
            } else if (tr.getResult() != ResultType.FAILED) {
                src.sendMessage(Text.builder(Util.messageBundle.getString("cost.error"))
                        .color(TextColors.YELLOW).build());
                return null;
            }
        }

        return cost;
    }

    private double getCost(CommandSource src) {
        if (bypassCost || !(src instanceof Player) || cost.isEmpty() || cost.stream().anyMatch(src::hasPermission)) {
            return 0.;
        }

        double cost = plugin.getConfig(CommandsConfig.class).get().getCommandNode(getAliases()[0]).getNode("cost").getDouble(0.);
        if (cost <= 0.) {
            return 0.;
        }

        return cost;
    }

    private void reverseCharge(Player p, EconomyService es, double cost) {
        if (cost > 0) {
            // We might be async, so put this on the sync tick loop.
            Optional<UniqueAccount> a = es.getAccount(p.getUniqueId());
            if (a.isPresent()) {
                final UniqueAccount ua = a.get();
                Sponge.getScheduler().createSyncExecutor(this.plugin).execute(() -> {
                    ua.deposit(es.getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.of(this));
                });
            }

        }
    }

    private CommandResult startExecute(T src, CommandContext args) {
        CommandResult cr;
        try {
            cr = executeCommand(src, args);
        } catch (TextMessageException e) {
            src.sendMessage(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, e.getText()));
            cr = CommandResult.empty();
        } catch (Exception e) {
            src.sendMessage(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, TextColors.RED, Util.messageBundle.getString("command.error")));
            cr = CommandResult.empty();
        }

        if (src instanceof Player) {
            // Cooldown
            final Player p = (Player)src;
            if (!cooldown.stream().anyMatch(src::hasPermission) && cr.getSuccessCount().orElse(0) > 0) {
                int cooldownTime = plugin.getConfig(CommandsConfig.class).get().getCommandNode(getAliases()[0]).getNode("cooldown").getInt();
                if (cooldownTime > 0 && cr.getSuccessCount().orElse(0) > 0) {
                    cooldownStore.put(p.getUniqueId(), new Date().getTime() + (cooldownTime*1000));
                }
            }

            // For the tests, keep this here so we can skip the hard to test code below.
            final double cost = getCost(p);
            if (cost > 0) {
                Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
                if (oes.isPresent()) {
                    final EconomyService es = oes.get();
                    if (cr.getSuccessCount().orElse(0) == 0) {
                        reverseCharge(p, es, cost);
                    } else {
                        String c = es.getDefaultCurrency().format(BigDecimal.valueOf(cost)).toPlain();
                        p.sendMessage(Text.of(TextColors.YELLOW, MessageFormat.format(Util.messageBundle.getString("cost.complete"), c)));
                    }
                }
            }
        }

        return cr;
    }

    private void cleanCooldowns() {
        long time = new Date().getTime();
        cooldownStore.entrySet().stream().filter(k -> k.getValue() < time).map(Map.Entry::getKey).forEach(cooldownStore::remove);
    }

    private boolean checkSourceType(CommandSource source) {
        if (sourceType.equals(Player.class) && !(source instanceof Player)) {
            source.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.playeronly")));
            return false;
        } else if (sourceType.equals(ConsoleSource.class) && !(source instanceof Player)) {
            source.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.consoleonly")));
            return false;
        } else if (sourceType.equals(CommandBlockSource.class) && !(source instanceof CommandBlockSource)) {
            source.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.commandblockonly")));
            return false;
        }

        return true;
    }

    @SafeVarargs
    protected final Map<List<String>, CommandCallable> createChildCommands(Class<? extends CommandBase>... bases) {
        Map<List<String>, CommandCallable> map = Maps.newHashMap();
        Arrays.asList(bases).forEach(cb -> {
            CommandBase c = plugin.getInjector().getInstance(cb);
            map.put(Arrays.asList(c.getAliases()), c.createSpec());
        });

        return map;
    }
}