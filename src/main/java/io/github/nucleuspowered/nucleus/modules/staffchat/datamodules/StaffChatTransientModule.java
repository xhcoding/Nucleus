/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.dataservices.modular.TransientModule;

public class StaffChatTransientModule extends TransientModule<ModularUserService> {

    private boolean inStaffChat;

    public boolean isInStaffChat() {
        return inStaffChat;
    }

    public void setInStaffChat(boolean inStaffChat) {
        this.inStaffChat = inStaffChat;
    }
}
