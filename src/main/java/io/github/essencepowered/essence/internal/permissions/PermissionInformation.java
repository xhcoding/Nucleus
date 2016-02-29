/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal.permissions;

import org.spongepowered.api.text.Text;

public class PermissionInformation {

    public final Text description;
    public final SuggestedLevel level;

    public PermissionInformation(String description, SuggestedLevel level) {
        this.description = Text.of(description);
        this.level = level;
    }

    public PermissionInformation(Text description, SuggestedLevel level) {
        this.description = description;
        this.level = level;
    }
}
