/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.message;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

@Permissions(includeMod = true)
@Modules(PluginModule.MESSAGES)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RootCommand
public class SocialSpyCommand extends CommandBase<Player> {
    private final String arg = "Social Spy";
    @Inject private UserConfigLoader userConfigLoader;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Sets whether the player can spy on messages."))
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(arg)))))
                .executor(this)
                .build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "socialspy" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        QuickStartUser qs = userConfigLoader.getUser(src);
        boolean spy = args.<Boolean>getOne(arg).orElse(!qs.isSocialSpy());
        if (qs.setSocialSpy(spy)) {
            String message = Util.getMessageWithFormat(spy ? "command.socialspy.on" : "command.socialspy.off");
            src.sendMessage(Text.of(TextColors.GREEN, message));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.socialspy.unable")));
        return CommandResult.empty();
    }
}
