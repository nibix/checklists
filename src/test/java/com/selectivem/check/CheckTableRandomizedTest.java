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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CheckTableRandomizedTest {
    public Integer seed;

    int rowCount;
    int columnCount;

    @Parameters(name = "{0}; {1}x{2}")
    public static Collection<Object[]> seeds() {
        ArrayList<Object[]> result = new ArrayList<>();
        Random random = new Random(1);

        for (int i = 101; i < 111; i++) {
            result.add(new Object[] { i, 1, 1 });
        }

        for (int i = 101; i < 111; i++) {
            result.add(new Object[] { i, 1, 2 });
        }

        for (int i = 101; i < 111; i++) {
            result.add(new Object[] { i, 2, 1 });
        }

        for (int i = 101; i < 111; i++) {
            result.add(new Object[] { i, 2, 2 });
        }

        for (int i = 111; i < 150; i++) {
            result.add(new Object[] { i, randomSize(random), randomSize(random) });
        }

        return result;
    }

    @Test
    public void basicTest() {
        Random random = new Random(seed);
        Set<Integer> rows = createIntegerElements(random, rowCount);
        Set<String> columns = createStringElements(random, columnCount);

        List<Integer> rowsList = new ArrayList<>(rows);
        Collections.shuffle(rowsList, random);
        List<String> columnsList = new ArrayList<>(columns);
        Collections.shuffle(columnsList, random);

        CheckTable<Integer, String> subject = CheckTable.create(rows, columns);

        Map<String, Set<Integer>> referenceCR = new HashMap<>();
        Map<Integer, Set<String>> referenceRC = new HashMap<>();

        int checkCount = 0;
        int totalCount = rows.size() * columns.size();

        for (int i = 0; i < Math.min(100, rowsList.size()); i++) {
            Integer row = rowsList.get(i);

            for (int k = 0; k < Math.min(100, columnsList.size()); k++) {
                String column = columnsList.get(k);

                boolean checkResult = subject.check(row, column);
                checkCount++;
                boolean checkResultReference = totalCount == checkCount;
                referenceCR.computeIfAbsent(column, (key) -> new HashSet<>()).add(row);
                referenceRC.computeIfAbsent(row, (key) -> new HashSet<>()).add(column);

                Assert.assertEquals(subject + " vs " + referenceCR, checkResultReference, checkResult);

                Assert.assertEquals(referenceCR.entrySet().stream().filter(entry -> entry.getValue().size() == rows.size())
                        .map(entry -> entry.getKey()).collect(Collectors.toSet()), subject.getCompleteColumns());

                Assert.assertEquals(referenceRC.entrySet().stream().filter(entry -> entry.getValue().size() == columns.size())
                        .map(entry -> entry.getKey()).collect(Collectors.toSet()), subject.getCompleteRows());

                Set<String> checkedColumns = new HashSet<>();
                subject.iterateCheckedColumns(row).forEach((c) -> checkedColumns.add(c));
                Assert.assertEquals(referenceRC.get(row), checkedColumns);

                Set<Integer> checkedRows = new HashSet<>();
                subject.iterateCheckedRows(column).forEach((r) -> checkedRows.add(r));
                Assert.assertEquals(referenceCR.get(column), checkedRows);

                Assert.assertTrue(subject.getIncompleteRows().toString(), !containsAny(subject.getIncompleteRows(), subject.getCompleteRows()));
                Assert.assertTrue(subject.getIncompleteColumns().toString(),
                        !containsAny(subject.getIncompleteColumns(), subject.getCompleteColumns()));

                Assert.assertEquals(referenceRC.get(row), subject.getCheckedColumns(row));
                Assert.assertEquals(referenceCR.get(column), subject.getCheckedRows(column));

                Set<String> uncheckedColumns = new HashSet<>();
                subject.iterateUncheckedColumns(row).forEach((c) -> uncheckedColumns.add(c));
                Assert.assertEquals(referenceRC.get(row), without(columns, uncheckedColumns));

                Set<Integer> uncheckedRows = new HashSet<>();
                subject.iterateUncheckedRows(column).forEach((r) -> uncheckedRows.add(r));
                Assert.assertEquals(uncheckedRows.toString(), referenceCR.get(column), without(rows, uncheckedRows));

                for (String c : checkedColumns) {
                    Assert.assertTrue(subject.isChecked(row, c));
                }

                for (Integer r : checkedRows) {
                    Assert.assertTrue(subject.isChecked(r, column));
                }
                
                boolean expectedColumnComplete = referenceCR.get(column).equals(rows);
                Assert.assertEquals(expectedColumnComplete, subject.isColumnComplete(column));              
            }
            
            boolean expectedRowComplete = referenceRC.get(row).equals(columns);
            Assert.assertEquals(expectedRowComplete, subject.isRowComplete(row));
        }

        int toUncheck = random.nextInt(checkCount);

        for (int i = 0; i < toUncheck; i++) {
            Integer rowToUncheck = rowsList.get(random.nextInt(rowsList.size()));
            List<String> checkedColumnsList = new ArrayList<String>();
            subject.iterateCheckedColumns(rowToUncheck).forEach(checkedColumnsList::add);

            if (checkedColumnsList.size() == 0) {
                continue;
            }

            String columnToUncheck = checkedColumnsList.get(random.nextInt(checkedColumnsList.size()));

            subject.uncheck(rowToUncheck, columnToUncheck);
            referenceCR.get(columnToUncheck).remove(rowToUncheck);
            referenceRC.get(rowToUncheck).remove(columnToUncheck);
            checkCount--;

            Set<String> checkedColumns = new HashSet<>();
            subject.iterateCheckedColumns(rowToUncheck).forEach((c) -> checkedColumns.add(c));
            Assert.assertEquals(referenceRC.get(rowToUncheck), checkedColumns);

            Set<Integer> checkedRows = new HashSet<>();
            subject.iterateCheckedRows(columnToUncheck).forEach((r) -> checkedRows.add(r));
            Assert.assertEquals(referenceCR.get(columnToUncheck), checkedRows);

            Assert.assertTrue(subject.getIncompleteRows().toString(), !containsAny(subject.getIncompleteRows(), subject.getCompleteRows()));
            Assert.assertTrue(subject.getIncompleteColumns().toString(), !containsAny(subject.getIncompleteColumns(), subject.getCompleteColumns()));

            Set<String> uncheckedColumns = new HashSet<>();
            subject.iterateUncheckedColumns(rowToUncheck).forEach((c) -> uncheckedColumns.add(c));
            Assert.assertEquals(referenceRC.get(rowToUncheck), without(columns, uncheckedColumns));

            Set<Integer> uncheckedRows = new HashSet<>();
            subject.iterateUncheckedRows(columnToUncheck).forEach((r) -> uncheckedRows.add(r));
            Assert.assertEquals(referenceCR.get(columnToUncheck), without(rows, uncheckedRows));

        }

        Collections.shuffle(columnsList, random);

        for (int i = 0; i < Math.min(50, columnsList.size()); i++) {
            String column = columnsList.get(i);

            Predicate<Integer> predicate = (r) -> r < 5000;
            Set<Integer> matchedRows = without(rows, referenceCR.computeIfAbsent(column, (key) -> new HashSet<>())).stream().filter(predicate)
                    .collect(Collectors.toSet());
            boolean complete = subject.checkIf(predicate, column);

            checkCount += matchedRows.size();
            Assert.assertEquals(checkCount == totalCount, complete);
            Assert.assertEquals(checkCount == totalCount, subject.isComplete());

            referenceCR.get(column).addAll(matchedRows);
            matchedRows.forEach((r) -> referenceRC.computeIfAbsent(r, (key) -> new HashSet<>()).add(column));

            Assert.assertEquals(referenceCR.get(column), subject.getCheckedRows(column));
        }

        Collections.shuffle(rowsList, random);

        for (int i = 0; i < Math.min(50, rowsList.size()); i++) {
            Integer row = rowsList.get(i);

            Predicate<String> predicate = (c) -> c.hashCode() < 0;
            Set<String> matchedColumns = without(columns, referenceRC.computeIfAbsent(row, (key) -> new HashSet<>())).stream().filter(predicate)
                    .collect(Collectors.toSet());
            boolean complete = subject.checkIf(row, predicate);

            checkCount += matchedColumns.size();
            Assert.assertEquals(checkCount == totalCount, complete);
            Assert.assertEquals(checkCount == totalCount, subject.isComplete());

            referenceRC.get(row).addAll(matchedColumns);
            matchedColumns.forEach((c) -> referenceCR.computeIfAbsent(c, (key) -> new HashSet<>()).add(row));

            Assert.assertEquals(referenceRC.get(row), subject.getCheckedColumns(row));
        }

        Collections.shuffle(columnsList, random);

        for (int i = 0; i < Math.min(50, columnsList.size()); i++) {
            String column = columnsList.get(i);

            Predicate<Integer> predicate = (r) -> r < 7000;
            Set<Integer> matchedRows = referenceCR.computeIfAbsent(column, (key) -> new HashSet<>()).stream().filter(predicate)
                    .collect(Collectors.toSet());
            subject.uncheckIf(predicate, column);

            checkCount -= matchedRows.size();
            referenceCR.get(column).removeAll(matchedRows);
            matchedRows.forEach((r) -> referenceRC.get(r).remove(column));

            Assert.assertEquals(referenceCR.get(column), subject.getCheckedRows(column));
        }

        Collections.shuffle(rowsList, random);

        for (int i = 0; i < Math.min(50, rowsList.size()); i++) {
            Integer row = rowsList.get(i);

            Predicate<String> predicate = (c) -> c.hashCode() < 1000;
            Set<String> matchedColumns = referenceRC.computeIfAbsent(row, (key) -> new HashSet<>()).stream().filter(predicate)
                    .collect(Collectors.toSet());
            subject.uncheckIf(row, predicate);

            checkCount -= matchedColumns.size();
            referenceRC.get(row).removeAll(matchedColumns);
            matchedColumns.forEach((c) -> referenceCR.get(c).remove(row));

            Assert.assertEquals(referenceRC.get(row), subject.getCheckedColumns(row));
        }
    }

    public CheckTableRandomizedTest(Integer seed, int rowCount, int columnCount) {
        this.seed = seed;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    private static Set<String> createStringElements(Random random, int size) {

        Set<String> result = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            result.add("e_" + i + "_" + random.nextInt(10));
        }

        return result;
    }

    private static Set<Integer> createIntegerElements(Random random, int size) {

        Set<Integer> result = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            result.add(random.nextInt(10000));
        }

        return result;
    }

    private static int randomSize(Random random) {
        float f = random.nextFloat();

        if (f < 0.02) {
            return 1;
        } else if (f < 0.1) {
            return 2;
        } else if (f < 0.2) {
            return random.nextInt(10) + 1;
        } else if (f < 0.7) {
            return random.nextInt(20) + 11;
        } else if (f < 0.95) {
            return random.nextInt(200) + 21;
        } else {
            return random.nextInt(400) + 201;
        }
    }

    static <E> boolean containsAny(Set<E> a, Set<E> b) {
        for (E be : b) {
            if (a.contains(be)) {
                return true;
            }
        }

        return false;
    }

    static <E> Set<E> without(Set<E> set, Set<E> without) {
        HashSet<E> result = new HashSet<E>(set);
        result.removeAll(without);
        return result;
    }

}
