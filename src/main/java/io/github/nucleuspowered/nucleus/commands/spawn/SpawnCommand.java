/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.spawn;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("spawn")
@Modules(PluginModule.SPAWN)
public class SpawnCommand extends CommandBase<Player> {

    private final String key = "world";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("otherworlds", new PermissionInformation(Util.getMessageWithFormat("permission.spawn.otherworlds"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.world(Text.of(key))),
                                permissions.getPermissionWithSuffix("otherworlds"))))
                .executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        WorldProperties wp = args.<WorldProperties>getOne(key).orElse(src.getWorld().getProperties());
        Optional<World> ow = Sponge.getServer().getWorld(wp.getUniqueId());

        if (!ow.isPresent()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.spawn.noworld"));
            return CommandResult.empty();
        }

        if (src.setLocationSafely(new Location<>(ow.get(), wp.getSpawnPosition()))) {
            src.sendMessage(Util.getTextMessageWithFormat("command.spawn.success"));
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.spawn.fail"));
        return CommandResult.empty();
    }
}
