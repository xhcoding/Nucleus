package io.github.nucleuspowered.nucleus.internal.traits;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;

public interface PermissionHandlerTrait {

    default CommandPermissionHandler getPermisisonHandlerFor(Class<? extends AbstractCommand<?>> clazz) {
        return Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(clazz);
    }

}
