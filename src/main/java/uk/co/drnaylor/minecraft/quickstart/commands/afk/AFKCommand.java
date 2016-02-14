package uk.co.drnaylor.minecraft.quickstart.commands.afk;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.AFKHandler;

@RootCommand
@Permissions(includeUser = true)
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
    public String[] getAliases() {
        return AFKCommand.getAfkAliases();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        AFKHandler afkHandler = plugin.getAfkHandler();
        if (src.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "afk.exempt") || afkHandler.getAFKData(src).notTracked()) {
            src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.afk.exempt")));
            return CommandResult.empty();
        }

        boolean isAFK = afkHandler.getAFKData(src).isAFK();

        if (isAFK) {
            afkHandler.updateUserActivity(src.getUniqueId());
            MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", Util.getName(src), TextColors.GRAY, " " + Util.messageBundle.getString("afk.fromafk")));
        } else {
            afkHandler.setAFK(src.getUniqueId(), true);
            MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", Util.getName(src), TextColors.GRAY, " " + Util.messageBundle.getString("afk.toafk")));
        }

        return CommandResult.success();
    }
}
