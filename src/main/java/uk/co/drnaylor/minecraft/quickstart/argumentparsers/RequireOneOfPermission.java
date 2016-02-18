package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

public class RequireOneOfPermission extends CommandElement {
    private final CommandElement wrapped;
    private final Set<String> validPermissions;
    private final boolean isWeak;

    public RequireOneOfPermission(CommandElement element, Set<String> validPermissions) {
        this(element, validPermissions, true);
    }

    public RequireOneOfPermission(CommandElement element, Set<String> validPermissions, boolean isWeak) {
        super(element.getKey());
        this.wrapped = element;
        this.validPermissions = validPermissions;
        this.isWeak = isWeak;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (!checkPermission(src)) {
            return ImmutableList.of();
        }

        return this.wrapped.complete(src, args, context);
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (!checkPermission(source)) {
            if (isWeak) {
                return;
            } else {
                throw args.createError(Text.of(TextColors.RED, MessageFormat.format(Util.getMessageWithFormat("args.permission.deny"), getKey())));
            }
        }

        this.wrapped.parse(source, args, context);
    }

    @Override
    public Text getUsage(CommandSource src) {
        return checkPermission(src) ? wrapped.getUsage(src) : Text.of();
    }

    private boolean checkPermission(CommandSource source) {
        return validPermissions.stream().anyMatch(source::hasPermission);
    }
}
