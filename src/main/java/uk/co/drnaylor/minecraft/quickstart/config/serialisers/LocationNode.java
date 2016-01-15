package uk.co.drnaylor.minecraft.quickstart.config.serialisers;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchWorldException;

import java.util.Optional;
import java.util.UUID;

public class LocationNode {

    private final double x;
    private final double y;
    private final double z;
    private final UUID world;

    public LocationNode(ConfigurationNode locationNode) {
        this.x = locationNode.getNode("x").getDouble();
        this.y = locationNode.getNode("y").getDouble();
        this.z = locationNode.getNode("z").getDouble();
        this.world = UUID.fromString(locationNode.getNode("world").getString());
    }

    public LocationNode(Location<World> length) {
        this.x = length.getX();
        this.y = length.getY();
        this.z = length.getZ();
        this.world = length.getExtent().getUniqueId();
    }

    public LocationNode(double x, double y, double z, UUID world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    /**
     * Populates the selected node with the values.
     *
     * @param cn The node.
     */
    public void populateNode(ConfigurationNode cn) {
        cn.getNode("x").setValue(x);
        cn.getNode("y").setValue(y);
        cn.getNode("z").setValue(z);
        cn.getNode("world").setValue(world.toString());
    }

    /**
     * Gets a {@link Location} from the node.
     *
     * @return The {@link Location]}
     * @throws NoSuchWorldException The world does not exist.
     */
    public Location<World> getLocation() throws NoSuchWorldException {
        Optional<World> ow = Sponge.getServer().getWorld(world);

        if (ow.isPresent()) {
            return new Location<World>(ow.get(), x, y, z);
        }

        throw new NoSuchWorldException();
    }
}
