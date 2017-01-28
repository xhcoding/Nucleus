/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Permissions(prefix = "nucleus", suggestedLevel = SuggestedLevel.NONE)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand(value = "resetuser", subcommandOf = NucleusCommand.class)
public class ResetUserCommand extends AbstractCommand<CommandSource> {

    private final String userKey = "user";
    private final String uuidKey = "UUID";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("a", "-all").buildWith(
                GenericArguments.firstParsing(
                    GenericArguments.user(Text.of(userKey)),
                    new UUIDArgument<>(Text.of(uuidKey), u -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(u))
                ))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        final User user = args.<User>getOne(userKey).orElseGet(() -> args.<User>getOne(uuidKey).get());
        final boolean deleteall = args.hasAny("a");

        List<Text> messages = new ArrayList<>();

        messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning"));
        messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning2", user.getName()));
        messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning3"));
        messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning4"));
        messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning5"));
        messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning6"));
        if (deleteall) {
            messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning8"));
        } else {
            messages.add(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.warning7"));
        }

        messages.add(Text.builder().append(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.reset")).style(TextStyles.UNDERLINE)
                .onClick(TextActions.executeCallback(new Delete(plugin, user, deleteall))).build());

        src.sendMessages(messages);
        return CommandResult.success();
    }

    private class Delete implements Consumer<CommandSource> {

        private final User user;
        private final NucleusPlugin plugin;
        private final boolean all;

        public Delete(NucleusPlugin plugin, User user, boolean all) {
            this.user = user;
            this.plugin = plugin;
            this.all = all;
        }

        @Override
        public void accept(CommandSource source) {
            if (user.isOnline()) {
                user.getPlayer().get().kick(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kick.defaultreason"));

                // Let Sponge do what it needs to close the user off.
                Task.builder().execute(() -> this.accept(source)).delayTicks(1).submit(this.plugin);
                return;
            }

            source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.starting", user.getName()));

            // Ban temporarily.
            final BanService bss = Sponge.getServiceManager().provideUnchecked(BanService.class);
            final boolean isBanned = bss.getBanFor(user.getProfile()).isPresent();
            bss.addBan(Ban.builder().type(BanTypes.PROFILE).expirationDate(Instant.now().plus(30, ChronoUnit.SECONDS)).profile(user.getProfile())
                    .build());

            // Unload the player in a second, just to let events fire.
            Sponge.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
                UserDataManager ucl = plugin.getUserDataManager();

                // Get the file to delete.
                try {
                    // Remove them from the cache immediately.
                    ucl.forceUnloadAndDelete(user.getUniqueId());
                    if (all) {
                        if (Sponge.getServiceManager().provideUnchecked(UserStorageService.class).delete(user)) {
                            source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider()
                                .getTextMessageWithFormat("command.nucleus.reset.completeall", user.getName()));
                        } else {
                            source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.completenonm", user.getName()));
                        }
                    } else {
                        source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider()
                            .getTextMessageWithFormat("command.nucleus.reset.complete", user.getName()));
                    }

                    source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider()
                        .getTextMessageWithFormat("command.nucleus.reset.restartadvised", user.getName()));
                } catch (Exception e) {
                    source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.reset.failed", user.getName()));
                } finally {
                    if (!isBanned) {
                        bss.getBanFor(user.getProfile()).ifPresent(bss::removeBan);
                    }
                }
            } , 1, TimeUnit.SECONDS);
        }
    }
}
