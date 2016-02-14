package uk.co.drnaylor.minecraft.quickstart.commands.misc;

import com.google.common.collect.Sets;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireOneOfPermission;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;

import java.util.Optional;
import java.util.Set;

@Permissions
@Modules(PluginModule.MISC)
@RootCommand
public class HealCommand extends CommandBase {
    private static final String player = "player";

    @Override
    public CommandSpec createSpec() {
        Set<String> ss = Sets.newHashSet(PermissionUtil.PERMISSIONS_PREFIX + "heal.others", PermissionUtil.PERMISSIONS_ADMIN);

        return CommandSpec.builder().executor(this).arguments(
                new RequireOneOfPermission(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.player(Text.of(player)))), ss)
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "heal" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = Util.getPlayerFromOptionalOrSource(args.<Player>getOne(player), src);
        if (!opl.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.playeronly")));
            return CommandResult.empty();
        }

        Player pl = opl.get();
        if (pl.offer(Keys.HEALTH, pl.get(Keys.MAX_HEALTH).get()).isSuccessful()) {
            pl.sendMessages(Text.of(TextColors.GREEN, Util.messageBundle.getString("command.heal.success")));
            if (!pl.equals(src)) {
                src.sendMessages(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.heal.success.other", pl.getName())));
            }

            return CommandResult.success();
        } else {
            src.sendMessages(Text.of(TextColors.RED, Util.messageBundle.getString("command.heal.error")));
            return CommandResult.empty();
        }
    }
}
