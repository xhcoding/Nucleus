/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.sanity;

import com.google.common.reflect.ClassPath;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.internal.annotations.NoPermissions;
import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.command.OldCommandBase;
import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SanityTests {

    @Test
    @SuppressWarnings("unchecked")
    public void testThatAnythingThatIsAConcreteModuleHasAModuleDataAnnotation() throws IOException {
        Set<ClassPath.ClassInfo> ci = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("io.github.nucleuspowered.nucleus.modules");
        Set<Class<? extends StandardModule>> sc = ci.stream().map(ClassPath.ClassInfo::load).filter(StandardModule.class::isAssignableFrom)
                .map(x -> (Class<? extends StandardModule>)x).collect(Collectors.toSet());

        List<Class<?>> moduleList = sc.stream().filter(x -> !x.isAnnotationPresent(ModuleData.class)).collect(Collectors.toList());
        if (!moduleList.isEmpty()) {
            StringBuilder sb = new StringBuilder("Some modules do not have the ModuleData annotation: ");
            moduleList.forEach(x -> sb.append(x.getName()).append(System.lineSeparator()));
            Assert.fail(sb.toString());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThatAllCommandsHaveAPermissionAnnotation() throws IOException {
        Set<ClassPath.ClassInfo> ci = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("io.github.nucleuspowered.nucleus.modules");
        Set<Class<? extends StandardModule>> sc = ci.stream().map(ClassPath.ClassInfo::load).filter(OldCommandBase.class::isAssignableFrom)
                .map(x -> (Class<? extends StandardModule>)x).collect(Collectors.toSet());

        List<Class<?>> moduleList = sc.stream().filter(x -> !x.isAnnotationPresent(NoPermissions.class) && !x.isAnnotationPresent(Permissions.class)).collect(Collectors.toList());
        if (!moduleList.isEmpty()) {
            StringBuilder sb = new StringBuilder("Some commands do not have the Permissions annotation: ");
            moduleList.forEach(x -> sb.append(x.getName()).append(System.lineSeparator()));
            Assert.fail(sb.toString());
        }
    }
}
