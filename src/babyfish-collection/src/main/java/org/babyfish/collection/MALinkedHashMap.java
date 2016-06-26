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
import java.util.Comparator;
import java.util.Map;

import org.babyfish.collection.spi.AbstractMAOrderedMap;
import org.babyfish.collection.spi.base.LinkedHashEntries;

/**
 * @author Tao Chen
 */
public class MALinkedHashMap<K, V> extends AbstractMAOrderedMap<K, V> implements Serializable {

    private static final long serialVersionUID = 1422936540719225147L;
    
    public MALinkedHashMap() {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(ReplacementRule keyReplacementRule) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator, EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator, Comparator<? super V> valueComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator, UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(ReplacementRule keyReplacementRule, EqualityComparator<? super K> keyEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(int initCapacity, Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(ReplacementRule keyReplacementRule, int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator, int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(boolean headAppend, OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(ReplacementRule keyReplacementRule, boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator, boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(int initCapacity, boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(ReplacementRule keyReplacementRule, Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(EqualityComparator<? super K> keyEqualityComparator, Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(int initCapacity, Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(boolean headAppend, Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }
    
    public MALinkedHashMap(BidiType bidiType) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator, 
            EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator, 
            Comparator<? super V> valueComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator, 
            UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule, 
            EqualityComparator<? super K> keyEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(BidiType bidiType, int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType,
            int initCapacity, 
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule, 
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator, 
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType,
            boolean headAppend, 
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule, 
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator, 
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity, 
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
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

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule, 
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator, 
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity, 
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            boolean headAppend, 
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        16,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
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
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        null,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueEqualityComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Comparator<? super V> valueComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        .75F,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        OrderAdjustMode.NONE
                )
        );
        this.putAll(m);
    }

    public MALinkedHashMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            int initCapacity,
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode,
            Map<? extends K, ? extends V> m) {
        super(
                new LinkedHashEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyEqualityComparator,
                        valueUnifiedComparator,
                        initCapacity,
                        loadFactor,
                        headAppend,
                        accessMode,
                        replaceMode
                )
        );
        this.putAll(m);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }
    
    public static class Builder<K, V> {

        private BidiType bidiType = BidiType.NONE;
        
        private ReplacementRule keyReplacementRule = ReplacementRule.NEW_REFERENCE_WIN;

        private EqualityComparator<? super K> keyEqualityComparator = null;

        private EqualityComparator<? super V> valueEqualityComparator = null;

        private Comparator<? super V> valueComparator = null;

        private UnifiedComparator<? super V> valueUnifiedComparator = null;

        private int initCapacity = 16;

        private Float loadFactor = .75F;

        private boolean headAppend = false;

        private OrderAdjustMode accessMode = OrderAdjustMode.NONE;

        private OrderAdjustMode replaceMode = OrderAdjustMode.NONE;

        public Builder<K, V> setBidiInfo(BidiType bidiType) {
            this.bidiType = bidiType != null ? bidiType : BidiType.NONE;
            return this;
        }
        
        public Builder<K, V> setKeyReplacementRule(ReplacementRule keyReplacementRule) {
            this.keyReplacementRule = keyReplacementRule != null ? keyReplacementRule : ReplacementRule.NEW_REFERENCE_WIN;
            return this;
        }

        public Builder<K, V> setKeyEqualityComparator(EqualityComparator<? super K> keyEqualityComparator) {
            this.keyEqualityComparator = keyEqualityComparator;
            return this;
        }

        public Builder<K, V> setValueEqualityComparator(EqualityComparator<? super V> valueEqualityComparator) {
            if (valueEqualityComparator != null) {
                if (this.valueComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "valueComparator",
                                    "valueEqualityComparator"
                            )
                    );
                }
                if (this.valueUnifiedComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "valueUnifiedComparator",
                                    "valueEqualityComparator"
                            )
                    );
                }
            }
            this.valueEqualityComparator = valueEqualityComparator;
            return this;
        }

        public Builder<K, V> setValueComparator(Comparator<? super V> valueComparator) {
            if (valueComparator != null) {
                if (this.valueEqualityComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "valueEqualityComparator",
                                    "valueComparator"
                            )
                    );
                }
                if (this.valueUnifiedComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "valueUnifiedComparator",
                                    "valueComparator"
                            )
                    );
                }
            }
            this.valueComparator = valueComparator;
            return this;
        }

        public Builder<K, V> setValueUnifiedComparator(UnifiedComparator<? super V> valueUnifiedComparator) {
            if (valueUnifiedComparator != null) {
                if (this.valueEqualityComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "valueEqualityComparator",
                                    "valueUnifiedComparator"
                            )
                    );
                }
                if (this.valueComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "valueComparator",
                                    "valueUnifiedComparator"
                            )
                    );
                }
            }
            this.valueUnifiedComparator = valueUnifiedComparator;
            return this;
        }

        public Builder<K, V> setInitCapacity(int initCapacity) {
            this.initCapacity = initCapacity;
            return this;
        }

        public Builder<K, V> setLoadFactor(Float loadFactor) {
            this.loadFactor = loadFactor;
            return this;
        }

        public Builder<K, V> setHeadAppend(boolean headAppend) {
            this.headAppend = headAppend;
            return this;
        }

        public Builder<K, V> setAccessMode(OrderAdjustMode accessMode) {
            this.accessMode = accessMode != null ? accessMode : OrderAdjustMode.NONE;
            return this;
        }

        public Builder<K, V> setReplaceMode(OrderAdjustMode replaceMode) {
            this.replaceMode = replaceMode != null ? replaceMode : OrderAdjustMode.NONE;
            return this;
        }

        public MALinkedHashMap<K, V> build() {
            if (this.valueEqualityComparator != null) {
                return new MALinkedHashMap<K, V>(
                        this.bidiType,
                        this.keyReplacementRule,
                        this.keyEqualityComparator,
                        this.valueEqualityComparator,
                        this.initCapacity,
                        this.loadFactor,
                        this.headAppend,
                        this.accessMode,
                        this.replaceMode
                );
            } else if (this.valueComparator != null) {
                return new MALinkedHashMap<K, V>(
                        this.bidiType,
                        this.keyReplacementRule,
                        this.keyEqualityComparator,
                        this.valueComparator,
                        this.initCapacity,
                        this.loadFactor,
                        this.headAppend,
                        this.accessMode,
                        this.replaceMode
                );
            } else {
                return new MALinkedHashMap<K, V>(
                        this.bidiType,
                        this.keyReplacementRule,
                        this.keyEqualityComparator,
                        this.valueUnifiedComparator,
                        this.initCapacity,
                        this.loadFactor,
                        this.headAppend,
                        this.accessMode,
                        this.replaceMode
                );
            }
        }
    }
}
