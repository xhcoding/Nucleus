package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

@Permissions(root = "teleport", includeUser = true, includeMod = true)
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
@RunAsync
public class TeleportToggleCommand extends CommandBase<Player> {
    private final String key = "toggle";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(key))))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tptoggle" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final InternalQuickStartUser iqsu = plugin.getUserLoader().getUser(src);
        boolean flip = args.<Boolean>getOne(key).orElseGet(() -> !iqsu.isTeleportToggled());
        iqsu.setTeleportToggled(flip);
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tptoggle.success", Util.messageBundle.getString(flip ? "enabled" : "disabled"))));
        return CommandResult.success();
    }
}
