/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.info.handlers.InfoHandler;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Optional;

@ModuleData(id = "info", name = "Info")
public class InfoModule extends StandardModule {

    public static final String MOTD_KEY = "motd";

    @Override
    public Optional<NucleusConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new InfoConfigAdapter());
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        nucleus.addTextFileController(
                MOTD_KEY,
                Sponge.getAssetManager().getAsset(nucleus, "motd.txt").get(),
                nucleus.getConfigDirPath().resolve("motd.txt"));

        InfoHandler ih = new InfoHandler(nucleus);
        serviceManager.registerService(InfoHandler.class, ih);
        nucleus.registerReloadable(ih);
        ih.onReload();
    }
}
