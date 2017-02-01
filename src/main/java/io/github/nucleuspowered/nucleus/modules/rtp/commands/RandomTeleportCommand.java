/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.commands;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoCooldownArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoCostArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoWarmupArgument;
import io.github.nucleuspowered.nucleus.internal.CostCancellableTask;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfig;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Permissions(supportsOthers = true)
@RegisterCommand({"rtp", "randomteleport", "rteleport"})
public class RandomTeleportCommand extends StandardAbstractCommand<CommandSource> {

    private final String otherKey = "player";

    private Set<BlockType> prohibitedTypes = null;
    // Works around a problem in the Sponge implementation.
    private final Vector3d direction = new Vector3d(0.00001, -1, 0.000001);

    private final Random random = new Random();
    @Inject private RTPConfigAdapter rca;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(
                GenericArguments.requiringPermission(
                    new NoCostArgument(new NoWarmupArgument(new NoCooldownArgument(
                      new NicknameArgument(Text.of(otherKey), plugin.getUserDataManager(), NicknameArgument.UnderlyingType.PLAYER)))),
                        permissions.getOthers())
            )
        };
    }

    @Override
    public CommandResult executeCommand(final SubjectPermissionCache<CommandSource> src, CommandContext args) throws Exception {
        Player player = this.getUserFromArgs(Player.class, src.getSubject(), otherKey, args);

        boolean self = (src.getSubject() instanceof Player && ((Player) src.getSubject()).getUniqueId().equals(player.getUniqueId()));

        // Get the current world.
        World currentWorld = player.getWorld();

        // World border
        WorldBorder wb = currentWorld.getWorldBorder();

        RTPConfig rc = rca.getNodeOrDefault();
        int diameter = Math.min(Math.abs(rc.getRadius() * 2), (int)wb.getDiameter());
        Vector3d centre = wb.getCenter();

        int count = Math.max(rc.getNoOfAttempts(), 1);
        src.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.searching"));


        if (self) {
            SubjectPermissionCache<Player> sp = new SubjectPermissionCache<>(player, src);
            Sponge.getScheduler().createTaskBuilder().execute(new RTPTask(plugin, count, diameter, centre, getCost(src, args),
                sp, currentWorld, rc.isMustSeeSky(), rc.getMinY(), rc.getMaxY())).submit(plugin);
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(new RTPTask(plugin, count, diameter, centre, getCost(src, args),
                player, currentWorld, rc.isMustSeeSky(), rc.getMinY(), rc.getMaxY(), src)).submit(plugin);
        }

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

        private final int minY;
        private final int maxY;
        private final SubjectPermissionCache<? extends CommandSource> source;
        private final Player target;
        private int count;
        private final int maxCount;
        private final int diameter;
        private final Vector3d centre;
        private final World currentWorld;
        private final boolean onSurface;
        private boolean isSelf = false;

        private RTPTask(NucleusPlugin plugin, int count, int diameter, Vector3d centre, double cost, SubjectPermissionCache<Player> target,
            World currentWorld, boolean onSurface, int minY, int maxY) {
            this(plugin, count, diameter, centre, cost, target.getSubject(), currentWorld, onSurface, minY, maxY, target);
            isSelf = true;
        }

        private RTPTask(NucleusPlugin plugin, int count, int diameter, Vector3d centre, double cost, Player target, World currentWorld, boolean onSurface,
                        int minY, int maxY, SubjectPermissionCache<? extends CommandSource> source) {
            super(plugin, source, cost);
            this.count = count;
            this.maxCount = count;
            this.diameter = diameter;
            this.centre = centre;
            this.currentWorld = currentWorld;
            this.onSurface = onSurface;
            this.minY = minY;
            this.maxY = maxY;
            this.target = target;
            this.source = source;
        }

        @Override
        public void accept(Task task) {
            count--;
            if (!target.isOnline()) {
                onCancel();
                return;
            }

            Transform<World> transform = target.getTransform();
            World world = transform.getExtent();

            plugin.getLogger().debug(String.format("RTP of %s, attempt %s of %s", target.getName(), maxCount - count, maxCount));

            // Generate random co-ords.
            int x = RandomTeleportCommand.this.random.nextInt(diameter) - diameter/2;
            int z = RandomTeleportCommand.this.random.nextInt(diameter) - diameter/2;

            // Load the chunk before continuing with /rtp. Sponge issue means we have to load the chunk first.
            currentWorld.loadChunk(new Vector3i(x, 0, z), true);

            int y;
            if (onSurface) {
                int startY = Math.min(world.getBlockMax().getY() - 11, maxY);
                int distance = startY - Math.max(world.getBlockMin().getY(), minY);
                if (distance < 0) {
                    onUnsuccesfulAttempt();
                    return;
                }

                // From the x and z co-ordinates, scan down from the top to get the next block.
                BlockRay<World> blockRay = BlockRay
                    .from(new Location<>(currentWorld, new Vector3d(x, Math.min(world.getBlockMax().getY() - 11, maxY), z)))
                    .direction(direction)
                    .distanceLimit(distance)
                    .stopFilter(BlockRay.onlyAirFilter()).build();
                Optional<BlockRayHit<World>> blockRayHitOptional = blockRay.end();
                if (blockRayHitOptional.isPresent()) {
                    y = blockRayHitOptional.get().getBlockY();
                } else {
                    onUnsuccesfulAttempt();
                    return;
                }
            } else {
                // We remove 11 to avoid getting a location too high up for the safe location teleporter to handle.
                y = Math.min(world.getBlockMax().getY() - 11, random.nextInt(maxY - minY + 1) + minY);
            }

            // To get within the world border, add the centre on.
            final Location<World> test = new Location<>(currentWorld, new Vector3d(x + centre.getX(), y, z + centre.getZ()));
            Optional<Location<World>> oSafeLocation = NucleusTeleportHandler.TELEPORT_HELPER.getSafeLocation(test, 10, 5);

            // getSafeLocation might have put us out of the world border. Best to check.
            // We also check to see that it's not in water or lava, and if enabled, we see if the subject would end up on the surface.
            try {
                if (oSafeLocation.isPresent() && isSafe(oSafeLocation.get()) && Util.isLocationInWorldBorder(oSafeLocation.get())
                    && isOnSurface(oSafeLocation.get())) {

                    Location<World> tpTarget = oSafeLocation.get();

                    plugin.getLogger().debug(String.format("RTP of %s, found location %s, %s, %s", target.getName(),
                            String.valueOf(tpTarget.getBlockX()),
                            String.valueOf(tpTarget.getBlockY()),
                            String.valueOf(tpTarget.getBlockZ())));
                    if (target.setLocation(tpTarget)) {
                        if (!isSelf) {
                            target.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.other"));
                            source.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.successother",
                                target.getName(),
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        }

                        target.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.success",
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        return;
                    } else {
                        source.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.cancelled"));
                        onCancel();
                        return;
                    }
                }
            } catch (PositionOutOfBoundsException e) {
                // Swallow - we treat it as a fail.
            }

            onUnsuccesfulAttempt();
        }

        private boolean isOnSurface(Location<World> worldLocation) {
            return isSolid(worldLocation.sub(0, 1, 0)) || isSolid(worldLocation.sub(0, 2, 0));
        }

        private void onUnsuccesfulAttempt() {
            if (count <= 0) {
                plugin.getLogger().debug(String.format("RTP of %s was unsuccessful", subject.getSubject().getName()));
                subject.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.error"));
                onCancel();
            } else {
                // We're using a scheduler to allow some ticks to go by between attempts to find a
                // safe place.
                Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(this).submit(plugin);
            }
        }

        private boolean isSafe(Location<World> location) {
            return !location.hasBlock() || !isProhibitedBlockType(location) || !isSolid(location);
        }

        private boolean isProhibitedBlockType(Location<World> location) {
            return getProhibitedTypes().contains(location.getBlockType());
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

        private boolean isSolid(Location<World> location) {
            Optional<MatterProperty> pp = location.getBlockType().getProperty(MatterProperty.class);
            return pp.isPresent() && pp.get().getValue() == MatterProperty.Matter.SOLID;
        }

        @Override
        public void onCancel() {
            super.onCancel();
            if (isSelf) {
                RandomTeleportCommand.this.removeCooldown(target.getUniqueId());
            }
        }
    }
}
