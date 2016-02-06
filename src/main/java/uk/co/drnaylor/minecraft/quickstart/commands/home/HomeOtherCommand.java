package uk.co.drnaylor.minecraft.quickstart.commands.home;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.HomeOtherParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

@Permissions(root = "home", alias = "other")
public class HomeOtherCommand extends CommandBase<Player> {
    private final String home = "home";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .arguments(GenericArguments.onlyOne(new HomeOtherParser(Text.of(home), plugin)))
                .build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "homeother" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        HomeOtherParser.HomeData wl = args.<HomeOtherParser.HomeData>getOne(home).get();

        // Warp to it safely.
        if (src.setLocationAndRotationSafely(wl.location.getLocation(), wl.location.getRotation())) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.homeother.success", wl.user.getName(), wl.location.getName())));
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.homeother.fail", wl.user.getName(), wl.location.getName())));
            return CommandResult.empty();
        }
    }
}
