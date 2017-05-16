/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusBlacklistMigrationService;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.blacklist.handler.BlacklistMigrationHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashSet;

@NonnullByDefault
@Permissions(prefix = "blacklist", mainOverride = "migrate")
@RegisterCommand(value = { "protectionperms", "pp" }, subcommandOf = BlacklistMigrateCommand.class)
public class BlacklistMigrateProtectionPermsCommand extends AbstractCommand<CommandSource> {

    private final BlacklistMigrationHandler blacklistMigrationHandler;

    @Inject
    public BlacklistMigrateProtectionPermsCommand(BlacklistMigrationHandler blacklistMigrationHandler) {
        this.blacklistMigrationHandler = blacklistMigrationHandler;
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Get the item types.
        PermissionService service = Sponge.getServiceManager().provide(PermissionService.class)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.blacklist.migrate.permissionabs"));
        SubjectData defaults = service.getDefaults().getSubjectData();

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.blacklist.migrate.protectionperms.start"));

        blacklistMigrationHandler.getBlacklistedBlockstates().forEach((k, v) -> applyPermissions(defaults, k.getType().getId(), v));
        blacklistMigrationHandler.getBlacklistedItemtypes().forEach((k, v) -> applyPermissions(defaults, k.getType().getId(), v));

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.blacklist.migrate.protectionperms.done"));

        return CommandResult.success();
    }

    private void applyPermissions(SubjectData defaults, String id, NucleusBlacklistMigrationService.Result v) {
        if (v.use()) {
            defaults.setPermission(new HashSet<>(), "protectionperms.item.use." + id, Tristate.FALSE);
        }

        if (v.environment()) {
            defaults.setPermission(new HashSet<>(), "protectionperms.block.interact" + id, Tristate.FALSE);
            defaults.setPermission(new HashSet<>(), "protectionperms.block.break" + id, Tristate.FALSE);
            defaults.setPermission(new HashSet<>(), "protectionperms.block.place" + id, Tristate.FALSE);
        }

        if (v.possession()) {
            defaults.setPermission(new HashSet<>(), "protectionperms.item.craft." + id, Tristate.FALSE);
        }
    }
}
