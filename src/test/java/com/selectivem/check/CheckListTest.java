/*
 * Copyright 2024 Nils Bandener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.selectivem.check;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CheckListTest {
    final Set<String> elements;
    final CheckList<String> subject;
    final int count;
    final String oneElement;
    final Set<String> someElements;

    @Test
    public void checkIf() {
        subject.checkIf((e) -> e.equals(oneElement));
        Assert.assertEquals(setOf(oneElement), subject.getCheckedElements());
        subject.checkIf((e) -> !e.equals(oneElement));
        Assert.assertEquals(elements, subject.getCheckedElements());
    }

    @Test
    public void uncheckIf() {
        subject.checkAll();
        subject.uncheckIf((e) -> e.equals(oneElement));
        Assert.assertEquals(without(elements, oneElement), subject.getCheckedElements());
    }

    @Test
    public void uncheckIf_blank() {
        subject.uncheckIf((e) -> {
            Assert.fail("Should not be called for " + e);
            return true;
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_unknown() {
        subject.check("xxx");
    }


    @Test(expected = IllegalArgumentException.class)
    public void uncheck_unknown() {
        subject.uncheck("xxx");
    }

    @Test
    public void uncheckIfPresent() {
        subject.checkAll();
        subject.uncheckIfPresent(oneElement);
        Assert.assertEquals(without(elements, oneElement), subject.getCheckedElements());
        Assert.assertEquals(setOf(oneElement), subject.getUncheckedElements());
    }

    @Test
    public void uncheckIfPresent_blank() {
        for (String element : elements) {
            if (!element.equals(oneElement)) {
                subject.check(element);
            }
        }
        Assert.assertFalse(subject.isChecked(oneElement));
        subject.uncheckIfPresent(oneElement);
        Assert.assertFalse(subject.isChecked(oneElement));
        Assert.assertEquals(without(elements, oneElement), subject.getCheckedElements());
    }

    @Test
    public void uncheckIfPresent_unknown() {
        subject.checkAll();
        subject.uncheckIfPresent("xxx");
        Assert.assertEquals(elements, subject.getCheckedElements());
        Assert.assertEquals(setOf(), subject.getUncheckedElements());
    }

    @Test
    public void isBlank() {
        Assert.assertTrue(subject.isBlank());
        subject.check(oneElement);
        Assert.assertFalse(subject.isBlank());
    }

    @Test
    public void isComplete() {
        Assert.assertFalse(subject.isComplete());

        for (String e : elements) {
            subject.check(e);
        }

        Assert.assertTrue(subject.isComplete());
    }

    @Test
    public void isChecked() {
        for (String e : elements) {
            Assert.assertFalse(subject.isChecked(e));
        }

        subject.check(oneElement);

        for (String e : elements) {
            if (e.equals(oneElement)) {
                Assert.assertTrue(subject.isChecked(e));
            } else {
                Assert.assertFalse(subject.isChecked(e));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void isChecked_unknown() {
        subject.uncheck("xxx");
    }

    @Test
    public void getCheckedElements_unknown() {
        subject.checkAll();
        Assert.assertFalse(subject.getCheckedElements().contains("xxx"));
    }

    @Test(expected = NoSuchElementException.class)
    public void getCheckedElements_overrun() {
        subject.checkAll();

        Iterator<String> iter = subject.getCheckedElements().iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        iter.next();
    }

    @Test
    public void getUncheckedElements() {
        Assert.assertEquals(elements, subject.getUncheckedElements());
        subject.checkAll();
        Assert.assertEquals(setOf(), subject.getUncheckedElements());
    }

    @Test
    public void getUncheckedElements_unknown() {
        Assert.assertFalse(subject.getCheckedElements().contains("xxx"));
    }

    @Test(expected = NoSuchElementException.class)
    public void getUncheckedElements_overrun() {
        subject.checkAll();

        Iterator<String> iter = subject.getUncheckedElements().iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        iter.next();
    }

    @Test
    public void getElements() {
        Assert.assertEquals(elements, subject.getElements());
    }


    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[]{setOf("a")}, new Object[]{setOf("a", "b")}, new Object[]{setOf("a", "b", "c", "d")}, new Object [] {IntStream.rangeClosed(1, 1000).mapToObj(Integer::toString).collect(Collectors.toSet())});
    }

    public CheckListTest(Set<String> elements) {
        this.elements = elements;
        this.subject = CheckList.create(elements);
        this.count = elements.size();
        this.oneElement = elements.iterator().next();
        this.someElements = elements.size() == 1 ? elements : minusOne(elements);
    }

    @SafeVarargs
    static <E> Set<E> setOf(E... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    static <E> Set<E> minusOne(Set<E> set) {
        HashSet<E> result = new HashSet<>(set);
        result.remove(set.iterator().next());
        return result;
    }

    static <E> Set<E> without(Set<E> set, E element) {
        HashSet<E> result = new HashSet<>(set);
        result.remove(element);
        return result;
    }

}
