/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.afk;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.NameUtil;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionService;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.AFKHandler;

import java.util.HashMap;
import java.util.Map;

@RootCommand
@Permissions(suggestedLevel = PermissionService.SuggestedLevel.USER)
@Modules(PluginModule.AFK)
@NoCooldown
@NoWarmup
@NoCost
@RunAsync
public class AFKCommand extends CommandBase<Player> {
    public static String[] getAfkAliases() { return new String[] { "afk" }; }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public Map<String, PermissionService.SuggestedLevel> permissionSuffixesToRegister() {
        Map<String, PermissionService.SuggestedLevel> m = new HashMap<>();
        m.put("exempt.kick", PermissionService.SuggestedLevel.ADMIN);
        return m;
    }

    @Override
    public String[] getAliases() {
        return AFKCommand.getAfkAliases();
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
