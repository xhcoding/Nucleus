package uk.co.drnaylor.minecraft.quickstart.commands.teleport;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.TeleportHandler;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCost
@NoCooldown
@RootCommand
@RunAsync
public class TeleportAskAllHereCommand extends CommandBase<Player> {
    @Inject private TeleportHandler tpHandler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.none())).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tpaall", "tpaskall" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Sponge.getServer().getOnlinePlayers().forEach(x -> {
            if (x.equals(src)) {
                return;
            }

            TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(x).setTo(src).setSafe(!args.<Boolean>getOne("f").orElse(false)).setBypassToggle(true).setSilentSource(true);
            tpHandler.addAskQuestion(x.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), null, 0, tb));

            x.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpahere.question", src.getName())));

            x.sendMessage(Text.builder()
                .append(
                    Text.builder(Util.getMessageWithFormat("standard.accept")).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                        .onHover(TextActions.showText(Text.of(Util.getMessageWithFormat("teleport.accept.hover")))).onClick(TextActions.runCommand("/tpaccept")).build())
                .append(Text.of(" - "))
                .append(Text.builder(Util.getMessageWithFormat("standard.deny")).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                        .onHover(TextActions.showText(Text.of(Util.getMessageWithFormat("teleport.deny.hover")))).onClick(TextActions.runCommand("/tpdeny")).build())
                .build());
        });

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpaall.success")));
        return CommandResult.success();
    }
}
