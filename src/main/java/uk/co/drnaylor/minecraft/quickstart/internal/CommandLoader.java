package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.commands.core.QuickStartCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.mute.CheckMuteCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.mute.MuteCommand;
import uk.co.drnaylor.minecraft.quickstart.commands.warp.WarpsCommand;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;

import java.io.IOException;
import java.util.Set;

public class CommandLoader {
    private final QuickStart quickStart;
    private final BaseLoader<CommandBase> base = new BaseLoader<>();

    public CommandLoader(QuickStart quickStart) {
        this.quickStart = quickStart;
    }

    private Set<Class<? extends CommandBase>> getCommands() {
        Set<Class<? extends CommandBase>> cmds = Sets.newHashSet();
        cmds.add(QuickStartCommand.class);
        cmds.add(WarpsCommand.class);
        cmds.add(MuteCommand.class);
        cmds.add(CheckMuteCommand.class);
        return cmds;
    }

    public void loadCommands() {
        Set<Class<? extends CommandBase>> commandsToLoad = base.filterOutModules(getCommands());
        Injector injector = quickStart.getInjector();

        // Commands config!

        CommandsConfig cc = quickStart.getConfig(CommandsConfig.class).get();
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
            cc.mergeDefaultsForCommand(c.getAliases()[0], c.getDefaults());

            // Register the commands.
            Sponge.getCommandManager().register(quickStart, c.createSpec(), c.getAliases());
        });

        try {
            cc.save();
        } catch (IOException | ObjectMappingException e) {
            quickStart.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }
}
