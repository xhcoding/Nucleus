/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Permissions(prefix = "nucleus", suggestedLevel = SuggestedLevel.NONE)
@NoModifiers
@NonnullByDefault
@RegisterCommand(value = {"setupperms", "setperms"}, subcommandOf = NucleusCommand.class)
public class SetupPermissionsCommand extends AbstractCommand<CommandSource> {

    private final PermissionRegistry permissionRegistry = Nucleus.getNucleus().getPermissionRegistry();

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
        // The GroupArgument should have already checked for this.
        Set<Context> globalContext = Sets.newHashSet();
        SuggestedLevel sl = args.<SuggestedLevel>getOne(roleKey).get();
        Subject group = args.<Subject>getOne(groupKey).get();

        // Register all the commands.
        permissionRegistry.getPermissions().entrySet().stream()
                .filter(x -> x.getValue().level == sl).forEach(x -> group.getSubjectData().setPermission(globalContext, x.getKey(), Tristate.TRUE));

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.permission.complete", sl.toString().toLowerCase(), group.getIdentifier()));
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
                return Sponge.getServiceManager().provide(PermissionService.class).get()
                        .getGroupSubjects().getSubject(ls.get()).get();
            }

            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.permissiongroup.nogroup", a));
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
                throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.permissiongroup.noservice"));
            }

            PermissionService ps = ops.get();
            try {
                return Sets.newHashSet(ps.getGroupSubjects().getAllIdentifiers().get());
            } catch (Exception e) {
                // TODO - Sort this out for API 7+
                if (Nucleus.getNucleus().isDebugMode()) {
                    e.printStackTrace();
                }

                throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.permissiongroup.failed"));
            }
        }
    }
}
