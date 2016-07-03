/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.configurate.datatypes.UserDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.WorldDataNode;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.Vector3dTypeSerialiser;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DataProviders {

    private final Nucleus plugin;
    private final TypeToken<UserDataNode> ttu = TypeToken.of(UserDataNode.class);
    private final TypeToken<WorldDataNode> ttw = TypeToken.of(WorldDataNode.class);

    public DataProviders(Nucleus plugin) {
        this.plugin = plugin;
    }

    public DataProvider<UserDataNode> getUserFileDataProviders(UUID uuid) {
        // For now, just the Configurate one.
        try {
            Path p = getFile("userdata%1$s%2$s%1$s%3$s.json", uuid);
            return new ConfigurateDataProvider<>(ttu, getBuilder().setPath(p).build(), p);
        } catch (Exception e) {
            return null;
        }
    }

    public DataProvider<WorldDataNode> getWorldFileDataProvider(UUID uuid) {
        // For now, just the Configurate one.
        try {
            Path p = getFile("worlddata%1$s%2$s%1$s%3$s.json", uuid);
            return new ConfigurateDataProvider<>(ttw, getBuilder().setPath(p).build(), p);
        } catch (Exception e) {
            return null;
        }
    }

    private Path getFile(String template, UUID uuid) throws Exception {
        String u = uuid.toString();
        String f = u.substring(0, 2);
        Path file = plugin.getDataPath().resolve(String.format(template, File.separator, f, u));

        if (Files.notExists(file)) {
            Files.createDirectories(file.getParent());
        }

        return file;
    }

    private GsonConfigurationLoader.Builder getBuilder() {
        GsonConfigurationLoader.Builder gsb = GsonConfigurationLoader.builder();
        ConfigurationOptions configurationOptions = gsb.getDefaultOptions();
        TypeSerializerCollection tsc = configurationOptions.getSerializers().registerType(TypeToken.of(Vector3d.class), new Vector3dTypeSerialiser());
        return gsb.setDefaultOptions(configurationOptions.setSerializers(tsc));
    }
}
