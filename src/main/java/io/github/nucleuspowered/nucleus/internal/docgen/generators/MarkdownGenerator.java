/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen.generators;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.docgen.CommandDoc;
import io.github.nucleuspowered.nucleus.internal.docgen.PermissionDoc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MarkdownGenerator<T> {

    private static final String EMPTY = "";

    abstract List<String> create(List<T> input);

    public final void create(Path file, List<T> input) throws Exception {
        Files.write(file, create(input), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public static class CommandMarkdownGenerator extends MarkdownGenerator<CommandDoc> {

        @Override List<String> create(List<CommandDoc> input) {
            List<String> output = Lists.newArrayList();

            Map<String, List<CommandDoc>> d = input.stream().collect(Collectors.groupingBy(CommandDoc::getModule, Collectors.toList()));
            List<String> i = Lists.newArrayList(d.keySet());
            i.sort(String::compareToIgnoreCase);

            // for each commanddoc
            for (String mod : i) {
                output.add("# Module: " + mod);

                List<CommandDoc> dd = d.get(mod);
                dd.sort(Comparator.comparing(CommandDoc::getCommandName));

                for (CommandDoc it : dd) {
                    // Header - command name
                    String commandName = it.getCommandName();
                    String key = it.getCommandName().toLowerCase().replace(" ", "_");
                    String prefix = commandName.contains(" ") ? commandName.substring(0, commandName.lastIndexOf(" ")) + " " : "";
                    output.add("## <a id=\"" + key + "\">/" + it.getCommandName() + "</a>");
                    output.add("### " + it.getOneLineDescription());
                    if (it.getExtendedDescription() != null && !it.getExtendedDescription().isEmpty()) {
                        output.add(it.getExtendedDescription());
                    }

                    output.add(EMPTY);
                    if (it.getEssentialsEquivalents() != null && !it.getEssentialsEquivalents().isEmpty()) {
                        output.add("**This command is similar to the Essentials commands:** " + String.join(", ", it.getEssentialsEquivalents()));
                        if (it.getEssNotes() != null && !it.getEssNotes().isEmpty()) {
                            output.add("**Essentials Migration Notes:** " + it.getEssNotes());
                        }
                    }

                    output.add("### Aliases");
                    String aliases = "`" + prefix + String.join("`, `" + prefix, it.getAliases().split(", ")) + "`";
                    output.add(aliases);
                    if (it.getRootAliases() != null && !it.getRootAliases().isEmpty()) {
                        output.add(EMPTY);
                        output.add("`/" + String.join("`, `/", it.getRootAliases().split(", ")) + "`");
                    }

                    output.add("### Usage");
                    output.add("`" + it.getSimpleUsage() + "`");

                    output.add("### Permissions");
                    if (it.getPermissions() != null && it.getPermissions().isEmpty()) {
                        output.add("_There are no permissions for this command_");
                    } else {
                        it.getPermissions().forEach(pd -> {
                            output.add("* `" + pd.getPermission() + "` - **Default Level:** `" + pd.getDefaultLevel() + "` - ");
                            output.add(pd.getDescription());
                        });
                    }

                    if (it.getSubcommands() != null && !it.getSubcommands().isEmpty()) {
                        output.add("### Subcommands");
                        output.add(createSubLink(key, it.getSubcommands()));
                    }
                }
            }

            return output;
        }

        private String createSubLink(String root, String subcommands) {
            String[] s = subcommands.split(", ");
            return Arrays.stream(s).map(x -> "<a href=\"#" + root + "_" + x.toLowerCase() + "\">" + x + "</a>").collect(Collectors.joining(", "));
        }
    }

    public static class PermissionMarkdownGenerator extends MarkdownGenerator<PermissionDoc> {

        @Override List<String> create(List<PermissionDoc> input) {
            List<String> output = Lists.newArrayList();
            output.add("Module | Permission | Default Role | Description");
            output.add("--- | --- | --- | ---");

            for (PermissionDoc permissionDoc : input) {
                output.add(permissionDoc.getModule() + " | " + permissionDoc.getPermission() + " | " + permissionDoc.getDefaultLevel() + " | " +
                        permissionDoc.getDescription().replace("\n", " "));
            }

            return output;
        }
    }
}
