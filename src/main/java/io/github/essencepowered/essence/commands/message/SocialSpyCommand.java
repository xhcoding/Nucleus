/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.message;

import com.google.inject.Inject;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.api.data.EssenceUser;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.NoCooldown;
import io.github.essencepowered.essence.internal.annotations.NoCost;
import io.github.essencepowered.essence.internal.annotations.NoWarmup;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.annotations.RunAsync;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.datastore.UserConfigLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@Modules(PluginModule.MESSAGES)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand("socialspy")
public class SocialSpyCommand extends CommandBase<Player> {

    private final String arg = "Social Spy";
    @Inject private UserConfigLoader userConfigLoader;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Sets whether the player can spy on messages."))
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(arg))))).executor(this).build();
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        EssenceUser qs = userConfigLoader.getUser(src);
        boolean spy = args.<Boolean>getOne(arg).orElse(!qs.isSocialSpy());
        if (qs.setSocialSpy(spy)) {
            Text message = Util.getTextMessageWithFormat(spy ? "command.socialspy.on" : "command.socialspy.off");
            src.sendMessage(message);
            return CommandResult.success();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.socialspy.unable"));
        return CommandResult.empty();
    }
}
