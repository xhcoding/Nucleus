/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * THIS MUST HAVE A NO-ARGS CONSTRUCTOR.
 *
 * @param <S> The {@link ModularDataService} that this represents.
 */
public abstract class DataModule<S extends ModularDataService<S>> {

    private static Map<Class<? extends DataModule<?>>, List<FieldData>> fieldData = Maps.newHashMap();

    private final List<FieldData> data;

    @SuppressWarnings("unchecked")
    public DataModule() {
        data = fieldData.computeIfAbsent((Class<? extends DataModule<?>>) this.getClass(), this::init);
    }

    protected void loadFrom(ConfigurationNode node) {
        for (FieldData d : data) {
            try {
                d.field.set(this, node.getNode((Object[]) d.path).getValue(d.clazz));
            } catch (IllegalArgumentException e) {
                // ignored, we'll stick with the default.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void saveTo(ConfigurationNode node) {
        for (FieldData d : data) {
            try {
                getObj(d.clazz, d.field, d.path, node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void getObj(TypeToken<T> typeToken, Field field, String[] path, ConfigurationNode node) throws ObjectMappingException {
        T t;
        try {
            t = (T)field.get(this);
        } catch (IllegalAccessException e) {
            t = null;
        }

        node.getNode((Object[])path).setValue(typeToken, t);
    }

    private List<FieldData> init(Class<? extends DataModule<?>> clazz) {
        // Get the fields.
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
            .filter(x -> x.isAnnotationPresent(DataKey.class))
            .collect(Collectors.toList());

        fields.forEach(x -> x.setAccessible(true));
        return fields.stream().map(x -> new FieldData(x.getAnnotation(DataKey.class).value(), TypeToken.of(x.getGenericType()), x)).collect(Collectors.toList());
    }

    private static class FieldData {

        private final String[] path;
        private final TypeToken<?> clazz;
        private final Field field;

        private FieldData(String[] path, TypeToken<?> clazz, Field field) {
            this.path = path;
            this.clazz = clazz;
            this.field = field;
        }
    }
}
