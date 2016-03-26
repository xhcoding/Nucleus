/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.config.bases.AbstractStandardNodeConfig;
import io.github.nucleuspowered.nucleus.internal.messages.ResourceMessageProvider;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Optional;

public class MessageConfig extends AbstractStandardNodeConfig<CommentedConfigurationNode, HoconConfigurationLoader> {

    public MessageConfig(Path file) throws Exception {
        super(file);
    }

    @Override
    protected CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();
        ResourceMessageProvider rmp = new ResourceMessageProvider();
        rmp.getKeys().forEach(x -> ccn.getNode((Object[])x.split("\\.")).setValue(rmp.getMessageFromKey(x).get()));

        return ccn;
    }

    @Override
    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    public Optional<String> getKey(@Nonnull String key) {
        Preconditions.checkNotNull(key);
        Object[] obj = key.split("\\.");
        return Optional.ofNullable(node.getNode(obj).getString());
    }
}
