/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.home;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.HomeOtherParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

@Permissions(root = "home", alias = "deleteother", includeUser = true)
@Modules(PluginModule.HOMES)
@RunAsync
@NoCooldown
@NoCost
@NoWarmup
@RootCommand
public class DeleteOtherHomeCommand extends CommandBase {

    private final String homeKey = "home";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new HomeOtherParser(Text.of(homeKey), plugin)))
                .executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "deletehomeother", "delhomeother" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        HomeOtherParser.HomeData wl = args.<HomeOtherParser.HomeData>getOne(homeKey).get();
        if (plugin.getUserLoader().getUser(wl.user).deleteHome(wl.location.getName())) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.home.delete.other.success", wl.user.getName(), wl.location.getName())));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.home.delete.other.fail", wl.user.getName(), wl.location.getName())));
        return CommandResult.empty();
    }
}
