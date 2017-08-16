/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.protection.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.protection.ProtectionModule;
import io.github.nucleuspowered.nucleus.modules.protection.config.ProtectionConfigAdapter;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

@SuppressWarnings("ALL")
public class MobProtectionListener extends ListenerBase implements Reloadable, ListenerBase.Conditional {

    @Inject
    private ProtectionConfigAdapter protectionConfigAdapter;
    private List<EntityType> whitelistedTypes;

    @Listener
    @Exclude({ChangeBlockEvent.Grow.class, ChangeBlockEvent.Decay.class})
    public void onMobChangeBlock(ChangeBlockEvent event, @Root Living living) {
        if (living instanceof Player || whitelistedTypes.contains(living.getType())) {
            return;
        }

        // If the entity is not in the whitelist, then cancel the event.
        event.setCancelled(true);
    }

    @Override
    public void onReload() throws Exception {
        whitelistedTypes = protectionConfigAdapter.getNodeOrDefault().getWhitelistedEntities();
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ProtectionModule.ID, ProtectionConfigAdapter.class, x -> x.isEnableProtection()).orElse(false);
    }
}
