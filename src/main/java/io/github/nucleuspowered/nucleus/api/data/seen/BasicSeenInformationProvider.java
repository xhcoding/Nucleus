/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.data.seen;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.BiFunction;

public class BasicSeenInformationProvider implements SeenInformationProvider {

    private final String permission;
    private final BiFunction<CommandSource, User, Collection<Text>> getterFunction;

    public BasicSeenInformationProvider(String permission, BiFunction<CommandSource, User, Collection<Text>> getterFunction) {
        this.permission = permission;
        this.getterFunction = getterFunction;
    }

    @Override
    public boolean hasPermission(@Nonnull CommandSource source, @Nonnull User user) {
        return source.hasPermission(permission);
    }

    @Nonnull
    @Override
    public Collection<Text> getInformation(@Nonnull CommandSource source, @Nonnull User user) {
        return getterFunction.apply(source, user);
    }
}
