package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.TeleportHandler;

import javax.inject.Inject;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCost
@NoCooldown
@RootCommand
public class TeleportAllHereCommand extends CommandBase<Player> {
    @Inject private TeleportHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.none())).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tpall", "tpallhere" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        MessageChannel.TO_ALL.send(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpall.broadcast", src.getName())));
        Sponge.getServer().getOnlinePlayers().forEach(x -> {
            if (x.equals(src)) {
                try {
                    handler.getBuilder().setFrom(x).setTo(src).setSafe(!args.<Boolean>getOne("f").get()).setSilentSource(true).setBypassToggle(true).startTeleport();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return CommandResult.success();
    }
}
