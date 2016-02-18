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
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireOneOfPermission;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.TeleportHandler;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Permissions(root = "teleport", includeMod = true)
@Modules(PluginModule.TELEPORT)
@RunAsync
@NoWarmup(generateConfigEntry = true)
@RootCommand
public class TeleportAskHereCommand extends CommandBase<Player> {
    @Inject
    private TeleportHandler tpHandler;

    private final String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                new RequireOneOfPermission(GenericArguments.flags().flag("f").buildWith(GenericArguments.none()), permissions.getPermissionWithSuffix("force")),
                GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey))))
            .executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "tpahere", "tpaskhere", "teleportaskhere" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(playerKey).get();
        if (src.equals(target)) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.teleport.self")));
            return CommandResult.empty();
        }

        TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(target).setTo(src).setSafe(!args.<Boolean>getOne("f").orElse(false));
        int warmup = getWarmup(target);
        if (warmup > 0) {
            tb.setWarmupTime(warmup);
        }

        double cost = getCost(src, args);
        if (cost > 0.) {
            tb.setCharge(src).setCost(cost);
        }

        // The question needs to be asked of the target
        tpHandler.addAskQuestion(target.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), src, cost, tb));
        target.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpahere.question", src.getName())));
        target.sendMessage(tpHandler.getAcceptDenyMessage());

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tpask.sent")));
        return CommandResult.success();
    }
}
