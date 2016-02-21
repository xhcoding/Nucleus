/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.ban;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.ban.Ban;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.util.Optional;
import java.util.Set;

@RootCommand
@Modules(PluginModule.BANS)
@Permissions(root = "ban", includeMod = true)
@NoWarmup
@NoCooldown
@NoCost
public class UnbanCommand extends CommandBase {
    private final String key = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new UserParser(Text.of(key)))).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "unban" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(key).get();

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        Optional<Ban.Profile> obp = service.getBanFor(u.getProfile());
        if (!obp.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.checkban.notset", u.getName())));
            return CommandResult.empty();
        }

        service.removeBan(obp.get());

        Set<String> n = permissions.getPermissionWithSuffixFromRootOnly("notify", PermissionUtil.PermissionLevel.DEFAULT_USER);
        n.add(PermissionUtil.PERMISSIONS_MOD);
        String[] notify = n.toArray(new String[n.size()]);
        Player pl = null;
        if (src instanceof Player) {
            pl = (Player)src;
        }

        Util.getMessageChannel(pl, notify).send(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.unban.success", obp.get().getProfile().getName(), src.getName())));
        return CommandResult.success();
    }
}
