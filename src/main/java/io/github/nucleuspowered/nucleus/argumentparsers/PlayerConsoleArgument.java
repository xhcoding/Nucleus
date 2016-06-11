/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
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
        String name = args.next().toLowerCase();
        if (name.equals("-")) {
            return Sponge.getServer().getConsole();
        }

        List<Player> players = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.getName().toLowerCase().startsWith(name))
                .sorted((x, y) -> x.getName().compareTo(y.getName())).collect(Collectors.toList());
        if (players.isEmpty()) {
            throw args.createError(Util.getTextMessageWithFormat("args.playerconsole.noexist"));
        }

        return players.get(0);
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
