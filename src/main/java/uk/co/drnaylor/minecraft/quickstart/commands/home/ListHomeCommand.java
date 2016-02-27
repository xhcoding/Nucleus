/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.home;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.PermissionInformation;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.SuggestedLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Permissions(root = "home", alias = "list", suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.HOMES)
@RunAsync
@NoCooldown
@NoWarmup
@NoCost
@RegisterCommand
public class ListHomeCommand extends CommandBase<CommandSource> {
    private final String player = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.others"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.requiringPermission(new UserParser(Text.of(player)), permissions.getPermissionWithSuffix("others")))))
                .executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "listhomes", "homes" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<User> ou = args.<User>getOne(player);
        String header;
        User user;
        boolean other = ou.isPresent();
        if (other) {
            header = Util.getMessageWithFormat("home.title.name", ou.get().getName());
            user = ou.get();
        } else {
            if (!(src instanceof Player)) {
                src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.listhome.player")));
                return CommandResult.empty();
            }

            user = (User)src;
            header = Util.getMessageWithFormat("home.title");
        }

        Map<String, WarpLocation> msw = plugin.getUserLoader().getUser(user).getHomes();
        List<Text> lt = msw.entrySet().stream().sorted((x, y) -> x.getKey().compareTo(y.getKey())).map(x -> {
            Location<World> lw = x.getValue().getLocation();
            return Text.builder().append(
                    Text.builder(x.getKey()).color(TextColors.GREEN).style(TextStyles.UNDERLINE).onHover(TextActions.showText(
                            Text.of(Util.getMessageWithFormat("home.warphover", x.getKey()))
                    )).onClick(TextActions.runCommand(other ? "/homeother " + user.getName() + " " + x.getValue().getName() : "/home " + x.getValue().getName())).build()
            ).append(Text.of(TextColors.YELLOW,
                    Util.getMessageWithFormat("home.location", lw.getExtent().getName(), String.valueOf(lw.getBlockX()), String.valueOf(lw.getBlockY()), String.valueOf(lw.getBlockZ())))).build();
        }).collect(Collectors.toList());

        PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        ps.builder().title(Text.of(TextColors.YELLOW, header)).paddingString("-").contents(lt).sendTo(src);
        return CommandResult.success();
    }
}
