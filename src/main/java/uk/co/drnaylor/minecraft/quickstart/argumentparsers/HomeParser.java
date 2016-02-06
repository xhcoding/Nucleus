package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeParser extends CommandElement {
    private final QuickStart plugin;

    public HomeParser(@Nullable Text key, QuickStart plugin) {
        super(key);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (!(source instanceof User)) {
            throw args.createError(Text.of(TextColors.RED, Util.messageBundle.getString("command.playeronly")));
        }

        return getHome((User)source, args.next(), args);
    }

    protected WarpLocation getHome(User user, String home, CommandArgs args) throws ArgumentParseException {
        try {
            Optional<WarpLocation> owl = plugin.getUserLoader().getUser(user).getHome(home.toLowerCase());
            if (owl.isPresent()) {
                return owl.get();
            }

            throw args.createError(Text.of(TextColors.RED, Util.getMessageWithFormat("args.home.nohome", home.toLowerCase())));
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            throw args.createError(Text.of(TextColors.RED, "An unspecified error occured"));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (!(src instanceof User)) {
            return null;
        }

        User u = (User)src;
        Set<String> s;
        try {
            s = plugin.getUserLoader().getUser(u).getHomes().keySet();
        } catch (IOException | ObjectMappingException e) {
            return null;
        }

        try {
            String n = args.peek();
            return s.stream().filter(x -> n.toLowerCase().startsWith(x.toLowerCase())).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList(s);
        }
    }
}
