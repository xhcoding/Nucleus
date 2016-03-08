/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.playerinfo;

import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.commands.misc.SpeedCommand;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
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
@RegisterCommand({"seen", "seenplayer"})
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
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey)))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        InternalNucleusUser iqsu = plugin.getUserLoader().getUser(user);

        List<Text> messages = new ArrayList<>();

        // Everyone gets the last online time.
        if (user.isOnline()) {
            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.iscurrently", user.getName())).append(Text.of(" "))
                    .append(Util.getTextMessageWithFormat("standard.online")).build());
            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.loggedon")).append(Text.of(" "))
                    .append(Text.of(TextColors.GREEN, Util.getTimeToNow(iqsu.getLastLogin()))).build());
        } else {
            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.iscurrently", user.getName()))
                    .append(Text.of(" ", TextColors.RED, Util.getTextMessageWithFormat("standard.offline"))).build());
            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.loggedoff"))
                    .append(Text.of(" ", TextColors.GREEN, Util.getTimeToNow(iqsu.getLastLogout()))).build());
        }

        messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.displayname")).append(Text.of(" "))
                .append(NameUtil.getName(user, iqsu)).build());
        if (permissions.testSuffix(src, "extended")) {
            if (user.isOnline()) {
                Player pl = user.getPlayer().get();
                messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.ipaddress"))
                        .append(Text.of(" ", pl.getConnection().getAddress().getAddress().toString())).build());

                messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.speed.walk")).append(Text.of(" "))
                        .append(Text.of(TextColors.YELLOW, Math.round(pl.get(Keys.WALKING_SPEED).orElse(0.1d) * SpeedCommand.multiplier))).build());

                messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.speed.flying")).append(Text.of(" "))
                        .append(Text.of(TextColors.YELLOW, Math.round(pl.get(Keys.FLYING_SPEED).orElse(0.05d) * SpeedCommand.multiplier))).build());
            }

            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.isjailed")).append(Text.of(" ")).color(TextColors.AQUA)
                    .append(getTrueOrFalse(iqsu.getJailData().isPresent(), TextActions.runCommand("/checkjail " + user.getName()))).build());
            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.ismuted")).append(Text.of(" ")).color(TextColors.AQUA)
                    .append(getTrueOrFalse(iqsu.getMuteData().isPresent(), TextActions.runCommand("/checkmute " + user.getName()))).build());

            BanService bs = Sponge.getServiceManager().provideUnchecked(BanService.class);
            messages.add(Text.builder().append(Util.getTextMessageWithFormat("command.seen.isbanned")).append(Text.of(" ")).color(TextColors.AQUA)
                    .append(getTrueOrFalse(bs.getBanFor(user.getProfile()).isPresent(), TextActions.runCommand("/checkban " + user.getName())))
                    .build());
        }

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        ps.builder().contents(messages).padding(Text.of(TextColors.GREEN, "-")).title(Util.getTextMessageWithFormat("command.seen.title", user.getName())).sendTo(src);
        return CommandResult.success();
    }

    private Text getTrueOrFalse(boolean is, ClickAction ifTrue) {
        if (is) {
            return Text.builder().append(Util.getTextMessageWithFormat("true")).style(TextStyles.UNDERLINE)
                    .onHover(TextActions.showText(Util.getTextMessageWithFormat("standard.clicktoseemore"))).onClick(ifTrue).build();
        }

        return Text.of(Util.getTextMessageWithFormat("false"));
    }
}
