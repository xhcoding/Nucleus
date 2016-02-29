/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal;

import io.github.essencepowered.essence.QuickStart;
import io.github.essencepowered.essence.Util;
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

    private final QuickStart plugin;

    public EconHelper(QuickStart plugin) {
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
            Optional<UniqueAccount> a = es.getAccount(src.getUniqueId());
            if (!a.isPresent()) {
                src.sendMessage(Text.builder(Util.getMessageWithFormat("cost.noaccount"))
                        .color(TextColors.YELLOW).build());
                return false;
            }

            TransactionResult tr = a.get().withdraw(es.getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.of(plugin));
            if (tr.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
                src.sendMessage(Text.builder(MessageFormat.format(Util.getMessageWithFormat("cost.nofunds"), es.getDefaultCurrency().format(BigDecimal.valueOf(cost)).toPlain()))
                        .color(TextColors.YELLOW).build());
                return false;
            } else if (tr.getResult() != ResultType.SUCCESS) {
                src.sendMessage(Text.builder(Util.getMessageWithFormat("cost.error"))
                        .color(TextColors.YELLOW).build());
                return false;
            }

            src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("cost.complete", getCurrencySymbol(cost))));
        }

        return true;
    }

    public boolean depositInPlayer(User src, double cost) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> a = es.getAccount(src.getUniqueId());
            if (!a.isPresent() && src.isOnline()) {
                src.getPlayer().get().sendMessage(Text.builder(Util.getMessageWithFormat("cost.noaccount"))
                        .color(TextColors.YELLOW).build());
                return false;
            }

            TransactionResult tr = a.get().deposit(es.getDefaultCurrency(), BigDecimal.valueOf(cost), Cause.of(plugin));
            if (tr.getResult() != ResultType.SUCCESS && src.isOnline()) {
                src.getPlayer().get().sendMessage(Text.builder(Util.getMessageWithFormat("cost.error"))
                        .color(TextColors.YELLOW).build());
                return false;
            }

            if (src.isOnline()) {
                src.getPlayer().get().sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("cost.refund", getCurrencySymbol(cost))));
            }
        }

        return true;
    }
}
