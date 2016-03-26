/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RegisterCommand({"lightning", "smite", "thor"})
public class LightningCommand extends CommandBase<CommandSource> {

    private final String player = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(GenericArguments.optional(GenericArguments.requiringPermission(
                GenericArguments.onlyOne(GenericArguments.player(Text.of(player))), permissions.getPermissionWithSuffix("others")))).build();
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = this.getUser(Player.class, src, player, args);
        if (!opl.isPresent()) {
            return CommandResult.empty();
        }

        Player pl = opl.get();

        if (pl == src) {
            BlockRay<World> playerBlockRay = BlockRay.from(pl).blockLimit(350).build();
            BlockRayHit<World> finalHitRay = null;

            while (playerBlockRay.hasNext()) {
                BlockRayHit<World> currentHitRay = playerBlockRay.next();

                if (!pl.getWorld().getBlockType(currentHitRay.getBlockPosition()).equals(BlockTypes.AIR)) {
                    finalHitRay = currentHitRay;
                    break;
                }
            }

            Location<World> lightningLocation = null;

            if (finalHitRay == null) {
                lightningLocation = pl.getLocation();
            } else {
                lightningLocation = finalHitRay.getLocation();
            }

            Text message = this.spawnLightning(lightningLocation, src) ? Util.getTextMessageWithFormat("command.lightning.success")
                    : Util.getTextMessageWithFormat("command.lightning.error");
            pl.sendMessage(message);
        } else {
            Text message = this.spawnLightning(pl.getLocation(), src) ? Util.getTextMessageWithFormat("command.lightning.success.other", pl.getName())
                    : Util.getTextMessageWithFormat("command.lightning.error");
            pl.sendMessage(message);
        }

        return CommandResult.success();
    }

    private boolean spawnLightning(Location<World> location, CommandSource src) {
        World world = location.getExtent();
        Optional<Entity> bolt = world.createEntity(EntityTypes.LIGHTNING, location.getPosition());

        if (bolt.isPresent()) {
            return world.spawnEntity(bolt.get(), Cause.of(NamedCause.source(src)));
        }

        return false;
    }
}
