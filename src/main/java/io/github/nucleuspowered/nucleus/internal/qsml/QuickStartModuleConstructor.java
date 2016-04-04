/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml;

import com.google.inject.Injector;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.constructors.ModuleConstructor;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;

public class QuickStartModuleConstructor implements ModuleConstructor {

    private final Injector injector;

    public QuickStartModuleConstructor(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Module constructModule(Class<? extends Module> moduleClass) throws QuickStartModuleLoaderException.Construction {
        return injector.getInstance(moduleClass);
    }

    @Override
    public void preEnableModule(Module module) throws QuickStartModuleLoaderException.Enabling {
        try {
            module.preEnable();
        } catch (RuntimeException e) {
            throw new QuickStartModuleLoaderException.Enabling(module.getClass(), "An error occurred.", e);
        }
    }

    @Override
    public void enableModule(Module module) throws QuickStartModuleLoaderException.Enabling {
        try {
            module.onEnable();
        } catch (RuntimeException e) {
            throw new QuickStartModuleLoaderException.Enabling(module.getClass(), "An error occurred.", e);
        }
    }
}
