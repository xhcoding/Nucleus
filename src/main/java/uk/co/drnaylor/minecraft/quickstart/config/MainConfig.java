package uk.co.drnaylor.minecraft.quickstart.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.config.enumerations.ModuleOptions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainConfig extends AbstractConfig<CommentedConfigurationNode, HoconConfigurationLoader> {

    private final Map<PluginModule, ModuleOptions> moduleOptions = new HashMap<>();
    private final String modulesSection = "modules";

    public MainConfig(Path file) throws IOException {
        super(file);
    }

    @Override
    public void load() throws IOException {
        super.load();

        // Recreate anything we need to re-create.
        moduleOptions.clear();
        CommentedConfigurationNode mNode = node.getNode(modulesSection);
        Arrays.asList(PluginModule.values()).forEach(p -> {
            try {
                moduleOptions.put(p, mNode.getNode(p.getKey().toLowerCase()).getValue(TypeToken.of(ModuleOptions.class), ModuleOptions.DEFAULT));
            } catch (ObjectMappingException e) {
                mNode.getNode(p.getKey().toLowerCase()).setValue(ModuleOptions.DEFAULT.name().toLowerCase());
                moduleOptions.put(p, ModuleOptions.DEFAULT);
            }
        });
    }

    @Override
    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();

        // Load in the modules.
        CommentedConfigurationNode modules = ccn.getNode(modulesSection)
                .setComment("Sets the modules to either load normally (unless another plugin requests it should be disabled, forceload (always load regardless) and disabled (never load).");
        Arrays.asList(PluginModule.values()).forEach(m -> modules.getNode(m.getKey().toLowerCase()).setValue(ModuleOptions.DEFAULT.name().toLowerCase()));

        return ccn;
    }

    public Map<PluginModule, ModuleOptions> getModuleOptions() {
        return ImmutableMap.copyOf(moduleOptions);
    }
}
