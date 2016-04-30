/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.configurate;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.LocalisedCommentSetting;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

import java.lang.reflect.Field;
import java.util.Map;

public class NucleusObjectMapper<T> extends ObjectMapper<T> {

    /**
     * Create a new object mapper of a given type
     *
     * @param clazz The type this object mapper will work with
     * @throws ObjectMappingException if the provided class is in someway invalid
     */
    public NucleusObjectMapper(Class<T> clazz) throws ObjectMappingException {
        super(clazz);
    }

    protected void collectFields(Map<String, FieldData> cachedFields, Class<? super T> clazz) throws ObjectMappingException {
        for (Field field : clazz.getDeclaredFields()) {
            Info setting = null;
            if (field.isAnnotationPresent(Setting.class)) {
                setting = setting(field);
            } else if (field.isAnnotationPresent(LocalisedCommentSetting.class)) {
                setting = locSetting(field);
            }

            if (setting != null) {
                addField(cachedFields, field, setting.path, setting.comment);
            }
        }
    }

    private Info setting(Field field) {
        Setting setting = field.getAnnotation(Setting.class);
        String path = setting.value();
        if (path.isEmpty()) {
            path = field.getName();
        }

        String comment = setting.comment();
        if (comment.startsWith("loc:")) {
            comment = Util.getMessageWithFormat(setting.comment().split(":", 2)[1]);
        }

        return new Info(path, comment);
    }

    private Info locSetting(Field field) {
        LocalisedCommentSetting setting = field.getAnnotation(LocalisedCommentSetting.class);
        String path = setting.value();
        if (path.isEmpty()) {
            path = field.getName();
        }

        String comment = setting.messageKey();
        if (!comment.isEmpty()) {
            return new Info(path, Util.getMessageWithFormat(comment));
        }

        return new Info(path, "");
    }

    private void addField(Map<String, FieldData> cachedFields, Field field, String path, String comment) throws ObjectMappingException {
        FieldData data = new FieldData(field, comment);
        field.setAccessible(true);
        if (!cachedFields.containsKey(path)) {
            cachedFields.put(path, data);
        }
    }

    private static class Info {
        private final String path;
        private final String comment;

        public Info(String path, String comment) {
            this.path = path;
            this.comment = comment;
        }
    }
}
