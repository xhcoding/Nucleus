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

    public TeleportHandler(QuickStart plugin) {
        this.plugin = plugin;
    }

    public boolean startTeleport(Player from, Player to, boolean checkToggle) throws Exception {
        return startTeleport(from, to, 0, true, checkToggle);
    }

    public boolean startTeleport(Player from, Player to, double cost, boolean chargeFrom, boolean checkToggle) throws Exception {
        InternalQuickStartUser toPlayer = plugin.getUserLoader().getUser(to);
        if (checkToggle && !toPlayer.isTeleportToggled() && !canBypass(from)) {
            from.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("teleport.fail.targettoggle", to.getName())));
            return false;
        }

        TeleportTask tt;
        if (cost > 0) {
            tt = new TeleportTask(from, to, chargeFrom ? from : to, cost);
        } else {
            tt = new TeleportTask(from, to);
        }

        if (requiresWarmup(from)) {
            plugin.getWarmupManager().addWarmup(from.getUniqueId(),
                    Sponge.getScheduler().createTaskBuilder().delay(plugin.getConfig(ConfigMap.MAIN_CONFIG).get().getTeleportWarmup(), TimeUnit.SECONDS)
                        .execute(tt).name("QuickStart - Teleport Waiter").submit(plugin));
            return true;
        }

        tt.run();
        return true;
    }

    public boolean canBypass(Player from) {
        return from.hasPermission(PermissionUtil.PERMISSIONS_ADMIN) || from.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "teleport.tptoggle.exempt");
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
            } else {
                to.sendMessage(Text.of(Util.messageBundle.getString("teleport.fail")));
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
