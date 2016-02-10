package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;

public class RequireMoreArguments extends CommandElement {
    private final CommandElement element;
    private final int number;

    // Number is the number of arguments that should exist before this is executed.
    public RequireMoreArguments(CommandElement element, int number) {
        super(element.getKey());
        this.element = element;
        this.number = number;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (args.getAll().size() >= number) {
            element.parse(source, args, context);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return element.complete(src, args, context);
    }

    @Override
    public Text getUsage(CommandSource src) {
        return element.getUsage(src);
    }
}
