/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal.services;

import com.google.common.collect.ImmutableSet;
import io.github.essencepowered.essence.Essence;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.api.exceptions.ModulesLoadedException;
import io.github.essencepowered.essence.api.exceptions.UnremovableModuleException;
import io.github.essencepowered.essence.api.service.EssenceModuleService;
import io.github.essencepowered.essence.config.enumerations.ModuleOptions;
import io.github.essencepowered.essence.internal.ConfigMap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleRegistration implements EssenceModuleService {

    private final Essence plugin;
    private final Map<PluginModule, Boolean> modulesToLoad;

    public ModuleRegistration(Essence plugin) {
        this.plugin = plugin;
        modulesToLoad = plugin.getConfig(ConfigMap.MAIN_CONFIG).get().getModuleOptions().entrySet().stream()
                .filter(s -> s.getValue() != ModuleOptions.DISABLED)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        s -> s.getValue().equals(ModuleOptions.FORCELOAD)));
    }

    @Override
    public Set<PluginModule> getModulesToLoad() {
        return ImmutableSet.copyOf(modulesToLoad.keySet());
    }

    @Override
    public void removeModule(PluginModule module) throws ModulesLoadedException, UnremovableModuleException {
        if (plugin.areModulesLoaded()) {
            throw new ModulesLoadedException();
        }

        boolean override = modulesToLoad.getOrDefault(module, false);
        if (override) {
            throw new UnremovableModuleException();
        }

        modulesToLoad.remove(module);
    }
}
