/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import org.babyfish.collection.spi.AbstractMASet;
import org.babyfish.collection.spi.base.HashEntries;

/**
 * @author Tao Chen
 */
public class MAHashSet<E> extends AbstractMASet<E> implements Serializable {

    private static final long serialVersionUID = 6004383205060638338L;

    public MAHashSet() {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F
                )
        );
    }

    public MAHashSet(ReplacementRule replacementRule) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F
                )
        );
    }

    public MAHashSet(EqualityComparator<? super E> equalityComparator) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F
                )
        );
    }

    public MAHashSet(ReplacementRule replacementRule, EqualityComparator<? super E> equalityComparator) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F
                )
        );
    }

    public MAHashSet(int initCapacity) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F
                )
        );
    }

    public MAHashSet(int initCapacity, Float loadFactor) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
    }

    public MAHashSet(ReplacementRule replacementRule, int initCapacity) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F
                )
        );
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
    }

    public MAHashSet(EqualityComparator<? super E> equalityComparator, int initCapacity) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F
                )
        );
    }

    public MAHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F
                )
        );
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
    }

    public MAHashSet(Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(ReplacementRule replacementRule, Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(EqualityComparator<? super E> equalityComparator, Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(int initCapacity, Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new HashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor
                )
        );
        this.addAll(c);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }
    
    public static class Builder<E> {

        private ReplacementRule replacementRule = ReplacementRule.NEW_REFERENCE_WIN;

        private EqualityComparator<? super E> equalityComparator = null;

        private int initCapacity = 16;

        private Float loadFactor = .75F;

        public Builder<E> setReplacementRule(ReplacementRule replacementRule) {
            this.replacementRule = replacementRule != null ? replacementRule : ReplacementRule.NEW_REFERENCE_WIN;
            return this;
        }

        public Builder<E> setEqualityComparator(EqualityComparator<? super E> equalityComparator) {
            this.equalityComparator = equalityComparator;
            return this;
        }

        public Builder<E> setInitCapacity(int initCapacity) {
            this.initCapacity = initCapacity;
            return this;
        }

        public Builder<E> setLoadFactor(Float loadFactor) {
            this.loadFactor = loadFactor;
            return this;
        }

        public MAHashSet<E> build() {
            return new MAHashSet<E>(
                    this.replacementRule,
                    this.equalityComparator,
                    this.initCapacity,
                    this.loadFactor
            );
        }
    }
}
