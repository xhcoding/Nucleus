/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.afk;

import io.github.essencepowered.essence.NameUtil;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.AFKHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

@RegisterCommand("afk")
@Permissions(suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.AFK)
@NoCooldown
@NoWarmup
@NoCost
@RunAsync
public class AFKCommand extends CommandBase<Player> {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        AFKHandler afkHandler = plugin.getAfkHandler();
        if (permissions.testSuffix(src, "exempt.toggle")) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.afk.exempt")));
            return CommandResult.empty();
        }

        boolean isAFK = afkHandler.getAFKData(src).isAFK();

        if (isAFK) {
            afkHandler.updateUserActivity(src.getUniqueId());
            MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", NameUtil.getName(src), TextColors.GRAY, " " + Util.getMessageWithFormat("afk.fromafk")));
        } else {
            afkHandler.setAFK(src.getUniqueId(), true);
            MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", NameUtil.getName(src), TextColors.GRAY, " " + Util.getMessageWithFormat("afk.toafk")));
        }

        return CommandResult.success();
    }
}
