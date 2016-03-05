/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.exceptions.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.exceptions.UnremovableModuleException;

import java.util.Set;

/**
 * A service that allows plugins to request certain modules are not loaded.
 */
public interface NucleusModuleService {
    /**
     * Gets the modules to load, or the modules that have been loaded.
     *
     * @return The modules that are to be loaded, or are being loaded.
     */
    Set<PluginModule> getModulesToLoad();

    /**
     * Removes a module from Nucleus pragmatically, so plugins can override the behaviour if required.
     *
     * @param module The {@link PluginModule} to disable.
     * @throws ModulesLoadedException Thrown if the modules have now been loaded and can no longer be removed.
     * @throws UnremovableModuleException Thrown if the module has been marked "cannot be disabled". Plugins are expected
     *         to honour this.
     */
    void removeModule(PluginModule module) throws ModulesLoadedException, UnremovableModuleException;
}
