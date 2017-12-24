/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.craftinggui.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.modules.craftinggui.BasicCraftingCommand;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions
@RegisterCommand({"workbench", "wb", "craft"})
@NonnullByDefault
public class WorkbenchCommand extends BasicCraftingCommand {

    @Override
    protected InventoryArchetype getArchetype() {
        return InventoryArchetypes.WORKBENCH;
    }
}
