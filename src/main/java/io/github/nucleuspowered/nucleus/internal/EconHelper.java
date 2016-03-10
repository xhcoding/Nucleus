/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Optional;

public class EconHelper {

    private final Nucleus plugin;

    public EconHelper(Nucleus plugin) {
        this.plugin = plugin;
    }

    public String getCurrencySymbol(double cost) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            return oes.get().getDefaultCurrency().format(BigDecimal.valueOf(cost)).toPlain();
        }

        return String.valueOf(cost);
    }

    public boolean withdrawFromPlayer(Player src, double cost) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> a = es.getOrCreateAccount(src.getUniqueId());
            if (!a.isPresent()) {
                src.sendMessage(Util.getTextMessageWithFormat("cost.noaccount"));
                return false;
            }

            TransactionResult tr = a.get().withdraw(es.getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.source(plugin).build());
            if (tr.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
                src.sendMessage(Util.getTextMessageWithFormat("cost.nofunds", getCurrencySymbol(cost)));
                return false;
            } else if (tr.getResult() != ResultType.SUCCESS) {
                src.sendMessage(Util.getTextMessageWithFormat("cost.error"));
                return false;
            }

            src.sendMessage(Util.getTextMessageWithFormat("cost.complete", getCurrencySymbol(cost)));
        }

        return true;
    }

    public boolean depositInPlayer(User src, double cost) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> a = es.getOrCreateAccount(src.getUniqueId());
            if (!a.isPresent() && src.isOnline()) {
                src.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("cost.noaccount"));
                return false;
            }

            TransactionResult tr = a.get().deposit(es.getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.source(plugin).build());
            if (tr.getResult() != ResultType.SUCCESS && src.isOnline()) {
                src.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("cost.error"));
                return false;
            }

            if (src.isOnline()) {
                src.getPlayer().get().sendMessage(Util.getTextMessageWithFormat("cost.refund", getCurrencySymbol(cost)));
            }
        }

        return true;
    }
}
