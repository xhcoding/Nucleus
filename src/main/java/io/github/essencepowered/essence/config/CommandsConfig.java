/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;

public class CommandsConfig extends AbstractConfig<CommentedConfigurationNode, HoconConfigurationLoader> {
    public CommandsConfig(Path file) throws IOException, ObjectMappingException {
        super(file);
    }

    @Override
    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected CommentedConfigurationNode getDefaults() {
        return SimpleCommentedConfigurationNode.root();
    }

    public CommentedConfigurationNode getCommandNode(String command) {
        return node.getNode(command.toLowerCase());
    }

    public void mergeDefaults(CommentedConfigurationNode node) {
        this.node.mergeValuesFrom(node);
    }

    public void mergeDefaultsForCommand(String command, CommentedConfigurationNode node) {
        this.node.getNode(command.toLowerCase()).mergeValuesFrom(node);
    }
}
