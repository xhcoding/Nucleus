package uk.co.drnaylor.minecraft.quickstart.commands.mail;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.*;
import uk.co.drnaylor.minecraft.quickstart.internal.services.MailHandler;

/**
 * Permission is "quickstart.mail.base", because a player should always be able to clear mail if they can read it.
 */
@Permissions(alias = "mail", includeUser = true)
@NoWarmup
@NoCooldown
@NoCost
@RunAsync
public class ClearMailCommand extends CommandBase<Player> {
    @Inject
    private MailHandler handler;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "clear" };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        handler.clearUserMail(src);
        src.sendMessage(Text.of(TextColors.GREEN, Util.messageBundle.getString("command.mail.clear")));
        return CommandResult.success();
    }
}
