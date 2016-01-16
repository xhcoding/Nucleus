package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import com.google.common.collect.Lists;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns a {@link WarpData}
 */
public class WarpParser extends CommandElement {
    private QuickStartWarpService service;
    private final QuickStart plugin;
    private final boolean permissionCheck;
    private final String prefix = QuickStart.PERMISSIONS_PREFIX + "warps.";

    public WarpParser(@Nullable Text key, QuickStart plugin, boolean permissionCheck) {
        super(key);
        this.plugin = plugin;
        this.permissionCheck = permissionCheck;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        getService();

        String warpName = args.next();
        String warp = warpName.toLowerCase();
        if (!service.warpExists(warp)) {
            throw args.createError(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, TextColors.RED, Util.messageBundle.getString("args.warps.noexist")));
        }

        if (!checkPermission(source, warpName)) {
            throw args.createError(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, TextColors.RED, Util.messageBundle.getString("args.warps.noperms")));
        }

        return new WarpData(warpName, service.getWarp(warp).get());
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        getService();

        try {
            String name = args.peek().toLowerCase();
            return service.getWarpNames().stream().filter(s -> s.startsWith(name)).filter(x -> checkPermission(src, name)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, String name) {
        if (!permissionCheck || !plugin.getConfig(CommandsConfig.class).get().getCommandNode("warp").getNode("separate-permissions").getBoolean(false)) {
            return true;
        }

        String permission = prefix + name.toLowerCase();

        // No permissions, no entry!
        return (src.hasPermission(permission) || src.hasPermission(QuickStart.PERMISSIONS_ADMIN));
    }

    private void getService() {
        if (service == null) {
            service = Sponge.getServiceManager().provideUnchecked(QuickStartWarpService.class);
        }
    }

    public class WarpData {
        public final String warp;
        public final WarpLocation loc;

        private WarpData(String warp, WarpLocation loc) {
            this.warp = warp;
            this.loc = loc;
        }
    }
}
