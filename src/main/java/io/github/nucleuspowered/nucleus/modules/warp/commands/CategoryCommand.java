/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.WarpCategory;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpCategoryArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.Scan;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Scan
@Permissions(prefix = "warp")
@RegisterCommand(value = {"category"}, subcommandOf = WarpCommand.class, hasExecutor = false)
public class CategoryCommand extends AbstractCommand<CommandSource> {

    private static String key = "category";
    private static String displayname = "displayname";
    private static String description = "displayname";

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        return null;
    }

    @Permissions(prefix = "warp.category", mainOverride = "list")
    @RunAsync
    @NoCooldown
    @NoCost
    @NoWarmup
    @RegisterCommand(value = {"list"}, subcommandOf = CategoryCommand.class)
    public static class ListCategoryCommand extends AbstractCommand<CommandSource> {

        @Inject private WarpHandler handler;

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            // Get all the categories.
            Util.getPaginationBuilder(src).contents(
                handler.getWarpsWithCategories().keySet().stream().filter(Objects::nonNull)
                    .sorted(Comparator.comparing(WarpCategory::getId)).map(x -> {
                List<Text> t = Lists.newArrayList();
                t.add(plugin.getMessageProvider().getTextMessageWithTextFormat("command.warp.category.listitem.simple",
                        Text.of(x.getId()), x.getDisplayName()));
                x.getDescription().ifPresent(y ->
                        t.add(plugin.getMessageProvider().getTextMessageWithTextFormat("command.warp.category.listitem.description", y)));
                return t;
            }).flatMap(Collection::stream).collect(Collectors.toList()))
            .title(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.category.listitem.title"))
            .padding(Text.of("-", TextColors.GREEN))
            .sendTo(src);

            return CommandResult.success();
        }
    }

    @Permissions(prefix = "warp.category", mainOverride = "displayname")
    @RunAsync
    @NoCooldown
    @NoCost
    @NoWarmup
    @RegisterCommand(value = {"setdisplayname"}, subcommandOf = CategoryCommand.class)
    public static class CategoryDisplayNameCommand extends AbstractCommand<CommandSource> {

        @Inject private WarpHandler handler;

        @Override public CommandElement[] getArguments() {
            return new CommandElement[] {
                    new WarpCategoryArgument(Text.of(key), handler),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of(displayname)))
            };
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            WarpCategory category = args.<WarpCategory>getOne(key).get();
            String displayName = args.<String>getOne(displayname).get();
            handler.setWarpCategoryDisplayName(category.getId(), TextSerializers.FORMATTING_CODE.deserialize(args.<String>getOne(displayname).get()));
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.category.displayname.set", category.getId(),
                    displayName));
            return CommandResult.success();
        }
    }

    @Permissions(prefix = "warp.category", mainOverride = "displayname")
    @RunAsync
    @NoCooldown
    @NoCost
    @NoWarmup
    @RegisterCommand(value = {"removedisplayname"}, subcommandOf = CategoryCommand.class)
    public static class CategoryRemoveDisplayNameCommand extends AbstractCommand<CommandSource> {

        @Inject private WarpHandler handler;

        @Override public CommandElement[] getArguments() {
            return new CommandElement[] {
                    new WarpCategoryArgument(Text.of(key), handler)
            };
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            WarpCategory category = args.<WarpCategory>getOne(key).get();
            handler.setWarpCategoryDisplayName(category.getId(), null);
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.category.displayname.removed", category.getId()));
            return CommandResult.success();
        }
    }

    @Permissions(prefix = "warp.category", mainOverride = "description")
    @RunAsync
    @NoCooldown
    @NoCost
    @NoWarmup
    @RegisterCommand(value = {"setdescription"}, subcommandOf = CategoryCommand.class)
    public static class CategoryDescriptionCommand extends AbstractCommand<CommandSource> {

        @Inject private WarpHandler handler;

        @Override public CommandElement[] getArguments() {
            return new CommandElement[] {
                    new WarpCategoryArgument(Text.of(key), handler),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of(description)))
            };
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            WarpCategory category = args.<WarpCategory>getOne(key).get();
            String d = args.<String>getOne(description).get();
            handler.setWarpCategoryDescription(category.getId(), TextSerializers.FORMATTING_CODE.deserialize(d));
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.category.description.set", category.getId(), d));
            return CommandResult.success();
        }
    }

    @Permissions(prefix = "warp.category", mainOverride = "description")
    @RunAsync
    @NoCooldown
    @NoCost
    @NoWarmup
    @RegisterCommand(value = {"removedescription"}, subcommandOf = CategoryCommand.class)
    public static class CategoryRemoveDescriptionCommand extends AbstractCommand<CommandSource> {

        @Inject private WarpHandler handler;

        @Override public CommandElement[] getArguments() {
            return new CommandElement[] {
                    new WarpCategoryArgument(Text.of(key), handler)
            };
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            WarpCategory category = args.<WarpCategory>getOne(key).get();
            handler.setWarpCategoryDescription(category.getId(), null);
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.category.description.removed", category.getId()));
            return CommandResult.success();
        }
    }
}
