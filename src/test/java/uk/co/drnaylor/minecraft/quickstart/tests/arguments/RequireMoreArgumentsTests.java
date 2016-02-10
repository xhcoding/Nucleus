package uk.co.drnaylor.minecraft.quickstart.tests.arguments;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.minecraft.quickstart.argumentparsers.RequireMoreArguments;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RequireMoreArgumentsTests extends ArgumentBase {

    private static String key = "key";

    @Test
    public void testOneArgument() throws ArgumentParseException {
        Assert.assertFalse(getElement(1, new CommandContext()).hasAny(key));
    }

    @Test
    public void testTwoArguments() throws ArgumentParseException {
        Assert.assertTrue(getElement(2, new CommandContext()).hasAny(key));
    }

    private CommandContext getElement(int args, CommandContext cc) throws ArgumentParseException {
        List<SingleArg> a = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= args; i++) {
            a.add(new SingleArg("a", i, i));
            sb.append("a ");
        }

        CommandSource s = Mockito.mock(Player.class);
        CommandArgs ca = new CommandArgs(sb.toString(), a);
        RequireMoreArguments nca = new RequireMoreArguments(new Element(), 2);
        nca.parse(s, ca, cc);
        return cc;
    }

    private class Element extends CommandElement {

        protected Element() {
            super(Text.of(key));
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
}
