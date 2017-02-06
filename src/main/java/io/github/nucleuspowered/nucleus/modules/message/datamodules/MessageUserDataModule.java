/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.datamodules;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.modules.message.commands.SocialSpyCommand;

public class MessageUserDataModule extends DataModule.ReferenceService<ModularUserService> {

    private static CommandPermissionHandler ssSocialSpy
        = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(SocialSpyCommand.class);

    @DataKey("socialspy")
    private boolean socialspy = false;

    public MessageUserDataModule(ModularUserService modularDataService) {
        super(modularDataService);
    }

    public boolean isSocialSpy() {
        // Only a spy if they have the permission!

        return (ssSocialSpy.testSuffix(getService().getUser(), "force") || socialspy) && ssSocialSpy.testBase(getService().getUser());
    }

    public boolean setSocialSpy(boolean socialSpy) {
        this.socialspy = socialSpy;

        // Permission checks! Return true if it's what we wanted.
        return isSocialSpy() == socialSpy;
    }
}
