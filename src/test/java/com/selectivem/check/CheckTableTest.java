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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CheckTableTest {

    final Set<Integer> rows;
    final Set<String> columns;
    final CheckTable<Integer, String> subject;
    final int count;
    final Set<Integer> someRows;
    final Set<String> someColumns;

    @Test
    public void check() {
        int checkedCount = 0;

        for (Integer row : rows) {
            for (String column : columns) {
                checkedCount++;

                subject.check(row, column);

                Assert.assertTrue(subject.isChecked(row, column));

                if (checkedCount != count) {
                    Assert.assertFalse(subject.isComplete());
                }
            }
        }

        Assert.assertTrue(subject.isComplete());
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_illegalArgument_row() {
        subject.check(99, "a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_illegalArgument_column() {
        subject.check(1, "abc");
    }

    @Test
    public void uncheck() {
        subject.checkIf(rows, (i) -> true);
        int checkedCount = count;

        for (Integer row : rows) {
            for (String column : columns) {
                checkedCount--;

                subject.uncheck(row, column);

                Assert.assertFalse(subject.isChecked(row, column));

                if (checkedCount != 0) {
                    Assert.assertFalse(subject.isBlank());
                }
            }
        }

        Assert.assertTrue(subject.isBlank());
    }

    @Test(expected = IllegalArgumentException.class)
    public void uncheck_illegalArgument_row() {
        subject.uncheck(99, "a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void uncheck_illegalArgument_column() {
        subject.uncheck(1, "abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isChecked_illegalArgument_row() {
        subject.isChecked(99, "a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isChecked_illegalArgument_column() {
        subject.isChecked(1, "abc");
    }

    @Test
    public void uncheckAll() {
        subject.checkIf(rows, (i) -> true);
        Assert.assertTrue(subject.isComplete());
        subject.uncheckAll();
        Assert.assertTrue(subject.isBlank());
    }

    @Test
    public void checkIf_row() {
        subject.checkIf((i) -> i == 1, "a");

        for (Integer row : rows) {
            Assert.assertEquals(row == 1, subject.isChecked(row, "a"));
        }
    }

    @Test
    public void checkIf_column() {
        subject.checkIf(1, (i) -> i.equals("a"));

        for (String column : columns) {
            Assert.assertEquals(column.equals("a"), subject.isChecked(1, column));
        }
    }

    @Test
    public void uncheckIf_row() {
        subject.checkIf(rows, (i) -> true);
        subject.uncheckIf((i) -> i == 1, "a");

        for (Integer row : rows) {
            Assert.assertEquals(!(row == 1), subject.isChecked(row, "a"));
        }
    }

    @Test
    public void uncheckIf_column() {
        subject.checkIf(rows, (i) -> true);
        subject.uncheckIf(1, (i) -> i.equals("a"));

        for (String column : columns) {
            Assert.assertEquals(!(column.equals("a")), subject.isChecked(1, column));
        }
    }

    @Test
    public void uncheckIf_iterable_row() {
        subject.checkIf(rows, (i) -> true);
        subject.uncheckIf((i) -> i == 1, someColumns);

        for (String column : columns) {
            for (Integer row : rows) {
                Assert.assertEquals(!(row == 1 && someColumns.contains(column)), subject.isChecked(row, column));
            }
        }
    }

    @Test
    public void uncheckIf_iterable_column() {
        subject.checkIf(rows, (i) -> true);
        subject.uncheckIf(someRows, (i) -> i.equals("a"));

        for (String column : columns) {
            for (Integer row : rows) {
                Assert.assertEquals(!(column.equals("a") && someRows.contains(row)), subject.isChecked(row, column));
            }
        }
    }

    @Test
    public void uncheckRowIf() {
        subject.checkIf(rows, (i) -> true);
        subject.uncheckRowIf((i) -> i == 1);

        for (String column : columns) {
            for (Integer row : rows) {
                Assert.assertEquals(row != 1, subject.isChecked(row, column));
            }
        }
    }

    @Test
    public void uncheckRow() {
        subject.checkIf(rows, (i) -> true);

        for (Integer row : rows) {
            subject.uncheckRow(row);
            Assert.assertFalse(subject.isChecked(row, "a"));
        }
    }

    @Test
    public void uncheckRowIfPresent() {
        subject.checkIf(rows, (i) -> true);

        for (Integer row : rows) {
            subject.uncheckRowIfPresent(row);
            Assert.assertFalse(subject.isChecked(row, "a"));
        }
    }

    @Test
    public void iterateUncheckedRows() {
        Set<Integer> uncheckedRows = new HashSet<>(rows);

        {
            Set<Integer> returned = new HashSet<>();
            subject.iterateUncheckedRows("a").forEach(returned::add);
            Assert.assertEquals(uncheckedRows, returned);
        }

        for (Integer row : rows) {
            subject.check(row, "a");
            uncheckedRows.remove(row);
            Set<Integer> returned = new HashSet<>();
            subject.iterateUncheckedRows("a").forEach(returned::add);
            Assert.assertEquals(uncheckedRows, returned);
        }
    }

    @Test
    public void iterateUncheckedColumns() {
        Set<String> uncheckedColumns = new HashSet<>(columns);

        {
            Set<String> returned = new HashSet<>();
            subject.iterateUncheckedColumns(1).forEach(returned::add);
            Assert.assertEquals(uncheckedColumns, returned);
        }

        for (String column : columns) {
            subject.check(1, column);
            uncheckedColumns.remove(column);
            Set<String> returned = new HashSet<>();
            subject.iterateUncheckedColumns(1).forEach(returned::add);
            Assert.assertEquals(uncheckedColumns, returned);
        }
    }

    @Test
    public void iterateCheckedRows() {
        Set<Integer> checkedRows = new HashSet<>();

        {
            Set<Integer> returned = new HashSet<>();
            subject.iterateCheckedRows("a").forEach(returned::add);
            Assert.assertEquals(checkedRows, returned);
        }

        for (Integer row : rows) {
            subject.check(row, "a");
            checkedRows.add(row);
            Set<Integer> returned = new HashSet<>();
            subject.iterateCheckedRows("a").forEach(returned::add);
            Assert.assertEquals(checkedRows, returned);
        }
    }

    @Test
    public void iterateCheckedColumns() {
        Set<String> checkedColumns = new HashSet<>();

        {
            Set<String> returned = new HashSet<>();
            subject.iterateCheckedColumns(1).forEach(returned::add);
            Assert.assertEquals(checkedColumns, returned);
        }

        for (String column : columns) {
            subject.check(1, column);
            checkedColumns.add(column);
            Set<String> returned = new HashSet<>();
            subject.iterateCheckedColumns(1).forEach(returned::add);
            Assert.assertEquals(checkedColumns, returned);
        }
    }

    @Test
    public void getRows() {
        Assert.assertEquals(rows, subject.getRows());
    }

    @Test
    public void getColumns() {
        Assert.assertEquals(columns, subject.getColumns());
    }

    @Test
    public void containsCellFor_positive() {
        Assert.assertTrue(subject.containsCellFor(1, "a"));
    }

    @Test
    public void containsCellFor_negative() {
        Assert.assertFalse(subject.containsCellFor(1, "xyz"));
    }

    @Test
    public void toStringTest() {
        subject.check(1, "a");
        String result = subject.toString();

        if (rows.size() == 1 && columns.size() == 1) {
            Assert.assertEquals("1/a: x", result);
        } else if (rows.size() == 1 && columns.size() == 2) {
            Assert.assertEquals(//
                    "  | a | b |\n" +//
                            "1 | x |   |", //
                    result);
        } else if (rows.size() == 2 && columns.size() == 1) {
            Assert.assertEquals(//
                    " | a |\n" +//
                            "1| x |\n" +//
                            "2|   |\n", //
                    result);
        } else if (rows.size() == 2 && columns.size() == 2) {
            Assert.assertEquals(//
                    " | a | b |\n" + //
                            "1| x |   |\n" +//
                            "2|   |   |\n", //
                    result);
        }
    }

    @Test
    public void toTableStringTest() {
        subject.check(1, "a");
        String result = subject.toTableString("*", ".");

        if (rows.size() == 1 && columns.size() == 1) {
            Assert.assertEquals(//
                    "  | a |\n" +//
                            "1 | * |", //
                    result);
        } else if (rows.size() == 1 && columns.size() == 2) {
            Assert.assertEquals(//
                    "  | a | b |\n" +//
                            "1 | * | . |", //
                    result);
        } else if (rows.size() == 2 && columns.size() == 1) {
            Assert.assertEquals(//
                    " | a |\n" +//
                            "1| * |\n" +//
                            "2| . |\n", //
                    result);
        } else if (rows.size() == 2 && columns.size() == 2) {
            Assert.assertEquals(//
                    " | a | b |\n" + //
                            "1| * | . |\n" +//
                            "2| . | . |\n", //
                    result);
        }
    }

    @Parameterized.Parameters(name = "{0} / {1}")
    public static Collection<Object[]> params() {
        ArrayList<Object[]> result = new ArrayList<>();

        for (Set<String> columns : Arrays.asList(setOf("a"), setOf("a", "b"), setOf("a", "b", "c", "d"))) {
            for (Set<Integer> rows : Arrays.asList(setOf(1), setOf(1, 2), setOf(1, 2, 3, 4))) {
                result.add(new Object[]{columns, rows});
            }
        }

        return result;
    }

    public CheckTableTest(Set<String> columns, Set<Integer> rows) {
        this.columns = columns;
        this.rows = rows;
        this.subject = CheckTable.create(rows, columns);
        this.count = columns.size() * rows.size();
        this.someColumns = columns.size() == 1 ? columns : minusOne(columns);
        this.someRows = rows.size() == 1 ? rows : minusOne(rows);
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

}
