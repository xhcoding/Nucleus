package uk.co.drnaylor.minecraft.quickstart.commands.misc;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;

@Permissions
@RootCommand
@Modules(PluginModule.MISC)
public class KillCommand extends CommandBase {
    private final String key = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.player(Text.of(key))).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "kill" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(key).get();
        GameMode gm = pl.gameMode().getDirect().orElse(pl.gameMode().getDefault());
        if (gm != GameModes.SURVIVAL && gm != GameModes.NOT_SET) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.kill.wronggm", pl.getName())));
            return CommandResult.empty();
        }

        pl.offer(Keys.HEALTH, 0d);
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.kill.killed", pl.getName())));
        pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.kill.killedby", src.getName())));
        return CommandResult.success();
    }
}
