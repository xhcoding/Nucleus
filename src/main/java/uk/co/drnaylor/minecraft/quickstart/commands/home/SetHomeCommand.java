package uk.co.drnaylor.minecraft.quickstart.commands.home;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Permissions(root = "home", alias = "set")
@Modules(PluginModule.HOMES)
@RunAsync
public class SetHomeCommand extends CommandBase<Player> {

    private final String homeKey = "home";
    private final Pattern warpName = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{1,15}$");

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(
                GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.string(Text.of(homeKey))))
        ).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "homeset", "sethome" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home key.
        String home = args.<String>getOne(homeKey).orElse("home").toLowerCase();

        // Get the homes.
        InternalQuickStartUser iqsu = plugin.getUserLoader().getUser(src);
        Map<String, WarpLocation> msw = iqsu.getHomes();

        if (!warpName.matcher(home).matches()) {
            src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.sethome.name")));
            return CommandResult.empty();
        }

        int c = getCount(src);
        if (msw.size() >= c) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.sethome.limit", String.valueOf(c))));
            return CommandResult.empty();
        }

        // Does the home exist?
        if (msw.containsKey(home) || !iqsu.setHome(home, src.getLocation(), src.getRotation())) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.sethome.seterror", home)));
            return CommandResult.empty();
        }

        src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.sethome.set", home)));
        return CommandResult.success();
    }

    private int getCount(Player src) {
        Set<String> s = permissions.getPermissionWithSuffix("unlimited");
        if (s.stream().anyMatch(src::hasPermission)) {
            return Integer.MAX_VALUE;
        }

        // If we have too many, then
        String homesAllowed = ((OptionSubject) src).getOption("home-count").orElse("1");
        int i = Integer.getInteger(homesAllowed, 1);
        if (i < 1) {
            i = 1;
        }

        return i;
    }
}
