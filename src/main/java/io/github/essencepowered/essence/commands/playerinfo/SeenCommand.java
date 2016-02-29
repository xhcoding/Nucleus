/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.playerinfo;


import io.github.essencepowered.essence.NameUtil;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.argumentparsers.UserParser;
import io.github.essencepowered.essence.commands.misc.SpeedCommand;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.interfaces.InternalQuickStartUser;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Permissions
@RunAsync
@Modules(PluginModule.PLAYERINFO)
@RegisterCommand({ "seen", "seenplayer" })
public class SeenCommand extends CommandBase<CommandSource> {
    private final String playerKey = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("extended", new PermissionInformation(Util.getMessageWithFormat("permission.seen.extended"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new UserParser(Text.of(playerKey)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        InternalQuickStartUser iqsu = plugin.getUserLoader().getUser(user);

        List<Text> messages = new ArrayList<>();

        // Everyone gets the last online time.
        if (user.isOnline()) {
            messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.iscurrently", user.getName()) + " ", TextColors.GREEN, Util.getMessageWithFormat("standard.online")));
            messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.loggedon") + " ", TextColors.GREEN, Util.getTimeToNow(iqsu.getLastLogin())));
        } else {
            messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.iscurrently", user.getName()) + " ", TextColors.RED, Util.getMessageWithFormat("standard.offline")));
            messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.loggedoff") + " ", TextColors.GREEN, Util.getTimeToNow(iqsu.getLastLogout())));
        }

        messages.add(Text.builder(Util.getMessageWithFormat("command.seen.displayname") + " ").color(TextColors.AQUA).append(NameUtil.getName(user, iqsu)).build());
        if (permissions.testSuffix(src, "extended")) {
            if (user.isOnline()) {
                Player pl = user.getPlayer().get();
                messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.ipaddress") + " ", TextColors.GREEN, pl.getConnection().getAddress().getAddress().toString()));

                messages.add(Text.builder()
                        .append(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.speed.walk")))
                        .append(Text.of(" "))
                        .append(Text.of(TextColors.YELLOW, Math.round(pl.get(Keys.WALKING_SPEED).orElse(0.1d) * SpeedCommand.multiplier))).build());

                messages.add(Text.builder()
                        .append(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.speed.flying")))
                        .append(Text.of(" "))
                        .append(Text.of(TextColors.YELLOW, Math.round(pl.get(Keys.FLYING_SPEED).orElse(0.05d) * SpeedCommand.multiplier))).build());
            }

            messages.add(Text.builder(Util.getMessageWithFormat("command.seen.isjailed") + " ").color(TextColors.AQUA).append(
                    getTrueOrFalse(iqsu.getJailData().isPresent(), TextActions.runCommand("/checkjail " + user.getName()))).build());
            messages.add(Text.builder(Util.getMessageWithFormat("command.seen.ismuted") + " ").color(TextColors.AQUA).append(
                    getTrueOrFalse(iqsu.getMuteData().isPresent(), TextActions.runCommand("/checkmute " + user.getName()))).build());

            BanService bs = Sponge.getServiceManager().provideUnchecked(BanService.class);
            messages.add(Text.builder(Util.getMessageWithFormat("command.seen.isbanned") + " ").color(TextColors.AQUA).append(
                    getTrueOrFalse(bs.getBanFor(user.getProfile()).isPresent(), TextActions.runCommand("/checkban " + user.getName()))).build());
        }

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        ps.builder().contents(messages).paddingString("-").title(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.seen.title", user.getName()))).sendTo(src);
        return CommandResult.success();
    }

    private Text getTrueOrFalse(boolean is, ClickAction ifTrue) {
        if (is) {
            return Text.builder(Util.getMessageWithFormat("true")).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                    .onHover(TextActions.showText(Text.of(Util.getMessageWithFormat("standard.clicktoseemore")))).onClick(ifTrue).build();
        }

        return Text.of(TextColors.RED, Util.getMessageWithFormat("false"));
    }
}
