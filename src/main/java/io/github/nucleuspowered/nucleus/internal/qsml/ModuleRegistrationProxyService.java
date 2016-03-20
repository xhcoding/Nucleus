/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.exceptions.NoModuleException;
import io.github.nucleuspowered.nucleus.api.exceptions.UnremovableModuleException;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import org.spongepowered.api.plugin.Plugin;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;

import java.util.Set;

public class ModuleRegistrationProxyService implements NucleusModuleService {

    private final ModuleContainer container;
    private final Nucleus nucleus;

    public ModuleRegistrationProxyService(Nucleus nucleus) {
        this.container = nucleus.getModuleContainer();
        this.nucleus = nucleus;
    }

    @Override
    public Set<String> getModulesToLoad() {
        return container.getModules(ModuleContainer.ModuleStatusTristate.ENABLE);
    }

    @Override
    public boolean canDisableModules() {
        return container.getCurrentPhase() == ConstructionPhase.DISCOVERED;
    }

    @Override
    public void removeModule(String module, Object plugin) throws ModulesLoadedException, UnremovableModuleException, NoModuleException {
        if (!canDisableModules()) {
            throw new ModulesLoadedException();
        }

        // The plugin must actually be a plugin.
        Preconditions.checkNotNull(plugin);
        Plugin pluginAnnotation = plugin.getClass().getAnnotation(Plugin.class);
        if (pluginAnnotation == null) {
            throw new IllegalArgumentException("plugin must be your plugin instance.");
        }

        try {
            container.disableModule(module);
            nucleus.getLogger().info(Util.getMessageWithFormat("nucleus.module.disabled", pluginAnnotation.name(), pluginAnnotation.id(), module));
        } catch (IllegalStateException e) {
            throw new ModulesLoadedException();
        } catch (UndisableableModuleException e) {
            nucleus.getLogger().warn(Util.getMessageWithFormat("nucleus.module.disabled.forceload", pluginAnnotation.name(), pluginAnnotation.id(), module));
            nucleus.getLogger().warn(Util.getMessageWithFormat("nucleus.module.disabled.forceloadtwo", pluginAnnotation.name()));
            throw new UnremovableModuleException();
        } catch (uk.co.drnaylor.quickstart.exceptions.NoModuleException e) {
            throw new NoModuleException();
        }
    }
}
