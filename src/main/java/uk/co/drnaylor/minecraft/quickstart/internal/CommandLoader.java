package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.commands.QuickStartCommand;

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
        return cmds;
    }

    public void loadCommands() {
        Set<Class<? extends CommandBase>> commandsToLoad = base.filterOutModules(getCommands());
        Injector injector = quickStart.getInjector();
        commandsToLoad.stream().map(injector::getInstance).forEach(c -> Sponge.getCommandManager().register(quickStart, c.getSpec(), c.getAliases()));
    }
}
