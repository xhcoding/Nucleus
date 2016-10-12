/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Util;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.Subject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Map<String, WeightedChatTemplateConfig> getGroupTemplates() {
        return ImmutableMap.copyOf(templates.getGroupTemplates());
    }
}
