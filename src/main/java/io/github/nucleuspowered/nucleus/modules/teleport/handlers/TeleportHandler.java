/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.handlers;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.CancellableTask;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TeleportHandler {

    private final Nucleus plugin;
    private final Map<UUID, TeleportPrep> ask = new HashMap<>();

    public static final String tptoggleBypassPermission = PermissionRegistry.PERMISSIONS_PREFIX + "teleport.tptoggle.exempt";
    private Text acceptDeny;

    public TeleportHandler(Nucleus plugin) {
        this.plugin = plugin;
    }

    public TeleportBuilder getBuilder() {
        return new TeleportBuilder(plugin);
    }

    public static boolean canBypassTpToggle(CommandSource from) {
        return from.hasPermission(tptoggleBypassPermission);
    }

    public void addAskQuestion(UUID target, TeleportPrep tp) {
        clearExpired();
        get(target).ifPresent(this::cancel);
        ask.put(target, tp);
    }

    public void clearExpired() {
        Instant now = Instant.now();
        ask.entrySet().stream().filter(x -> now.isAfter(x.getValue().getExpire())).map(Map.Entry::getKey).collect(Collectors.toList())
                .forEach(x -> cancel(ask.remove(x)));
    }

    public boolean getAndExecute(UUID uuid) throws Exception {
        Optional<TeleportPrep> otp = get(uuid);
        return otp.isPresent() && otp.get().tpbuilder.startTeleport();

    }

    public Optional<TeleportPrep> get(UUID uuid) {
        clearExpired();
        return Optional.ofNullable(ask.remove(uuid));
    }

    public boolean remove(UUID uuid) {
        TeleportPrep tp = ask.remove(uuid);
        cancel(tp);
        return tp != null;
    }

    public Text getAcceptDenyMessage() {
        if (acceptDeny == null) {
            acceptDeny = Text.builder()
                    .append(Text.builder().append(Util.getTextMessageWithFormat("standard.accept")).style(TextStyles.UNDERLINE)
                            .onHover(TextActions.showText(Util.getTextMessageWithFormat("teleport.accept.hover")))
                            .onClick(TextActions.runCommand("/tpaccept")).build())
                    .append(Text.of(" - "))
                    .append(Text.builder().append(Util.getTextMessageWithFormat("standard.deny")).style(TextStyles.UNDERLINE)
                            .onHover(TextActions.showText(Util.getTextMessageWithFormat("teleport.deny.hover")))
                            .onClick(TextActions.runCommand("/tpdeny")).build())
                    .build();
        }

        return acceptDeny;
    }

    private void cancel(@Nullable TeleportPrep prep) {
        if (prep == null) {
            return;
        }

        if (prep.charged != null && prep.cost > 0) {
            if (prep.charged.isOnline()) {
                prep.charged.getPlayer().ifPresent(x -> x
                        .sendMessage(Util.getTextMessageWithFormat("teleport.prep.cancel", plugin.getEconHelper().getCurrencySymbol(prep.cost))));
            }

            plugin.getEconHelper().depositInPlayer(prep.charged, prep.cost);
        }
    }

    public static class TeleportTask implements CancellableTask {

        private final Player playerToTeleport;
        private final Player playerToTeleportTo;
        private final Player charged;
        private final double cost;
        private final boolean safe;
        private final CommandSource source;
        private final Nucleus plugin;
        private final boolean silentSource;
        private final boolean silentTarget;

        private TeleportTask(Nucleus plugin, CommandSource source, Player playerToTeleport, Player playerToTeleportTo, boolean safe, boolean silentSource, boolean silentTarget) {
            this(plugin, source, playerToTeleport, playerToTeleportTo, null, 0, safe, silentSource, silentTarget);
        }

        private TeleportTask(Nucleus plugin, CommandSource source, Player playerToTeleport, Player playerToTeleportTo, Player charged, double cost, boolean safe,
                             boolean silentSource, boolean silentTarget) {
            this.plugin = plugin;
            this.source = source;
            this.playerToTeleport = playerToTeleport;
            this.playerToTeleportTo = playerToTeleportTo;
            this.cost = cost;
            this.charged = charged;
            this.safe = safe;
            this.silentSource = silentSource;
            this.silentTarget = silentTarget;
        }

        private void run() {
            Location<World> current = playerToTeleport.getLocation();
            if (playerToTeleportTo.isOnline()) {
                if (safe && !playerToTeleport.setLocationAndRotationSafely(playerToTeleportTo.getLocation(), playerToTeleportTo.getRotation())) {
                    if (!silentSource) {
                        source.sendMessage(Util.getTextMessageWithFormat("teleport.nosafe"));
                    }

                    onCancel();
                    return;
                } else {
                    playerToTeleport.setLocationAndRotation(playerToTeleportTo.getLocation(), playerToTeleportTo.getRotation());
                }

                if (!source.equals(playerToTeleport) && !silentSource) {
                    source.sendMessage(Util.getTextMessageWithFormat("teleport.success.source", playerToTeleport.getName(), playerToTeleportTo.getName()));
                }

                playerToTeleport.sendMessage(Util.getTextMessageWithFormat("teleport.to.success", playerToTeleportTo.getName()));
                if (!silentTarget) {
                    playerToTeleportTo.sendMessage(Util.getTextMessageWithFormat("teleport.from.success", playerToTeleport.getName()));
                }
            } else {
                if (!silentSource) {
                    source.sendMessage(Util.getTextMessageWithFormat("teleport.fail.offline"));
                }

                onCancel();
            }
        }

        @Override
        public void accept(Task task) {
            run();
        }

        @Override
        public void onCancel() {
            if (!silentSource) {
                source.sendMessage(Util.getTextMessageWithFormat("teleport.cancelled"));
            }

            if (charged != null && cost > 0) {
                plugin.getEconHelper().depositInPlayer(charged, cost);
            }
        }
    }

    public static class TeleportBuilder {

        private CommandSource source;
        private Player from;
        private Player to;
        private Player charge;
        private double cost;
        private int warmupTime = 0;
        private boolean bypassToggle = false;
        private boolean safe = true;
        private boolean silentSource = false;
        private boolean silentTarget = false;

        private final Nucleus plugin;

        private TeleportBuilder(Nucleus plugin) {
            this.plugin = plugin;
        }

        public TeleportBuilder setSafe(boolean safe) {
            this.safe = safe;
            return this;
        }

        public TeleportBuilder setSource(CommandSource source) {
            this.source = source;
            return this;
        }

        public TeleportBuilder setFrom(Player from) {
            this.from = from;
            return this;
        }

        public TeleportBuilder setTo(Player to) {
            this.to = to;
            return this;
        }

        public TeleportBuilder setCharge(Player charge) {
            this.charge = charge;
            return this;
        }

        public TeleportBuilder setCost(double cost) {
            this.cost = cost;
            return this;
        }

        public TeleportBuilder setWarmupTime(int warmupTime) {
            this.warmupTime = warmupTime;
            return this;
        }

        public TeleportBuilder setBypassToggle(boolean bypassToggle) {
            this.bypassToggle = bypassToggle;
            return this;
        }

        public TeleportBuilder setSilentSource(boolean silent) {
            this.silentSource = silent;
            return this;
        }

        public TeleportBuilder setSilentTarget(boolean silentTarget) {
            this.silentTarget = silentTarget;
            return this;
        }

        public boolean startTeleport() throws Exception {
            Preconditions.checkNotNull(from);
            Preconditions.checkNotNull(to);

            if (source == null) {
                source = from;
            }

            if (from.equals(to)) {
                source.sendMessage(Util.getTextMessageWithFormat("command.teleport.self"));
                return false;
            }

            InternalNucleusUser toPlayer = plugin.getUserLoader().getUser(to);
            if (!bypassToggle && !toPlayer.isTeleportToggled() && !canBypassTpToggle(source)) {
                source.sendMessage(Util.getTextMessageWithFormat("teleport.fail.targettoggle", to.getName()));
                return false;
            }

            if (plugin.getUserLoader().getUser(from).getJailData().isPresent()) {
                // Don't teleport a jailed player.
                if (!silentSource) {
                    source.sendMessage(Util.getTextMessageWithFormat("teleport.fail.jailed", from.getName()));
                }

                return false;
            }

            TeleportTask tt;
            if (cost > 0 && charge != null) {
                tt = new TeleportTask(plugin, source, from, to, charge, cost, safe, silentSource, silentTarget);
            } else {
                tt = new TeleportTask(plugin, source, from, to, safe, silentSource, silentTarget);
            }

            if (warmupTime > 0) {
                from.sendMessage(Util.getTextMessageWithFormat("teleport.warmup", String.valueOf(warmupTime)));
                plugin.getWarmupManager().addWarmup(from.getUniqueId(), Sponge.getScheduler().createTaskBuilder().delay(warmupTime, TimeUnit.SECONDS)
                        .execute(tt).name("Nucleus - Teleport Waiter").submit(plugin));
            } else {
                tt.run();
            }

            return true;
        }
    }

    public static class TeleportPrep {

        private final Instant expire;
        private final User charged;
        private final double cost;
        private final TeleportBuilder tpbuilder;

        public TeleportPrep(Instant expire, User charged, double cost, TeleportBuilder tpbuilder) {
            this.expire = expire;
            this.charged = charged;
            this.cost = cost;
            this.tpbuilder = tpbuilder;
        }

        public Instant getExpire() {
            return expire;
        }

        public User getCharged() {
            return charged;
        }

        public double getCost() {
            return cost;
        }

        public TeleportBuilder getTpbuilder() {
            return tpbuilder;
        }
    }
}
