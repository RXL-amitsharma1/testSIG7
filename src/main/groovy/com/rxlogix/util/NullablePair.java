package com.rxlogix.util;

import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
public class NullablePair<T1, T2> implements Iterable<Object>, Serializable {

    private static final long serialVersionUID = 5826004112854062133L;

    @Nullable final T1 t1;
    @Nullable final T2 t2;

    NullablePair(@Nullable T1 t1, @Nullable T2 t2) {
        this.t1 =t1;
        this.t2 =t2;
    }

    /**
     * Type-safe way to get the fist object of this {}.
     *
     * @return The first object
     */
    @Nullable
    public T1 getT1() {
        return t1;
    }

    /**
     * Type-safe way to get the second object of this {}.
     *
     * @return The second object
     */
    @Nullable
    public T2 getT2() {
        return t2;
    }


    /**
     * Get the object at the given index.
     *
     * @param index The index of the object to retrieve. Starts at 0.
     * @return The object or {@literal null} if out of bounds.
     */
    @Nullable
    public Object get(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            default:
                return null;
        }
    }

    /**
     * Turn this {@literal Tuples} into a plain Object list.
     *
     * @return A new Object list.
     */
    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    /**
     * Turn this {@literal Tuples} into a plain Object array.
     *
     * @return A new Object array.
     */
    public Object[] toArray() {
        return new Object[]{t1, t2};
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NullablePair<?, ?> pair = (NullablePair<?, ?>) o;

        return (t1 != null ? t1.equals(pair.t1) : pair.t1 == null) && (t2 != null ?
                t2.equals(pair.t2) : pair.t2 == null);
    }

    @Override
    public int hashCode() {
        int result = t1 != null ? t1.hashCode() : 0;
        result = 31 * result + (t2 != null ? t2.hashCode() : 0);
        return result;
    }

    /**
     * Return the number of elements in this {@literal Tuples}.
     *
     * @return The size of this {@literal Tuples}.
     */
    public int size() {
        return 2;
    }

    /**
     * A nullable tuple String representation is the comma separated list of values, enclosed
     * in square brackets. Note that intermediate {@literal null} values are represented
     * as the empty String or {@code [,value2]} for a {@link NullablePair}.
     * @return the nullable tuple String representation
     */
    @Override
    public final String toString() {
        return NullablePair.tupleStringRepresentation(toArray()).insert(0, '[').append(']').toString();
    }

    /**
     * Prepare a string representation of the values suitable for a Tuple of any
     * size by accepting an array of elements. This builds a {@link StringBuilder}
     * containing the String representation of each object, comma separated. It manages
     * nulls as well by putting an empty string and the comma.
     *
     * @param values the values of the tuple to represent
     * @return a {@link StringBuilder} initialized with the string representation of the
     * values in the Tuple.
     */
    static StringBuilder tupleStringRepresentation(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            Object t = values[i];
            if (i != 0) {
                sb.append(',');
            }
            if (t != null) {
                sb.append(t);
            }
        }
        return sb;
    }
}
