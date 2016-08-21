/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.commands;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CostCancellableTask;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfig;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

@Permissions
@RegisterCommand({"rtp", "randomteleport", "rteleport"})
public class RandomTeleportCommand extends CommandBase<Player> {

    private Set<BlockType> prohibitedTypes = null;
    private final int maxHeight = 255;
    private final Vector3d maxHeightVector = new Vector3d(Double.MIN_VALUE, maxHeight + 1, Double.MIN_VALUE);

    private final Random random = new Random();
    @Inject private RTPConfigAdapter rca;

    @Override
    public CommandResult executeCommand(final Player src, CommandContext args) throws Exception {
        // Get the current world.
        World currentWorld = src.getWorld();

        // World border
        WorldBorder wb = currentWorld.getWorldBorder();

        RTPConfig rc = rca.getNodeOrDefault();
        int diameter = Math.min(Math.abs(rc.getRadius() * 2), (int)wb.getDiameter());
        Vector3d centre = wb.getCenter();

        int count = Math.max(rc.getNoOfAttempts(), 1);
        src.sendMessage(Util.getTextMessageWithFormat("command.rtp.searching"));

        Sponge.getScheduler().createTaskBuilder().execute(new RTPTask(plugin, count, diameter, centre, getCost(src, args), src, currentWorld, rc.isMustSeeSky())).submit(plugin);
        return CommandResult.success();
    }

    /*
     * (non-Javadoc)
     *
     * The RTPTask class encapsulates the logic for the /rtp. Because TeleportHelper#getSafeLocation(Location) can be slow, particularly if there is a
     * large area to check, we opt for smaller areas, but to try multiple times. We separate each check by a couple of ticks so that the server
     * still gets to keep ticking, avoiding timeouts and too much lag.
     */
    private class RTPTask extends CostCancellableTask {

        private int count;
        private final int maxCount;
        private final int diameter;
        private final Vector3d centre;
        private final TeleportHelper th = Sponge.getGame().getTeleportHelper();
        private final World currentWorld;
        private final boolean onSurface;

        private RTPTask(Nucleus plugin, int count, int diameter, Vector3d centre, double cost, Player src, World currentWorld, boolean onSurface) {
            super(plugin, src, cost);
            this.count = count;
            this.maxCount = count;
            this.diameter = diameter;
            this.centre = centre;
            this.currentWorld = currentWorld;
            this.onSurface = onSurface;
        }

        @Override
        public void accept(Task task) {
            count--;
            if (!player.isOnline()) {
                onCancel();
                return;
            }

            plugin.getLogger().debug(String.format("RTP of %s, attempt %s of %s", player.getName(), maxCount - count, maxCount));

            // Generate random co-ords.
            int x = RandomTeleportCommand.this.random.nextInt(diameter) - diameter/2;
            int z = RandomTeleportCommand.this.random.nextInt(diameter) - diameter/2;

            // We remove 10 to avoid getting a location too high up for the safe location teleporter to handle.
            int y = random.nextInt(maxHeight - 10);

            // To get within the world border, add the centre on.
            final Location<World> test = new Location<>(currentWorld, new Vector3d(x + centre.getX(), y, z + centre.getZ()));
            Optional<Location<World>> oSafeLocation = th.getSafeLocation(test, 10, 5);

            // getSafeLocation might have put us out of the world border. Best to check.
            // We also check to see that it's not in water or lava, and if enabled, we see if the player would end up on the surface.
            try {
                if (oSafeLocation.isPresent() && isSafe(oSafeLocation.get()) && Util.isLocationInWorldBorder(oSafeLocation.get()) && (!onSurface || isOnSurface(oSafeLocation.get()))) {
                    Location<World> tpTarget = oSafeLocation.get().add(0, 1, 0);

                    plugin.getLogger().debug(String.format("RTP of %s, found location %s, %s, %s", player.getName(),
                            String.valueOf(tpTarget.getBlockX()),
                            String.valueOf(tpTarget.getBlockY()),
                            String.valueOf(tpTarget.getBlockZ())));
                    if (player.setLocation(tpTarget)) {
                        player.sendMessage(Util.getTextMessageWithFormat("command.rtp.success",
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        return;
                    } else {
                        player.sendMessage(Util.getTextMessageWithFormat("command.rtp.cancelled"));
                        onCancel();
                        return;
                    }
                }
            } catch (PositionOutOfBoundsException e) {
                // Swallow - we treat it as a fail.
            }

            if (count <= 0) {
                plugin.getLogger().debug(String.format("RTP of %s was unsuccessful", player.getName()));
                player.sendMessage(Util.getTextMessageWithFormat("command.rtp.error"));
                onCancel();
            } else {
                // We're using a scheduler to allow some ticks to go by between attempts to find a
                // safe place.
                Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(this).submit(plugin);
            }
        }

        private boolean isSafe(Location<World> location) {
            return !location.hasBlock() || !getProhibitedTypes().contains(location.getBlockType());
        }

        private Set<BlockType> getProhibitedTypes() {
            if (prohibitedTypes == null) {
                prohibitedTypes = Sets.newHashSet(
                    BlockTypes.WATER,
                    BlockTypes.LAVA,
                    BlockTypes.FLOWING_WATER,
                    BlockTypes.FLOWING_LAVA
                );
            }

            return prohibitedTypes;
        }

        private boolean isOnSurface(Location<World> location) {
            SurfaceCheckPredicate predicate = new SurfaceCheckPredicate();
            Optional<BlockRayHit<World>> blockRayHitOptional = BlockRay.from(location).to(location.getPosition().max(maxHeightVector))
                    .filter(predicate).end();

            // If we have no hit, then this is the top of the world and we should allow it.
            return !blockRayHitOptional.isPresent();
        }

        @Override
        public void onCancel() {
            super.onCancel();
            RandomTeleportCommand.this.removeCooldown(player.getUniqueId());
        }

        private class SurfaceCheckPredicate implements Predicate<BlockRayHit<World>> {

            private int count = 0;

            @Override
            public boolean test(BlockRayHit<World> l) {
                BlockType type = l.getLocation().getBlockType();

                // If passable - treat it as seeing the sky.
                if (Boolean.TRUE.equals(type.getProperty(PassableProperty.class).orElse(new PassableProperty(false)).getValue())) {
                    return true;
                }

                // If leaves, treat it as on the surface.
                if (count++ > 1 && type.equals(BlockTypes.LEAVES) || type.equals(BlockTypes.LEAVES2)) {
                    return true;
                }

                // Nope, we're not on the surface
                return false;
            }
        }
    }
}
