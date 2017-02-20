/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.configurate.ConfigurateHelper;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigurateDataProvider<T> extends AbstractConfigurateDataProvider<T> {

    private final TypeToken<T> typeToken;
    private final Supplier<T> defaultSupplier;

    @SuppressWarnings("unchecked")
    public ConfigurateDataProvider(TypeToken<T> type, Function<Path, ConfigurationLoader<?>>  loaderProvider, Path file, Logger logger) {
        this(type, loaderProvider, () -> {
            try {
                return (T)type.getRawType().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }, file, true, logger);
    }

    public ConfigurateDataProvider(TypeToken<T> type, Function<Path, ConfigurationLoader<?>>  loaderProvider, Supplier<T> defaultSupplier, Path file, boolean requiresChildren, Logger logger) {
        super(loaderProvider, file, requiresChildren, logger);
        this.typeToken = type;
        this.defaultSupplier = defaultSupplier;
    }

    @Override protected T transformOnLoad(ConfigurationNode node) throws Exception {
        if (node.getOptions().acceptsType(Short.class)) {
            return node.getValue(typeToken, defaultSupplier);
        }

        ConfigurationOptions options = node.getOptions().setAcceptedTypes(
                ImmutableSet.of(Map.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class, Short.class));
        return SimpleCommentedConfigurationNode.root(options).setValue(node).getValue(typeToken, defaultSupplier);
    }

    @Override protected ConfigurationNode transformOnSave(T info) throws Exception {
        ConfigurationOptions configurateOptions = ConfigurateHelper.setOptions(loader.getDefaultOptions());
        ConfigurationNode node = loader.createEmptyNode(configurateOptions);

        if (node.getOptions().acceptsType(Short.class)) {
            return node.setValue(typeToken, info);
        }

        // Allow short datatypes again.
        ConfigurationOptions options = node.getOptions().setAcceptedTypes(
            ImmutableSet.of(Map.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class, Short.class));
        return SimpleCommentedConfigurationNode.root(options).setValue(typeToken, info);
    }
}
