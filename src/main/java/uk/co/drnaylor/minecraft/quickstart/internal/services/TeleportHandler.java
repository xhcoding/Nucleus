package uk.co.drnaylor.minecraft.quickstart.internal.services;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.CancellableTask;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TeleportHandler {

    private final QuickStart plugin;

    public static final String tptoggleBypassPermission = PermissionUtil.PERMISSIONS_PREFIX + "teleport.tptoggle.exempt";

    public TeleportHandler(QuickStart plugin) {
        this.plugin = plugin;
    }

    /**
     * Requests a teleportation for a player to another player.
     *
     * @param from The {@link Player} teleport.
     * @param to The {@link Player} to teleport to.
     * @param checkToggle <code>true</code> if /tptoggle needs to be checked.
     * @param bypassWarmup <code>true</code> if the warmup is to be bypassed
     * @return <code>true</code> if the teleportation is to go ahead.
     * @throws Exception If there is a problem!
     */
    public boolean startTeleport(Player from, Player to, boolean checkToggle, boolean bypassWarmup) throws Exception {
        return startTeleport(from, to, 0, null, checkToggle, bypassWarmup);
    }

    /**
     * Requests a teleportation for a player to another player.
     *
     * @param from The {@link Player} teleport.
     * @param to The {@link Player} to teleport to.
     * @param cost The cost to refund, if any, if the teleportation fails.
     * @param charge The {@link Player} to refund, if the teleportation fails.
     * @param checkToggle <code>true</code> if /tptoggle needs to be checked.
     * @param bypassWarmup <code>true</code> if the warmup is to be bypassed
     * @return <code>true</code> if the teleportation is to go ahead.
     * @throws Exception If there is a problem!
     */
    public boolean startTeleport(Player from, Player to, double cost, Player charge, boolean checkToggle, boolean bypassWarmup) throws Exception {
        if (from.equals(to)) {
            from.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.teleport.self")));
            return false;
        }

        InternalQuickStartUser toPlayer = plugin.getUserLoader().getUser(to);
        if (checkToggle && !toPlayer.isTeleportToggled() && !canBypassTpToggle(from)) {
            from.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("teleport.fail.targettoggle", to.getName())));
            return false;
        }

        TeleportTask tt;
        if (cost > 0 && charge != null) {
            tt = new TeleportTask(from, to, charge, cost);
        } else {
            tt = new TeleportTask(from, to);
        }

        long time = plugin.getConfig(ConfigMap.MAIN_CONFIG).get().getTeleportWarmup();
        if (!bypassWarmup && time > 0 && requiresWarmup(from)) {
            from.sendMessage(Text.of(Util.getMessageWithFormat("teleport.warmup", String.valueOf(time))));
            plugin.getWarmupManager().addWarmup(from.getUniqueId(),
                    Sponge.getScheduler().createTaskBuilder().delay(time, TimeUnit.SECONDS)
                        .execute(tt).name("QuickStart - Teleport Waiter").submit(plugin));
        } else {
            tt.run();
        }

        return true;
    }

    public static boolean canBypassTpToggle(Player from) {
        return from.hasPermission(PermissionUtil.PERMISSIONS_ADMIN) || from.hasPermission(tptoggleBypassPermission);
    }

    public boolean requiresWarmup(Player from) {
        return from.hasPermission(PermissionUtil.PERMISSIONS_ADMIN) || from.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "teleport.warmup.exempt");
    }

    public static class TeleportTask implements CancellableTask {

        private final Player from;
        private final Player to;
        private final Player charged;
        private final double cost;

        private TeleportTask(Player from, Player to) {
            this.from = from;
            this.to = to;
            this.cost = 0;
            this.charged = null;
        }

        private TeleportTask(Player from, Player to, Player charged, double cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.charged = charged;
        }

        private void run() {
            if (to.isOnline()) {
                from.setLocationAndRotationSafely(to.getLocation(), to.getRotation());
                from.sendMessage(Text.of(Util.getMessageWithFormat("teleport.success", to.getName())));
                to.sendMessage(Text.of(Util.getMessageWithFormat("teleport.from.success", from.getName())));
            } else {
                from.sendMessage(Text.of(Util.messageBundle.getString("teleport.fail")));
                onCancel();
            }
        }

        @Override
        public void accept(Task task) {
            run();
        }

        @Override
        public void onCancel() {
            if (charged == null || cost <= 0) {
                return;
            }

            Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
            if (oes.isPresent()) {
                Optional<UniqueAccount> oua = oes.get().getAccount(charged.getUniqueId());
                oua.get().deposit(oes.get().getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.of(this));
            }
        }
    }
}
