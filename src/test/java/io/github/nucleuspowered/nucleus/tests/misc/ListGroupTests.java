/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.misc;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.playerinfo.commands.ListPlayerCommand;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListGroupTests {

    @Test
    public void testNonWeightedGroups() {
        Subject subject1 = createSubject("subject1");
        Subject subject2 = createSubject("subject2", subject1);
        Subject subject3 = createSubject("subject3", subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject2, subject1, subject3);

        List<Subject> sorted = subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(f -> 0, x, y)).collect(Collectors.toList());

        Assert.assertEquals(subject3, sorted.get(0));
        Assert.assertEquals(subject2, sorted.get(1));
        Assert.assertEquals(subject1, sorted.get(2));
    }

    @Test
    public void testWeightedGroups() {
        Subject subject1 = createSubject("subject1");
        Subject subject2 = createSubject("subject2", subject1);
        Subject subject2a = createSubjectWithWeight("subject2a", -1, subject1);
        Subject subject3 = createSubject("subject3", subject1, subject2);
        Subject subject4 = createSubjectWithWeight("subject4", 1, subject1, subject2);
        Subject subject5 = createSubjectWithWeight("subject5", 2, subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject1, subject2, subject2a, subject3, subject4, subject5);

        List<Subject> sorted = subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y))
            .collect(Collectors.toList());

        Assert.assertEquals(subject5, sorted.get(0));
        Assert.assertEquals(subject4, sorted.get(1));
        Assert.assertEquals(subject3, sorted.get(2));
        Assert.assertEquals(subject2, sorted.get(3));
        Assert.assertEquals(subject1, sorted.get(4));
        Assert.assertEquals(subject2a, sorted.get(5));
    }

    @Test
    public void testTreeWeightedGroups() {
        Subject subject1 = createSubject("subject1");
        Subject subject2 = createSubjectWithWeight("subject2", 4, subject1);
        Subject subject2a = createSubjectWithWeight("subject2a", -1, subject1);
        Subject subject3 = createSubject("subject3", subject1, subject2);
        Subject subject4 = createSubjectWithWeight("subject4", 1, subject1, subject2);
        Subject subject5 = createSubjectWithWeight("subject5", 2, subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject1, subject2, subject2a, subject3, subject4, subject5);

        List<Subject> sorted = printWeights(subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y))
            .collect(Collectors.toList()));

        List<Subject> expectedOrder = Lists.newArrayList(subject3, subject2, subject5, subject4, subject1, subject2a);

        for (int i = 0; i < sorted.size(); i++) {
            Assert.assertEquals(
                "Index " + i + " is wrong! (expected: " + expectedOrder.get(i).getIdentifier() + ", got: " + sorted.get(i).getIdentifier() + ")",
                expectedOrder.get(i), sorted.get(i));
        }
    }

    @Test
    public void tesMultipleWeightedTrees() {
        // Using example from Rasgnarok

        // Gym Leader has a weight of 6, and Moderator 9
        // I tried as Helper(5) and QuestBuilder(8)
        // and Helper showed instead of QB
        // They are not in the same inheritance tree either
        // Swapping Helper for GymLeader reproduced the issue
        // Helper parents DeputyMod which in turn parents Moderator
        // Gym Leader parents 8 other groups
        // and QB inherits from Builder
        // So Helpers that are also Leaders are showing as Leaders

        List<Subject> subjects = Lists.newArrayList();

        Subject gymLeader = createSubjectWithWeight("gymleader", 6);
        subjects.add(gymLeader);

        List<Subject> gymleaders = createSubjects(8, "gym", gymLeader);
        subjects.addAll(gymleaders);

        Subject builder = createSubject("builder");
        subjects.add(builder);
        Subject questBuilder = createSubjectWithWeight("QuestBuilder", 8, builder);
        subjects.add(questBuilder);

        Subject helper = createSubjectWithWeight("helper", 5);
        Subject dep = createSubjectWithWeight("depmod", 7, helper);
        Subject mod = createSubjectWithWeight("mod", 9, dep);
        subjects.add(helper);
        subjects.add(dep);
        subjects.add(mod);

        List<Subject> sorted = printWeights(subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y))
            .collect(Collectors.toList()));

        List<Integer> integers = sorted.stream().map(x -> Util.getIntOptionFromSubject(x, "nucleus.list.weight").orElse(0)).collect(Collectors.toList());
        for (int i = 1; i < integers.size(); i++) {
            Assert.assertTrue(integers.get(i-1) >= integers.get(i));
        }
    }

    private static List<Subject> printWeights(List<Subject> subjects) {
        subjects.forEach(x -> System.out.println(x.getIdentifier() + " - " + Util.getIntOptionFromSubject(x, "nucleus.list.weight")));
        return subjects;
    }

    private static List<Subject> createSubjects(int number, String prefix, Subject... parents) {
        List<Subject> subjects = Lists.newArrayList();
        for (int j = 1; j <= number; j++) {
            subjects.add(createSubject(prefix + j, parents));
        }

        return subjects;
    }

    private static Subject createSubject(String name, Subject... parents) {
        Subject subject = Mockito.mock(Subject.class);
        List<Subject> ls = Arrays.asList(parents);
        Mockito.when(subject.getIdentifier()).thenReturn(name);
        Mockito.when(subject.getOption(Mockito.anySetOf(Context.class), Mockito.eq("nucleus.list.weight")))
            .then(x -> ls.stream().map(y -> y.getOption("nucleus.list.weight")).filter(Optional::isPresent).findFirst().orElse(Optional.empty()));
        Mockito.when(subject.getOption(Mockito.eq("nucleus.list.weight")))
            .then(x -> ls.stream().map(y -> y.getOption("nucleus.list.weight")).filter(Optional::isPresent).findFirst().orElse(Optional.empty()));
        Mockito.when(subject.getParents()).thenReturn(Arrays.asList(parents));
        Mockito.when(subject.getParents(Mockito.anySetOf(Context.class))).thenReturn(Arrays.asList(parents));
        return subject;
    }

    private static Subject createSubjectWithWeight(String name, int weight, Subject... parents) {
        Subject subject = Mockito.mock(Subject.class);
        Mockito.when(subject.getIdentifier()).thenReturn(name);
        Mockito.when(subject.getOption(Mockito.anySetOf(Context.class), Mockito.eq("nucleus.list.weight")))
            .thenReturn(Optional.of(String.valueOf(weight)));
        Mockito.when(subject.getOption(Mockito.eq("nucleus.list.weight"))).thenReturn(Optional.of(String.valueOf(weight)));
        Mockito.when(subject.getParents()).thenReturn(Arrays.asList(parents));
        Mockito.when(subject.getParents(Mockito.anySetOf(Context.class))).thenReturn(Arrays.asList(parents));
        return subject;
    }
}
