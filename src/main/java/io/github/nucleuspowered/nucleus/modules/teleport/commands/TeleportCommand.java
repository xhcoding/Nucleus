/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoModifiersArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.TwoPlayersArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.command.ContinueMode;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Permissions(prefix = "teleport", mainOverride = "teleport", suggestedLevel = SuggestedLevel.MOD, supportsOthers = true)
@RegisterCommand(value = "teleport", rootAliasRegister = "tp")
@EssentialsEquivalent(value = {"tp", "tele", "tp2p", "teleport", "tpo"}, isExact = false,
        notes = "If you have permission, this will override '/tptoggle' automatically.")
public class TeleportCommand extends AbstractCommand<CommandSource> {

    private final String playerFromKey = "playerFrom";
    private final String playerKey = "subject";
    private final String quietKey = "quiet";

    @Inject private TeleportHandler handler;
    @Inject private TeleportConfigAdapter tca;
    @Inject private UserDataManager userDataManager;

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("offline", PermissionInformation.getWithTranslation("permission.teleport.offline", SuggestedLevel.ADMIN));
        m.put("quiet", PermissionInformation.getWithTranslation("permission.teleport.quiet", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
       return new CommandElement[]{
                GenericArguments.flags().flag("f")
                    .valueFlag(GenericArguments.requiringPermission(GenericArguments.bool(Text.of(quietKey)), permissions.getPermissionWithSuffix("quiet")), "q")
                    .buildWith(GenericArguments.none()),

                    // Either we get two arguments, or we get one.
                    GenericArguments.firstParsing(
                        // <subject> <subject>
                        GenericArguments.requiringPermission(new NoModifiersArgument<Player>(
                            new TwoPlayersArgument(Text.of(playerFromKey), Text.of(playerKey), permissions), (c, o) -> true),
                                permissions.getOthers()),

                    // <subject>
                    GenericArguments.onlyOne(SelectorWrapperArgument.nicknameSelector(Text.of(playerKey), NicknameArgument.UnderlyingType.USER)))
       };
    }

    @Override protected ContinueMode preProcessChecks(SubjectPermissionCache<CommandSource> source, CommandContext args) {
        return TeleportHandler.canTeleportTo(source, args.<User>getOne(playerKey).get()) ? ContinueMode.CONTINUE : ContinueMode.STOP;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean beQuiet = args.<Boolean>getOne(quietKey).orElse(tca.getNodeOrDefault().isDefaultQuiet());
        Optional<Player> ofrom = args.getOne(playerFromKey);
        Player from;
        if (ofrom.isPresent()) {
            from = ofrom.get();
            if (from.equals(src)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.teleport.player.noself"));
                return CommandResult.empty();
            }
        } else if (src instanceof Player) {
            from = (Player) src;
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.playeronly"));
            return CommandResult.empty();
        }

        User pl = args.<Player>getOne(playerKey).get();
        if (pl.getPlayer().isPresent()) {
            if (handler.getBuilder().setSource(src).setFrom(from).setTo(pl.getPlayer().get()).setSafe(!args.<Boolean>getOne("f").orElse(false))
                    .setSilentTarget(beQuiet).startTeleport()) {
                return CommandResult.success();
            }

            return CommandResult.empty();
        }

        // We have an offline player.
        permissions.checkSuffix(src, "offline", () -> ReturnMessageException.fromKey("command.teleport.noofflineperms"));

        // Can we get a location?
        Supplier<ReturnMessageException> r = () -> ReturnMessageException.fromKey("command.teleport.nolastknown", pl.getName());
        Location<World> l = plugin.getUserDataManager().get(pl.getUniqueId()).orElseThrow(r).get(CoreUserDataModule.class).getLogoutLocation()
                .orElseThrow(r);

        MessageProvider provider = plugin.getMessageProvider();
        if (plugin.getTeleportHandler()
                .teleportPlayer(from, l, NucleusTeleportHandler.TeleportMode.FLYING_THEN_SAFE, Cause.of(NamedCause.owner(src))).isSuccess()) {
            if (!(src instanceof Player && ((Player) src).getUniqueId().equals(from.getUniqueId()))) {
                src.sendMessage(provider.getTextMessageWithFormat("command.teleport.offline.other", from.getName(), pl.getName()));
            }

            from.sendMessage(provider.getTextMessageWithFormat("command.teleport.offline.self", pl.getName()));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.teleport.error");
    }
}
