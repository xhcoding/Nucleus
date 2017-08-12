package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@NonnullByDefault
@Permissions
@RegisterCommand({"break", "breakblock"})
@EssentialsEquivalent(
        value = {"break"},
        isExact = false,
        notes = "Requires co-ordinates, whereas Essentials required you to look at the block."
)
public class BreakCommand extends AbstractCommand<Player> {

    @Nullable
    private ItemStackSnapshot itemStack = null;

    public CommandResult executeCommand(Player player, CommandContext args) throws Exception {
        if (this.itemStack == null) {
            this.itemStack = ItemStack.of(ItemTypes.DIAMOND_PICKAXE, 1).createSnapshot();
        }

        BlockRay<World> bl = BlockRay.from(player).distanceLimit(10.0D)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1)).build();
        Optional<BlockRayHit<World>> ob = bl.end();
        Location location = ob.orElseThrow(() -> ReturnMessageException.fromKey("command.break.failray")).getLocation();

        if (location.getBlockType() == BlockTypes.AIR) {
            throw new ReturnMessageException(this.plugin.getMessageProvider().getTextMessageWithFormat("command.blockzap.alreadyair",
                    location.getPosition().toString(), ((World) location.getExtent()).getName()));
        } else if (player.getWorld().digBlockWith(location.getBlockPosition(), this.itemStack.createStack(), Cause.of(NamedCause.simulated(player),
                NamedCause.owner(Sponge.getPluginManager().getPlugin("nucleus").get())))) {
            player.sendMessage(
                    this.plugin.getMessageProvider().getTextMessageWithFormat("command.blockzap.success", location.getPosition().toString(),

                            ((World) location.getExtent()).getName()));
            return CommandResult.success();
        } else {
            throw new ReturnMessageException(this.plugin.getMessageProvider().getTextMessageWithFormat("command.blockzap.fail",
                    location.getPosition().toString(), ((World) location.getExtent()).getName()));
        }
    }
}