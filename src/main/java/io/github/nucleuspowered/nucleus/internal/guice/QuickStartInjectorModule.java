/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.guice;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.services.WarmupManager;
import io.github.nucleuspowered.nucleus.modules.admin.config.AdminConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.connectionmessages.config.ConnectionMessagesConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import io.github.nucleuspowered.nucleus.modules.jump.config.JumpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import io.github.nucleuspowered.nucleus.modules.mob.config.MobConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

public class QuickStartInjectorModule extends QuickStartModuleLoaderInjector {

    public QuickStartInjectorModule(Nucleus plugin) {
        super(plugin);
    }

    @Override
    protected void configure() {
        super.configure();

        bind(CoreConfigAdapter.class).toProvider(() -> getAdapter("core", CoreConfigAdapter.class));
        bind(ChatConfigAdapter.class).toProvider(() -> getAdapter("chat", ChatConfigAdapter.class));
        bind(AFKConfigAdapter.class).toProvider(() -> getAdapter("afk", AFKConfigAdapter.class));
        bind(JailConfigAdapter.class).toProvider(() -> getAdapter("jail", JailConfigAdapter.class));
        bind(WarpConfigAdapter.class).toProvider(() -> getAdapter("warp", WarpConfigAdapter.class));
        bind(NicknameConfigAdapter.class).toProvider(() -> getAdapter("nickname", NicknameConfigAdapter.class));
        bind(AdminConfigAdapter.class).toProvider(() -> getAdapter("admin", AdminConfigAdapter.class));
        bind(JumpConfigAdapter.class).toProvider(() -> getAdapter("jump", JumpConfigAdapter.class));
        bind(ConnectionMessagesConfigAdapter.class).toProvider(() -> getAdapter("connection-messages", ConnectionMessagesConfigAdapter.class));
        bind(MobConfigAdapter.class).toProvider(() -> getAdapter("mob", MobConfigAdapter.class));

        bind(AFKHandler.class).toProvider(() -> getService(AFKHandler.class));
        bind(MessageHandler.class).toProvider(() -> getService(MessageHandler.class));
        bind(MailHandler.class).toProvider(() -> getService(MailHandler.class));
        bind(JailHandler.class).toProvider(() -> getService(JailHandler.class));
        bind(TeleportHandler.class).toProvider(() -> getService(TeleportHandler.class));
        bind(WarpHandler.class).toProvider(() -> getService(WarpHandler.class));
        bind(KitHandler.class).toProvider(() -> getService(KitHandler.class));
        bind(WarmupManager.class).toProvider(plugin::getWarmupManager);
    }

    private <T extends AbstractConfigAdapter<?>> T getAdapter(String module, Class<T> clazz) {
        try {
            return plugin.getModuleContainer().getConfigAdapterForModule(module, clazz);
        } catch (NoModuleException | IncorrectAdapterTypeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> T getService(Class<T> service) {
        return plugin.getInternalServiceManager().getService(service).orElse(null);
    }
}
