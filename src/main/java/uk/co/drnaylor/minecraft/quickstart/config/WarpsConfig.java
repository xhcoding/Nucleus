package uk.co.drnaylor.minecraft.quickstart.config;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchWorldException;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;
import uk.co.drnaylor.minecraft.quickstart.config.serialisers.LocationNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WarpsConfig extends AbstractConfig<ConfigurationNode, GsonConfigurationLoader> implements QuickStartWarpService {

    private Map<String, LocationNode> warpNodes;

    public WarpsConfig(Path file) throws IOException, ObjectMappingException {
        super(file);
    }

    @Override
    public void load() throws IOException, ObjectMappingException {
        // In this case, we don't want to get/save any defaults, don't call the super method.
        node = loader.load();

        // There is a comment in the MainConfig that explains what this line is about. I'm not repeating it here.
        // Go find the comment if you're interested.
        if (warpNodes == null) {
            warpNodes = Maps.newHashMap();
        }

        warpNodes.clear();
        node.getChildrenMap().forEach((k, v) -> warpNodes.put(k.toString().toLowerCase(), new LocationNode(v)));
    }

    @Override
    public void save() throws IOException, ObjectMappingException {
        node = SimpleConfigurationNode.root();
        warpNodes.forEach((k, v) -> v.populateNode(node.getNode(k.toLowerCase())));
        super.save();
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected ConfigurationNode getDefaults() {
        return SimpleConfigurationNode.root();
    }

    @Override
    public Set<String> getWarpNames() {
        return ImmutableSet.copyOf(warpNodes.keySet());
    }

    @Override
    public boolean warpExists(String name) {
        return warpNodes.containsKey(name.toLowerCase());
    }

    @Override
    public Optional<WarpLocation> getWarp(String warpName) {
        LocationNode ln = warpNodes.get(warpName.toLowerCase());

        try {
            if (ln != null) {
                return Optional.of(new WarpLocation(warpName.toLowerCase(), ln.getLocation(), ln.getRotation()));
            }
        } catch (NoSuchWorldException ex) {
            // Yeah... we know
        }

        return Optional.empty();
    }

    @Override
    public boolean removeWarp(String warpName) {
        if (warpNodes.remove(warpName.toLowerCase()) != null) {
            try {
                save();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean setWarp(String warpName, Location<World> location, Vector3d rotation) {
        String warp = warpName.toLowerCase();
        if (getWarp(warp).isPresent()) {
            return false;
        }

        warpNodes.put(warp, new LocationNode(location, rotation));
        try {
            save();
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }

        return true;
    }
}
