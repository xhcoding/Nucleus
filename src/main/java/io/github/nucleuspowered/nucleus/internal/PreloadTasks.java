/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public abstract class PreloadTasks {

    private PreloadTasks() {}

    @SuppressWarnings("unchecked")
    public static List<Consumer<NucleusPlugin>> getPreloadTasks() {
        return Lists.newArrayList(
                // Move the misc.require-god-permission-on-login to invulnerability.require-invulnerbility-permission-on-login
                plugin -> {
                    try {
                        // Get the main conf file
                        Path main = plugin.getConfigDirPath().resolve("main.conf");
                        if (!Files.exists(main)) {
                            return;
                        }

                        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(main).build();
                        CommentedConfigurationNode node = loader.load();
                        if (node.getNode("invulnerability", "require-invulnerbility-permission-on-login").isVirtual()) {
                            CommentedConfigurationNode cn = node.getNode("misc", "require-god-permission-on-login");
                            if (!cn.isVirtual()) {
                                boolean c = node.getNode("misc", "require-god-permission-on-login").getBoolean();
                                node.getNode("invulnerability", "require-invulnerbility-permission-on-login").setValue(c);
                                node.getNode("misc", "require-god-permission-on-login").setValue(null);
                            }

                            loader.save(node);
                        }
                    } catch (Exception ignored) {
                        // ignored
                    }
                }
            );
    }

    @SuppressWarnings("unchecked")
    public static List<Consumer<NucleusPlugin>> getPreloadTasks2() {
        return Lists.newArrayList();
    }
}
