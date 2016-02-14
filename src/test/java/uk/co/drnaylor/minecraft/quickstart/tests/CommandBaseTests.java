package uk.co.drnaylor.minecraft.quickstart.tests;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.config.CommandsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class CommandBaseTests {

    /**
     * Tests that if a {@link Player} is provided, they can execute a command for players.
     *
     * @throws CommandException
     */
    @Test
    public void testThatPlayersCanExecutePlayerCommand() throws CommandException {
        PlayerCommand cmd = new PlayerCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        Player mock = getMockPlayer();
        CommandResult result = cmd.execute(mock, getContext());

        Assert.assertTrue("There should have been one success!", result.getSuccessCount().orElse(0) == 1);
    }

    /**
     * Tests that if a {@link CommandSource} that is not a {@link Player} is provided, the command fails with
     * {@link CommandResult#empty()}.
     *
     * @throws CommandException
     */
    @Test
    public void testThatCommandSourcesCannotExecutePlayerCommands() throws CommandException {
        PlayerCommand cmd = new PlayerCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        CommandSource mock = getMockCommandSource();
        CommandResult result = cmd.execute(mock, getContext());
        Assert.assertTrue("There should have been no successes!", !result.getSuccessCount().isPresent() || result.getSuccessCount().get() == 0);
    }

    /**
     * Tests that if a {@link CommandSource} that is not a player is provided, they can execute a standard command.
     *
     * @throws CommandException
     */
    @Test
    public void testThatCommandSourcesCanExecuteStandardCommand() throws CommandException {
        BasicCommand cmd = new BasicCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        CommandSource mock = getMockCommandSource();
        CommandResult result = cmd.execute(mock, getContext());
        Assert.assertTrue("There should have been one success!", result.getSuccessCount().orElse(0) == 1);
    }

    /**
     * Tests that if a {@link Player} is provided, they can execute a standard command.
     *
     * @throws CommandException
     */
    @Test
    public void testThatPlayerSourcesCanExecuteStandardCommand() throws CommandException {
        BasicCommand cmd = new BasicCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        Player mock = getMockPlayer();
        CommandResult result = cmd.execute(mock, getContext());
        Assert.assertTrue("There should have been one success!", result.getSuccessCount().orElse(0) == 1);
    }

    private CommandContext getContext() {
        return new CommandContext();
    }

    private Player getMockPlayer() {
        return Mockito.mock(Player.class);
    }

    private CommandSource getMockCommandSource() {
        return Mockito.mock(CommandSource.class);
    }

    private Injector getInjector() {
        return Guice.createInjector(new TestModule());
    }

    private class PlayerCommand extends CommandBase<Player> {

        @Override
        public CommandSpec createSpec() {
            return CommandSpec.builder().executor(this).build();
        }

        @Override
        public String[] getAliases() {
            return new String[] { "test" };
        }

        @Override
        public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
            return CommandResult.success();
        }
    }

    private class BasicCommand extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return CommandSpec.builder().executor(this).build();
        }

        @Override
        public String[] getAliases() {
            return new String[] { "test" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return CommandResult.success();
        }
    }

    private class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            Path test;
            Path test2;
            try {
                test = Files.createTempDirectory("quick");
                test2 = Files.createTempFile(test, "quick", "conf");
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            this.bind(Path.class).annotatedWith(DefaultConfig.class).toInstance(test2);
            this.bind(Path.class).annotatedWith(ConfigDir.class).toInstance(test);
            this.bind(Game.class).toInstance(Mockito.mock(Game.class));
            this.bind(Logger.class).toInstance(Mockito.mock(Logger.class));
            this.bind(QuickStart.class).toInstance(getMockPlugin());
        }

        private QuickStart getMockPlugin() {
            QuickStart plugin = Mockito.mock(QuickStart.class);
            try {
                Path file = Files.createTempFile("quickstartcmdtest", "conf");
                CommandsConfig cc = new CommandsConfig(file);
                Mockito.when(plugin.getConfig(ConfigMap.COMMANDS_CONFIG)).thenReturn(Optional.of(cc));
                return plugin;
            } catch (IOException | ObjectMappingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
