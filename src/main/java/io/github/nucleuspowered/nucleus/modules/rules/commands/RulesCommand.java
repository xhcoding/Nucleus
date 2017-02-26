/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rules.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.rules.RulesModule;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfig;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand("rules")
@NoWarmup
@NoCooldown
@NoCost
@EssentialsEquivalent("rules")
public class RulesCommand extends AbstractCommand<CommandSource> implements StandardAbstractCommand.Reloadable {

    @Inject private RulesConfigAdapter rca;
    private Text title = Text.EMPTY;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        plugin.getTextFileController(RulesModule.RULES_KEY)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.rules.empty"))
                .sendToPlayer(src, title);
        return CommandResult.success();
    }

    @Override public void onReload() {
        RulesConfig config = rca.getNodeOrDefault();
        String title = config.getRulesTitle();
        if (title.isEmpty()) {
            this.title = Text.EMPTY;
        } else {
            this.title = TextSerializers.FORMATTING_CODE.deserialize(title);
        }
    }
}
