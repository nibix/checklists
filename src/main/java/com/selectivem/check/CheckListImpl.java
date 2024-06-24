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

import java.util.*;
import java.util.function.Predicate;

class CheckListImpl {

    static <E> CheckList<E> create(Set<E> elements) {
        return create(elements, "element");
    }

    static <E> CheckList<E> create(Set<E> elements, String elementName) {
        int size = elements.size();

        if (size == 2) {
            Iterator<E> iter = elements.iterator();
            return new CheckListImpl.TwoElementCheckList<>(iter.next(), iter.next(), elementName);
        } else if (size >= 800) {
            return new CheckListImpl.HashMapCheckList<>(elements, elementName);
        } else {
            return new CheckListImpl.ArrayCheckList<>(elements, elementName);
        }
    }

    final static class TwoElementCheckList<E> implements CheckList<E> {

        private final E e1;
        private final E e2;
        private final String elementName;
        private Set<E> elements;
        private boolean e1checked;
        private boolean e2checked;

        TwoElementCheckList(E e1, E e2, String elementName) {
            this.e1 = e1;
            this.e2 = e2;
            this.elementName = elementName;
        }

        @Override
        public boolean check(E element) {
            if (element.equals(e1)) {
                e1checked = true;
            } else if (element.equals(e2)) {
                e2checked = true;
            } else {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            return e1checked && e2checked;
        }

        @Override
        public void uncheck(E element) {
            if (element.equals(e1)) {
                e1checked = false;
            } else if (element.equals(e2)) {
                e2checked = false;
            } else {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }
        }

        @Override
        public void uncheckIfPresent(E element) {
            if (element.equals(e1)) {
                e1checked = false;
            } else if (element.equals(e2)) {
                e2checked = false;
            }
        }

        @Override
        public boolean checkIf(Predicate<E> checkPredicate) {
            if (!e1checked && checkPredicate.test(e1)) {
                e1checked = true;
            }

            if (!e2checked && checkPredicate.test(e2)) {
                e2checked = true;
            }

            return e1checked && e2checked;

        }

        @Override
        public void uncheckIf(Predicate<E> checkPredicate) {
            if (e1checked && checkPredicate.test(e1)) {
                e1checked = false;
            }

            if (e2checked && checkPredicate.test(e2)) {
                e2checked = false;
            }
        }

        @Override
        public void checkAll() {
            e1checked = true;
            e2checked = true;
        }

        @Override
        public void uncheckAll() {
            e1checked = false;
            e2checked = false;
        }

        @Override
        public boolean isChecked(E element) {
            if (element.equals(e1)) {
                return e1checked;
            } else if (element.equals(e2)) {
                return e2checked;
            } else {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }
        }

        @Override
        public boolean isComplete() {
            return e1checked && e2checked;
        }

        @Override
        public boolean isBlank() {
            return !e1checked && !e2checked;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public Set<E> getElements() {
            if (elements == null) {
                elements = new BackingCollections.TwoElementSet<>(e1, e2);
            }
            return elements;
        }

        @Override
        public Set<E> getCheckedElements() {
            if (e1checked) {
                if (e2checked) {
                    return getElements();
                } else {
                    return BackingCollections.IndexedUnmodifiableSet.of(e1);
                }
            } else if (e2checked) {
                return BackingCollections.IndexedUnmodifiableSet.of(e2);
            } else {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            }
        }

        @Override
        public Set<E> getUncheckedElements() {
            if (e1checked) {
                if (e2checked) {
                    return BackingCollections.IndexedUnmodifiableSet.empty();
                } else {
                    return BackingCollections.IndexedUnmodifiableSet.of(e2);
                }
            } else if (e2checked) {
                return BackingCollections.IndexedUnmodifiableSet.of(e1);
            } else {
                return getElements();
            }
        }

        @Override
        public Iterable<E> iterateCheckedElements() {
            return getCheckedElements();
        }

        @Override
        public Iterable<E> iterateUncheckedElements() {
            return getUncheckedElements();
        }

    }

    final static class ArrayCheckList<E> implements CheckList<E> {

        private final BackingCollections.IndexedUnmodifiableSet<E> elements;
        private final boolean[] checked;
        private final String elementName;
        private int uncheckedCount;
        private final int size;

        ArrayCheckList(Set<E> elements, String elementName) {
            this.elements = BackingCollections.IndexedUnmodifiableSet.of(elements);
            this.size = this.elements.size();
            this.checked = new boolean[this.size];
            this.uncheckedCount = this.size;
            this.elementName = elementName;
        }

        @Override
        public boolean check(E element) {
            doCheck(element);

            return this.uncheckedCount == 0;
        }

        private void doCheck(E element) {
            int tablePos = elements.elementToIndex(element);

            if (tablePos == -1) {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            if (!this.checked[tablePos]) {
                this.checked[tablePos] = true;
                this.uncheckedCount--;
            }
        }

        @Override
        public void uncheck(E element) {
            int tablePos = elements.elementToIndex(element);

            if (tablePos == -1) {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            if (this.checked[tablePos]) {
                this.checked[tablePos] = false;
                this.uncheckedCount++;
            }
        }

        @Override
        public void uncheckIfPresent(E element) {
            int tablePos = elements.elementToIndex(element);

            if (tablePos == -1) {
                return;
            }

            if (this.checked[tablePos]) {
                this.checked[tablePos] = false;
                this.uncheckedCount++;
            }
        }

        @Override
        public boolean checkIf(Predicate<E> checkPredicate) {
            for (int i = 0; i < size; i++) {
                if (!this.checked[i] && checkPredicate.test(this.elements.indexToElement(i))) {
                    this.checked[i] = true;
                    this.uncheckedCount--;
                }
            }

            return this.uncheckedCount == 0;
        }

        @Override
        public void uncheckIf(Predicate<E> checkPredicate) {
            for (int i = 0; i < size; i++) {
                if (this.checked[i] && checkPredicate.test(this.elements.indexToElement(i))) {
                    this.checked[i] = false;
                    this.uncheckedCount++;
                }
            }
        }

        @Override
        public void checkAll() {
            Arrays.fill(this.checked, true);
            this.uncheckedCount = 0;
        }

        @Override
        public void uncheckAll() {
            Arrays.fill(this.checked, false);
            this.uncheckedCount = this.size;
        }

        @Override
        public boolean isChecked(E element) {
            int tablePos = elements.elementToIndex(element);

            if (tablePos == -1) {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            return this.checked[tablePos];
        }

        @Override
        public boolean isComplete() {
            return this.uncheckedCount == 0;
        }

        @Override
        public boolean isBlank() {
            return this.uncheckedCount == this.size;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public Set<E> getElements() {
            return elements;
        }

        @Override
        public Set<E> getCheckedElements() {
            if (isComplete()) {
                return elements;
            } else if (isBlank()) {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            } else {
                return new BackingCollections.UnmodifiableSet<E>() {

                    @Override
                    public boolean contains(Object o) {
                        int tablePos = ArrayCheckList.this.elements.elementToIndex(o);

                        if (tablePos == -1) {
                            return false;
                        } else {
                            return ArrayCheckList.this.checked[tablePos];
                        }
                    }

                    @Override
                    public Iterator<E> iterator() {
                        int tableSize = ArrayCheckList.this.elements.size();

                        return new Iterator<E>() {

                            private int pos = findNext(0);
                            private boolean ready = true;

                            @Override
                            public boolean hasNext() {
                                if (ready) {
                                    return pos != -1;
                                } else {
                                    if (pos != -1) {
                                        this.pos = findNext(this.pos + 1);
                                        this.ready = true;
                                    }
                                }

                                return pos != -1;
                            }

                            @Override
                            public E next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }

                                E element = ArrayCheckList.this.elements.indexToElement(pos);
                                ready = false;
                                return element;
                            }

                            int findNext(int start) {
                                for (int i = start; i < tableSize; i++) {
                                    if (ArrayCheckList.this.checked[i]) {
                                        return i;
                                    }
                                }

                                return -1;
                            }

                        };
                    }

                    @Override
                    public int size() {
                        return size - uncheckedCount;
                    }
                };
            }
        }

        @Override
        public Set<E> getUncheckedElements() {
            if (isComplete()) {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            } else if (isBlank()) {
                return elements;
            } else {
                return new BackingCollections.UnmodifiableSet<E>() {

                    @Override
                    public boolean contains(Object o) {
                        int tablePos = ArrayCheckList.this.elements.elementToIndex(o);

                        if (tablePos == -1) {
                            return false;
                        } else {
                            return !ArrayCheckList.this.checked[tablePos];
                        }
                    }

                    @Override
                    public Iterator<E> iterator() {
                        int tableSize = ArrayCheckList.this.elements.size();

                        return new Iterator<E>() {

                            private int pos = findNext(0);

                            @Override
                            public boolean hasNext() {
                                return pos != -1;
                            }

                            @Override
                            public E next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }

                                E element = ArrayCheckList.this.elements.indexToElement(pos);
                                this.pos = findNext(this.pos + 1);
                                return element;
                            }

                            int findNext(int start) {
                                for (int i = start; i < tableSize; i++) {
                                    if (!ArrayCheckList.this.checked[i]) {
                                        return i;
                                    }
                                }

                                return -1;
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return uncheckedCount;
                    }
                };
            }
        }

        @Override
        public Iterable<E> iterateCheckedElements() {
            if (isComplete()) {
                return elements;
            } else if (isBlank()) {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            } else {
                return new Iterable<E>() {

                    @Override
                    public Iterator<E> iterator() {
                        int tableSize = ArrayCheckList.this.elements.size();

                        return new Iterator<E>() {

                            private int pos = findNext(0);

                            @Override
                            public boolean hasNext() {
                                return pos != -1;
                            }

                            @Override
                            public E next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }

                                E element = ArrayCheckList.this.elements.indexToElement(pos);
                                this.pos = findNext(this.pos + 1);
                                return element;
                            }

                            int findNext(int start) {
                                for (int i = start; i < tableSize; i++) {
                                    if (ArrayCheckList.this.checked[i]) {
                                        return i;
                                    }
                                }

                                return -1;
                            }
                        };
                    }

                };

            }
        }

        @Override
        public Iterable<E> iterateUncheckedElements() {
            if (isComplete()) {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            } else if (isBlank()) {
                return elements;
            } else {
                return new Iterable<E>() {

                    @Override
                    public Iterator<E> iterator() {
                        int tableSize = ArrayCheckList.this.elements.size();

                        return new Iterator<E>() {

                            private int pos = findNext(0);

                            @Override
                            public boolean hasNext() {
                                return pos != -1;
                            }

                            @Override
                            public E next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }

                                E element = ArrayCheckList.this.elements.indexToElement(pos);
                                this.pos = findNext(this.pos + 1);
                                return element;
                            }

                            int findNext(int start) {
                                for (int i = start; i < tableSize; i++) {
                                    if (!ArrayCheckList.this.checked[i]) {
                                        return i;
                                    }
                                }

                                return -1;
                            }
                        };
                    }
                };
            }
        }
    }

    final static class HashMapCheckList<E> implements CheckList<E> {
        private final Set<E> elements;
        private final Map<E, Boolean> checked;
        private final String elementName;
        private int uncheckedCount;
        private final int size;

        HashMapCheckList(Set<E> elements, String elementName) {
            this.checked = createCheckedMap(elements);
            this.elements = Collections.unmodifiableSet(this.checked.keySet());
            this.size = this.elements.size();
            this.uncheckedCount = this.size;
            this.elementName = elementName;
        }

        @Override
        public boolean check(E element) {
            doCheck(element);

            return this.uncheckedCount == 0;
        }

        private void doCheck(E element) {
            Boolean current = this.checked.get(element);

            if (current == null) {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            if (!current) {
                this.checked.put(element, Boolean.TRUE);
                this.uncheckedCount--;
            }
        }

        @Override
        public void uncheck(E element) {
            Boolean current = this.checked.get(element);

            if (current == null) {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            if (current) {
                this.checked.put(element, Boolean.FALSE);
                this.uncheckedCount++;
            }
        }

        @Override
        public void uncheckIfPresent(E element) {
            Boolean current = this.checked.get(element);

            if (current == null) {
                return;
            }

            if (current) {
                this.checked.put(element, Boolean.FALSE);
                this.uncheckedCount++;
            }
        }

        @Override
        public boolean checkIf(Predicate<E> checkPredicate) {
            if (isComplete()) {
                return true;
            }

            this.checked.forEach((e, v) -> {
                if (!v && checkPredicate.test(e)) {
                    this.checked.put(e, Boolean.TRUE);
                    this.uncheckedCount--;
                }
            });

            return this.uncheckedCount == 0;
        }

        @Override
        public void uncheckIf(Predicate<E> checkPredicate) {
            if (isBlank()) {
                return;
            }

            this.checked.forEach((e, v) -> {
                if (v && checkPredicate.test(e)) {
                    this.checked.put(e, Boolean.FALSE);
                    this.uncheckedCount++;
                }
            });
        }

        @Override
        public void checkAll() {
            if (isComplete()) {
                return;
            }

            for (E element : elements) {
                this.checked.put(element, Boolean.TRUE);
            }

            this.uncheckedCount = 0;
        }

        @Override
        public void uncheckAll() {
            if (isBlank()) {
                return;
            }

            for (E element : elements) {
                this.checked.put(element, Boolean.FALSE);
            }

            this.uncheckedCount = this.size;
        }

        @Override
        public boolean isChecked(E element) {
            Boolean current = this.checked.get(element);

            if (current == null) {
                throw new IllegalArgumentException("Invalid " + elementName + ": " + element);
            }

            return current;
        }

        @Override
        public boolean isComplete() {
            return this.uncheckedCount == 0;
        }

        @Override
        public boolean isBlank() {
            return this.uncheckedCount == this.size;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public Set<E> getElements() {
            return elements;
        }

        @Override
        public Set<E> getCheckedElements() {
            if (isComplete()) {
                return elements;
            } else if (isBlank()) {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            } else {
                return new BackingCollections.UnmodifiableSet<E>() {
                    @Override
                    public boolean contains(Object o) {
                        Boolean checked = HashMapCheckList.this.checked.get(o);

                        if (checked == null) {
                            return false;
                        } else {
                            return checked;
                        }
                    }

                    @Override
                    public Iterator<E> iterator() {
                        Iterator<E> delegate = HashMapCheckList.this.elements.iterator();

                        return new Iterator<E>() {
                            private E next = findNext();

                            @Override
                            public boolean hasNext() {
                                return next != null;
                            }

                            @Override
                            public E next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }

                                E result = this.next;
                                this.next = findNext();
                                return result;
                            }

                            E findNext() {
                                while (delegate.hasNext()) {
                                    E e = delegate.next();

                                    if (HashMapCheckList.this.checked.get(e)) {
                                        return e;
                                    }
                                }

                                return null;
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return size - uncheckedCount;
                    }
                };
            }
        }

        @Override
        public Set<E> getUncheckedElements() {
            if (isComplete()) {
                return BackingCollections.IndexedUnmodifiableSet.empty();
            } else if (isBlank()) {
                return elements;
            } else {
                return new BackingCollections.UnmodifiableSet<E>() {
                    @Override
                    public boolean contains(Object o) {
                        Boolean checked = HashMapCheckList.this.checked.get(o);

                        if (checked == null) {
                            return false;
                        } else {
                            return !checked;
                        }
                    }

                    @Override
                    public Iterator<E> iterator() {
                        Iterator<E> delegate = HashMapCheckList.this.elements.iterator();

                        return new Iterator<E>() {
                            private E next = findNext();

                            @Override
                            public boolean hasNext() {
                                return next != null;
                            }

                            @Override
                            public E next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }

                                E result = this.next;
                                this.next = findNext();
                                return result;
                            }

                            E findNext() {
                                while (delegate.hasNext()) {
                                    E e = delegate.next();

                                    if (!HashMapCheckList.this.checked.get(e)) {
                                        return e;
                                    }
                                }

                                return null;
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return uncheckedCount;
                    }
                };
            }
        }

        @Override
        public Iterable<E> iterateCheckedElements() {
            return getCheckedElements();
        }

        @Override
        public Iterable<E> iterateUncheckedElements() {
            return getUncheckedElements();
        }

        static <E> Map<E, Boolean> createCheckedMap(Set<E> elements) {
            HashMap<E, Boolean> result = new HashMap<>(elements.size());

            for (E e : elements) {
                result.put(e, Boolean.FALSE);
            }

            return result;
        }
    }

}
