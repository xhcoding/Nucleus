package uk.co.drnaylor.minecraft.quickstart.internal.services;

import com.google.common.collect.ImmutableSet;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.ModulesLoadedException;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.UnremovableModuleException;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.config.MainConfig;
import uk.co.drnaylor.minecraft.quickstart.config.enumerations.ModuleOptions;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleRegistration implements QuickStartModuleService {

    private final QuickStart plugin;
    private final Map<PluginModule, Boolean> modulesToLoad;

    public ModuleRegistration(QuickStart plugin) {
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
