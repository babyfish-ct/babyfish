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

import org.babyfish.collection.spi.AbstractMAOrderedSet;
import org.babyfish.collection.spi.base.LinkedHashEntries;

/**
 * @author Tao Chen
 */
public class MALinkedHashSet<E> extends AbstractMAOrderedSet<E> implements Serializable {
    
    private static final long serialVersionUID = 3170953286473132073L;

    public MALinkedHashSet() {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(ReplacementRule replacementRule) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(ReplacementRule replacementRule, EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(int initCapacity) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(int initCapacity, Float loadFactor) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(ReplacementRule replacementRule, int initCapacity) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(EqualityComparator<? super E> equalityComparator, int initCapacity) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(boolean headAppend, OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(ReplacementRule replacementRule, boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(EqualityComparator<? super E> equalityComparator, boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(int initCapacity, boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashSet(Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(ReplacementRule replacementRule, Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(EqualityComparator<? super E> equalityComparator, Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(int initCapacity, Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(boolean headAppend, Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.addAll(c);
    }

    public MALinkedHashSet(
            ReplacementRule replacementRule,
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Collection<? extends E> c) {
        super(
                new LinkedHashEntries.TransientValue<E, Object>(
                        replacementRule,
                        equalityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
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

        private boolean headAppend = false;

        private OrderAdjustMode accessMode = OrderAdjustMode.NONE;

        private OrderAdjustMode replaceMode = OrderAdjustMode.NONE;

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

        public Builder<E> setHeadAppend(boolean headAppend) {
            this.headAppend = headAppend;
            return this;
        }

        public Builder<E> setAccessMode(OrderAdjustMode accessMode) {
            this.accessMode = accessMode != null ? accessMode : OrderAdjustMode.NONE;
            return this;
        }

        public Builder<E> setReplaceMode(OrderAdjustMode replaceMode) {
            this.replaceMode = replaceMode != null ? replaceMode : OrderAdjustMode.NONE;
            return this;
        }

        public MALinkedHashSet<E> build() {
            return new MALinkedHashSet<E>(
                    this.replacementRule,
                    this.equalityComparator,
                    this.initCapacity,
                    this.loadFactor,
                    this.headAppend,
                    this.accessMode,
                    this.replaceMode
            );
        }
    }
}
