/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.misc;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RegisterCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.PermissionInformation;
import uk.co.drnaylor.minecraft.quickstart.internal.permissions.SuggestedLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions
@Modules(PluginModule.MISC)
@RegisterCommand
public class FeedCommand extends CommandBase {
    private static final String player = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", new PermissionInformation(Util.getMessageWithFormat("permission.others", this.getAliases()[0]), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.requiringPermission(
                        GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.player(Text.of(player)))), permissions.getPermissionWithSuffix("others")
                )
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "feed" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = args.<Player>getOne(player);
        Player pl;
        if (opl.isPresent()) {
            pl = opl.get();
        } else {
            if (src instanceof Player) {
                pl = (Player)src;
            } else {
                src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.playeronly")));
                return CommandResult.empty();
            }
        }

        // TODO: If max food level appears, use that instead.
        if (pl.offer(Keys.FOOD_LEVEL, 20).isSuccessful()) {
            pl.sendMessages(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.feed.success")));
            if (!pl.equals(src)) {
                src.sendMessages(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.feed.success.other", pl.getName())));
            }

            return CommandResult.success();
        } else {
            src.sendMessages(Text.of(TextColors.RED, Util.getMessageWithFormat("command.feed.error")));
            return CommandResult.empty();
        }
    }
}
