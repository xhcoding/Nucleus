/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.service;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.vanish.commands.VanishCommand;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;

import java.util.Collection;
import java.util.List;

public class VanishService implements Reloadable {

    private final String canseePerm = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(VanishCommand.class).getPermissionWithSuffix("see");
    private boolean isAlter = false;

    @Override
    public void onReload() throws Exception {
        this.isAlter = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(VanishConfigAdapter.class).getNodeOrDefault()
                .isAlterTabList();
    }

    public void vanishPlayer(Player player) {
        VanishUserDataModule service = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class);
        service.setVanished(true);
        player.offer(Keys.VANISH, true);
        player.offer(Keys.VANISH_IGNORES_COLLISION, true);
        player.offer(Keys.VANISH_PREVENTS_TARGETING, true);

        if (this.isAlter) {
            Sponge.getServer().getOnlinePlayers().stream().filter(x -> !player.equals(x) || !x.hasPermission(this.canseePerm))
                    .forEach(x -> x.getTabList().removeEntry(player.getUniqueId()));
        }
    }

    public void unvanishPlayer(Player player) {
        VanishUserDataModule service = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class);
        service.setVanished(false);
        player.offer(Keys.VANISH, false);
        player.offer(Keys.VANISH_IGNORES_COLLISION, false);
        player.offer(Keys.VANISH_PREVENTS_TARGETING, false);

        if (this.isAlter) {
            Sponge.getServer().getOnlinePlayers().forEach(x -> {
                if (!x.getTabList().getEntry(player.getUniqueId()).isPresent()) {
                    x.getTabList().addEntry(TabListEntry.builder().profile(player.getProfile()).build());
                }
            });
        }
    }

    public void resetPlayerTabLists() {
        if (this.isAlter) {
            Collection<Player> players = Sponge.getServer().getOnlinePlayers();
            List<TabListEntry> standard = Lists.newArrayList();
            List<TabListEntry> enhanced = Lists.newArrayList();

            players.forEach(x -> {
                TabListEntry t = TabListEntry.builder().profile(x.getProfile()).build();
                if (!x.get(Keys.VANISH).orElse(false)) {
                    standard.add(t);
                }

                enhanced.add(t);
            });

            players.forEach(x -> {
                TabList list = x.getTabList();
                Collection<TabListEntry> toRemove = list.getEntries();
                toRemove.forEach(y -> list.removeEntry(y.getProfile().getUniqueId()));

                if (x.hasPermission(this.canseePerm)) {
                    enhanced.forEach(list::addEntry);
                } else {
                    standard.forEach(list::addEntry);
                }
            });
        }

    }
}
