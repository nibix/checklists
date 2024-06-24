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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

class BackingCollections {
    abstract static class UnmodifiableSet<E> extends AbstractSet<E> implements Set<E> {

        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            for (Object o : collection) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }
    }

    abstract static class IndexedUnmodifiableSet<E> extends UnmodifiableSet<E> implements Set<E> {
        static <E> IndexedUnmodifiableSet<E> of(E e1) {
            return new OneElementSet<E>(e1);
        }

        static <E> IndexedUnmodifiableSet<E> of(E e1, E e2) {
            return new TwoElementSet<E>(e1, e2);
        }

        static <E> IndexedUnmodifiableSet<E> of(Set<E> set) {
            int size = set.size();

            if (size == 0) {
                return empty();
            } else if (size == 1) {
                return of(set.iterator().next());
            } else if (size == 2) {
                Iterator<E> iter = set.iterator();
                return of(iter.next(), iter.next());
            } else if (size < 5) {
                return new ArrayBackedSet<>(set);
            } else {
                if (size <= 800) {
                    InternalBuilder<E> internalBuilder = new HashArrayBackedSet.Builder<E>(
                            size <= 8 ? 16 : size <= 40 ? 64 : size < 200 ? 256 : 1024, size);

                    for (E e : set) {
                        internalBuilder = internalBuilder.with(e);
                    }

                    return internalBuilder.build();
                } else {
                    return new SetBackedSet.Builder<E>(set).build();
                }
            }
        }

        static <E> InternalBuilder<E> builder(int size) {
            if (size <= 800) {
                return new HashArrayBackedSet.Builder<E>(size <= 10 ? 16 : size <= 50 ? 64 : size < 200 ? 256 : 1024, size);
            } else {
                return new SetBackedSet.Builder<E>(size);
            }
        }

        static <E> IndexedUnmodifiableSet<E> empty() {
            @SuppressWarnings("unchecked")
            IndexedUnmodifiableSet<E> result = (IndexedUnmodifiableSet<E>) EMPTY;
            return result;
        }

        private final int size;

        IndexedUnmodifiableSet(int size) {
            this.size = size;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            return this.size == 0;
        }

        abstract int elementToIndex(Object element);

        abstract E indexToElement(int i);

        static final Set<Object> EMPTY = new IndexedUnmodifiableSet<Object>(0) {

            @Override
            public Iterator<Object> iterator() {
                return Collections.emptyIterator();
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            int elementToIndex(Object element) {
                return -1;
            }

            @Override
            Object indexToElement(int i) {
                return null;
            }
        };

        static abstract class InternalBuilder<E> implements Iterable<E> {
            abstract InternalBuilder<E> with(E e);

            abstract InternalBuilder<E> with(E [] flat, int size);

            abstract boolean contains(Object o);

            abstract IndexedUnmodifiableSet<E> build();

            abstract int size();

            public abstract Iterator<E> iterator();

            @Override
            public String toString() {
                StringBuilder result = new StringBuilder("[");
                boolean first = true;

                for (E e : this) {
                    if (first) {
                        first = false;
                    } else {
                        result.append(", ");
                    }

                    result.append(e);
                }

                result.append("]");

                return result.toString();
            }
        }
    }

    final static class OneElementSet<E> extends IndexedUnmodifiableSet<E> {

        private final E element;

        OneElementSet(E element) {
            super(1);
            this.element = element;
        }

        @Override
        public boolean contains(Object o) {
            return element.equals(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < 1;
                }

                @Override
                public E next() {
                    if (i == 0) {
                        i++;
                        return element;
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        @Override
        public Object[] toArray() {
            return new Object[] { element };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result = a.length >= 1 ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 1);

            result[0] = (T) element;

            return result;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        int elementToIndex(Object element) {
            if (element.equals(this.element)) {
                return 0;
            } else {
                return -1;
            }
        }

        @Override
        E indexToElement(int i) {
            if (i == 0) {
                return element;
            } else {
                return null;
            }
        }
    }

    final static class TwoElementSet<E> extends IndexedUnmodifiableSet<E> {

        private final E e1;
        private final E e2;

        TwoElementSet(E e1, E e2) {
            super(2);
            this.e1 = e1;
            this.e2 = e2;
        }

        @Override
        public boolean contains(Object o) {
            return e1.equals(o) || e2.equals(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {

                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < 2;
                }

                @Override
                public E next() {
                    if (i == 0) {
                        i++;
                        return e1;
                    } else if (i == 1) {
                        i++;
                        return e2;
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        @Override
        public Object[] toArray() {
            return new Object[] { e1, e2 };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result = a.length >= 2 ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 2);

            result[0] = (T) e1;
            result[1] = (T) e2;

            return result;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        int elementToIndex(Object element) {
            if (element.equals(this.e1)) {
                return 0;
            } else if (element.equals(this.e2)) {
                return 1;
            } else {
                return -1;
            }
        }

        @Override
        E indexToElement(int i) {
            if (i == 0) {
                return e1;
            } else if (i == 1) {
                return e2;
            } else {
                return null;
            }
        }
    }

    final static class ArrayBackedSet<E> extends IndexedUnmodifiableSet<E> {
        private final E[] elements;

        @SuppressWarnings("unchecked")
        ArrayBackedSet(Set<E> elements) {
            super(elements.size());
            this.elements = (E[]) elements.toArray();

            for (int i = 0; i < this.elements.length; i++) {
                if (this.elements[i] == null) {
                    throw new IllegalArgumentException("Does not support null elements");
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals(o)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {

                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < elements.length;
                }

                @Override
                public E next() {
                    if (i >= elements.length) {
                        throw new NoSuchElementException();
                    }

                    E element = (E) elements[i];
                    i++;
                    return element;
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[elements.length];
            System.arraycopy(elements, 0, result, 0, elements.length);
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result = a.length >= elements.length ? a
                    : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), elements.length);

            System.arraycopy(elements, 0, result, 0, elements.length);

            return result;
        }

        @Override
        int elementToIndex(Object element) {
            int l = elements.length;

            for (int i = 0; i < l; i++) {
                if (elements[i].equals(element)) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        E indexToElement(int i) {
            if (i >= 0 && i < elements.length) {
                return elements[i];
            } else {
                return null;
            }
        }
    }

    final static class HashArrayBackedSet<E> extends IndexedUnmodifiableSet<E> {

        private static final int COLLISION_HEAD_ROOM = 10;
        private static final int NO_SPACE = Integer.MAX_VALUE;

        final int tableSize;
        private final int size;

        private final E[] table;
        private final E[] flat;
        private final short[] indices;

        HashArrayBackedSet(int tableSize, int size, E[] table, short[] indices, E[] flat) {
            super(size);
            this.tableSize = tableSize;
            this.size = size;
            this.table = table;
            this.indices = indices;
            this.flat = flat;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return checkTable(o, hashPosition(o)) < 0;
        }

        @Override
        int elementToIndex(Object o) {
            int hashPosition = hashPosition(o);

            if (table[hashPosition] == null) {
                return -1;
            } else if (table[hashPosition].equals(o)) {
                return indices[hashPosition];
            }

            int max = hashPosition + COLLISION_HEAD_ROOM;

            for (int i = hashPosition + 1; i <= max; i++) {
                if (table[i] == null) {
                    return -1;
                } else if (table[i].equals(o)) {
                    return indices[i];
                }
            }

            return -1;
        }

        @Override
        E indexToElement(int i) {
            if (i >= 0 && i < flat.length) {
                return flat[i];
            } else {
                return null;
            }
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < HashArrayBackedSet.this.size;
                }

                @Override
                public E next() {
                    if (i >= HashArrayBackedSet.this.size) {
                        throw new NoSuchElementException();
                    }

                    E element = HashArrayBackedSet.this.flat[i];

                    i++;

                    return element;
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[size];
            System.arraycopy(flat, 0, result, 0, size);
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result = a.length >= size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
            System.arraycopy(flat, 0, result, 0, size);
            return result;
        }

        int hashPosition(Object e) {
            return hashPosition(tableSize, e);
        }

        final static int hashPosition(int tableSize, Object e) {
            if (e == null) {
                throw new IllegalArgumentException("null values are not supported");
            }

            int hash = e.hashCode();

            switch (tableSize) {
            case 16:
                int h8 = hashTo8bit(hash);
                return (h8 & 0xf) ^ (h8 >> 4 & 0xf);
            case 64:
                return (hash & 0x3f) ^ (hash >> 6 & 0x3f) ^ (hash >> 12 & 0x3f) ^ (hash >> 18 & 0x3f) ^ (hash >> 24 & 0xf) ^ (hash >> 28 & 0xf);
            case 256:
                return hashTo8bit(hash);
            case 1024:
                return (hash & 0x3ff) ^ (hash >> 10 & 0x3ff) ^ (hash >> 20 & 0x3ff) ^ (hash >> 30 & 0x3);
            default:
                throw new RuntimeException("Invalid tableSize " + tableSize);
            }
        }

        final static int hashTo8bit(int hash) {
            return (hash & 0xff) ^ (hash >> 8 & 0xff) ^ (hash >> 16 & 0xff) ^ (hash >> 24 & 0xff);
        }

        int checkTable(Object e, int hashPosition) {
            return checkTable(table, e, hashPosition);
        }

        static <E> int checkTable(E[] table, Object e, int hashPosition) {
            if (table[hashPosition] == null) {
                return hashPosition;
            } else if (table[hashPosition].equals(e)) {
                return -1 - hashPosition;
            }

            int max = hashPosition + COLLISION_HEAD_ROOM;

            for (int i = hashPosition + 1; i <= max; i++) {
                if (table[i] == null) {
                    return i;
                } else if (table[i].equals(e)) {
                    return -1 - i;
                }
            }

            return NO_SPACE;
        }

        static class Builder<E> extends IndexedUnmodifiableSet.InternalBuilder<E> {
            private E[] table;
            private E[] flat;
            private short[] indices;
            private short size = 0;
            private final int tableSize;

            public Builder(int tableSize) {
                this.tableSize = tableSize;
            }

            public Builder(int tableSize, int flatSize) {
                this.tableSize = tableSize;
                if (flatSize > 0) {
                    this.flat = createEArray(flatSize);
                }
            }

            public InternalBuilder<E> with(E e) {
                if (e == null) {
                    throw new IllegalArgumentException("Null elements are not supported");
                }

                if (table == null) {
                    int hashPosition = hashPosition(e);
                    table = createEArray(tableSize + COLLISION_HEAD_ROOM);
                    indices = new short[tableSize + COLLISION_HEAD_ROOM];

                    if (flat == null) {
                        flat = createEArray(tableSize <= 64 ? tableSize : tableSize / 2);
                    }

                    table[hashPosition] = e;
                    indices[hashPosition] = 0;
                    flat[0] = e;
                    size++;
                    return this;
                } else {
                    int position = hashPosition(e);

                    if (table[position] == null) {
                        table[position] = e;
                        indices[position] = size;
                        extendFlat();
                        flat[size] = e;
                        size++;
                        return this;
                    } else if (table[position].equals(e)) {
                        // done
                        return this;
                    } else {
                        // collision
                        int check = checkTable(e, position);

                        if (check < 0) {
                            // done     
                            return this;
                        } else if (check == NO_SPACE) {
                            // collision
                            if (tableSize < 64) {
                                return new HashArrayBackedSet.Builder<E>(64).with(flat, size).with(e);
                            } else if (tableSize < 256) {
                                return new HashArrayBackedSet.Builder<E>(256).with(flat, size).with(e);
                            } else if (tableSize < 1024) {
                                return new HashArrayBackedSet.Builder<E>(1024).with(flat, size).with(e);
                            } else {
                                return new SetBackedSet.Builder<E>(this.size).with(flat, size).with(e);
                            }
                        } else {
                            table[check] = e;
                            indices[check] = size;
                            extendFlat();
                            flat[size] = e;
                            size++;
                            return this;
                        }
                    }
                }
            }

            @Override
            InternalBuilder<E> with(E [] flat, int size) {
                if (this.flat == null) {
                    this.flat = flat;
                }

                InternalBuilder<E> builder = this;

                for (int i = 0; i < size; i++) {
                    builder = builder.with(flat[i]);
                }

                return builder;
            }

            public IndexedUnmodifiableSet<E> build() {
                if (size == 0) {
                    return IndexedUnmodifiableSet.empty();
                } else if (size == 1) {
                    return new OneElementSet<E>(this.flat[0]);
                } else if (size == 2) {
                    return new TwoElementSet<>(this.flat[0], this.flat[1]);
                } else {
                    E[] flat = this.flat;
                    if (flat.length > size + 16) {
                        flat = createEArray(size);
                        System.arraycopy(this.flat, 0, flat, 0, size);
                    }
                    return new HashArrayBackedSet<>(tableSize, size, table, indices, flat);
                }
            }

            @Override
            int size() {
                return size;
            }

            @Override
            public Iterator<E> iterator() {
                if (size == 0) {
                    return Collections.emptyIterator();
                }

                return new Iterator<E>() {
                    int pos = 0;

                    @Override
                    public boolean hasNext() {
                        if (pos < size) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public E next() {
                        if (pos < size) {
                            E result = flat[pos];
                            pos++;
                            return result;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            }

            private int hashPosition(Object e) {
                return HashArrayBackedSet.hashPosition(tableSize, e);
            }

            private void extendFlat() {
                if (size >= flat.length) {
                    E [] newFlat = createEArray(Math.min(flat.length + flat.length / 2 + 8, this.table.length));
                    System.arraycopy(this.flat, 0, newFlat, 0, this.flat.length);
                    this.flat = newFlat;
                }
            }

            int checkTable(Object e, int hashPosition) {
                int max = hashPosition + COLLISION_HEAD_ROOM;

                for (int i = hashPosition + 1; i <= max; i++) {
                    if (table[i] == null) {
                        return i;
                    } else if (table[i].equals(e)) {
                        return -1 - i;
                    }
                }

                return NO_SPACE;
            }

            @Override
            boolean contains(Object o) {
                if (table == null) {
                    return false;
                } else {
                    int hashPosition = hashPosition(o);
                    int max = hashPosition + COLLISION_HEAD_ROOM;

                    for (int i = hashPosition; i <= max; i++) {
                        if (table[i] == null) {
                            return false;
                        } else if (table[i].equals(o)) {
                            return true;
                        }
                    }

                    return false;
                }
            }
        }
    }

    final static class SetBackedSet<E> extends IndexedUnmodifiableSet<E> {

        private final Map<E, Integer> elements;
        private final E[] flat;

        SetBackedSet(Map<E, Integer> elements, E[] flat) {
            super(elements.size());
            this.elements = elements;
            this.flat = flat;
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return elements.containsKey(o);
        }

        @Override
        public Iterator<E> iterator() {
            return elements.keySet().iterator();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return elements.keySet().toArray(a);
        }

        @Override
        public Object[] toArray() {
            return elements.keySet().toArray();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return elements.keySet().containsAll(c);
        }

        @Override
        int elementToIndex(Object element) {
            Integer pos = this.elements.get(element);

            if (pos != null) {
                return pos.intValue();
            } else {
                return -1;
            }
        }

        @Override
        E indexToElement(int i) {
            if (i >= 0 && i < flat.length) {
                return flat[i];
            } else {
                return null;
            }
        }

        static class Builder<E> extends InternalBuilder<E> {
            private HashMap<E, Integer> delegate;
            private E [] flat;

            Builder(int expectedCapacity) {
                this.delegate = new HashMap<>(expectedCapacity);
            }

            Builder(Collection<E> set) {
                this.delegate = new HashMap<>(set.size());
                this.flat = createEArray(set.size());

                int i = 0;

                for (E e : set) {
                    this.delegate.put(e, i);
                    this.flat[i] = e;
                    i++;
                }
            }

            @Override
            public Builder<E> with(E e) {
                int pos = this.delegate.size();
                this.delegate.put(e, pos);
                extendFlat();
                this.flat[pos] = e;
                return this;
            }

            @Override
            InternalBuilder<E> with(E [] flat, int size) {
                if (this.flat == null) {
                    this.flat = flat;

                    for (int i = 0; i < size; i++) {
                        this.delegate.put(flat[i], i);
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        with(flat[i]);
                    }
                }

                return this;
            }

            @Override
            IndexedUnmodifiableSet<E> build() {
                if (delegate.isEmpty()) {
                    return IndexedUnmodifiableSet.empty();
                } else {
                    return new SetBackedSet<>(this.delegate, this.flat);
                }
            }

            @Override
            int size() {
                return delegate.size();
            }

            @Override
            public Iterator<E> iterator() {
                return delegate.keySet().iterator();
            }

            @Override
            public String toString() {
                return delegate.keySet().toString();
            }

            @Override
            boolean contains(Object o) {
                return delegate.keySet().contains(o);
            }

            private void extendFlat() {
                if (delegate.size() >= flat.length) {
                    E [] newFlat = createEArray(flat.length + flat.length / 2 + 8);
                    System.arraycopy(this.flat, 0, newFlat, 0, this.flat.length);
                    this.flat = newFlat;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> E[] createEArray(int size) {
        return (E[]) new Object[size];
    }
}
