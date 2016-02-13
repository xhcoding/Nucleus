package uk.co.drnaylor.minecraft.quickstart.internal.annotations;

import java.lang.annotation.*;

/**
 * Marks a command as not having a warmup, even if one is defined.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NoWarmup {

    /**
     * Whether to generate the Warmup Config entry.
     *
     * @return <code>true</code> if so.
     */
    boolean generateConfigEntry() default false;
}
