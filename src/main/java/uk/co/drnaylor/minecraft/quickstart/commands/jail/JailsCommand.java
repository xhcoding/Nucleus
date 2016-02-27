/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.jail;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.SuggestedLevel;
import uk.co.drnaylor.minecraft.quickstart.internal.services.JailHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Modules(PluginModule.JAILS)
@NoCooldown
@NoCost
@NoWarmup
@RunAsync
@RegisterCommand
@Permissions(root = "jail", alias = "list", suggestedLevel = SuggestedLevel.MOD)
public class JailsCommand extends CommandBase {
    @Inject
    private JailHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).children(this.createChildCommands(SetJailCommand.class, DeleteJailCommand.class, JailInfoCommand.class)).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "jails" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);

        Map<String, WarpLocation> mjs = handler.getJails();
        List<Text> lt = mjs.entrySet().stream().map(x -> Text.builder(x.getKey().toLowerCase()).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                .onClick(TextActions.runCommand("/jails info " + x.getKey().toLowerCase()))
                .onHover(TextActions.showText(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.jails.jailprompt", x.getKey().toLowerCase())))).build()).collect(Collectors.toList());

        ps.builder().title(Text.of(TextColors.YELLOW, Util.getMessageWithFormat("command.jails.list.header")))
                .paddingString("-").contents(lt).sendTo(src);
        return CommandResult.success();
    }
}
