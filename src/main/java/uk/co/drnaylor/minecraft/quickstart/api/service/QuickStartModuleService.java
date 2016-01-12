package uk.co.drnaylor.minecraft.quickstart.api.service;

import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.ModulesLoadedException;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.UnremovableModuleException;

import java.util.Set;

public interface QuickStartModuleService {
    /**
     * Gets the modules to load, or the modules that have been loaded.
     *
     * @return The modules that are to be loaded, or are being loaded.
     */
    Set<PluginModule> getModulesToLoad();

    /**
     * Removes a module from QuickStart pragmatically, so plugins can override the behaviour if required.
     *
     * @param module The {@link PluginModule} to disable.
     * @throws ModulesLoadedException Thrown if the modules have now been loaded.
     * @throws UnremovableModuleException Thrown if the module has been marked cannot be disabled.
     */
    void removeModule(PluginModule module) throws ModulesLoadedException, UnremovableModuleException;
}
