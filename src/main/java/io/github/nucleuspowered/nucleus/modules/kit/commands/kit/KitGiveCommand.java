/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

/**
 * Gives a kit to a subject.
 */
@Permissions(prefix = "kit")
@RegisterCommand(value = "give", subcommandOf = KitCommand.class)
@NonnullByDefault
public class KitGiveCommand extends AbstractCommand<CommandSource> {

    private final KitHandler handler;
    private final KitConfigAdapter kitConfigAdapter;
    private final UserDataManager userDataManager;

    @Inject
    public KitGiveCommand(KitHandler handler, KitConfigAdapter kitConfigAdapter,
            UserDataManager userDataManager) {
        this.handler = handler;
        this.kitConfigAdapter = kitConfigAdapter;
        this.userDataManager = userDataManager;
    }

    private final String playerKey = "subject";
    private final String kitKey = "kit";

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = Maps.newHashMap();
        mspi.put("overridecheck", PermissionInformation.getWithTranslation("permission.kit.give.override", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().permissionFlag(permissions.getPermissionWithSuffix("overridecheck"), "i", "-ignore").buildWith(
                GenericArguments.none()
            ),
            GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey))),
            GenericArguments.onlyOne(new KitArgument(Text.of(kitKey), false))
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        KitArgument.KitInfo kitInfo = args.<KitArgument.KitInfo>getOne(kitKey).get();
        Player player = args.<Player>getOne(playerKey).get();
        boolean skip = args.hasAny("i");

        if (src instanceof Player && player.getUniqueId().equals(((Player) src).getUniqueId())) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.give.self"));
        }

        handler.redeemKit(kitInfo.kit, kitInfo.name, player, src, !skip);
        return CommandResult.success();
    }
}
