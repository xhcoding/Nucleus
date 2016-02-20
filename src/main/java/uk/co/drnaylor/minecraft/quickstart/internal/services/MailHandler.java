/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal.services;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.BetweenInstantsData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailFilter;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchPlayerException;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartMailService;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MailHandler implements QuickStartMailService {

    private final Game game;
    private final QuickStart plugin;

    public MailHandler(Game game, QuickStart plugin) {
        this.game = game;
        this.plugin = plugin;
    }

    @Override
    public List<MailData> getMail(User player, MailFilter... filters) {
        InternalQuickStartUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return null;
        }

        List<MailData> lmd = iqsu.getMail();
        if (filters.length == 0 || lmd.isEmpty()) {
            return lmd;
        }

        List<MailFilter> lmf = Arrays.asList(filters);
        Optional<DateFilter> odf = lmf.stream().filter(d -> d instanceof DateFilter).map(d -> (DateFilter)d).findFirst();
        if (odf.isPresent()) {
            BetweenInstantsData df = odf.get().getSuppliedData();
            lmd = lmd.stream().filter(x -> df.from().orElseGet(() -> Instant.ofEpochSecond(0)).isBefore(x.getDate()) &&
                    df.to().orElseGet(() -> Instant.now().plus(1, ChronoUnit.DAYS)).isAfter(x.getDate())).collect(Collectors.toList());
        }

        // Get players.
        List<UUID> pf = lmf.stream().filter(x -> x instanceof PlayerFilter).map(d -> ((PlayerFilter) d).getSuppliedData()).collect(Collectors.toList());
        if (lmf.stream().filter(x -> x instanceof ConsoleFilter).findFirst().isPresent()) {
            pf.add(Util.consoleFakeUUID);
        }

        // Add the predicates
        if (!pf.isEmpty()) {
            // Check the UUIDs - if they are in the list, let them through.
            lmd = lmd.stream().filter(x -> pf.contains(x.getUuid())).collect(Collectors.toList());
        }

        // Message parts
        List<String> m = lmf.stream().filter(x -> x instanceof MessageFilter).map(d -> ((MessageFilter) d).getSuppliedData().toLowerCase())
                .collect(Collectors.toList());
        if (!m.isEmpty()) {
            // For each mail, check to see if any filters match after everything goes lowercase.
            lmd = lmd.stream().filter(x -> m.stream().allMatch(a -> x.getMessage().toLowerCase().contains(a.toLowerCase()))).collect(Collectors.toList());
        }

        return lmd;
    }

    @Override
    public void sendMail(User playerFrom, User playerTo, String message) {
        InternalQuickStartUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(playerTo);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return;
        }

        MailData md = new MailData(playerFrom == null ? Util.consoleFakeUUID : playerFrom.getUniqueId(), Instant.now(), message);
        iqsu.addMail(md);

        Text from = playerFrom == null ? Text.of(game.getServer().getConsole().getName()) : Util.getName(playerFrom);
        if (playerTo.isOnline()) {
            playerTo.getPlayer().get().sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("mail.youvegotmail") + " ", from));
        }
    }

    @Override
    public void sendMailFromConsole(User playerTo, String message) {
        sendMail(null, playerTo, message);
    }

    @Override
    public void clearUserMail(User player) {
        InternalQuickStartUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return;
        }

        iqsu.clearMail();
    }

    @Override
    public MailFilter<UUID> createPlayerFilter(UUID player) throws NoSuchPlayerException {
        if (!game.getServiceManager().provideUnchecked(UserStorageService.class).get(player).isPresent()) {
            throw new NoSuchPlayerException();
        }

        return new PlayerFilter(player);
    }

    @Override
    public MailFilter<Void> createConsoleFilter() {
        return new ConsoleFilter();
    }

    @Override
    public MailFilter<BetweenInstantsData> createDateFilter(Instant from, Instant to) {
        Preconditions.checkState(from != null || to != null);
        return new DateFilter(new BetweenInstantsData(from, to));
    }

    @Override
    public MailFilter<String> createMessageFilter(String message) {
        return new MessageFilter(message);
    }

    private static class ConsoleFilter implements MailFilter<Void> {
        @Override
        public Void getSuppliedData() { return null; }
    }

    private static class PlayerFilter implements MailFilter<UUID> {

        private final UUID uuid;

        public PlayerFilter(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public UUID getSuppliedData() {
            return uuid;
        }
    }

    private static class DateFilter implements MailFilter<BetweenInstantsData> {

        private final BetweenInstantsData bid;

        public DateFilter(BetweenInstantsData bid) {
            this.bid = bid;
        }

        @Override
        public BetweenInstantsData getSuppliedData() {
            return bid;
        }
    }

    private static class MessageFilter implements MailFilter<String> {

        private final String message;

        public MessageFilter(String message) {
            this.message = message;
        }

        @Override
        public String getSuppliedData() {
            return message;
        }
    }
}
