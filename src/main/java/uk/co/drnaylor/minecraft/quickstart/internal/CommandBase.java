package uk.co.drnaylor.minecraft.quickstart.internal;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;

public abstract class CommandBase implements CommandExecutor {

    protected final QuickStart plugin;

    protected CommandBase(QuickStart plugin) {
        this.plugin = plugin;
    }

    public abstract CommandSpec getSpec();

    public abstract String[] getAliases();

    /**
     * Runs the command. If the command is marked as {@link RunAsync}, runs it using a {@link org.spongepowered.api.scheduler.AsynchronousExecutor}
     *
     * @param src The source of the command.
     * @param args The arguments sent to the command.
     * @return The {@link CommandResult}
     * @throws CommandException Generally not thrown.
     */
    @Override
    @NonnullByDefault
    public final CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (this.getClass().getAnnotation(RunAsync.class) != null) {
            plugin.getLogger().debug("Running " + this.getClass().getName() + " in async mode.");
            Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
                try {
                    executeCommand(src, args);
                } catch (CommandException e) {
                    src.sendMessage(Text.of(TextColors.RED, "[" + QuickStart.NAME + "] ", e.getText()));
                }
            });
            return CommandResult.success();
        } else {
            try {
                return executeCommand(src, args);
            } catch (CommandException e) {
                src.sendMessage(Text.of(TextColors.RED, "[" + QuickStart.NAME + "] ", e.getText()));
                return CommandResult.empty();
            }
        }
    }

    /**
     * Replacement for {@link #execute(CommandSource, CommandContext)} in the {@link CommandExecutor}
     *
     * @param src The source of the command.
     * @param args The arguments sent to the command.
     * @return The {@link CommandResult}
     * @throws CommandException Should be thrown for an error condition.
     */
    public abstract CommandResult executeCommand(CommandSource src, CommandContext args) throws CommandException;
}
