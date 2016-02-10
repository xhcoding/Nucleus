package uk.co.drnaylor.minecraft.quickstart.tests.arguments;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.NoCostArgument;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NoCostArgumentTests extends ArgumentBase {

    @Test
    public void testNoCostOk() throws ArgumentParseException {
        Assert.assertTrue(getNoCostArgument(new OKArgument(), new CommandContext()).<Boolean>getOne(NoCostArgument.NO_COST_ARGUMENT).get());
    }

    @Test
    public void testNoCostNull() throws ArgumentParseException {
        Assert.assertFalse(getNoCostArgument(new NullElement(), new CommandContext()).<Boolean>getOne(NoCostArgument.NO_COST_ARGUMENT).isPresent());
    }

    @Test
    public void testNoCostException() {
        CommandContext cc = new CommandContext();
        try {
            getNoCostArgument(new ThrowsElement(), cc);
        } catch (ArgumentParseException e) { /* Swallow */ }

        Assert.assertFalse(cc.<Boolean>getOne(NoCostArgument.NO_COST_ARGUMENT).isPresent());
    }

    private CommandContext getNoCostArgument(CommandElement toWrap, CommandContext cc) throws ArgumentParseException {
        CommandSource s = Mockito.mock(Player.class);
        CommandArgs ca = new CommandArgs("", new ArrayList<>());
        NoCostArgument nca = new NoCostArgument(toWrap);
        nca.parse(s, ca, cc);
        return cc;
    }

    private static class OKArgument extends CommandElement {

        protected OKArgument() {
            super(Text.of("key"));
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            return true;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return null;
        }
    }

    private static class ThrowsElement extends CommandElement {

        protected ThrowsElement() {
            super(Text.of("key"));
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            throw args.createError(Text.of("Nope"));
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return null;
        }
    }

    private static class NullElement extends CommandElement {

        protected NullElement() {
            super(Text.of("key"));
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            return null;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return null;
        }
    }
}
