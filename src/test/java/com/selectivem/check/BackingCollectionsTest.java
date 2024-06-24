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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({BackingCollectionsTest.IndexedImmutableSet.RandomizedTestBig.class,
        BackingCollectionsTest.IndexedImmutableSet.ImmutableSetRandomizedTestSmall.class, BackingCollectionsTest.IndexedImmutableSet.ImmutableSetTest.class,
BackingCollectionsTest.IndexedImmutableSet.ImmutableSetParameterizedTest.class})
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

                int size = random.nextInt(100) + 4;

                for (int k = 0; k < size; k++) {
                    String string = randomString(random);

                    reference.add(string);
                    subject = subject.with(string);
                }

                assertEquals(reference, subject);

                int insertionCount = random.nextInt(30) + 1;

                for (int k = 0; k < insertionCount; k++) {
                    String string = randomString(random);

                    reference.add(string);
                    subject = subject.with(string);
                    assertEquals(reference, subject);
                }

                insertionCount = random.nextInt(30) + 1;

                for (int k = 0; k < insertionCount; k++) {
                    String string = randomString(random);

                    reference.add(string);
                    subject = subject.with(string);
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

                int size = random.nextInt(200) + 4;
                if (random.nextFloat() < 0.1) {
                    size += random.nextInt(800);
                }

                for (int k = 0; k < size; k++) {
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

                int size = random.nextInt(200) + 4;
                if (random.nextFloat() < 0.1) {
                    size += random.nextInt(800);
                }

                for (int k = 0; k < size; k++) {
                    String string = randomString(random);
                    reference.add(string);
                }

                BackingCollections.IndexedUnmodifiableSet<String> subject = BackingCollections.IndexedUnmodifiableSet.of(reference);

                assertEquals(reference, subject);
                Assert.assertEquals(reference.size(), subject.size());
                Set<String> subjectCopy = new HashSet<>(subject.size());

                for (String e : subject) {
                    subjectCopy.add(e);
                }

                Assert.assertEquals(reference, subjectCopy);
            }

            @Test
            public void elementToIndex() {
                Random random = new Random(seed + 2);

                LinkedHashMap<String, Integer> reference = new LinkedHashMap<>();
                List<String> referenceList = new ArrayList<>();

                int size = random.nextInt(200) + 4;
                if (random.nextFloat() < 0.1) {
                    size += random.nextInt(800);
                }

                for (int k = 0; k < size; k++) {
                    String string = randomString(random);

                    if (!reference.containsKey(string)) {
                        reference.put(string, reference.size());
                        referenceList.add(string);
                    }
                }

                BackingCollections.IndexedUnmodifiableSet<String> subject = BackingCollections.IndexedUnmodifiableSet.of(reference.keySet());

                assertEquals(reference.keySet(), subject);

                List<String> shuffledReferenceList = new ArrayList<>(referenceList);
                Collections.shuffle(shuffledReferenceList, random);

                for (int k = 0; k < reference.size(); k++) {
                    String string1 = referenceList.get(k);
                    int index = subject.elementToIndex(string1);
                    Assert.assertEquals(reference.get(string1).intValue(), index);
                    String reverseString = subject.indexToElement(index);
                    Assert.assertEquals(string1, reverseString);

                    String string2 = string1 + "X";
                    if (!reference.containsKey(string2)) {
                        Assert.assertEquals(-1, subject.elementToIndex(string2));
                    }
                }
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
                        new HashSet<>(Arrays.asList(subjectArray).stream().map((e) -> e.toString()).collect(Collectors.toSet())));

                String[] subjectStringArray = subject.toArray(new String[0]);
                Assert.assertEquals(reference, new HashSet<>(Arrays.asList(subjectStringArray)));

                String[] subjectStringArray2 = subject.toArray(new String[subject.size()]);
                Assert.assertEquals(reference, new HashSet<>(Arrays.asList(subjectStringArray2)));
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

        public static class ImmutableSetTest {
            @Test
            public void builder_toString() {
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> builder = BackingCollections.IndexedUnmodifiableSet.builder(10);
                builder = builder.with("a").with("b").with("c");
                Assert.assertEquals("[a, b, c]", builder.toString());
            }

            @Test(expected = IllegalArgumentException.class)
            public void builder_nullElement() {
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> builder = BackingCollections.IndexedUnmodifiableSet.builder(10);
                builder = builder.with(null);
            }

            @Test
            public void builder_contains_positive() {
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> builder = BackingCollections.IndexedUnmodifiableSet.builder(10);
                builder = builder.with("a").with("b").with("c");
                Assert.assertTrue(builder.contains("a"));
            }

            @Test
            public void builder_contains_negative() {
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> builder = BackingCollections.IndexedUnmodifiableSet.builder(10);
                Assert.assertFalse(builder.contains("xyz"));
                builder = builder.with("a").with("b").with("c");
                Assert.assertFalse(builder.contains("xyz"));
            }

            @Test
            public void builder_iterator_empty() {
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> builder = BackingCollections.IndexedUnmodifiableSet.builder(10);
                Assert.assertFalse(builder.iterator().hasNext());
            }


            @Test(expected = NoSuchElementException.class)
            public void builder_iterator_exhausted() {
                BackingCollections.IndexedUnmodifiableSet.InternalBuilder<String> builder = BackingCollections.IndexedUnmodifiableSet.<String>builder(10).with("a");
                Iterator<String> iter = builder.iterator();

                while (iter.hasNext()) {
                    iter.next();
                }

                iter.next();
            }

        }

        @RunWith(Parameterized.class)
        public static class ImmutableSetParameterizedTest {
            final BackingCollections.IndexedUnmodifiableSet<String> subject;
            final Set<String> reference;

            @Test
            public void equals() {
                Assert.assertEquals(reference, subject);
            }

            @Test
            public void isEmpty() {
                Assert.assertEquals(reference.isEmpty(), subject.isEmpty());
            }

            @Test
            public void containsAll_positive() {
                Assert.assertTrue(subject.containsAll(reference));
            }

            @Test
            public void containsAll_negative() {
                HashSet<String> more = new HashSet<>(reference);
                more.add("xyz");
                Assert.assertFalse(subject.containsAll(more));
            }

            @Test
            public void elementToIndex_notExists() {
                Assert.assertEquals(-1, subject.elementToIndex("xyz"));
            }

            @Test
            public void indexToElement_notExists() {
                Assert.assertNull(subject.indexToElement(99999));
            }

            @Test
            public void toArray() {
                Assert.assertArrayEquals(reference.toArray(), subject.toArray());
            }

            @Test
            public void toArray_param() {
                Assert.assertArrayEquals(reference.toArray(new String[0]), subject.toArray(new String[0]));
            }

            @Test(expected = UnsupportedOperationException.class)
            public void add() {
                subject.add("x");
            }

            @Test(expected = UnsupportedOperationException.class)
            public void addAll() {
                subject.addAll(Arrays.asList("x"));
            }

            @Test(expected = UnsupportedOperationException.class)
            public void clear() {
                subject.clear();
            }

            @Test(expected = UnsupportedOperationException.class)
            public void remove() {
                subject.remove("x");
            }

            @Test(expected = UnsupportedOperationException.class)
            public void removeAll() {
                subject.removeAll(Arrays.asList("x"));
            }

            @Test(expected = UnsupportedOperationException.class)
            public void retainAll() {
                subject.retainAll(Arrays.asList("x"));
            }

            @Test(expected = NoSuchElementException.class)
            public void iterator_exhausted() {
                Iterator<String> iter = subject.iterator();

                while (iter.hasNext()) {
                    iter.next();
                }

                iter.next();
            }

            public ImmutableSetParameterizedTest(Set<String> reference, BackingCollections.IndexedUnmodifiableSet<String> subject) {
                this.subject = subject;
                this.reference = reference;
            }

            @Parameterized.Parameters(name = "{0}")
            public static Collection<Object[]> params() {
                ArrayList<Object[]> result = new ArrayList<>();

                result.add(new Object [] {new HashSet<>(), BackingCollections.IndexedUnmodifiableSet.empty()});
                result.add(new Object [] {new HashSet<>(), BackingCollections.IndexedUnmodifiableSet.builder(10).build()});
                result.add(new Object [] {new HashSet<>(Arrays.asList("a")), BackingCollections.IndexedUnmodifiableSet.of("a")});
                result.add(new Object [] {new HashSet<>(Arrays.asList("a", "b")), BackingCollections.IndexedUnmodifiableSet.of("a", "b")});
                result.add(new Object [] {new HashSet<>(Arrays.asList("a", "b", "c")), BackingCollections.IndexedUnmodifiableSet.of(new HashSet<>(Arrays.asList("a", "b", "c")))});
                result.add(new Object [] {new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")), BackingCollections.IndexedUnmodifiableSet.of(new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")))});
                result.add(new Object [] {IntStream.rangeClosed(1, 1000).mapToObj(Integer::toString).collect(Collectors.toSet()), BackingCollections.IndexedUnmodifiableSet.of(IntStream.rangeClosed(1, 1000).mapToObj(Integer::toString).collect(Collectors.toSet()))});

                return result;
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
            String[] p1 = new String[]{"Schön", "Schöner", "Tempel", "Friedens", "Friedrichs", "Blanken", "Rosen", "Charlotten", "Malch", "Lichten",
                    "Lichter", "Hasel", "Kreuz", "Pank", "Marien", "Adlers", "Zehlen", "Haken", "Witten", "Jungfern", "Hellers", "Finster", "Birken",
                    "Falken", "Freders", "Karls", "Grün", "Wilmers", "Heiners", "Lieben", "Marien", "Wiesen", "Biesen", "Schmachten", "Rahns",
                    "Rangs", "Herms", "Rüders", "Wuster", "Hoppe", "Waidmanns", "Wolters", "Schmargen"};
            String[] p2 = new String[]{"au", "ow", "berg", "feld", "felde", "tal", "thal", "höhe", "burg", "horst", "hausen", "dorf", "hof",
                    "heide", "weide", "hain", "walde", "linde", "hagen", "eiche", "witz", "rade", "werder", "see", "fließ", "krug", "mark", "lust"};

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
