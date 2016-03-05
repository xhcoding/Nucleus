/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerConsoleArgument extends CommandElement {

    public PlayerConsoleArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String name = args.next();
        if (name.equals("-")) {
            return Sponge.getServer().getConsole();
        }

        return Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst().orElseThrow(() ->
            args.createError(Text.builder().append(Text.of(PluginInfo.ERROR_MESSAGE_PREFIX)).append(Util.getTextMessageWithFormat("args.playerconsole.noexist")).build()));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        List<String> list = Sponge.getServer().getOnlinePlayers().stream().map(User::getName).collect(Collectors.toList());
        // Console.
        list.add("-");

        String a;
        try {
            a = args.peek();
        } catch (ArgumentParseException e) {
            return list;
        }

        return list.stream().filter(x -> x.toLowerCase().startsWith(a.toLowerCase())).collect(Collectors.toList());
    }
}
