/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.commands.playerinfo;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.CommandBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunAsync
@Permissions(suggestedLevel = SuggestedLevel.USER)
@Modules(PluginModule.PLAYERINFO)
@RegisterCommand({"list", "listplayers"})
public class ListPlayerCommand extends CommandBase<CommandSource> {

    @Inject private UserConfigLoader loader;
    private Text hidden = Text.of(TextColors.GRAY, Util.getMessageWithFormat("command.list.hidden") + " ");

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("seevanished", new PermissionInformation(Util.getMessageWithFormat("permission.list.seevanished"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean showVanished = permissions.testSuffix(src, "seevanished");
        long hiddenCount = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.get(Keys.INVISIBLE).orElse(false)).count();

        List<Text> playerList = Sponge.getServer().getOnlinePlayers().stream().filter(x -> showVanished || !x.get(Keys.INVISIBLE).orElse(false))
                .sorted((x, y) -> x.getName().compareToIgnoreCase(y.getName())).map(x -> {
                    Text.Builder tb = Text.builder();
                    if (x.get(Keys.INVISIBLE).orElse(false)) {
                        tb.append(hidden);
                    }

                    return tb.append(NameUtil.getName(x, loader)).build();
                }).collect(Collectors.toList());

        Text header;
        if (showVanished && hiddenCount > 0) {
            header = Util.getTextMessageWithFormat("command.list.playercount.hidden", String.valueOf(playerList.size()),
                    String.valueOf(Sponge.getServer().getMaxPlayers()), String.valueOf(hiddenCount));
        } else {
            header = Util.getTextMessageWithFormat("command.list.playercount", String.valueOf(playerList.size()),
                    String.valueOf(Sponge.getServer().getMaxPlayers()));
        }

        src.sendMessage(header);

        if (!playerList.isEmpty()) {
            boolean isFirst = true;
            Text.Builder tb = Text.builder();
            for (Text text : playerList) {
                if (!isFirst) {
                    tb.append(Text.of(TextColors.WHITE, ", "));
                }

                tb.append(text);
                isFirst = false;
            }

            src.sendMessage(tb.build());
        }

        return CommandResult.success();
    }
}
