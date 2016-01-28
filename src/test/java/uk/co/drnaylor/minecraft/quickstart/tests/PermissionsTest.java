package uk.co.drnaylor.minecraft.quickstart.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandBase;
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
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionOne.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionOne.class },
                    { QuickStart.PERMISSIONS_PREFIX + "root.test.base", PermissionRoot.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionRoot.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.sub.base", PermissionSub.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionSub.class },
                    { QuickStart.PERMISSIONS_PREFIX + "root.test.sub.base", PermissionRootSub.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionRootSub.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionNoAdmin.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionNoDefault.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionCustom.class },
                    { "test.test", PermissionCustom.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionCustom.class },
                    { QuickStart.PERMISSIONS_PREFIX + "alias.base", PermissionAlias.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionAlias.class }
            });
        }

        @Parameterized.Parameter(0)
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends CommandBase> clazz;

        @Test
        public void testPermissionIsValid() throws IllegalAccessException, InstantiationException {
            Assert.assertTrue("The permission " + permission + " was not available for " + clazz.getName(), clazz.newInstance().getCommandPermissions().contains(permission));
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
                    { QuickStart.PERMISSIONS_PREFIX + "test2.base", PermissionOne.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionRoot.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionSub.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionRootSub.class },
                    { QuickStart.PERMISSIONS_PREFIX + "root.test.base", PermissionRootSub.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.sub.base", PermissionRootSub.class },
                    { QuickStart.PERMISSIONS_ADMIN, PermissionNoAdmin.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionNoDefault.class },
                    { QuickStart.PERMISSIONS_PREFIX + "test.base", PermissionAlias.class }
            });
        }

        @Parameterized.Parameter(0)
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends CommandBase> clazz;

        @Test
        public void testPermissionIsNotValid() throws IllegalAccessException, InstantiationException {
            Assert.assertFalse("The permission " + permission + " was not available for " + clazz.getName(), clazz.newInstance().getCommandPermissions().contains(permission));
        }
    }

    @Permissions
    public static class PermissionOne extends CommandBase {

        @Override
        public CommandSpec createSpec() {
            return null;
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
}
