/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.staffchat.config.StaffChatConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Optional;

@ModuleData(id = StaffChatModule.moduleID, name = "Staff Chat")
public class StaffChatModule extends StandardModule {

    static final String moduleID = "staff-chat";

    @Override
    public Optional<NucleusConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new StaffChatConfigAdapter());
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        StaffChatMessageChannel.INSTANCE = new StaffChatMessageChannel(nucleus);
    }
}
