/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Permissions(suggestedLevel = SuggestedLevel.NONE)
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand(value = "setupperms", subcommandOf = NucleusCommand.class)
public class SetupPermissionsCommand extends CommandBase<CommandSource> {

    @Inject private CoreConfigAdapter cca;
    @Inject private PermissionRegistry permissionRegistry;

    private final String roleKey = "Nucleus Role";
    private final String groupKey = "Permission Group";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(GenericArguments.enumValue(Text.of(roleKey), SuggestedLevel.class)),
            GenericArguments.onlyOne(new GroupArgument(Text.of(groupKey)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String command = cca.getNodeOrDefault().getPermissionCommand();
        if (command.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.permission.nocommand"));
            return CommandResult.empty();
        }

        if (!command.toLowerCase().contains("{{group}}") || !command.toLowerCase().contains("{{perm}}")) {
            src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.permission.notokens"));
            return CommandResult.empty();
        }

        SuggestedLevel sl = args.<SuggestedLevel>getOne(roleKey).get();
        String group = args.<String>getOne(groupKey).get();

        // Register all the commands.
        List<Map.Entry<String, PermissionInformation>> l = permissionRegistry.getPermissions().entrySet().stream()
                .filter(x -> x.getValue().level == sl).collect(Collectors.toList());
        for (Map.Entry<String, PermissionInformation> x : l) {
            String c = command.replaceAll("\\{\\{group\\}\\}", group).replaceAll("\\{\\{perm\\}\\}", x.getKey());
            Sponge.getCommandManager().process(src, c);
        }

        src.sendMessage(Util.getTextMessageWithFormat("command.nucleus.permission.success"));
        return CommandResult.success();
    }

    private static class GroupArgument extends CommandElement {

        GroupArgument(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String a = args.next();
            Optional<String> ls = getGroups(args).stream().filter(x -> x.equalsIgnoreCase(a)).findFirst();
            if (ls.isPresent()) {
                return ls.get();
            }

            throw args.createError(Util.getTextMessageWithFormat("args.permissiongroup.nogroup", a));
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                String a = args.peek();
                return getGroups(args).stream().filter(x -> x.toLowerCase().contains(a)).collect(Collectors.toList());
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        private Set<String> getGroups(CommandArgs args) throws ArgumentParseException {
            Optional<PermissionService> ops = Sponge.getServiceManager().provide(PermissionService.class);
            if (!ops.isPresent()) {
                throw args.createError(Util.getTextMessageWithFormat("args.permissiongroup.noservice"));
            }

            PermissionService ps = ops.get();
            Iterable<Subject> is = ps.getGroupSubjects().getAllSubjects();
            Set<String> groups = Sets.newHashSet();
            is.forEach(x -> groups.add(x.getIdentifier()));
            return groups;
        }
    }
}
