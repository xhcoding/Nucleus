package uk.co.drnaylor.minecraft.quickstart.commands.misc;

import com.google.common.collect.Sets;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
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
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;

@Permissions
@Modules(PluginModule.MISC)
@RootCommand
public class FlyCommand extends CommandBase {
    private static final String player = "player";
    private static final String toggle = "toggle";

    @Override
    public CommandSpec createSpec() {
        Set<String> ss = Sets.newHashSet(PermissionUtil.PERMISSIONS_PREFIX + "fly.others", PermissionUtil.PERMISSIONS_ADMIN);

        return CommandSpec.builder().executor(this).arguments(
                new RequireOneOfPermission(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.player(Text.of(player)))), ss),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggle))))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "fly" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = args.<Player>getOne(player);
        Player pl;
        if (opl.isPresent()) {
            pl = opl.get();
        } else {
            if (src instanceof Player) {
                pl = (Player)src;
            } else {
                src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.playeronly")));
                return CommandResult.empty();
            }
        }

        InternalQuickStartUser uc = plugin.getUserLoader().getUser(pl);
        boolean fly = args.<Boolean>getOne(toggle).orElse(!uc.isFlying());

        if (!uc.setFlying(fly)) {
            src.sendMessages(Text.of(TextColors.RED, Util.getMessageWithFormat("command.fly.error")));
            return CommandResult.empty();
        }

        if (pl != src) {
            src.sendMessages(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat(fly ? "command.fly.player.on" : "command.fly.player.off"), pl.getName())));
        }

        pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat(fly ? "command.fly.on" : "command.fly.off")));
        return CommandResult.success();
    }
}
