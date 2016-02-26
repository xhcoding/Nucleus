/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.core;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartModuleService;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;

/**
 * Gives information about QuickStart.
 *
 * Command Usage: /quickstart
 * Permission: quickstart.quickstart.base
 */
@RunAsync
@Permissions
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class QuickStartCommand extends CommandBase {

    private final Text version = Text.of(QuickStart.MESSAGE_PREFIX, TextColors.GREEN, QuickStart.NAME + " version " + QuickStart.VERSION);
    private Text modules = null;

    @Override
    @SuppressWarnings("unchecked")
    public CommandSpec createSpec() {
        return CommandSpec.builder().children(this.createChildCommands(ReloadCommand.class, ResetUser.class, SuggestedPermissionsCommand.class)).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "quickstart" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        if (modules == null) {
            QuickStartModuleService qs = Sponge.getServiceManager().provideUnchecked(QuickStartModuleService.class);

            Text.Builder tb = Text.builder("Modules: ").color(TextColors.GREEN);

            boolean addComma = false;
            for (PluginModule x : PluginModule.values()) {
                if (addComma) {
                    tb.append(Text.of(TextColors.GREEN, ", "));
                }

                tb.append(Text.of(qs.getModulesToLoad().contains(x) ? TextColors.GREEN : TextColors.RED, x.getKey()));
                addComma = true;
            }

            modules = tb.append(Text.of(TextColors.GREEN, ".")).build();
        }

        src.sendMessage(version);
        src.sendMessage(modules);
        return CommandResult.success();
    }
}
