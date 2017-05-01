/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"toggleredeemmessage", "togglemessage"}, subcommandOf = KitCommand.class)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@NonnullByDefault
public class KitRedeemMessageCommand extends AbstractCommand<CommandSource> {

    private KitHandler handler;

    @Inject
    public KitRedeemMessageCommand(KitHandler handler) {
        this.handler = handler;
    }

    private final String kit = "kit";
    private final String toggle = "displayMessageToggle";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.seq(GenericArguments.onlyOne(new KitArgument(Text.of(kit), true)),
                        GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggle))))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource player, CommandContext args) throws Exception {
        KitArgument.KitInfo kitInfo = args.<KitArgument.KitInfo>getOne(kit).get();
        boolean b = args.<Boolean>getOne(toggle).get();

        // This Kit is a reference back to the version in list, so we don't need
        // to update it explicitly
        kitInfo.kit.setDisplayMessageOnRedeem(b);
        handler.saveKit(kitInfo.name, kitInfo.kit);
        player.sendMessage(plugin.getMessageProvider()
                .getTextMessageWithFormat(b ? "command.kit.displaymessage.on" : "command.kit.displaymessage.off", kitInfo.name));

        return CommandResult.success();
    }
}
