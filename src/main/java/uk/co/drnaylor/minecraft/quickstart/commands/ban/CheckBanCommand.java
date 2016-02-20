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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.ban.Ban;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

import java.util.Optional;

@RootCommand
@Permissions(root = "ban", includeMod = true)
@Modules(PluginModule.BANS)
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
public class CheckBanCommand extends CommandBase {
    private final String key = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new UserParser(Text.of(key)))).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "checkban" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(key).get();

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        Optional<Ban.Profile> obp = service.getBanFor(u.getProfile());
        if (!obp.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.checkban.notset", u.getName())));
            return CommandResult.success();
        }

        Ban.Profile bp = obp.get();
        String f = "";
        String t = "";
        if (bp.getExpirationDate().isPresent()) {
            f = Util.getMessageWithFormat("standard.for");
            t = Util.getTimeToNow(bp.getExpirationDate().get());
        }

        String reason;
        if (bp.getBanSource().isPresent()) {
            reason = bp.getBanSource().get().toPlain();
        } else {
            reason = Util.getMessageWithFormat("standard.unknown");
        }

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.checkban.banned", u.getName(), reason, f, t)));
        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("standard.reason", bp.getReason().toPlain())));
        return CommandResult.success();
    }
}
