package uk.co.drnaylor.minecraft.quickstart.commands.home;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.HomeParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

import java.util.Optional;

@Permissions(includeUser = true)
@Modules(PluginModule.HOMES)
public class HomeCommand extends CommandBase<Player> {
    private final String home = "home";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.onlyOne(GenericArguments.optional(new HomeParser(Text.of(home), plugin))))
                .build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "home" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Optional<WarpLocation> owl = args.<WarpLocation>getOne(home);
        if (!owl.isPresent()) {
            owl = plugin.getUserLoader().getUser(src).getHome("home");

            if (!owl.isPresent()) {
                src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("args.home.nohome", "home")));
                return CommandResult.empty();
            }
        }

        WarpLocation wl = owl.get();

        // Warp to it safely.
        if (src.setLocationAndRotationSafely(wl.getLocation(), wl.getRotation())) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.home.success", wl.getName())));
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.home.fail", wl.getName())));
            return CommandResult.empty();
        }
    }
}
