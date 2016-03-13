/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.exceptions.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.exceptions.UnremovableModuleException;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import io.github.nucleuspowered.nucleus.config.enumerations.ModuleOptions;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import org.spongepowered.api.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleRegistration implements NucleusModuleService {

    private final Nucleus nucleus;
    private final Map<PluginModule, Boolean> modulesToLoad;

    public ModuleRegistration(Nucleus plugin) {
        this.nucleus = plugin;
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
    public void removeModule(PluginModule module, Object plugin) throws ModulesLoadedException, UnremovableModuleException {
        if (this.nucleus.areModulesLoaded()) {
            throw new ModulesLoadedException();
        }

        // The plugin must actually be a plugin.
        Preconditions.checkNotNull(plugin);
        Plugin pluginAnnotation = plugin.getClass().getAnnotation(Plugin.class);
        if (pluginAnnotation == null) {
            throw new IllegalArgumentException("plugin must be your plugin instance.");
        }

        boolean override = modulesToLoad.getOrDefault(module, false);
        if (override) {
            nucleus.getLogger().warn(Util.getMessageWithFormat("nucleus.module.disabled.forceload", pluginAnnotation.name(), pluginAnnotation.id(), module.getKey()));
            nucleus.getLogger().warn(Util.getMessageWithFormat("nucleus.module.disabled.forceloadtwo", pluginAnnotation.name()));
            throw new UnremovableModuleException();
        }

        nucleus.getLogger().info(Util.getMessageWithFormat("nucleus.module.disabled", pluginAnnotation.name(), pluginAnnotation.id(), module.getKey()));
        modulesToLoad.remove(module);
    }
}
