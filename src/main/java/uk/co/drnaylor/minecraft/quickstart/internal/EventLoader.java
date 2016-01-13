package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.listeners.CoreListener;
import uk.co.drnaylor.minecraft.quickstart.listeners.MuteListener;

import java.util.Set;

public class EventLoader {
    private BaseLoader<ListenerBase> base = new BaseLoader<>();
    private final QuickStart quickStart;

    public EventLoader(QuickStart quickStart) {
        this.quickStart = quickStart;
    }

    private Set<Class<? extends ListenerBase>> getEvents() {
        Set<Class<? extends ListenerBase>> events = Sets.newHashSet();
        events.add(CoreListener.class);
        events.add(MuteListener.class);
        return events;
    }

    public void loadCommands() {
        Set<Class<? extends ListenerBase>> commandsToLoad = base.filterOutModules(getEvents());
        Injector injector = quickStart.getInjector();
        commandsToLoad.stream().map(injector::getInstance).forEach(c -> Sponge.getEventManager().registerListeners(quickStart, c));
    }
}
