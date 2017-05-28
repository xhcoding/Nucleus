/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
            },
            // Move rules into their own file.
            plugin -> {
                try {
                    Path rules = plugin.getConfigDirPath().resolve("rules.txt");
                    if (Files.exists(rules)) {
                        return;
                    }

                    // Get the main conf file, check to see if the "rules" section exists.
                    Path main = plugin.getConfigDirPath().resolve("main.conf");
                    if (!Files.exists(main)) {
                        return;
                    }

                    HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(main).build();
                    CommentedConfigurationNode node = loader.load();
                    CommentedConfigurationNode c = node.getNode("rules", "rules");
                    if (!c.isVirtual() && c.getValue() != null) {
                        // Transform!
                        List<String> r = c.getList(TypeToken.of(String.class));
                        List<String> newRules = Lists.newArrayList();
                        if (r.isEmpty()) {
                            return;
                        }

                        int i = 1;
                        for (String s : r) {
                            newRules.add(String.format("&a%d: %s", i++, s));
                        }

                        Files.write(rules, newRules, Charsets.UTF_8, StandardOpenOption.CREATE_NEW);
                        c.setValue(null);
                        node.getNode("rules").removeChild("rules");
                        loader.save(node);
                    }

                } catch (Exception ignored) {
                    // ignored
                }
            },
            // Move the modules location
            plugin -> {
                try {
                    // Get the main conf file, move the "modules" section
                    Path main = plugin.getConfigDirPath().resolve("main.conf");
                    if (!Files.exists(main)) {
                        return;
                    }

                    HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(main).build();
                    CommentedConfigurationNode node = loader.load();
                    CommentedConfigurationNode c = node.getNode("modules");
                    if (!c.isVirtual()) {
                        node.getNode("-modules").setValue(c);
                        node.getNode("modules").setValue(null);
                    }

                    loader.save(node);
                } catch (Exception ignored) {
                    // ignored
                }
            });
    }

    @SuppressWarnings("unchecked")
    public static List<Consumer<NucleusPlugin>> getPreloadTasks2() {
        return Lists.newArrayList(
        plugin -> {
            try {
                Path path = plugin.getDataPath().resolve("general.json");
                if (Files.exists(path)) {
                    GsonConfigurationLoader old = GsonConfigurationLoader.builder().setPath(path).build();
                    ConfigurationNode cn = old.load();
                    if (!cn.getNode("blacklistedTypes").isVirtual()) {
                        plugin.getLogger().info("Moving blacklist to items.conf");
                        List<String> types = cn.getNode("blacklistedTypes").getList(TypeToken.of(String.class));
                        final ItemDataService service = plugin.getItemDataService();
                        types.forEach(s -> {
                            ItemDataNode idn = service.getDataForItem(s);
                            idn.getBlacklist().setInventory(true);
                            idn.getBlacklist().setEnvironment(true);
                            idn.getBlacklist().setUse(true);
                            service.setDataForItem(s, idn);
                        });

                        if (!types.isEmpty()) {
                            service.save();
                        }

                        cn.removeChild("blacklistedTypes");
                        old.save(cn);
                    }
                }
            } catch (Exception ignored) {
                // ignored
            }
        });
    }
}
