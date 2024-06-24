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
 * Based on code which is:
 * 
 * Copyright 2022-2024 floragunn GmbH
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CheckListRandomizedTest {
    @Parameter
    public Params params;

    @Parameters(name = "{0}")
    public static Collection<Params> seeds() {
        ArrayList<Params> result = new ArrayList<>(10000);

        for (int size : Arrays.asList(1, 2, 3, 4, 6, 10, 20, 60, 100, 200, 500, 700, 1000)) {
            for (int seed = 100; seed < 1000; seed++) {
                result.add(new Params(size, seed));
            }
        }

        return result;
    }

    @Test
    public void basicTest() {
        Random random = new Random(params.seed);
        Set<String> elements = createElements(params, random);
        List<String> elementsList = new ArrayList<>(elements);
        Collections.shuffle(elementsList, random);

        CheckList<String> subject = CheckList.create(elements);
        Set<String> reference = new HashSet<>();

        for (int i = 0; i < Math.min(100, elementsList.size()); i++) {
            String element = elementsList.get(i);

            boolean checkResult = subject.check(element);
            reference.add(element);

            boolean checkResultReference = reference.size() == elements.size();

            Assert.assertEquals(subject + " vs " + reference, checkResultReference, checkResult);
            Assert.assertEquals(reference, subject.getCheckedElements());

            Set<String> checked = new HashSet<>();
            subject.iterateCheckedElements().forEach((e) -> checked.add(e));
            Assert.assertEquals(reference, checked);

            Assert.assertTrue(subject.getUncheckedElements().toString(),
                    subject.getUncheckedElements().stream().noneMatch(e -> subject.getCheckedElements().contains(e)));
            Assert.assertEquals(elements.size(), subject.getCheckedElements().size() + subject.getUncheckedElements().size());

            Set<String> unchecked = new HashSet<>();
            subject.iterateUncheckedElements().forEach((e) -> unchecked.add(e));
            Assert.assertEquals(subject.getUncheckedElements(), unchecked);

            for (String e : checked) {
                Assert.assertTrue(subject.isChecked(e));
            }

            for (String e : unchecked) {
                Assert.assertFalse(subject.isChecked(e));
            }
        }

        List<String> checkedElements = new ArrayList<>(reference);
        Collections.shuffle(checkedElements, random);

        int toUncheck = random.nextInt(checkedElements.size());

        for (int i = 0; i < toUncheck; i++) {
            String element = checkedElements.get(i);

            subject.uncheck(element);
            reference.remove(element);

            Assert.assertEquals(subject.getCheckedElements(), reference);

            Set<String> checked = new HashSet<>();
            subject.iterateCheckedElements().forEach((e) -> checked.add(e));
            Assert.assertEquals(reference, checked);

            Assert.assertTrue(subject.getUncheckedElements().toString(),
                    !subject.getUncheckedElements().stream().anyMatch(e -> subject.getCheckedElements().contains(e)));
            Assert.assertEquals(elements.size(), subject.getCheckedElements().size() + subject.getUncheckedElements().size());
        }

        for (int i = 90; i < Math.min(200, elementsList.size()); i++) {
            String element = elementsList.get(i);

            boolean checkResult = subject.check(element);
            reference.add(element);

            boolean checkResultReference = reference.size() == elements.size();

            Assert.assertEquals(subject + " vs " + reference, checkResultReference, checkResult);
            Assert.assertEquals(subject.getCheckedElements(), reference);

            Set<String> checked = new HashSet<>();
            subject.iterateCheckedElements().forEach((e) -> checked.add(e));
            Assert.assertEquals(reference, checked);

            Assert.assertTrue(subject.getUncheckedElements().toString(),
                    !subject.getUncheckedElements().stream().anyMatch(e -> subject.getCheckedElements().contains(e)));
            Assert.assertEquals(elements.size(), subject.getCheckedElements().size() + subject.getUncheckedElements().size());
        }

        checkedElements = new ArrayList<>(reference);
        Collections.shuffle(checkedElements, random);

        toUncheck = random.nextInt(checkedElements.size());

        for (int i = 0; i < toUncheck; i++) {
            String element = checkedElements.get(i);

            subject.uncheck(element);
            reference.remove(element);

            Assert.assertEquals(reference, subject.getCheckedElements());

            Set<String> checked = new HashSet<>();
            subject.iterateCheckedElements().forEach((e) -> checked.add(e));
            Assert.assertEquals(reference, checked);
        }

        for (int i = 0; i < elementsList.size(); i++) {
            String element = elementsList.get(i);

            boolean checkResult = subject.check(element);
            reference.add(element);

            boolean checkResultReference = reference.size() == elements.size();

            Assert.assertEquals(subject + " vs " + reference, checkResultReference, checkResult);
            Assert.assertEquals(subject.getCheckedElements(), reference);
        }
    }

    @Test
    public void checkIf_uncheckIf() {
        Random random = new Random(params.seed + 1000);
        Set<String> elements = createElements(params, random);
        List<String> elementsList = new ArrayList<>(elements);
        Collections.shuffle(elementsList, random);

        CheckList<String> subject = CheckList.create(elements);

        subject.checkIf(e -> e.contains("7"));
        Set<String> expected = elements.stream().filter(e -> e.contains("7")).collect(Collectors.toSet());

        Assert.assertEquals(expected, subject.getCheckedElements());

        subject.uncheckIf(e -> e.contains("9"));
        expected.removeAll(elements.stream().filter(e -> e.contains("9")).collect(Collectors.toSet()));

        Assert.assertEquals(expected, subject.getCheckedElements());

        for (int i = 0; i <= 9; i++) {
            String s = i + "";
            subject.checkIf(e -> e.contains(s));
            expected.addAll(elements.stream().filter(e -> e.contains(s)).collect(Collectors.toSet()));

            Assert.assertEquals(expected, subject.getCheckedElements());
            Assert.assertEquals(expected.size() == elements.size(), subject.isComplete());
        }

        subject.uncheckAll();
        Assert.assertEquals(Collections.emptySet(), subject.getCheckedElements());
        subject.checkAll();
        Assert.assertEquals(elements, subject.getCheckedElements());
    }

    static Set<String> createElements(Params params, Random random) {
        int size = params.size;

        if (size > 8) {
            size = random.nextInt(size - 8) + 8;
        }

        Set<String> result = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            result.add("e_" + i + "_" + random.nextInt(10));
        }

        return result;
    }

    static class Params {
        final int size;
        final int seed;

        Params(int size, int seed) {
            this.size = size;
            this.seed = seed;
        }

        @Override
        public String toString() {
            return size + "/" + seed;
        }
    }
}
