package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.commands.afk.AFKCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.core.QuickStartCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.environment.TimeCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.environment.WeatherCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.home.*;
import uk.co.drnaylor.minecraft.quickstart.commands.jail.CheckJailCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.jail.JailsCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.kick.KickAllCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.kick.KickCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.mail.MailCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.message.MessageCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.message.ReplyCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.message.SocialSpyCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.misc.*;
import uk.co.drnaylor.minecraft.quickstart.commands.mute.CheckMuteCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.mute.MuteCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.teleport.*;
import uk.co.drnaylor.minecraft.quickstart.commands.warp.WarpsCommand;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.listeners.*;
import uk.co.drnaylor.minecraft.quickstart.runnables.AFKTask;
import uk.co.drnaylor.minecraft.quickstart.runnables.JailTask;
import uk.co.drnaylor.minecraft.quickstart.runnables.TeleportTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PluginSystemsLoader {
    private final QuickStart quickStart;

    public PluginSystemsLoader(QuickStart quickStart) {
        this.quickStart = quickStart;
    }

    private final QuickStartModuleService service = Sponge.getServiceManager().provideUnchecked(QuickStartModuleService.class);

    private final Predicate<Class<?>> moduleCheck = o -> {
        Modules annotation = o.getAnnotation(Modules.class);
        // No annotation, include it.
        return annotation == null || service.getModulesToLoad().stream().anyMatch(a -> Arrays.asList(annotation.value()).contains(a));
    };

    <T> Set<Class<? extends T>> filterOutModules(Set<Class<? extends T>> objectsToFilter) {
        return objectsToFilter.stream().filter(moduleCheck).collect(Collectors.toSet());
    }

    private Set<Class<? extends CommandBase>> getCommands() {
        Set<Class<? extends CommandBase>> cmds = Sets.newHashSet();
        cmds.add(QuickStartCommand.class);

        // AFK
        cmds.add(AFKCommand.class);

        // Warps
        cmds.add(WarpsCommand.class);

        // Chat
        cmds.add(MuteCommand.class);
        cmds.add(CheckMuteCommand.class);

        // Messages
        cmds.add(MessageCommand.class);
        cmds.add(ReplyCommand.class);
        cmds.add(SocialSpyCommand.class);

        // Kick
        cmds.add(KickAllCommand.class);
        cmds.add(KickCommand.class);

        // Environment
        cmds.add(WeatherCommand.class);
        cmds.add(TimeCommand.class);

        // Misc
        cmds.add(GodCommand.class);
        cmds.add(FlyCommand.class);
        cmds.add(HealCommand.class);
        cmds.add(FeedCommand.class);
        cmds.add(BroadcastCommand.class);

        // Mail
        cmds.add(MailCommand.class);

        // Jail
        cmds.add(JailsCommand.class);
        cmds.add(CheckJailCommand.class);

        // Homes
        cmds.add(HomeCommand.class);
        cmds.add(HomeOtherCommand.class);
        cmds.add(ListHomeCommand.class);
        cmds.add(SetHomeCommand.class);
        cmds.add(DeleteHomeCommand.class);
        cmds.add(DeleteOtherHomeCommand.class);

        // Teleportation
        cmds.add(TeleportToggleCommand.class);
        cmds.add(TeleportCommand.class);
        cmds.add(TeleportHereCommand.class);
        cmds.add(TeleportAllHereCommand.class);
        cmds.add(TPNativeCommand.class);
        cmds.add(TeleportPositionCommand.class);

        return cmds;
    }

    private Set<Class<? extends ListenerBase>> getEvents() {
        Set<Class<? extends ListenerBase>> events = Sets.newHashSet();
        events.add(CoreListener.class);
        events.add(MuteListener.class);
        events.add(WarmupListener.class);
        events.add(AFKListener.class);
        events.add(MailListener.class);
        events.add(MiscListener.class);
        events.add(JailListener.class);
        events.add(CommandLoggingListener.class);
        return events;
    }

    private Set<Class<? extends TaskBase>> getTasks() {
        Set<Class<? extends TaskBase>> runnables = Sets.newHashSet();
        runnables.add(AFKTask.class);
        runnables.add(JailTask.class);
        runnables.add(TeleportTask.class);
        return runnables;
    }

    public void load() {
        loadCommands();
        loadEvents();
        loadRunnables();
    }

    private void loadCommands() {
        Set<Class<? extends CommandBase>> commandsToLoad = filterOutModules(getCommands());
        Injector injector = quickStart.getInjector();

        // Commands config!

        CommandsConfig cc = quickStart.getConfig(ConfigMap.COMMANDS_CONFIG).get();
        CommentedConfigurationNode sn = SimpleCommentedConfigurationNode.root();
        commandsToLoad.stream().map(x -> {
            try {
                CommandBase cb = x.newInstance();
                injector.injectMembers(cb);
                return cb;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(x -> x != null).forEach(c -> {
            // Merge in config defaults.
            if (c.mergeDefaults()) {
                sn.getNode(c.getAliases()[0].toLowerCase()).setValue(c.getDefaults());
            }

            // Register the commands.
            Sponge.getCommandManager().register(quickStart, c.createSpec(), c.getAliases());
        });

        try {
            cc.mergeDefaults(sn);
            cc.save();
        } catch (IOException | ObjectMappingException e) {
            quickStart.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        Set<Class<? extends ListenerBase>> commandsToLoad = filterOutModules(getEvents());
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

    private void loadRunnables() {
        Set<Class<? extends TaskBase>> commandsToLoad = filterOutModules(getTasks());
        Injector injector = quickStart.getInjector();
        commandsToLoad.stream().map(x -> {
            try {
                TaskBase lb = x.newInstance();
                injector.injectMembers(lb);
                return lb;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(lb -> lb != null).forEach(c -> {
            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().execute(c).interval(c.secondsPerRun(), TimeUnit.SECONDS);
            if (c.isAsync()) {
                tb.async();
            }

            tb.submit(quickStart);
        });
    }
}
