/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.BoundedIntegerArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoDescriptionArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

@Permissions(prefix = "teleport", supportsOthers = true)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"tppos"})
public class TeleportPositionCommand extends AbstractCommand<CommandSource> {

    private final String key = "subject";
    private final String location = "world";
    private final String x = "x";
    private final String y = "y";
    private final String z = "z";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                // "Flags", otherwise putting -1 wouldn't work.
                GenericArguments.optionalWeak(GenericArguments.literal(Text.of("f"), "-f")),
                GenericArguments.optionalWeak(GenericArguments.literal(Text.of("f"), "--force")),
                GenericArguments.optionalWeak(GenericArguments.literal(Text.of("c"), "-c")),
                GenericArguments.optionalWeak(GenericArguments.literal(Text.of("c"), "--chunk")),
                new NoDescriptionArgument(GenericArguments.optionalWeak(GenericArguments.literal(Text.of("f"), "-f"))),
                new NoDescriptionArgument(GenericArguments.optionalWeak(GenericArguments.literal(Text.of("f"), "--force"))),

                // Actual arguments
                GenericArguments.optionalWeak(
                    GenericArguments.onlyOne(
                        GenericArguments.requiringPermission(
                        SelectorWrapperArgument.nicknameSelector(Text.of(key), NicknameArgument.UnderlyingType.PLAYER), permissions.getOthers()))),
                GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.world(Text.of(location)))),
                GenericArguments.onlyOne(new BoundedIntegerArgument(Text.of(x), Integer.MIN_VALUE, Integer.MAX_VALUE)),
                GenericArguments.onlyOne(new BoundedIntegerArgument(Text.of(y), 0, 255)),
                GenericArguments.onlyOne(new BoundedIntegerArgument(Text.of(z), Integer.MIN_VALUE, Integer.MAX_VALUE))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = this.getUserFromArgs(Player.class, src, key, args);
        WorldProperties wp = args.<WorldProperties>getOne(location).orElse(pl.getWorld().getProperties());
        World world = Sponge.getServer().getWorld(wp.getUniqueId()).get();

        int xx = args.<Integer>getOne(x).get();
        int zz = args.<Integer>getOne(z).get();
        int yy = args.<Integer>getOne(y).get();
        if (yy < 0) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.ysmall"));
            return CommandResult.empty();
        }

        // Chunks are 16 in size, chunk 0 is from 0 - 15, -1 from -1 to -16.
        if (args.hasAny("c")) {
            xx = xx * 16 + 8;
            yy = yy * 16 + 8;
            zz = zz * 16 + 8;
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.fromchunk",
                    String.valueOf(xx), String.valueOf(yy), String.valueOf(zz)));
        }

        Vector3i max = world.getBlockMax();
        Vector3i min = world.getBlockMin();
        if (!(isBetween(xx, max.getX(), min.getX()) && isBetween(yy, max.getY(), min.getY()) && isBetween(zz, max.getZ(), min.getZ()))) {
            throw ReturnMessageException.fromKey("command.tppos.invalid");
        }

        // Create the location
        Location<World> loc = new Location<>(world, xx, yy, zz);
        if (!Util.isLocationInWorldBorder(loc)) {
            throw ReturnMessageException.fromKey("command.tppos.worldborder");
        }

        NucleusTeleportHandler teleportHandler = Nucleus.getNucleus().getTeleportHandler();

        // Don't bother with the safety if the flag is set.
        if (args.<Boolean>getOne("f").orElse(false)) {
            if (teleportHandler.teleportPlayer(pl, loc, NucleusTeleportHandler.TeleportMode.NO_CHECK, Cause.of(NamedCause.owner(src)))) {
                pl.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.success.self"));
                if (!src.equals(pl)) {
                    src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.success.other", pl.getName()));
                }

                return CommandResult.success();
            }

            throw ReturnMessageException.fromKey("command.tppos.cancelledevent");
        }

        // If we have a chunk, scan the whole chunk.
        NucleusTeleportHandler.TeleportMode mode = teleportHandler.getTeleportModeForPlayer(pl);
        if (args.hasAny("c") && mode == NucleusTeleportHandler.TeleportMode.FLYING_THEN_SAFE) {
            mode = NucleusTeleportHandler.TeleportMode.FLYING_THEN_SAFE_CHUNK;
        }

        if (teleportHandler.teleportPlayer(pl, loc, mode, Cause.of(NamedCause.owner(src)))) {
            pl.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.success.self"));
            if (!src.equals(pl)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.success.other", pl.getName()));
            }

            return CommandResult.success();
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.tppos.nosafe"));
            return CommandResult.empty();
        }
    }

    private boolean isBetween(int toCheck, int max, int min) {
        return toCheck >= min && toCheck <= max;
    }
}
