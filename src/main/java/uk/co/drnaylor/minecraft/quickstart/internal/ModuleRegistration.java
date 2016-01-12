package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.ImmutableSet;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.ModulesLoadedException;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.UnremovableModuleException;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;

import java.util.Map;
import java.util.Set;

public class ModuleRegistration implements QuickStartModuleService {

    private final QuickStart plugin;
    private Map<PluginModule, Boolean> modulesToLoad;

    public ModuleRegistration(QuickStart plugin) {
        this.plugin = plugin;


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
    }
}
