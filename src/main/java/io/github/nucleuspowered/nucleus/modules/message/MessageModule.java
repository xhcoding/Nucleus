/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "message", name = "message")
public class MessageModule extends StandardModule {

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        serviceManager.registerService(MessageHandler.class, new MessageHandler());
    }
}
