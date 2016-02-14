package uk.co.drnaylor.minecraft.quickstart.commands.playerinfo;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.UserParser;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.util.ArrayList;
import java.util.List;

@Permissions
@RunAsync
@Modules(PluginModule.PLAYERINFO)
@RootCommand
public class SeenCommand extends CommandBase {
    private final String playerKey = "player";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().arguments(GenericArguments.onlyOne(new UserParser(Text.of(playerKey)))).executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "seen", "seenplayer" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        InternalQuickStartUser iqsu = plugin.getUserLoader().getUser(user);

        List<Text> messages = new ArrayList<>();

        // Everyone gets the last online time.
        if (user.isOnline()) {
            messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.iscurrently", user.getName()) + " ", TextColors.GREEN, Util.messageBundle.getString("standard.online")));
            messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.displayname") + " ", Util.getName(user)));
            messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.loggedon") + " ", TextColors.GREEN, Util.getTimeToNow(iqsu.getLastLogin())));
        } else {
            messages.add(Text.of(TextColors.AQUA, Util.getMessageWithFormat("command.seen.iscurrently", user.getName()) + " ", TextColors.RED, Util.messageBundle.getString("standard.offline")));
            messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.loggedoff") + " ", TextColors.GREEN, Util.getTimeToNow(iqsu.getLastLogout())));
        }

        if (permissions.getPermissionWithSuffix("extended").stream().anyMatch(src::hasPermission)) {
            if (user.isOnline()) {
                messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.ipaddress") + " ", TextColors.GREEN, user.getPlayer().get().getConnection().getAddress().getAddress().toString()));
            }

            messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.isjailed") + " ",
                    getTrueOrFalse(iqsu.getJailData().isPresent(), TextActions.runCommand("/checkjail " + user.getName()))));
            messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.ismuted") + " ",
                    getTrueOrFalse(iqsu.getMuteData().isPresent(), TextActions.runCommand("/checkmute " + user.getName()))));

            BanService bs = Sponge.getServiceManager().provideUnchecked(BanService.class);
            messages.add(Text.of(TextColors.AQUA, Util.messageBundle.getString("command.seen.isbanned") + " ",
                    getTrueOrFalse(bs.getBanFor(user.getProfile()).isPresent(), TextActions.runCommand("/checkban " + user.getName()))));
        }

        src.sendMessages(messages);
        return CommandResult.success();
    }

    private Text getTrueOrFalse(boolean is, ClickAction ifTrue) {
        if (is) {
            return Text.builder(Util.messageBundle.getString("true")).color(TextColors.GREEN).style(TextStyles.UNDERLINE)
                    .onHover(TextActions.showText(Text.of(Util.messageBundle.getString("standard.clicktoseemore")))).onClick(ifTrue).build();
        }

        return Text.of(TextColors.RED, Util.messageBundle.getString("false"));
    }
}
