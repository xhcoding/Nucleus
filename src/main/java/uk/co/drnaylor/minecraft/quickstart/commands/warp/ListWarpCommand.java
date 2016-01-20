package uk.co.drnaylor.minecraft.quickstart.commands.warp;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lists all the warps that a player can access.
 *
 * Command Usage: /warp list
 * Permission: quickstart.warp.list.base
 */
@Permissions(root = "warp")
@RunAsync
public class ListWarpCommand extends CommandBase {
    private final QuickStartWarpService service = Sponge.getServiceManager().provideUnchecked(QuickStartWarpService.class);
    @Inject private CommandsConfig commandsConfig;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this)
                .description(Text.of("Lists the warp available to you.")).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "list" };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws CommandException {
        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);

        // Get the warp list.
        Set<String> ws = service.getWarpNames();
        List<Text> lt = ws.stream()
                .filter(s -> canView(src, s.toLowerCase()))
                .map(s -> Text.builder(s).color(TextColors.GREEN)
                    .style(TextStyles.UNDERLINE).onClick(TextActions.runCommand("warp " + s))
                    .onHover(TextActions.showText(Text.of(TextColors.YELLOW, "Click to warp to " + s))).build())
                .collect(Collectors.toList());

        ps.builder().title(Text.of(TextColors.YELLOW, Util.messageBundle.getString("command.warps.list.header")))
                .paddingString("-").contents(lt).sendTo(src);
        return CommandResult.success();
    }

    private boolean canView(CommandSource src, String warp) {
        return !commandsConfig.getCommandNode("warp").getNode(WarpsCommand.seperatePermissionsConfigEntryName).getBoolean(false)
                || src.hasPermission(QuickStart.PERMISSIONS_ADMIN)
                || src.hasPermission(QuickStart.PERMISSIONS_PREFIX + "warp." + warp);
    }
}
