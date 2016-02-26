/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.spawn;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionService;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions(suggestedLevel = PermissionService.SuggestedLevel.USER)
@RootCommand
@Modules(PluginModule.SPAWN)
public class SpawnCommand extends CommandBase<Player> {
    private final String key = "world";

    @Override
    public Map<String, PermissionService.SuggestedLevel> permissionSuffixesToRegister() {
        Map<String, PermissionService.SuggestedLevel> m = new HashMap<>();
        m.put("otherworlds", PermissionService.SuggestedLevel.ADMIN);
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.world(Text.of(key))), permissions.getPermissionWithSuffix("otherworlds"))))
                .executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "spawn" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        WorldProperties wp = args.<WorldProperties>getOne(key).orElse(src.getWorld().getProperties());
        Optional<World> ow = Sponge.getServer().getWorld(wp.getUniqueId());

        if (!ow.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.spawn.noworld")));
            return CommandResult.empty();
        }

        if (src.setLocationSafely(new Location<>(ow.get(), wp.getSpawnPosition()))) {
            src.sendMessage(Text.of(TextColors.GREEN, Util.getMessageWithFormat("command.spawn.success")));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.spawn.fail")));
        return CommandResult.empty();
    }
}
