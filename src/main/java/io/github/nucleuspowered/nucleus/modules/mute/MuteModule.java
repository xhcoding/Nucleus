/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.MuteData;
import io.github.nucleuspowered.nucleus.api.service.NucleusMuteService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.mute.commands.CheckMuteCommand;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "mute", name = "Mute")
public class MuteModule extends ConfigurableModule<MuteConfigAdapter> {

    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    public MuteConfigAdapter getAdapter() {
        return new MuteConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            MuteHandler m = new MuteHandler(nucleus);
            nucleus.getInjector().injectMembers(m);
            game.getServiceManager().setProvider(nucleus, NucleusMuteService.class, m);
            serviceManager.registerService(MuteHandler.class, m);
        } catch (Exception ex) {
            logger.warn("Could not load the mute module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        createSeenModule(CheckMuteCommand.class, (c, u) -> {

            // If we have a ban service, then check for a ban.
            MuteHandler jh = nucleus.getInternalServiceManager().getService(MuteHandler.class).get();
            if (jh.isMuted(u)) {
                MuteData jd = jh.getPlayerMuteData(u).get();
                // Lightweight checkban.
                Text.Builder m;
                if (jd.getEndTimestamp().isPresent()) {
                    m = Util.getTextMessageWithFormat("seen.ismuted.temp", Util.getTimeToNow(jd.getEndTimestamp().get())).toBuilder();
                } else {
                    m = Util.getTextMessageWithFormat("seen.ismuted.perm").toBuilder();
                }

                return Lists.newArrayList(
                        m.onClick(TextActions.runCommand("/checkmute " + u.getName()))
                                .onHover(TextActions.showText(Util.getTextMessageWithFormat("standard.clicktoseemore"))).build(),
                        Util.getTextMessageWithFormat("standard.reason", jd.getReason()));
            }

            return Lists.newArrayList(Util.getTextMessageWithFormat("seen.notmuted"));
        });
    }
}
