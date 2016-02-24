/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.playerinfo;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.NameUtil;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RootCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;
import uk.co.drnaylor.minecraft.quickstart.internal.services.UserConfigLoader;

import java.util.List;
import java.util.stream.Collectors;

@RunAsync
@Permissions(includeUser = true)
@Modules(PluginModule.PLAYERINFO)
@RootCommand
public class ListPlayerCommand extends CommandBase {
    @Inject private UserConfigLoader loader;
    private Text hidden;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "list", "listplayers" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean showVanished = permissions.getPermissionWithSuffix("seevanished").stream().anyMatch(src::hasPermission);
        long hiddenCount = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.get(Keys.INVISIBLE).orElse(false)).count();

        List<Text> playerList = Sponge.getServer().getOnlinePlayers().stream().filter(x -> showVanished || !x.get(Keys.INVISIBLE).orElse(false))
                .sorted((x, y) -> x.getName().compareToIgnoreCase(y.getName())).map(x -> {
                    Text.Builder tb = Text.builder();
                    if (x.get(Keys.INVISIBLE).orElse(false)) {
                        tb.append(getHidden());
                    }

                    return tb.append(NameUtil.getName(x, loader)).build();
                })
                .collect(Collectors.toList());

        String header;
        if (showVanished && hiddenCount > 0) {
            header = Util.getMessageWithFormat("command.list.playercount.hidden", String.valueOf(playerList.size()), String.valueOf(Sponge.getServer().getMaxPlayers()), String.valueOf(hiddenCount));
        } else {
            header = Util.getMessageWithFormat("command.list.playercount", String.valueOf(playerList.size()), String.valueOf(Sponge.getServer().getMaxPlayers()));
        }

        src.sendMessage(Text.of(TextColors.YELLOW, header));

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

    private Text getHidden() {
        return Text.of(TextColors.GRAY, Util.getMessageWithFormat("command.list.hidden") + " ");
    }
}
