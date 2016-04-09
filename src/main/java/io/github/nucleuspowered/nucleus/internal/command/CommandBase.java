/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;

public abstract class CommandBase<T extends CommandSource> extends AbstractCommand<T> {

    @Inject protected CoreConfigAdapter cca;

    /**
     * Gets the arguments of the command.
     *
     * @return The arguments of the command.
     */
    public CommandElement[] getArguments() {
        return new CommandElement[]{};
    }

    /**
     * Gets the description for the command.
     *
     * @return The description.
     */
    public String getDescription() {
        String key = String.format("description.%sdesc", this.commandPath);
        try {
            return Util.getMessageWithFormat(key);
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                return key;
            }

            return "";
        }
    }

    @Override
    public final CommandSpec createSpec() {
        Preconditions.checkState(permissions != null);
        RegisterCommand rc = getClass().getAnnotation(RegisterCommand.class);

        CommandSpec.Builder cb = CommandSpec.builder();
        if (rc == null || rc.hasExecutor()) {
            cb.executor(this).arguments(getArguments());
        }

        if (!permissions.isPassthrough()) {
            cb.permission(permissions.getBase());
        }

        String description = getDescription();
        if (!description.isEmpty()) {
            cb.description(Text.of(getDescription()));
        }

        Map<List<String>, CommandCallable> m = createChildCommands();
        if (!m.isEmpty()) {
            cb.children(m);
        }

        return cb.build();
    }
}
