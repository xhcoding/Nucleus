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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.enums.SuggestedLevel;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions
@Modules(PluginModule.MISC)
@NoCooldown
@NoWarmup
@NoCost
@RootCommand
public class GodCommand extends CommandBase {
    private final String playerKey = "player";
    private final String invulnKey = "invuln";

    @Override
    public Map<String, SuggestedLevel> permissionSuffixesToRegister() {
        Map<String, SuggestedLevel> m = new HashMap<>();
        m.put("others", SuggestedLevel.ADMIN);
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
                GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of(playerKey))), permissions.getPermissionWithSuffix("others")),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(invulnKey))))
        ).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "god", "invuln", "invulnerability" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> opl = args.<Player>getOne(playerKey);
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

        InternalQuickStartUser uc = plugin.getUserLoader().getUser(pl);
        boolean god = args.<Boolean>getOne(invulnKey).orElse(!uc.isInvulnerable());

        if (!uc.setInvulnerable(god)) {
            src.sendMessages(Text.of(TextColors.RED, Util.getMessageWithFormat("command.god.error")));
            return CommandResult.empty();
        }

        if (pl != src) {
            src.sendMessages(Text.of(TextColors.GREEN, MessageFormat.format(Util.getMessageWithFormat(god ? "command.god.player.on" : "command.god.player.off"), pl.getName())));
        }

        pl.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat(god ? "command.god.on" : "command.god.off")));
        return CommandResult.success();
    }
}
