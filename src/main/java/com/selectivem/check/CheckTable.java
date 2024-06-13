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

import java.util.Set;
import java.util.function.Predicate;

/**
 * Specialized data structure which models a two-dimensional matrix. Each cell of the matrix can be marked as checked or unchecked.
 * Initially, all cells will be unchecked. The matrix is defined by supplying a set of row elements and a set of column elements. Each
 * cell can be addressed by a row element and a column element.
 * 
 * You can use the check() and related methods to mark one or more cells as checked. You can use the isComplete() method to check whether
 * all cells are checked. There are additional methods, which allow you to retrieve the checked and unchecked cells.
 *
 * The implementation aims at runtime CPU efficiency. Checking/unchecking single cells is possible in constant (O(1)) time.
 * 
 * The implementation is not thread safe. Objects of this class are not meant to be shared between threads.
 * 
 * The data structure does not support null rows or null columns.  The element class must implement hashCode() and equals().
 * 
 * @author Nils Bandener 
 */
public interface CheckTable<R, C> {

    /**
     * Creates a new check table with the given row and columns. All cells will be initially marked as unchecked.
     */
    static <R, C> CheckTable<R, C> create(R row, Set<C> columns) {
        return CheckTableImpl.create(row, columns);
    }

    /**
     * Creates a new check table with the given rows and column. All cells will be initially marked as unchecked.
     */
    static <R, C> CheckTable<R, C> create(Set<R> rows, C column) {
        return CheckTableImpl.create(rows, column);
    }

    /**
     * Creates a new check table with the given rows and columns. All cells will be initially marked as unchecked.
     */
    static <R, C> CheckTable<R, C> create(Set<R> rows, Set<C> columns) {
        return CheckTableImpl.create(rows, columns);
    }

    /**
     * Marks the cell in the given row and column as checked. If that cell is already checked, this will be a no-op.
     * 
     * @param row The row of the cell to be checked.
     * @param column The column of the cell to be checked.
     * @return Returns true, if the check table is complete. Returns false, if the check table is not yet complete.
     * @throws IllegalArgumentException If the supplied row or column is not known by this instance.
     */
    boolean check(R row, C column);

    /**
     * Iterates through all unchecked cells of the given row and applies the given columnCheckPredicate. 
     * If the predicate returns true, the cell will be marked as checked.
     * 
     * @param row The row to be considered.
     * @param columnCheckPredicate The predicate to be applied to the unchecked cells of the row. 
     * @return Returns true, if the check table is complete. Returns false, if the check table is not yet complete.
     * @throws IllegalArgumentException If the supplied row is not known by this instance.
     */
    boolean checkIf(R row, Predicate<C> columnCheckPredicate);

    /**
     * Iterates through all unchecked cells of the given rows and applies the given columnCheckPredicate. 
     * If the predicate returns true, the cell will be marked as checked.
     * 
     * @param rows The rows to be considered.
     * @param columnCheckPredicate The predicate to be applied to the unchecked cells of the row. 
     * @return Returns true, if the check table is complete. Returns false, if the check table is not yet complete.
     * @throws IllegalArgumentException If the supplied row is not known by this instance.
     */
    boolean checkIf(Iterable<R> rows, Predicate<C> columnCheckPredicate);

    /**
     * Iterates through all unchecked cells of the given column and applies the given rowCheckPredicate. 
     * If the predicate returns true, the cell will be marked as checked.
     * 
     * @param rowCheckPredicate The predicate to be applied to the unchecked cells of the column. 
     * @param column The column to be considered.
     * @return Returns true, if the check table is complete. Returns false, if the check table is not yet complete.
     * @throws IllegalArgumentException If the supplied column is not known by this instance.
     */
    boolean checkIf(Predicate<R> rowCheckPredicate, C column);

    /**
     * Marks the cell in the given row and column as not checked. If that cell is not checked, this will be a no-op.
     * 
     * @param row The row of the cell to be unchecked.
     * @param column The column of the cell to be unchecked.
     * @throws IllegalArgumentException If the supplied row or column is not known by this instance.
     */
    void uncheck(R row, C column);

    /**
     * Iterates through all checked cells of the given row and applies the given columnCheckPredicate. 
     * If the predicate returns true, the cell will be marked as not checked.
     * 
     * @param row The row to be considered.
     * @param columnCheckPredicate The predicate to be applied to the checked cells of the row. 
     * @throws IllegalArgumentException If the supplied row is not known by this instance.
     */
    void uncheckIf(R row, Predicate<C> columnCheckPredicate);

    /**
     * Iterates through all checked cells of the given rows and applies the given columnCheckPredicate. 
     * If the predicate returns true, the cell will be marked as not checked.
     * 
     * @param rows The rows to be considered.
     * @param columnCheckPredicate The predicate to be applied to the checked cells of the row. 
     * @throws IllegalArgumentException If the supplied row is not known by this instance.
     */
    void uncheckIf(Iterable<R> rows, Predicate<C> columnCheckPredicate);

    /**
     * Iterates through all checked cells of the given column and applies the given rowCheckPredicate. 
     * If the predicate returns true, the cell will be marked as not checked.
     * 
     * @param rowCheckPredicate The predicate to be applied to the checked cells of the column. 
     * @param column The column to be considered.
     * @throws IllegalArgumentException If the supplied column is not known by this instance.
     */
    void uncheckIf(Predicate<R> rowCheckPredicate, C column);

    /**
     * Iterates through all checked cells of the given columns and applies the given rowCheckPredicate. 
     * If the predicate returns true, the cell will be marked as not checked.
     * 
     * @param rowCheckPredicate The predicate to be applied to the checked cells of the column. 
     * @param columns The columns to be considered.
     * @throws IllegalArgumentException If one of the supplied columns is not known by this instance.
     */
    void uncheckIf(Predicate<R> rowCheckPredicate, Iterable<C> columns);

    /**
     * Un-checks all columns of the specified row.
     * 
     * @param row The row to be un-checked, 
     * @throws IllegalArgumentException If the supplied row is not known by this instance.
     */
    void uncheckRow(R row);

    /**
     * Un-checks all columns of the specified row. 
     * 
     * In contrast to uncheckRow(), this will not throw an exception if the given row is not known to this instance.
     * Instead, the call will be silently ignored.
     * 
     * @param row The row to be un-checked, 
     */
    void uncheckRowIfPresent(R row);

    /**
     * Iterates through all rows of this instance which contain checked elements and applies the given rowCheckPredicate. 
     * If the predicate returns true, the whole row will be marked as not checked.
     * 
     * @param rowCheckPredicate The predicate to be applied to a row. 
     */
    void uncheckRowIf(Predicate<R> rowCheckPredicate);

    /**
     * Marks all the cells of this table as not checked.
     */
    void uncheckAll();

    /**
     * Returns true if the cell in the given row and column is checked.
     * 
     * @throws IllegalArgumentException if the given row or column is not known to this instance.
     */
    boolean isChecked(R row, C column);

    /**
     * Returns true if all cells of the given row are marked as checked.
     * 
     * @throws IllegalArgumentException if the given row is not known to this instance.
     */
    boolean isRowComplete(R row);

    /**
     * Returns true if all cells of the given column are marked as checked.
     * 
     * @throws IllegalArgumentException if the given column is not known to this instance.
     */
    boolean isColumnComplete(C column);

    /**
     * Returns true if there is at least one cell in the given row, which is not marked as checked.
     * 
     * @throws IllegalArgumentException if the given row is not known to this instance.
     */
    boolean isRowIncomplete(R row);

    /**
     * Returns true if there is at least one cell in the given column, which is not marked as checked.
     * 
     * @throws IllegalArgumentException if the given column is not known to this instance.
     */
    boolean isColumnIncomplete(C column);

    /**
     * Returns true if all cells in this table are marked as checked.
     */
    boolean isComplete();

    /**
     * Returns true if there is not any cell in this table which is marked as checked.
     */
    boolean isBlank();

    /**
     * Returns true if both the row and the column are managed by this instance.
     */
    boolean containsCellFor(R row, C column);

    /**
     * Returns a string representation of the current state of the check table. This might be a compact single line representation or the multi-line representation produced by toTableString().
     */
    String toString();

    /**
     * Returns a string representation of the current state of the check table. This might be a compact single line representation or the multi-line representation produced by toTableString().
     * 
     * @param checkedIndicator a string to represent checked cells in the result
     * @param uncheckedIndicator a string to represent unchecked cells in the result
     */
    String toString(String checkedIndicator, String uncheckedIndicator);

    /**
     * Returns a tabular, multi-line string representation of the current state of the check table. 
     */
    String toTableString();

    /**
     * Returns a tabular, multi-line string representation of the current state of the check table. 
     * 
     * @param checkedIndicator a string to represent checked cells in the result
     * @param uncheckedIndicator a string to represent unchecked cells in the result
     */
    String toTableString(String checkedIndicator, String uncheckedIndicator);

    /**
     * Returns the rows of this table. The returned set cannot be modified.
     */
    Set<R> getRows();

    /**
     * Returns the columns of this table. The returned set cannot be modified.
     */
    Set<C> getColumns();

    /**
     * Returns the rows of this table where all cells are checked. The returned set cannot be modified.
     */
    Set<R> getCompleteRows();

    /**
     * Returns the columns of this table where all cells are checked. The returned set cannot be modified.
     */
    Set<C> getCompleteColumns();

    /**
     * Returns the rows of this table where there is at least one cell that is not checked. The returned set cannot be modified.
     */
    Set<R> getIncompleteRows();

    /**
     * Returns the columns of this table where there is at least one cell that is not checked. The returned set cannot be modified.
     */
    Set<C> getIncompleteColumns();

    /**
     * Returns all rows where the given column is checked. The returned set cannot be modified.
     * 
     * @throws IllegalArgumentException if the given column is not known to this instance.
     */
    Set<R> getCheckedRows(C column);

    /**
     * Returns all columns where the given row is checked. The returned set cannot be modified.
     * 
     * @throws IllegalArgumentException if the given row is not known to this instance.
     */
    Set<C> getCheckedColumns(R row);

    /**
     * Returns all rows where the given column is checked. 
     * 
     * @throws IllegalArgumentException if the given column is not known to this instance.
     */
    Iterable<R> iterateCheckedRows(C column);

    /**
     * Returns all columns where the given row is checked. 
     * 
     * @throws IllegalArgumentException if the given r0w is not known to this instance.
     */
    Iterable<C> iterateCheckedColumns(R row);

    /**
     * Returns all rows where the given column is not checked. 
     * 
     * @throws IllegalArgumentException if the given column is not known to this instance.
     */    
    Iterable<R> iterateUncheckedRows(C column);

    /**
     * Returns all columns where the given row is checked. 
     * 
     * @throws IllegalArgumentException if the given r0w is not known to this instance.
     */
    Iterable<C> iterateUncheckedColumns(R row);

}
