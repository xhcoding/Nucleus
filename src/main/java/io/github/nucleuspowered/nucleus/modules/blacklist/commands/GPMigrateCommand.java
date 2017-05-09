/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.commands;

import com.google.inject.Inject;
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
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Permissions(prefix = "blacklist", mainOverride = "migrate")
@RegisterCommand(value = { "griefprevention", "gp" }, subcommandOf = DummyCommands.MigrateCommand.class)
public class GPMigrateCommand extends AbstractCommand<CommandSource> {

    private final BlacklistMigrationHandler blacklistMigrationHandler;

    @Inject
    public GPMigrateCommand(BlacklistMigrationHandler blacklistMigrationHandler) {
        this.blacklistMigrationHandler = blacklistMigrationHandler;
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Get the item types.
        PermissionService service = Sponge.getServiceManager().provide(PermissionService.class)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.blacklist.migrate.permissionabs"));
        SubjectData defaults = service.getDefaults().getSubjectData();

        throw ReturnMessageException.fromKey("command.blacklist.migrate.gp.soon");

//        return CommandResult.success();
    }

}
