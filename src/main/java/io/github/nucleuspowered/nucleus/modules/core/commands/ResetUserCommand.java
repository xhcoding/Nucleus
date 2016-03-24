/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Permissions(root = "nucleus")
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand(value = "resetuser", subcommandOf = NucleusCommand.class)
public class ResetUserCommand extends CommandBase<CommandSource> {

    private final String userKey = "user";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.user(Text.of(userKey))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        final User user = args.<User>getOne(userKey).get();

        List<Text> messages = new ArrayList<>();

        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning"));
        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning2", user.getName()));
        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning3"));
        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning4"));
        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning5"));
        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning6"));
        messages.add(Util.getTextMessageWithFormat("command.nucleus.reset.warning7"));
        messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.nucleus.reset.reset")).style(TextStyles.UNDERLINE)
                .onClick(TextActions.executeCallback(new Delete(plugin, user))).build());

        src.sendMessages(messages);
        return CommandResult.success();
    }

    private class Delete implements Consumer<CommandSource> {

        private final User user;
        private final Nucleus plugin;

        public Delete(Nucleus plugin, User user) {
            this.user = user;
            this.plugin = plugin;
        }

        @Override
        public void accept(CommandSource source) {
            if (user.isOnline()) {
                user.getPlayer().get().kick(Util.getTextMessageWithFormat("command.kick.defaultreason"));
            }

            // Ban temporarily.
            final BanService bss = Sponge.getServiceManager().provideUnchecked(BanService.class);
            final boolean isBanned = bss.getBanFor(user.getProfile()).isPresent();
            bss.addBan(Ban.builder().expirationDate(Instant.now().plus(30, ChronoUnit.SECONDS)).profile(user.getProfile()).type(BanTypes.PROFILE)
                    .build());

            // Unload the player in a second, just to let events fire.
            Sponge.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
                UserConfigLoader ucl = plugin.getUserLoader();

                // Remove them from the cache immediately.
                ucl.forceUnloadPlayerWithoutSaving(user.getUniqueId());

                // Get the file to delete.
                try {
                    Path file = ucl.getUserPath(user.getUniqueId());
                    Files.delete(file);
                    source.sendMessage(Util.getTextMessageWithFormat("command.nucleus.reset.complete", user.getName()));
                } catch (IOException e) {
                    source.sendMessage(Util.getTextMessageWithFormat("command.nucleus.reset.failed", user.getName()));
                } finally {
                    if (!isBanned) {
                        bss.getBanFor(user.getProfile()).ifPresent(bss::removeBan);
                    }
                }
            } , 1, TimeUnit.SECONDS);
        }
    }
}
