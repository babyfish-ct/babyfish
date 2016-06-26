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
package org.babyfish.collection.spi.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.ReplacementRule;

/**
 * @author Tao Chen
 */
public abstract class AbstractRootBaseEntriesImpl<K, V> extends AbstractBaseEntriesImpl<K, V> implements Serializable {

    private static final long serialVersionUID = -6659880645844452222L;

    public AbstractRootBaseEntriesImpl(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            Object keyComparatorOrEqualityComparatorOrUnifiedComparator,
            Object valueComparatorOrEqualityComparatorOrUnifiedComparator) {
        super(
                bidiType,
                keyReplacementRule, 
                keyComparatorOrEqualityComparatorOrUnifiedComparator,
                valueComparatorOrEqualityComparatorOrUnifiedComparator);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.write(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.read(in);
    }
}
