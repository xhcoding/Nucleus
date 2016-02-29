/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

@Permissions(root = "teleport")
@Modules(PluginModule.TELEPORT)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({ "tppos" })
public class TeleportPositionCommand extends CommandBase<CommandSource> {
    private final String key = "player";
    private final String location = "world";
    private final String x = "x";
    private final String y = "y";
    private final String z = "z";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.flags().flag("f").buildWith(GenericArguments.none()),
                GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(key))),
                GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.world(Text.of(location)))),
                GenericArguments.onlyOne(GenericArguments.integer(Text.of(x))),
                GenericArguments.onlyOne(GenericArguments.integer(Text.of(y))),
                GenericArguments.onlyOne(GenericArguments.integer(Text.of(z)))
        ).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(key).get();
        WorldProperties wp = args.<WorldProperties>getOne(location).orElse(pl.getWorld().getProperties());
        World world = Sponge.getServer().getWorld(wp.getUniqueId()).get();

        int yy = args.<Integer>getOne(y).get();
        if (yy < 0) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.tppos.ysmall")));
            return CommandResult.empty();
        }

        // Create the location
        Location<World> loc = new Location<>(world, args.<Integer>getOne(x).get(), yy, args.<Integer>getOne(z).get());

        // Don't bother with the safety if the flag is set.
        if (args.<Boolean>getOne("f").orElse(false)) {
            pl.setLocation(loc);
            pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tppos.success")));
            if (!src.equals(pl)) {
                src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tppos.success.other", pl.getName())));
            }

            return CommandResult.success();
        }

        if (pl.setLocationSafely(loc)) {
            pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tppos.success")));
            if (!src.equals(pl)) {
                src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.tppos.success.other", pl.getName())));
            }

            return CommandResult.success();
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.tppos.nosafe")));
            return CommandResult.empty();
        }
    }
}
