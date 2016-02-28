/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.warp;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RegisterCommand;

import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * Creates a warp where the player is currently standing. The warp must not exist.
 *
 * Command Usage: /warp set [warp]
 * Permission: quickstart.warp.set.base
 */
@Permissions(root = "warp")
@RegisterCommand(value = { "set" }, subcommandOf = WarpCommand.class)
public class SetWarpCommand extends CommandBase<Player> {
    private final Pattern warpRegex = Pattern.compile("^[A-Za-z][A-Za-z0-9]{0,25}$");

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of(WarpCommand.warpNameArg)))
                )
                .description(Text.of("Sets a warp at the player's location."))
                .build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "set" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        String warp = args.<String>getOne(WarpCommand.warpNameArg).get();

        // Needs to match the name...
        if (!warpRegex.matcher(warp).matches()) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.warps.invalidname")));
            return CommandResult.empty();
        }

        // Get the service, does the warp exist?
        QuickStartWarpService qs = Sponge.getServiceManager().provideUnchecked(QuickStartWarpService.class);
        if (qs.getWarp(warp).isPresent()) {
            // You have to delete to set the same name
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.warps.nooverwrite")));
            return CommandResult.empty();
        }

        // OK! Set it.
        if (qs.setWarp(warp, src.getLocation(), src.getRotation())) {
            // Worked. Tell them.
            src.sendMessage(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat("command.warps.set"), warp)));
            return CommandResult.success();
        }

        // Didn't work. Tell them.
        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.warps.seterror")));
        return CommandResult.empty();
    }
}
