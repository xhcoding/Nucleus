/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.commands.HomeOtherCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
public class HomeOtherArgument extends HomeArgument {

    private final NicknameArgument nickArg;
    private final String exemptperm;

    public HomeOtherArgument(@Nullable Text key, NucleusPlugin plugin, CoreConfigAdapter cca) {
        super(key, plugin, cca);
        nickArg = new NicknameArgument(key, plugin.getUserDataManager(), NicknameArgument.UnderlyingType.USER);
        this.exemptperm = plugin.getPermissionRegistry().getService(HomeOtherCommand.class).getPermissionWithSuffix(HomeOtherCommand.OTHER_EXEMPT_PERM_SUFFIX);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String player = args.next();
        Optional<String> ohome = args.nextIfPresent();

        if (!ohome.isPresent()) {
            throw args.createError(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("args.homeother.notenough"));
        }

        // We know it's an instance of a user.
        User user = ((List<User>)nickArg.parseInternal(player.toLowerCase(), source, args)).get(0);
        if (user.hasPermission(this.exemptperm)) {
            throw args.createError(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("args.homeother.exempt"));
        }

        return this.getHome(user, ohome.get(), args);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        Object saveState = null;
        try {
            saveState = args.getState();

            // Do we have two args?
            String arg1 = args.next();
            Optional<String> arg2 = args.nextIfPresent();
            if (arg2.isPresent()) {
                // Get the user
                User user = (User)this.nickArg.parseInternal(arg1, src, args);
                return this.complete(user, arg2.get());
            } else {
                args.setState(saveState);
                return nickArg.complete(src, args, context);
            }

        } catch (Exception e) {
            //
        } finally {
            if (saveState != null) {
                args.setState(saveState);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of("<user> <home>");
    }
}
