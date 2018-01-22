/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.interceptors;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ICommandInterceptor;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;

import java.util.Objects;

import javax.annotation.Nullable;

public class AFKCommandInterceptor implements ICommandInterceptor, Reloadable {

    private final AFKHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class);
    @Nullable private NucleusTextTemplate message = null;

    @Override
    public void onPreCommand(Class<? extends AbstractCommand<?>> commandClass, CommandSource source, CommandContext context) { }

    @Override
    public void onPostCommand(Class<? extends AbstractCommand<?>> commandClass, CommandSource source, CommandContext context, CommandResult result) {
        if (this.message != null && result.getSuccessCount().orElse(0) > 0 && commandClass.isAnnotationPresent(NotifyIfAFK.class)) {
            NotifyIfAFK annotation = commandClass.getAnnotation(NotifyIfAFK.class);
            for (String key : annotation.value()) {
                context.getAll(key).stream()
                        .filter(x -> x instanceof User)
                        .map(x -> ((User) x).getPlayer().orElse(null))
                        .filter(Objects::nonNull)
                        .filter(this.handler::isAFK)
                        .forEach(x -> source.sendMessage(this.message.getForCommandSource(source)));
            }
        }
    }

    @Override
    public void onReload() throws Exception {
        AFKConfig config = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKConfigAdapter.class).getNodeOrDefault();
        if (config.isAlertSenderOnAfk()) {
            NucleusTextTemplate textTemplate = config.getMessages().getOnCommand();
            if (textTemplate.isEmpty()) {
                this.message = null;
            } else {
                this.message = textTemplate;
            }
        } else {
            this.message = null;
        }
    }
}
