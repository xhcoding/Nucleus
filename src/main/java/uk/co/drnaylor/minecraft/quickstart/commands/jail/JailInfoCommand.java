/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.jail;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.JailParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.SuggestedLevel;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;

@NoCooldown
@NoCost
@NoWarmup
@Permissions(root = "jail", suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@ChildOf(parentCommandClass = JailInfoCommand.class, parentCommand = "jails")
public class JailInfoCommand extends CommandBase {
    private final String jailKey = "jail";
    @Inject private JailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new JailParser(Text.of(jailKey), handler))).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "info" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WarpLocation wl = args.<WarpLocation>getOne(jailKey).get();
        src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.jail.info.name") + ": ", TextColors.GREEN, wl.getName()));
        src.sendMessage(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.jail.info.location") + ": ", TextColors.GREEN, wl.toLocationString()));
        return CommandResult.success();
    }
}
