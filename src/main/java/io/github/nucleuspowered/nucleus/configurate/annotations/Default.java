/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a config entry is null or empty and is decorated with this, Nucleus will attempt to parse this value instead.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Default {

    String value();
}
