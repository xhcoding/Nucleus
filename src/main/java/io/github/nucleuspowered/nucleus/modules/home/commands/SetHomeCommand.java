/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.option.OptionSubjectData;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Permissions(root = "home", alias = "set", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand({"homeset", "sethome"})
public class SetHomeCommand extends CommandBase<Player> {

    private final String homeKey = "home";
    private final Pattern warpName = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{1,15}$");

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.string(Text.of(homeKey))))};
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("unlimited", new PermissionInformation(Util.getMessageWithFormat("permission.homes.unlimited"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home key.
        String home = args.<String>getOne(homeKey).orElse("home").toLowerCase();

        // Get the homes.
        InternalNucleusUser iqsu = plugin.getUserLoader().getUser(src);
        Map<String, WarpLocation> msw = iqsu.getHomes();

        if (!warpName.matcher(home).matches()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.sethome.name"));
            return CommandResult.empty();
        }

        int c = getCount(src);
        if (msw.size() >= c) {
            src.sendMessage(Util.getTextMessageWithFormat("command.sethome.limit", String.valueOf(c)));
            return CommandResult.empty();
        }

        // Does the home exist?
        if (msw.containsKey(home) || !iqsu.setHome(home, src.getLocation(), src.getRotation())) {
            src.sendMessage(Util.getTextMessageWithFormat("command.sethome.seterror", home));
            return CommandResult.empty();
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.sethome.set", home));
        return CommandResult.success();
    }

    private int getCount(Player src) {
        if (permissions.testSuffix(src, "unlimited")) {
            return Integer.MAX_VALUE;
        }

        int homesAllowed = Math.max(getHomeCountFromSubjectData(src, src.getSubjectData()), getHomeCountFromSubjectData(src, src.getTransientSubjectData()));
        return Math.max(homesAllowed, 1);
    }

    private int getHomeCountFromSubjectData(Player src, SubjectData sd) {
        if (sd instanceof OptionSubjectData) {
            String count = ((OptionSubjectData) sd).getOptions(src.getActiveContexts()).get("home-count");
            if (count == null) {
                count = ((OptionSubjectData) sd).getOptions(Sets.newHashSet()).getOrDefault("home-count", "1");
            }

            try {
                return Integer.parseUnsignedInt(count);
            } catch (NumberFormatException e) {
                // Nope.
            }
        }

        return 1;
    }
}
