package uk.co.drnaylor.minecraft.quickstart.runnables;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class JailTask implements Consumer<Task> {
    private final QuickStart plugin;

    public JailTask(QuickStart plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(Task task) {
        Collection<Player> pl = Sponge.getServer().getOnlinePlayers();
        UserConfigLoader ucl = plugin.getUserLoader();
        pl.stream().map(x -> {
            try {
                return ucl.getUser(x);
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(x -> x == null || x.getJailData().isPresent()).forEach(x -> Util.testForEndTimestamp(x.getJailData(), () -> plugin.getJailHandler().unjailPlayer(x.getUser())));
    }
}
