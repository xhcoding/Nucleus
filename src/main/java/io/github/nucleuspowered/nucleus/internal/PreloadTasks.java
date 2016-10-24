/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public abstract class PreloadTasks {

    private PreloadTasks() {}

    @SuppressWarnings("unchecked")
    public static List<Consumer<NucleusPlugin>> getPreloadTasks() {
        return Lists.newArrayList(
            // Changes the Items file to HOCON, as intended.
            plugin -> {
                try {
                    Path path = plugin.getConfigDirPath().resolve("items.conf");
                    if (Files.exists(path)) {
                        GsonConfigurationLoader old = GsonConfigurationLoader.builder().setPath(path).build();
                        ConfigurationNode cn = old.load();
                        if (!cn.hasMapChildren()) {
                            return;
                        }

                        plugin.getLogger().info("Converting items.conf to be in HOCON format");
                        HoconConfigurationLoader to = HoconConfigurationLoader.builder().setPath(path).build();
                        to.save(cn);
                    }
                } catch (Exception ignored) {
                    // Failed to load it, means it's HOCON
                }
            },
            // Moves kits into their own file.
            plugin -> {
                Path newLoc = plugin.getDataPath().resolve("kits.json");
                if (Files.exists(newLoc)) {
                    return;
                }

                Path path = plugin.getDataPath().resolve("general.json");
                GsonConfigurationLoader old = GsonConfigurationLoader.builder().setPath(path).build();
                try {
                    boolean save = false;
                    ConfigurationNode cn = old.load();
                    SimpleConfigurationNode simpleConfigurationNode = SimpleConfigurationNode.root();
                    if (!cn.getNode("kits").isVirtual()) {
                        save = true;
                        simpleConfigurationNode.getNode("kits").setValue(cn.getNode("kits").getValue());
                        cn.removeChild("kits");
                    }

                    if (!cn.getNode("firstKit").isVirtual()) {
                        save = true;
                        simpleConfigurationNode.getNode("firstKit").setValue(cn.getNode("firstKit").getValue());
                        cn.removeChild("firstKit");
                    }

                    if (save) {
                        plugin.getLogger().info("Migrating kits to kits.conf");
                        GsonConfigurationLoader newCl = GsonConfigurationLoader.builder().setPath(newLoc).build();
                        newCl.save(simpleConfigurationNode);
                        old.save(cn);
                    }
                } catch (IOException e) {
                    // ignored
                }
            });
    }
}
