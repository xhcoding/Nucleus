package uk.co.drnaylor.minecraft.quickstart.commands.message;

import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.io.IOException;

@Permissions
@Modules(PluginModule.MESSAGES)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
public class SocialSpyCommand extends CommandBase<Player> {
    private final String arg = "Social Spy";
    @Inject private UserConfigLoader userConfigLoader;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().description(Text.of("Sets whether the player can spy on messages."))
                .arguments(GenericArguments.onlyOne(GenericArguments.bool(Text.of(arg))))
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
        boolean spy = args.<Boolean>getOne(arg).get();
        if (qs.setSocialSpy(spy)) {
            String message = Util.messageBundle.getString(spy ? "command.socialspy.on" : "command.socialspy.off");
            src.sendMessage(Text.of(TextColors.GREEN, message));
            return CommandResult.success();
        }

        src.sendMessage(Text.of(TextColors.RED, Util.messageBundle.getString("command.socialspy.unable")));
        return CommandResult.empty();
    }
}
