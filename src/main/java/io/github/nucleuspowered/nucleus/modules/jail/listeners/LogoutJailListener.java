/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;


import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.jail.JailModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;

public class LogoutJailListener extends ListenerBase implements ListenerBase.Conditional {

    private final UserDataManager loader;

    @Inject
    public LogoutJailListener(UserDataManager loader) {
        this.loader = loader;
    }

    @Listener
    public void onLogout(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        loader.get(player).ifPresent(mus -> {
            Optional<JailData> ojd = mus.get(JailUserDataModule.class).getJailData();
            ojd.ifPresent(jailData -> {
                JailData jd = jailData;
                Optional<Instant> end = jd.getEndTimestamp();
                end.ifPresent(instant -> jd.setTimeFromNextLogin(Duration.between(Instant.now(), instant)));
                mus.get(JailUserDataModule.class).setJailData(jd);
            });
        });
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(JailModule.ID, JailConfigAdapter.class, JailConfig::isJailOnlineOnly).orElse(false);
    }

}
