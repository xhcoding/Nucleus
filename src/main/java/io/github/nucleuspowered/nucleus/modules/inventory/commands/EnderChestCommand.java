/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.inventory.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Iterator;
import java.util.Map;

@NonnullByDefault
@Permissions(supportsOthers = true)
@RegisterCommand({"enderchest", "ec", "echest"})
@Since(minecraftVersion = "1.10.2", spongeApiVersion = "5.0.0", nucleusVersion = "0.13.0")
@EssentialsEquivalent({"enderchest", "echest", "endersee", "ec"})
public class EnderChestCommand extends AbstractCommand<Player> {

    private final String player = "subject";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = super.permissionSuffixesToRegister();
        mspi.put("exempt.target", PermissionInformation.getWithTranslation("permission.enderchest.exempt.inspect", SuggestedLevel.ADMIN));
        mspi.put("exempt.modify", PermissionInformation.getWithTranslation("permission.enderchest.exempt.modify", SuggestedLevel.ADMIN));
        mspi.put("modify", PermissionInformation.getWithTranslation("permission.enderchest.modify", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(
                    GenericArguments.requiringPermission(
                        SelectorWrapperArgument.nicknameSelector(Text.of(player), NicknameArgument.UnderlyingType.PLAYER),
                        permissions.getPermissionWithSuffix("others")
                    ))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(player).orElse(src);

        if (!target.getUniqueId().equals(src.getUniqueId())) {
            if (permissions.testSuffix(target, "exempt.target")) {
                throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.enderchest.targetexempt", target.getName()));
            }

            if (this.permissions.testSuffix(target, "exempt.modify") || !this.permissions.testSuffix(src, "modify")) {
                // Create a read only copy.
                Inventory wrapper = Inventory.builder().of(InventoryArchetypes.CHEST)
                        .property(InventoryTitle.PROPERTY_NAME,
                            InventoryTitle.of(plugin.getMessageProvider().getTextMessageWithFormat("command.enderchest.readonly")))
                        .listener(InteractInventoryEvent.class, e -> {
                            if (!(e instanceof ClickInventoryEvent.Close) && !(e instanceof ClickInventoryEvent.Open)) {
                                e.setCancelled(true);
                            }
                        }).build(plugin);
                Iterable<Slot> slotIterable = wrapper.slots();
                Iterator<Slot> slotIterator = slotIterable.iterator();
                target.getEnderChestInventory().slots().forEach(x -> {
                    Slot s = slotIterator.next();
                    x.peek().ifPresent(s::set);
                });

                src.openInventory(wrapper, Cause.of(NamedCause.of("plugin", plugin), NamedCause.source(src)));
                return CommandResult.success();
            }
        }

        src.openInventory(target.getEnderChestInventory(), Cause.of(NamedCause.of("plugin", plugin), NamedCause.source(src)));
        return CommandResult.success();
    }
}
