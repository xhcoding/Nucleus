/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import io.github.nucleuspowered.nucleus.Util;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.service.permission.Subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class ChatConfig {

    @Setting(value = "modifychat", comment = "loc:config.chat.modify")
    private boolean modifychat = true;

    @Setting(value = "template", comment = "loc:config.chat.default-template")
    private ChatTemplateConfig template = new ChatTemplateConfig();

    @Setting(value = "group-templates", comment = "loc:config.chat.group-templates")
    private Map<String, ChatTemplateConfig> groupTemplates = new HashMap<String, ChatTemplateConfig>() {{
        // We don't want this affecting the default group, but we need an example.
        put("DefaultTemplate", new ChatTemplateConfig());
    }};

    public boolean isModifychat() {
        return modifychat;
    }

    public ChatTemplateConfig getDefaultTemplate() {
        return template;
    }

    public ChatTemplateConfig getTemplate(Subject subject) {
        List<Subject> groups;
        try {
             groups = Util.getParentSubjects(subject);
        } catch (Exception e) {
            return template;
        }

        if (groups == null || groups.isEmpty()) {
            return template;
        }

        groups.sort((x, y) -> y.getParents().size() - x.getParents().size());

        // Iterate through all groups the player is in.
        for (Subject group : groups) {
            if (groupTemplates.containsKey(group.getIdentifier())) {
                return groupTemplates.get(group.getIdentifier());
            }
        }

        // Return the default.
        return template;
    }
}
