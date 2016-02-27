/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.permission.Subject;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandPermissionHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;
import uk.co.drnaylor.minecraft.quickstart.tests.util.TestModule;

import java.util.Arrays;

/**
 * Tests permissions are assigned correctly.
 */
public class PermissionsTest {

    /**
     * Tests that the specified permissions are in the permission list.
     */
    @RunWith(Parameterized.class)
    public static class ValidTest {

        @Parameterized.Parameters(name = "{index}: Permission {0} on {1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.base", PermissionOne.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "root.test.base", PermissionRoot.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.sub.base", PermissionSub.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "root.test.sub.base", PermissionRootSub.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.base", PermissionCustom.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "alias.base", PermissionAlias.class }
            });
        }

        @Parameterized.Parameter(0)
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends CommandBase> clazz;

        @Test
        public void testPermissionIsValid() throws IllegalAccessException, InstantiationException {
            CommandBase c = clazz.newInstance();
            getInjector().injectMembers(c);
            c.postInit();
            Subject s = Mockito.mock(Subject.class);
            Mockito.when(s.hasPermission(Matchers.any())).thenReturn(false);
            Mockito.when(s.hasPermission(Matchers.eq(permission))).thenReturn(true);
            Mockito.validateMockitoUsage();
            Assert.assertTrue("The permission " + permission + " was not available for " + clazz.getName(), c.getPermissionHandler().testBase(s));
        }

    }

    /**
     * Tests that the specified permissions are not in the permission list.
     */
    @RunWith(Parameterized.class)
    public static class InvalidTest {

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test2.base", PermissionOne.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.base", PermissionRoot.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.base", PermissionSub.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.base", PermissionRootSub.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "root.test.base", PermissionRootSub.class },
                    { CommandPermissionHandler.PERMISSIONS_PREFIX + "test.sub.base", PermissionRootSub.class }
            });
        }

        @Parameterized.Parameter(0)
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends CommandBase> clazz;

        @Test
        public void testPermissionIsNotValid() throws IllegalAccessException, InstantiationException {
            CommandBase c = clazz.newInstance();
            getInjector().injectMembers(c);
            c.postInit();
            Subject s = Mockito.mock(Subject.class);
            Mockito.when(s.hasPermission(Matchers.any())).thenReturn(false);
            Mockito.when(s.hasPermission(Matchers.eq(permission))).thenReturn(true);
            Mockito.validateMockitoUsage();
            Assert.assertFalse("The permission " + permission + " was not available for " + clazz.getName(), c.getPermissionHandler().testBase(s));
        }
    }

    @Permissions
    public static class PermissionOne extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return CommandSpec.builder().executor(this).build();
        }

        @Override
        public String[] getAliases() {
            return new String[] { "test", "test2" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return null;
        }
    }

    @Permissions(root = "root")
    public static class PermissionRoot extends PermissionOne { }

    @Permissions(sub = "sub")
    public static class PermissionSub extends PermissionOne { }

    @Permissions(root = "root", sub = "sub")
    public static class PermissionRootSub extends PermissionOne { }

    @Permissions(alias = "alias")
    public static class PermissionAlias extends PermissionOne { }

    @Permissions({"test.test"})
    public static class PermissionCustom extends PermissionOne { }

    private static Injector getInjector() {
        return Guice.createInjector(new TestModule());
    }
}
