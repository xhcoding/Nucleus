/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * This annotation indicates that an API element is considered complete for Nucleus 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Stable {}
