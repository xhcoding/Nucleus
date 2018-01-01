/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import io.github.nucleuspowered.nucleus.modules.vanish.service.VanishService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

@Permissions(supportsOthers = true)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"vanish", "v"})
@EssentialsEquivalent({"vanish", "v"})
public class VanishCommand extends AbstractCommand<CommandSource> {

    private final String player = "player";
    private final String b = "toggle";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optionalWeak(GenericArguments.requiringPermission(GenericArguments.user(Text.of(player)), permissions.getOthers())),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(b))))
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = Maps.newHashMap();
        mspi.put("see", PermissionInformation.getWithTranslation("permission.vanish.see", SuggestedLevel.ADMIN));
        mspi.put("persist", PermissionInformation.getWithTranslation("permission.vanish.persist", SuggestedLevel.ADMIN));
        mspi.put("onlogin", PermissionInformation.getWithTranslation("permission.vanish.onlogin", SuggestedLevel.NONE));
        return mspi;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User ou = getUserFromArgs(User.class, src, player, args);
        if (ou.getPlayer().isPresent()) {
            return onPlayer(src, args, ou.getPlayer().get());
        }

        if (!permissions.testSuffix(ou, "persist")) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.noperm", ou.getName()));
        }

        VanishUserDataModule uss = Nucleus.getNucleus().getUserDataManager().getUnchecked(ou).get(VanishUserDataModule.class);
        uss.setVanished(args.<Boolean>getOne(b).orElse(!uss.isVanished()));

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.successuser",
            ou.getName(),
            uss.isVanished() ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") : plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));

        return CommandResult.success();
    }

    private CommandResult onPlayer(CommandSource src, CommandContext args, Player playerToVanish) throws Exception {
        if (playerToVanish.get(Keys.GAME_MODE).orElse(GameModes.NOT_SET).equals(GameModes.SPECTATOR)) {
            throw ReturnMessageException.fromKey("command.vanish.fail");
        }

        // If we don't specify whether to vanish, toggle
        boolean toVanish = args.<Boolean>getOne(b).orElse(!playerToVanish.get(Keys.VANISH).orElse(false));
        if (toVanish) {
            getServiceUnchecked(VanishService.class).vanishPlayer(playerToVanish);
        } else {
            getServiceUnchecked(VanishService.class).unvanishPlayer(playerToVanish);
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
