package uk.co.drnaylor.minecraft.quickstart.internal.annotations;

import java.lang.annotation.*;

/**
 * Marks that the command does not have a cooldown.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NoCooldown {
}
