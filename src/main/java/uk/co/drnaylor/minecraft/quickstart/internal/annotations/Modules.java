package uk.co.drnaylor.minecraft.quickstart.internal.annotations;

import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;

import java.lang.annotation.*;

/**
 * An annotation to specify what modules a command or a listener belongs to.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Modules {
    PluginModule[] value() default { };
}
