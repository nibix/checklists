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

import static org.junit.Assert.assertTrue;

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
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BackingCollectionsTest.IndexedImmutableSet.RandomizedTestBig.class,
        BackingCollectionsTest.IndexedImmutableSet.ImmutableSetRandomizedTestSmall.class })
public class BackingCollectionsTest {

    public static class IndexedImmutableSet {
        @RunWith(Parameterized.class)
        public static class RandomizedTestBig {
            @Parameter
            public Integer seed;

            @Parameters(name = "{0}")
            public static Collection<Integer> seeds() {
                ArrayList<Integer> result = new ArrayList<>(10000);

                for (int i = 1000; i < 10000; i++) {
                    result.add(i);
                }

                return result;
            }

            @Test
            public void builder() {
                Random random = new Random(seed);

                HashSet<String> reference = new HashSet<>();
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> subject = BackingCollections.IndexedUnmodifiableSet
                        .builder(10);
                HashSet<String> addedInRound1 = new HashSet<>();
                HashSet<String> addedInRound2 = new HashSet<>();
                HashSet<String> addedInRound3 = new HashSet<>();

                for (int k = 0; k < random.nextInt(100) + 4; k++) {
                    String string = randomString(random);

                    reference.add(string);
                    subject = subject.with(string);
                    addedInRound1.add(string);
                }

                assertEquals(reference, subject);

                int insertionCount = random.nextInt(30) + 1;

                for (int k = 0; k < insertionCount; k++) {
                    String string = randomString(random);

                    reference.add(string);
                    subject = subject.with(string);
                    addedInRound2.add(string);
                    assertEquals(reference, subject);
                }

                insertionCount = random.nextInt(30) + 1;

                for (int k = 0; k < insertionCount; k++) {
                    String string = randomString(random);

                    reference.add(string);
                    subject = subject.with(string);
                    addedInRound3.add(string);
                    assertEquals(reference, subject);

                }

                assertEquals(reference, subject);
                assertEquals(reference, subject.build());
            }

            @Test
            public void contains() {
                Random random = new Random(seed);

                HashSet<String> reference = new HashSet<>();
                List<String> referenceList = new ArrayList<>();

                for (int k = 0; k < random.nextInt(1000) + 4; k++) {
                    String string = randomString(random);

                    if (!reference.contains(string)) {
                        reference.add(string);
                        referenceList.add(string);
                    }
                }

                BackingCollections.IndexedUnmodifiableSet<String> subject = BackingCollections.IndexedUnmodifiableSet.of(reference);

                assertEquals(reference, subject);
                Collections.shuffle(referenceList, random);

                for (int k = 0; k < reference.size(); k++) {
                    String string1 = referenceList.get(k);
                    assertTrue("String " + string1 + " not found in " + subject, subject.contains(string1));

                    String string2 = string1 + "X";
                    Assert.assertEquals("String " + string2 + " found in " + subject, reference.contains(string2), subject.contains(string2));

                    String string3 = randomString(random);
                    Assert.assertEquals("String " + string3 + " found in " + subject, reference.contains(string3), subject.contains(string3));
                }
            }

            @Test
            public void iterator() {
                Random random = new Random(seed);

                HashSet<String> reference = new HashSet<>();
                List<String> referenceList = new ArrayList<>();

                for (int k = 0; k < random.nextInt(1000) + 4; k++) {
                    String string = randomString(random);

                    if (!reference.contains(string)) {
                        reference.add(string);
                        referenceList.add(string);
                    }
                }

                BackingCollections.IndexedUnmodifiableSet<String> subject = BackingCollections.IndexedUnmodifiableSet.of(reference);

                assertEquals(reference, subject);
                Set<String> subjectCopy = new HashSet<>(subject.size());

                for (String e : subject) {
                    subjectCopy.add(e);
                }

                Assert.assertEquals(reference, subjectCopy);
            }

            private static <E> void assertEquals(HashSet<E> expected, BackingCollections.IndexedUnmodifiableSet.InternalBuilder<E> actual) {
                for (E e : expected) {
                    if (!actual.contains(e)) {
                        Assert.fail("Not found in actual: " + e + ";\nexpected (" + expected.size() + "): " + expected + "\nactual (" + actual.size()
                                + "): " + actual);
                    }
                }

                for (E e : actual) {
                    if (!expected.contains(e)) {
                        Assert.fail("Not found in expected: " + e + ";\nexpected (" + expected.size() + "): " + expected + "\nactual ("
                                + actual.size() + "): " + actual);
                    }
                }

                if (expected.size() != actual.size()) {
                    Assert.fail(
                            "Size does not match: " + expected.size() + " vs " + actual.size() + ";\nexpected: " + expected + "\nactual: " + actual);
                }
            }

            private static <E> void assertEquals(Set<E> expected, BackingCollections.IndexedUnmodifiableSet<E> actual) {
                for (E e : expected) {
                    if (!actual.contains(e)) {
                        Assert.fail("Not found in actual: " + e + ";\nexpected (" + expected.size() + "): " + expected + "\nactual (" + actual.size()
                                + "): " + actual);
                    }
                }

                for (E e : actual) {
                    if (!expected.contains(e)) {
                        Assert.fail("Not found in expected: " + e + ";\nexpected (" + expected.size() + "): " + expected + "\nactual ("
                                + actual.size() + "): " + actual);
                    }
                }

                if (expected.size() != actual.size()) {
                    Assert.fail(
                            "Size does not match: " + expected.size() + " vs " + actual.size() + ";\nexpected: " + expected + "\nactual: " + actual);
                }
            }

        }

        @RunWith(Parameterized.class)
        public static class ImmutableSetRandomizedTestSmall {
            @Parameter
            public Integer seed;

            @Parameters(name = "{0}")
            public static Collection<Integer> seeds() {
                ArrayList<Integer> result = new ArrayList<>(1000);

                for (int i = 100; i < 1000; i++) {
                    result.add(i);
                }

                return result;
            }

            @Test
            public void toArray() {
                Random random = new Random(seed);

                int initialCount = initialCount(random);

                List<String> initialContent = new ArrayList<>();

                for (int i = 0; i < initialCount; i++) {
                    initialContent.add(randomString(random));
                }

                HashSet<String> reference = new HashSet<>(initialContent);
                BackingCollections.IndexedUnmodifiableSet<String> subject = BackingCollections.IndexedUnmodifiableSet.of(reference);

                assertEquals(reference, subject);

                Object[] subjectArray = subject.toArray();
                Assert.assertEquals(reference,
                        new HashSet<String>(Arrays.asList(subjectArray).stream().map((e) -> e.toString()).collect(Collectors.toSet())));

                String[] subjectStringArray = subject.toArray(new String[0]);
                Assert.assertEquals(reference, new HashSet<String>(Arrays.asList(subjectStringArray)));

                String[] subjectStringArray2 = subject.toArray(new String[subject.size()]);
                Assert.assertEquals(reference, new HashSet<String>(Arrays.asList(subjectStringArray2)));
            }

            @Test
            public void hashCodeEquals() {
                Random random = new Random(seed);

                int initialCount = initialCount(random);

                List<String> initialContent = new ArrayList<>();

                for (int i = 0; i < initialCount; i++) {
                    initialContent.add(randomString(random));
                }

                HashSet<String> reference = new HashSet<>(initialContent);
                BackingCollections.IndexedUnmodifiableSet<String> subject = BackingCollections.IndexedUnmodifiableSet.of(reference);

                Assert.assertEquals(reference, subject);
                Assert.assertEquals(reference.hashCode(), subject.hashCode());
            }

            private static int initialCount(Random random) {
                float f = random.nextFloat();

                if (f < 0.3) {
                    return random.nextInt(10);
                } else if (f < 0.7) {
                    return random.nextInt(40);
                } else {
                    return random.nextInt(300);
                }
            }

            private static <E> void assertEquals(Set<E> expected, BackingCollections.IndexedUnmodifiableSet<E> actual) {

                for (E e : expected) {

                    if (!actual.contains(e)) {
                        Assert.fail("Not found in actual: " + e + ";\nexpected (" + expected.size() + "): " + expected + "\nactual (" + actual.size()
                                + "): " + actual);
                    }
                }

                for (E e : actual) {
                    if (!expected.contains(e)) {
                        Assert.fail("Not found in expected: " + e + ";\nexpected (" + expected.size() + "): " + expected + "\nactual ("
                                + actual.size() + "): " + actual);
                    }
                }

                if (expected.size() != actual.size()) {
                    Assert.fail(
                            "Size does not match: " + expected.size() + " vs " + actual.size() + ";\nexpected: " + expected + "\nactual: " + actual);
                }
            }
        }

        static String[] ipAddresses = createRandomIpAddresses(new Random(9));
        static String[] locationNames = createRandomLocationNames(new Random(2));

        private static String randomString(Random random) {
            if (random.nextFloat() < 0.5) {
                return randomIpAddress(random);
            } else {
                return randomLocationName(random);
            }
        }

        private static String randomIpAddress(Random random) {
            return ipAddresses[random.nextInt(ipAddresses.length)];
        }

        private static String randomLocationName(Random random) {
            int i = (int) Math.floor(random.nextGaussian() * locationNames.length * 0.333 + locationNames.length);

            if (i < 0 || i >= locationNames.length) {
                i = random.nextInt(locationNames.length);
            }

            return locationNames[i];
        }

        private static String[] createRandomIpAddresses(Random random) {
            String[] result = new String[2000];

            for (int i = 0; i < result.length; i++) {
                result[i] = (random.nextInt(10) + 100) + "." + (random.nextInt(5) + 100) + "." + random.nextInt(255) + "." + random.nextInt(255);
            }

            return result;
        }

        private static String[] createRandomLocationNames(Random random) {
            String[] p1 = new String[] { "Schön", "Schöner", "Tempel", "Friedens", "Friedrichs", "Blanken", "Rosen", "Charlotten", "Malch", "Lichten",
                    "Lichter", "Hasel", "Kreuz", "Pank", "Marien", "Adlers", "Zehlen", "Haken", "Witten", "Jungfern", "Hellers", "Finster", "Birken",
                    "Falken", "Freders", "Karls", "Grün", "Wilmers", "Heiners", "Lieben", "Marien", "Wiesen", "Biesen", "Schmachten", "Rahns",
                    "Rangs", "Herms", "Rüders", "Wuster", "Hoppe", "Waidmanns", "Wolters", "Schmargen" };
            String[] p2 = new String[] { "au", "ow", "berg", "feld", "felde", "tal", "thal", "höhe", "burg", "horst", "hausen", "dorf", "hof",
                    "heide", "weide", "hain", "walde", "linde", "hagen", "eiche", "witz", "rade", "werder", "see", "fließ", "krug", "mark", "lust" };

            ArrayList<String> result = new ArrayList<>(p1.length * p2.length);

            for (int i = 0; i < p1.length; i++) {
                for (int k = 0; k < p2.length; k++) {
                    result.add(p1[i] + p2[k]);
                }
            }

            Collections.shuffle(result, random);

            return result.toArray(new String[result.size()]);
        }

    }

}
