/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Permissions;

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
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionOne.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionOne.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "root.test.base", PermissionRoot.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionRoot.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.sub.base", PermissionSub.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionSub.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "root.test.sub.base", PermissionRootSub.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionRootSub.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionNoAdmin.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionNoDefault.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionCustom.class },
                    { "test.test", PermissionCustom.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionCustom.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "alias.base", PermissionAlias.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionAlias.class },
                    { PermissionUtil.PERMISSIONS_MOD, PermissionMod.class },
                    { PermissionUtil.PERMISSIONS_USER, PermissionUser.class }
            });
        }

        @Parameterized.Parameter(0)
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends CommandBase> clazz;

        @Test
        public void testPermissionIsValid() throws IllegalAccessException, InstantiationException {
            CommandBase c = clazz.newInstance();
            c.postInit();
            Assert.assertTrue("The permission " + permission + " was not available for " + clazz.getName(), c.getCommandPermissions().contains(permission));
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
                    { PermissionUtil.PERMISSIONS_PREFIX + "test2.base", PermissionOne.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionRoot.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionSub.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionRootSub.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "root.test.base", PermissionRootSub.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.sub.base", PermissionRootSub.class },
                    { PermissionUtil.PERMISSIONS_ADMIN, PermissionNoAdmin.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionNoDefault.class },
                    { PermissionUtil.PERMISSIONS_PREFIX + "test.base", PermissionAlias.class },
                    { PermissionUtil.PERMISSIONS_MOD, PermissionNoAdmin.class },
                    { PermissionUtil.PERMISSIONS_USER, PermissionNoAdmin.class }
            });
        }

        @Parameterized.Parameter(0)
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends CommandBase> clazz;

        @Test
        public void testPermissionIsNotValid() throws IllegalAccessException, InstantiationException {
            CommandBase c = clazz.newInstance();
            c.postInit();
            Assert.assertFalse("The permission " + permission + " was not available for " + clazz.getName(), c.getCommandPermissions().contains(permission));
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

    @Permissions(includeAdmin = false)
    public static class PermissionNoAdmin extends PermissionOne { }

    @Permissions(useDefault = false)
    public static class PermissionNoDefault extends PermissionOne { }

    @Permissions({"test.test"})
    public static class PermissionCustom extends PermissionOne { }

    @Permissions(includeMod = true)
    public static class PermissionMod extends PermissionOne { }

    @Permissions(includeUser = true)
    public static class PermissionUser extends PermissionOne { }
}
