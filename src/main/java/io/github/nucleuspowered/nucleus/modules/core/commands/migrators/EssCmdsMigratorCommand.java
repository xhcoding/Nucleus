/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.migrators;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.MigratorCommand;
import io.github.nucleuspowered.nucleus.internal.migrators.EssCmdsMigrator;
import io.github.nucleuspowered.nucleus.modules.core.commands.MigrateCommand;

@NoModifiers
@Permissions(prefix = "nucleus.migrate")
@RegisterCommand(value = "esscmds", subcommandOf = MigrateCommand.class)
public class EssCmdsMigratorCommand extends MigratorCommand<EssCmdsMigrator> {
    public EssCmdsMigratorCommand() {
        super(EssCmdsMigrator.class);
    }
}
