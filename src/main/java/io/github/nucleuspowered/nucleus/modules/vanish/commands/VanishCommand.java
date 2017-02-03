/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.commands;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.dataservices.UserService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;

@Permissions
@NoCooldown
@NoCost
@NoWarmup
@RegisterCommand({"vanish", "v"})
public class VanishCommand extends AbstractCommand<CommandSource> {

    @Inject
    private UserDataManager userDataManager;

    private final String b = "toggle";
    private final String playerKey = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                    GenericArguments.onlyOne(new NicknameArgument(Text.of(playerKey), userDataManager, NicknameArgument.UnderlyingType.USER)),
                    permissions.getPermissionWithSuffix("other"))),
            GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(b))))
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = Maps.newHashMap();
        mspi.put("see", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.vanish.see"), SuggestedLevel.ADMIN));
        mspi.put("other", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.vanish.other"), SuggestedLevel.ADMIN));
        mspi.put("persist", new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.vanish.persist"), SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User playerToVanish = this.getUserFromArgs(User.class, src, playerKey, args);
        if (playerToVanish.getPlayer().isPresent()) {
            return onPlayer(src, args, playerToVanish.getPlayer().get());
        }

        if (!permissions.testSuffix(playerToVanish, "persist")) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.noperm", playerToVanish.getName()));
        }

        UserService uss = userDataManager.get(playerToVanish).get();
        uss.setVanished(args.<Boolean>getOne(b).orElse(!uss.isVanished()));

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.successuser",
            playerToVanish.getName(),
            uss.isVanished() ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") : plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));

        return CommandResult.success();
    }

    private CommandResult onPlayer(CommandSource src, CommandContext args, Player playerToVanish) throws Exception {
        // If we don't specify whether to vanish, toggle
        boolean toVanish = args.<Boolean>getOne(b).orElse(!playerToVanish.get(Keys.VANISH).orElse(false));

        userDataManager.get(playerToVanish).get().setVanished(toVanish);
        if (!playerToVanish.get(Keys.GAME_MODE).orElse(GameModes.NOT_SET).equals(GameModes.SPECTATOR)) {
            DataTransactionResult dtr = playerToVanish.offer(Keys.VANISH, toVanish);
            playerToVanish.offer(Keys.VANISH_PREVENTS_TARGETING, toVanish);
            playerToVanish.offer(Keys.VANISH_IGNORES_COLLISION, toVanish);
            playerToVanish.offer(Keys.IS_SILENT, toVanish);

            if (!dtr.isSuccessful()) {
                throw ReturnMessageException.fromKey("command.vanish.fail");
            }
        }

        playerToVanish.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.success",
            toVanish ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") :
                plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));

        if (!(src instanceof Player) || !(((Player) src).getUniqueId().equals(playerToVanish.getUniqueId()))) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.successplayer",
                TextSerializers.FORMATTING_CODE.serialize(plugin.getNameUtil().getName(playerToVanish)),
                toVanish ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") :
                    plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));
        }

        return CommandResult.success();
    }
}
