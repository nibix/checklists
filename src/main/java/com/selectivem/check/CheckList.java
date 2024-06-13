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
 * Specialized data structure which models a set of elements, which can be checked and unchecked. Initially, all elements will be unchecked. 
 * You can use the check() and related methods to mark one or more elements as checked. You can use the isComplete() method to check whether
 * all elements are checked. There are additional methods, which allow you to retrieve the checked and unchecked elements.
 *
 * The implementation aims at runtime CPU efficiency. Checking/unchecking single elements is possible in constant (O(1)) time.
 * 
 * The implementation is not thread safe. Objects of this class are not meant to be shared between threads.
 * 
 * The data structure does not support null elements. The element class must implement hashCode() and equals().
 * 
 * @author Nils Bandener 
 */
public interface CheckList<E> {

    /**
     * Creates a new check list of the given elements. All elements will be initially marked as unchecked.
     */
    public static <E> CheckList<E> create(Set<E> elements) {
        return CheckListImpl.create(elements);
    }

    /**
     * Marks the given element as checked.  If the given element is already checked, this will be a no-op.
     * 
     * @param element The element to be checked.
     * @return Returns true, if the check list is complete afterwards. Returns false, if the check list is not yet complete.
     * @throws IllegalArgumentException If the supplied element is not known by this instance.
     */
    boolean check(E element);

    /**
     * Marks the given element as unchecked.  If the given element is unchecked, this will be a no-op.
     * 
     * @param element The element to be checked.
     * @throws IllegalArgumentException If the supplied element is not known by this instance.
     */
    void uncheck(E element);

    /**
     * Marks the given element as unchecked.  If the given element is unchecked, this will be a no-op. In contrast to the uncheck() method, this will not throw an IllegalArgumentException for unknown elements. 
     * Rather, unknown elements will be silently ignored.
     * 
     * @param element The element to be checked.
     */
    void uncheckIfPresent(E element);

    /**
     * Applies the given predicate on all unchecked element. If the predicate returns true for a particular element, it will be marked as checked.
     * 
     * @param checkPredicate the predicate to be applied.
     * @return Returns true, if the check list is complete afterwards. Returns false, if the check list is not yet complete.
     */
    boolean checkIf(Predicate<E> checkPredicate);

    /**
     * Applies the given predicate on all checked element. If the predicate returns true for a particular element, it will be marked as unchecked.
     * 
     * @param checkPredicate the predicate to be applied.
     */
    void uncheckIf(Predicate<E> checkPredicate);

    /**
     * Marks all elements as checked.
     */
    void checkAll();

    /**
     * Resets the check list and marks all elements as un-checked.
     */
    void uncheckAll();

    /**
     * Tests whether an element is checked. Returns true if the supplied element is checked.
     * 
     * @param element the element to be tested.
     * @return Returns true if the supplied element is checked.
     * @throws IllegalArgumentException If the supplied element is not known by this instance.
     */
    boolean isChecked(E element);

    /**
     * Tests whether all elements of this instance are marked as checked.
     * 
     * @return true if all elements have been marked as checked.
     */
    boolean isComplete();

    /**
     * Tests whether all elements of this instance are not marked as checked.
     * 
     * @return true if all elements have been marked as not checked.
     */
    boolean isBlank();

    /**
     * Returns the number of elements managed by this check list. 
     */
    int size();

    /**
     * Returns all the elements that are managed by this check list. 
     * The returned Set cannot be modified.
     */
    Set<E> getElements();

    /**
     * Returns all the elements that are marked as checked.
     * The returned Set cannot be modified.
     * 
     * See also iterateCheckedElements() for a method which mostly returns the same information, but with a minimal heap footprint.
     */
    Set<E> getCheckedElements();

    /**
     * Returns all the elements that are marked as not checked.
     * The returned Set cannot be modified.
     * 
     * See also iterateUncheckedElements() for a method which mostly returns the same information, but with a minimal heap footprint.
     */
    Set<E> getUncheckedElements();

    /**
     * Returns an Iterable that can be used to iterate through all the elements that are marked as checked.
     */
    Iterable<E> iterateCheckedElements();

    /**
     * Returns an Iterable that can be used to iterate through all the elements that are marked as unchecked.
     */
    Iterable<E> iterateUncheckedElements();

}
