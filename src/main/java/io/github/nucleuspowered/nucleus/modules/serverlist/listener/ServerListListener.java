/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.serverlist.listener;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.serverlist.ServerListModule;
import io.github.nucleuspowered.nucleus.modules.serverlist.config.ServerListConfig;
import io.github.nucleuspowered.nucleus.modules.serverlist.config.ServerListConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.profile.GameProfile;

import java.util.Collection;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ConditionalListener(ServerListListener.Condition.class)
public class ServerListListener extends ListenerBase.Reloadable {

    private final Random random = new Random();
    private ServerListConfig config;

    @Listener
    public void onServerListPing(ClientPingServerEvent event, @Getter("getResponse") ClientPingServerEvent.Response response) {
        if (this.config.isModifyServerList() && !this.config.getMessages().isEmpty()) {
            NucleusTextTemplate template = config.getMessages().get(random.nextInt(config.getMessages().size()));
            response.setDescription(template.getForCommandSource(Sponge.getServer().getConsole()));
        }

        if (this.config.isHidePlayerCount()) {
            response.setHidePlayers(true);
        } else if (this.config.isHideVanishedPlayers()) {
            Collection<GameProfile> players = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.get(Keys.VANISH).orElse(false))
                    .map(User::getProfile).collect(Collectors.toList());

            response.getPlayers().ifPresent(y -> {
                y.getProfiles().removeIf(players::contains);
                y.setOnline(y.getProfiles().size());
            });
        }
    }

    @Override public void onReload() throws Exception {
        this.config = plugin.getConfigValue(ServerListModule.ID, ServerListConfigAdapter.class, Function.identity())
                .orElseGet(ServerListConfig::new);
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            return nucleus.getConfigValue(ServerListModule.ID, ServerListConfigAdapter.class, ServerListConfig::enableListener).orElse(false);
        }
    }
}
