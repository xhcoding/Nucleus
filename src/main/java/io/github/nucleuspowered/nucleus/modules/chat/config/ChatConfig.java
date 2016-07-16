/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigSerializable
public class ChatConfig {

    @Setting(value = "modifychat", comment = "loc:config.chat.modify")
    private boolean modifychat = true;

    @Setting("templates")
    private Map<String, ChatTemplateConfig> template = new HashMap<String, ChatTemplateConfig>() {{
        put("Default", new ChatTemplateConfig());
    }};

    public boolean isModifychat() {
        return modifychat;
    }

    public ChatTemplateConfig getTemplate(Player player) {
        List<Subject> groups = player.getSubjectData().getAllParents().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        groups.sort((x, y) -> y.getParents().size() - x.getParents().size());

        return template.containsKey(groups.get(0).getIdentifier()) ? template.get(groups.get(0).getIdentifier()) : template.get("Default");
    }
}
