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
    /**
     * The modules that this command or listener is a part of.
     *
     * @return An array of {@link PluginModule}s.
     */
    PluginModule[] value() default { };
}
