package uk.co.drnaylor.minecraft.quickstart.internal;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;

import java.util.Set;

public abstract class CommandBase implements CommandExecutor {

    private final boolean isAsync = this.getClass().getAnnotation(RunAsync.class) != null;
    private final Set<String> additionalPermissions;

    @Inject protected QuickStart plugin;

    protected CommandBase() {
        // Additional permissions
        Permissions op = this.getClass().getAnnotation(Permissions.class);
        if (op != null) {
            additionalPermissions = Sets.newHashSet(op.value());
            if (op.includeAdmin()) {
                additionalPermissions.add(QuickStart.PERMISSIONS_ADMIN);
            }
        } else {
            additionalPermissions = Sets.newHashSet();
        }
    }

    public abstract CommandSpec createSpec();

    public abstract String[] getAliases();

    public abstract CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException;

    @Override
    @NonnullByDefault
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // If they don't match ANY permission, throw 'em.
        if (!additionalPermissions.isEmpty() && !additionalPermissions.stream().anyMatch(src::hasPermission)) {
            throw new CommandPermissionException();
        }

        if (isAsync) {
            plugin.getLogger().debug("Running " + this.getClass().getName() + " in async mode.");
            Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
                try {
                    executeCommand(src, args);
                } catch (CommandException e) {
                    src.sendMessage(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, e.getText()));
                }
            });

            return CommandResult.success();
        }

        try {
            return executeCommand(src, args);
        } catch (CommandException e) {
            src.sendMessage(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, e.getText()));
            return CommandResult.empty();
        }
    }
}