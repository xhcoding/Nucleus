/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.inventory.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.inventory.InventoryModule;
import io.github.nucleuspowered.nucleus.modules.inventory.config.InventoryConfig;
import io.github.nucleuspowered.nucleus.modules.inventory.config.InventoryConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.inventory.listeners.InvSeeListener;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;
import java.util.Optional;

@NoModifiers
@Permissions
@RegisterCommand("invsee")
@EssentialsEquivalent("invsee")
@Since(minecraftVersion = "1.10.2", spongeApiVersion = "5.0.0", nucleusVersion = "0.13.0")
@NonnullByDefault
public class InvSeeCommand extends AbstractCommand<Player> implements AbstractCommand.Reloadable {

    private final String player = "subject";
    private boolean self = false;

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = super.permissionSuffixesToRegister();
        mspi.put("exempt.target", PermissionInformation.getWithTranslation("permission.invsee.exempt.inspect", SuggestedLevel.ADMIN));
        mspi.put("exempt.interact", PermissionInformation.getWithTranslation("permission.invsee.exempt.interact", SuggestedLevel.ADMIN));
        mspi.put("modify", PermissionInformation.getWithTranslation("permission.invsee.modify", SuggestedLevel.ADMIN));
        mspi.put("offline", PermissionInformation.getWithTranslation("permission.invsee.offline", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            SelectorWrapperArgument.nicknameSelector(Text.of(player), NicknameArgument.UnderlyingType.USER)
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        User target = args.<User>getOne(player).get();

        if (!target.isOnline() && !permissions.testSuffix(src, "offline")) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.invsee.nooffline"));
        }

        if (!this.self && target.getUniqueId().equals(src.getUniqueId())) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.invsee.self"));
        }

        if (permissions.testSuffix(target, "exempt.target", src, false)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.invsee.targetexempt", target.getName()));
        }

        // Just in case, get the subject inventory if they are online.
        try {
            Inventory targetInv = target.isOnline() ? target.getPlayer().get().getInventory() : target.getInventory();
            Optional<Container> oc = src.openInventory(targetInv, Cause.of(NamedCause.of("plugin", plugin), NamedCause.source(src)));
            if (oc.isPresent()) {
                if (!permissions.testSuffix(src, "modify") || permissions.testSuffix(target, "exempt.interact")) {
                    InvSeeListener.addEntry(src.getUniqueId(), oc.get());
                }

                return CommandResult.success();
            }

            throw ReturnMessageException.fromKey("command.invsee.failed");
        } catch (UnsupportedOperationException e) {
            throw ReturnMessageException.fromKey("command.invsee.offlinenotsupported");
        }
    }

    @Override public void onReload() {
        this.self = plugin.getConfigValue(InventoryModule.ID, InventoryConfigAdapter.class,
                InventoryConfig::isAllowInvseeOnSelf).orElse(false);
    }
}
