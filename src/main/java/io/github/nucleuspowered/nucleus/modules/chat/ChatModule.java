/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "chat", name = "Chat")
public class ChatModule extends ConfigurableModule<ChatConfigAdapter> {

    @Override
    public ChatConfigAdapter getAdapter() {
        return new ChatConfigAdapter();
    }
}
