package uk.co.drnaylor.minecraft.quickstart.internal.annotations;

import java.lang.annotation.*;

/**
 * Specifies a command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RootCommand {
}
