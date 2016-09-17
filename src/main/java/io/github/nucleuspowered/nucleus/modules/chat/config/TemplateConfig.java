/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class TemplateConfig {

    @Setting(value = "default", comment = "loc:config.chat.default-template")
    private ChatTemplateConfig defaultTemplate = new ChatTemplateConfig();

    @Setting(value = "group-templates", comment = "loc:config.chat.group-templates")
    private Map<String, ChatTemplateConfig> groupTemplates = new HashMap<String, ChatTemplateConfig>() {{
        // We don't want this affecting the default group, but we need an example.
        put("DefaultTemplate", new ChatTemplateConfig());
    }};

    public ChatTemplateConfig getDefaultTemplate() {
        return defaultTemplate;
    }

    public Map<String, ChatTemplateConfig> getGroupTemplates() {
        return groupTemplates;
    }
}
