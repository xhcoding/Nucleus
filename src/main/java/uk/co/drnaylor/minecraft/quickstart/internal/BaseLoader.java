package uk.co.drnaylor.minecraft.quickstart.internal;

import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class BaseLoader<T> {

    private final QuickStartModuleService service = Sponge.getServiceManager().provideUnchecked(QuickStartModuleService.class);

    private final Predicate<Class<? extends T>> moduleCheck = o -> {
        Modules annotation = o.getAnnotation(Modules.class);
        // No annotation, include it.
        return annotation == null || service.getModulesToLoad().stream().anyMatch(a -> Arrays.asList(annotation.value()).contains(a));
    };

    Set<Class<? extends T>> filterOutModules(Set<Class<? extends T>> objectsToFilter) {
        return objectsToFilter.stream().filter(moduleCheck).collect(Collectors.toSet());
    }
}