/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.commands.core;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.RunAsync;
import uk.co.drnaylor.minecraft.quickstart.internal.enums.SuggestedLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RunAsync
@Permissions(root = "quickstart")
public class SuggestedPermissionsCommand extends CommandBase {
    private final String file = "quickstart-essentials-perms.txt";

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "printperms" };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Map<String, SuggestedLevel> l = plugin.getPermissionRegistry().getPermissions();
        List<String> notsuggested = l.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.NONE).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> admin = l.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.ADMIN).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> mod = l.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.MOD).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> user = l.entrySet().stream().filter(x -> x.getValue() == SuggestedLevel.USER).map(Map.Entry::getKey).collect(Collectors.toList());

        BufferedWriter f = new BufferedWriter(new FileWriter(file));
        Consumer<String> permWriter = x -> {
            try {
                f.write(x);
                f.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        f.write("Not Suggested");
        f.write("-----");
        f.newLine();
        notsuggested.stream().sorted().forEach(permWriter);
        f.newLine();

        f.write("Admin");
        f.write("-----");
        f.newLine();

        admin.stream().sorted().forEach(permWriter);
        f.newLine();
        f.write("Mod");
        f.write("-----");
        f.newLine();

        mod.stream().sorted().forEach(permWriter);
        f.newLine();
        f.write("User");
        f.write("-----");
        f.newLine();

        user.stream().sorted().forEach(permWriter);
        f.flush();
        f.close();

        src.sendMessage(Text.of(Util.getMessageWithFormat("command.printperms", file)));
        return CommandResult.success();
    }
}
