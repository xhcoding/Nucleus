/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.misc;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.modules.playerinfo.commands.ListPlayerCommand;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListGroupTests {

    @Test
    public void testNonWeightedGroups() {
        Subject subject1 = createSubject();
        Subject subject2 = createSubject(subject1);
        Subject subject3 = createSubject(subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject2, subject1, subject3);

        List<Subject> sorted = subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(f -> 0, x, y)).collect(Collectors.toList());

        Assert.assertEquals(subject3, sorted.get(0));
        Assert.assertEquals(subject2, sorted.get(1));
        Assert.assertEquals(subject1, sorted.get(2));
    }

    @Test
    public void testWeightedGroups() {
        Subject subject1 = createSubject();
        Subject subject2 = createSubject(subject1);
        Subject subject2a = createSubject(subject1);
        Subject subject3 = createSubject(subject1, subject2);
        Subject subject4 = createSubject(subject1, subject2);
        Subject subject5 = createSubject(subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject1, subject2, subject2a, subject3, subject4, subject5);

        List<Subject> sorted = subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(f -> {
            if (f == subject4) {
                return 1;
            }

            if (f == subject5) {
                return 2;
            }

            if (f == subject2a) {
                return -1;
            }

            return 0;
        }, x, y)).collect(Collectors.toList());

        Assert.assertEquals(subject5, sorted.get(0));
        Assert.assertEquals(subject4, sorted.get(1));
        Assert.assertEquals(subject3, sorted.get(2));
        Assert.assertEquals(subject2, sorted.get(3));
        Assert.assertEquals(subject1, sorted.get(4));
        Assert.assertEquals(subject2a, sorted.get(5));
    }

    private static Subject createSubject(Subject... parents) {
        Subject subject = Mockito.mock(Subject.class);
        Mockito.when(subject.getParents()).thenReturn(Arrays.asList(parents));
        Mockito.when(subject.getParents(Mockito.anySetOf(Context.class))).thenReturn(Arrays.asList(parents));
        return subject;
    }

}
