package uk.co.drnaylor.minecraft.quickstart.commands.misc;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireOneOfPermission;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.text.MessageFormat;
import java.util.Optional;

@Permissions
@Modules(PluginModule.MISC)
@NoCooldown
@NoWarmup
@NoCost
public class GodCommand extends CommandBase {
    private final String playerKey = "player";
    private final String invulnKey = "invuln";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                new RequireOneOfPermission(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey))), permissions.getPermissionWithSuffix("others")),
                GenericArguments.onlyOne(GenericArguments.bool(Text.of(invulnKey)))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "god", "invuln", "invulnerability" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = args.<Player>getOne(playerKey);
        Player pl;
        if (opl.isPresent()) {
            pl = opl.get();
        } else {
            if (src instanceof Player) {
                pl = (Player)src;
            } else {
                src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.playeronly")));
                return CommandResult.empty();
            }
        }

        boolean god = args.<Boolean>getOne(invulnKey).get();
        plugin.getUserLoader().getUser(pl).setInvulnerable(god);

        DataTransactionResult tr;
        if (god) {
            tr = pl.offer(Keys.INVULNERABILITY, Integer.MAX_VALUE);
        } else {
            tr = pl.remove(Keys.INVULNERABILITY);
        }

        if (!tr.isSuccessful()) {
            src.sendMessages(Text.of(TextColors.RED, Util.messageBundle.getString("command.god.error")));
            return CommandResult.empty();
        }

        if (pl != src) {
            src.sendMessages(Text.of(TextColors.GREEN, MessageFormat.format(Util.messageBundle.getString(god ? "command.god.player.on" : "command.god.player.off"), pl.getName())));
        }

        pl.sendMessage(Text.of(TextColors.GREEN, Util.messageBundle.getString(god ? "command.god.on" : "command.god.off")));
        return CommandResult.success();
    }
}
