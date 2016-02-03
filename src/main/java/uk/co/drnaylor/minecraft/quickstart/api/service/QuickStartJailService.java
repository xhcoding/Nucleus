package uk.co.drnaylor.minecraft.quickstart.api.service;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Daniel on 03/02/2016.
 */
public interface QuickStartJailService {
    boolean setJail(String name, Location<World> location, Vector3d rotation);

    Set<String> getJails();

    Optional<WarpLocation> getJail(String name);

    boolean removeJail(String name);

    boolean jailPlayer(User user, JailData data);

    boolean unjailPlayer(User user);
}
