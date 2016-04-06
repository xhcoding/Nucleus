/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusPrivateMessagingService;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.message.config.MessageConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "message", name = "message")
public class MessageModule extends StandardModule {

    @Inject private Game game;

    @Override
    public Optional<AbstractConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new MessageConfigAdapter());
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        MessageHandler m = new MessageHandler();
        serviceManager.registerService(MessageHandler.class, m);
        game.getServiceManager().setProvider(nucleus, NucleusPrivateMessagingService.class, m);
    }
}
