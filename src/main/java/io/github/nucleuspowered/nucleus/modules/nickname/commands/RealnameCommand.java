/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.commands;

import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.UserService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RegisterCommand({"realname"})
@Permissions(suggestedLevel = SuggestedLevel.USER)
public class RealnameCommand extends CommandBase<CommandSource> {

    private final String playerKey = "name";

    @Inject private UserDataManager udm;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.string(Text.of(playerKey))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String argname = args.<String>getOne(playerKey).get();
        String name = argname.toLowerCase();

        // First, get all online players.
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();

        List<Text> realNames = players.stream().map(x -> {
            // I can't get display name to work!
            Optional<UserService> ous = udm.get(x);
            if (ous.isPresent()) {
                Optional<Text> ot = ous.get().getNicknameAsText();
                if (ot.isPresent()) {
                    return new NameTuple(ot.get().toPlain().toLowerCase(), x);
                }
            }

            Optional<Text> displayName = x.getDisplayNameData().displayName().getDirect();
            if (displayName.isPresent()) {
                return new NameTuple(displayName.get().toPlain().toLowerCase(), x);
            }

            return new NameTuple(x.getName().toLowerCase(), x);
        }).filter(x -> x.nickname.startsWith(name.toLowerCase()))
                .map(x -> Text.builder().append(NameUtil.getName(x.player, udm)).append(Text.of(TextColors.GRAY, " -> ")).append(Text.of(x.player.getName())).toText())
                .collect(Collectors.toList());

        if (realNames.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.realname.nonames", argname));
        } else {
            PaginationList.Builder plb = Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder()
                    .contents(realNames)
                    .padding(Text.of(TextColors.GREEN, "-"))
                    .title(Util.getTextMessageWithFormat("command.realname.title", argname));

            if (!(src instanceof Player)) {
                plb.linesPerPage(-1);
            }

            plb.sendTo(src);
        }

        return CommandResult.success();
    }

    private class NameTuple {
        private final String nickname;
        private final Player player;

        private NameTuple(String nickname, Player player) {
            this.nickname = nickname;
            this.player = player;
        }
    }
}
