/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.annotation.Nullable;

public class WorldCorrector {

    public static void worldCheck() throws IOException, ObjectMappingException {
        final Map<String, UUID> m = get();

        // Now get the file out.
        Path path = Nucleus.getNucleus().getDataPath().resolve("worlduuids.json");
        ConfigurationLoader<ConfigurationNode> cl = GsonConfigurationLoader.builder().setPath(path).build();
        Map<String, UUID> ms = cl.load().getValue(new TypeToken<Map<String, UUID>>() {}, Maps.newHashMap());

        final Map<UUID, UUID> fromToConverter = Maps.newHashMap();
        for (String r : ms.keySet()) {
            UUID oldUuid = ms.get(r);
            @Nullable UUID newUuid = m.get(r);
            if (newUuid != null && !oldUuid.equals(newUuid)) {
                fromToConverter.put(newUuid, oldUuid);
            }
        }

        cl.save(cl.createEmptyNode().setValue(new TypeToken<Map<String, UUID>>() {}, m));

        if (fromToConverter.isEmpty()) {
            return;
        }

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        ConsoleSource cs = Sponge.getServer().getConsole();
        List<Text> msg = Lists.newArrayList();
        Nucleus.getNucleus().addX(msg, 0);
        msg.forEach(cs::sendMessage);
        cs.sendMessage(Text.of(TextColors.RED, "--------------------"));
        cs.sendMessage(mp.getTextMessageWithFormat("worldrepair.detected"));
        for (Map.Entry<UUID, UUID> u : fromToConverter.entrySet()) {
            cs.sendMessage(mp.getTextMessageWithFormat("worldrepair.worldlist",
                    Sponge.getServer().getWorldProperties(u.getKey()).get().getWorldName(),
                    u.getValue().toString(),
                    u.getKey().toString()));
        }

        Method method;
        try {
            method = Sponge.getServer().getDefaultWorld().get().getClass().getDeclaredMethod("setUniqueId", UUID.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            cs.sendMessage(mp.getTextMessageWithFormat("worldrepair.whitelist.nocmd"));
            return;
        }

        cs.sendMessage(mp.getTextMessageWithFormat("worldrepair.whitelist.cmd"));
        cs.sendMessage(Text.of(TextColors.RED, "--------------------"));
        cs.sendMessage(mp.getTextMessageWithFormat("worldrepair.whitelist.cmd2"));

        Sponge.getServer().setHasWhitelist(true);

        Sponge.getCommandManager().register(Nucleus.getNucleus(), CommandSpec.builder().executor((s, a) -> {
            MessageProvider mpr = Nucleus.getNucleus().getMessageProvider();
            if (s instanceof ConsoleSource) {
                cs.sendMessage(mpr.getTextMessageWithFormat("worldrepair.repair.start"));
                Sponge.getEventManager().registerListener(Nucleus.getNucleus(), GameStoppedServerEvent.class, event -> {
                    for (Map.Entry<UUID, UUID> meuu : fromToConverter.entrySet()) {
                        // Magic!
                        WorldProperties wp = Sponge.getServer().getWorldProperties(meuu.getKey())
                                .orElseThrow(() -> new NoSuchElementException(
                                        mpr.getMessageWithFormat("worldrepair.repair.nouuid", meuu.getKey().toString())
                                ));
                        final String name = wp.getWorldName();
                        try {
                            cs.sendMessage(mpr.getTextMessageWithFormat("worldrepair.repair.try", name));
                            method.invoke(wp, meuu.getValue());
                            Sponge.getServer().saveWorldProperties(wp);
                            cs.sendMessage(mpr.getTextMessageWithFormat("worldrepair.repair.success", name));
                        } catch (Exception e) {
                            cs.sendMessage(mpr.getTextMessageWithFormat("worldrepair.repair.fail", name));
                            e.printStackTrace();
                        }
                    }

                    try {
                        cl.save(cl.createEmptyNode().setValue(new TypeToken<Map<String, UUID>>() {}, get()));
                    } catch (IOException | ObjectMappingException e) {
                        e.printStackTrace();
                    }
                });

                Sponge.getServer().shutdown();
                return CommandResult.success();
            } else {
                s.sendMessage(mpr.getTextMessageWithFormat("command.consoleonly"));
                return CommandResult.empty();
            }
        }).build(), "repairuuids");

    }

    public static void delete() throws IOException {
        Files.deleteIfExists(Nucleus.getNucleus().getDataPath().resolve("worlduuids.json"));
    }

    public static Map<String, UUID> save() throws IOException, ObjectMappingException {
        Path path = Nucleus.getNucleus().getDataPath().resolve("worlduuids.json");
        ConfigurationLoader<ConfigurationNode> cl = GsonConfigurationLoader.builder().setPath(path).build();
        Map<String, UUID> g = get();
        cl.save(cl.createEmptyNode().setValue(new TypeToken<Map<String, UUID>>() {}, g));
        return g;
    }

    private static Map<String, UUID> get() {
        Map<String, UUID> m = Maps.newHashMap();
        Collection<WorldProperties> lwp = Sponge.getServer().getAllWorldProperties();
        for (WorldProperties wp : lwp) {
            m.put(wp.getWorldName(), wp.getUniqueId());
        }
        return m;
    }
}
