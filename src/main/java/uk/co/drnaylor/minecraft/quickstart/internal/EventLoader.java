package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.commands.mail.MailCommand;
import uk.co.drnaylor.minecraft.quickstart.listeners.*;

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
        events.add(WarmupListener.class);
        events.add(AFKListener.class);
        events.add(MailListener.class);
        events.add(MiscListener.class);
        return events;
    }

    public void loadEvents() {
        Set<Class<? extends ListenerBase>> commandsToLoad = base.filterOutModules(getEvents());
        Injector injector = quickStart.getInjector();
        commandsToLoad.stream().map(x -> {
            try {
                ListenerBase lb = x.newInstance();
                injector.injectMembers(lb);
                return lb;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(lb -> lb != null).forEach(c -> Sponge.getEventManager().registerListeners(quickStart, c));
    }
}
