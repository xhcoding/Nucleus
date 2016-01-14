package uk.co.drnaylor.minecraft.quickstart.commands.core;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
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
import uk.co.drnaylor.minecraft.quickstart.internal.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.RunAsync;

@RunAsync
@Permissions(QuickStart.PERMISSIONS_PREFIX + "quickstart.base")
public class QuickStartCommand extends CommandBase {

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "quickstart" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException {
        QuickStartModuleService qs = Sponge.getServiceManager().provideUnchecked(QuickStartModuleService.class);

        StringBuilder sb = new StringBuilder();
        qs.getModulesToLoad().stream().map(PluginModule::getKey).forEach(s -> {
            if (sb.length() == 0) {
                sb.append(", ");
            }

            sb.append(s);
        });

        sb.insert(0, "Modules loaded: ").append(".");
        src.sendMessage(Text.of(QuickStart.MESSAGE_PREFIX, TextColors.GREEN, QuickStart.NAME + " version " + QuickStart.VERSION));
        src.sendMessage(Text.of(TextColors.GREEN, sb.toString()));
        return CommandResult.success();
    }
}
