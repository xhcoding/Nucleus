/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.MixinConfigProxy;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.internal.services.WarmupManager;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.util.ThrowableAction;
import org.slf4j.Logger;
import uk.co.drnaylor.quickstart.modulecontainers.DiscoveryModuleContainer;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public abstract class Nucleus {

    private static Nucleus nucleus;

    static void setNucleus(Nucleus nucleus) {
        if (Nucleus.nucleus == null) {
            Nucleus.nucleus = nucleus;
        }
    }

    public static Nucleus getNucleus() {
        return nucleus;
    }

    public abstract void saveData();

    public abstract Logger getLogger();

    public abstract UserDataManager getUserDataManager();

    public abstract WorldDataManager getWorldDataManager();

    public abstract void saveSystemConfig() throws IOException;

    public abstract boolean reload();

    public abstract WarmupManager getWarmupManager();

    public abstract EconHelper getEconHelper();

    public abstract PermissionRegistry getPermissionRegistry();

    public abstract DiscoveryModuleContainer getModuleContainer();

    public abstract boolean isModuleLoaded(String moduleId);

    public abstract <T extends NucleusConfigAdapter<?>> Optional<T> getConfigAdapter(String id, Class<T> configAdapterClass);

    public <R, C, T extends NucleusConfigAdapter<C>> Optional<R> getConfigValue(String id, Class<T> configAdapterClass, Function<C, R> fnToGetValue) {
        Optional<T> tOptional = getConfigAdapter(id, configAdapterClass);
        if (tOptional.isPresent()) {
            return Optional.of(fnToGetValue.apply(tOptional.get().getNodeOrDefault()));
        }

        return Optional.empty();
    }

    public abstract InternalServiceManager getInternalServiceManager();

    public abstract ModularGeneralService getGeneralService();

    public abstract ItemDataService getItemDataService();

    public abstract NameUtil getNameUtil();

    public abstract TextParsingUtils getTextParsingUtils();

    public abstract MessageProvider getMessageProvider();

    public abstract MessageProvider getCommandMessageProvider();

    public abstract int traceUserCreations();

    public abstract void registerReloadable(ThrowableAction<? extends Exception> reloadable);

    public abstract Optional<MixinConfigProxy> getMixinConfigIfAvailable();

    public abstract NucleusTeleportHandler getTeleportHandler();

    public abstract NucleusMessageTokenService getMessageTokenService();

    public abstract boolean isDebugMode();
}
