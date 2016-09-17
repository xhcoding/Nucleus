/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import io.github.nucleuspowered.nucleus.Util;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.service.permission.Subject;

import java.util.List;

@ConfigSerializable
public class ChatConfig {

    @Setting(value = "modifychat", comment = "loc:config.chat.modify")
    private boolean modifychat = true;

    @Setting(value = "templates")
    private TemplateConfig templates = new TemplateConfig();

    public boolean isModifychat() {
        return modifychat;
    }

    public ChatTemplateConfig getDefaultTemplate() {
        return templates.getDefaultTemplate();
    }

    public ChatTemplateConfig getTemplate(Subject subject) {
        List<Subject> groups;
        try {
             groups = Util.getParentSubjects(subject);
        } catch (Exception e) {
            return getDefaultTemplate();
        }

        if (groups == null || groups.isEmpty()) {
            return getDefaultTemplate();
        }

        groups.sort((x, y) -> y.getParents().size() - x.getParents().size());

        // Iterate through all groups the player is in.
        for (Subject group : groups) {
            if (templates.getGroupTemplates().containsKey(group.getIdentifier())) {
                return templates.getGroupTemplates().get(group.getIdentifier());
            }
        }

        // Return the default.
        return getDefaultTemplate();
    }
}
