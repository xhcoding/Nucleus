package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Maps;
import uk.co.drnaylor.minecraft.quickstart.config.AbstractConfig;

import java.util.Map;
import java.util.Optional;

public class ConfigMap {

    private final Map<Class<? extends AbstractConfig>, AbstractConfig> configMap = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> Optional<T> getConfig(Class<T> configClass) {
        return Optional.ofNullable((T)configMap.get(configClass));
    }

    public <T extends AbstractConfig> void putConfig(T config) {
        configMap.put(config.getClass(), config);
    }
}
