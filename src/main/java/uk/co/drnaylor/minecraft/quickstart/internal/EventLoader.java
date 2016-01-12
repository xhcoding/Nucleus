package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;

import java.util.Set;

public class EventLoader {
    private BaseLoader<EventBase> base = new BaseLoader<>();
    private final QuickStart quickStart;

    public EventLoader(QuickStart quickStart) {
        this.quickStart = quickStart;
    }

    private Set<Class<? extends EventBase>> getEvents() {
        Set<Class<? extends EventBase>> events = Sets.newHashSet();
        return events;
    }

    public void loadCommands() {
        Set<Class<? extends EventBase>> commandsToLoad = base.filterOutModules(getEvents());
        Injector injector = quickStart.getInjector();
        commandsToLoad.stream().map(injector::getInstance).forEach(c -> Sponge.getEventManager().registerListeners(quickStart, c));
    }
}
