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
import java.util.Comparator;

import org.babyfish.collection.spi.AbstractMANavigableSet;
import org.babyfish.collection.spi.base.RedBlackTreeEntries;

/**
 * @author Tao Chen
 */
public class MATreeSet<E> extends AbstractMANavigableSet<E> implements Serializable {

    private static final long serialVersionUID = -7135734186865322793L;

    public MATreeSet() {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null
                )
        );
    }

    public MATreeSet(ReplacementRule replacementRule) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null
                )
        );
    }

    public MATreeSet(Comparator<? super E> comparator) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        comparator,
                        null
                )
        );
    }

    public MATreeSet(ReplacementRule replacementRule, Comparator<? super E> comparator) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        replacementRule,
                        comparator,
                        null
                )
        );
    }

    public MATreeSet(Collection<? extends E> c) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null
                )
        );
        this.addAll(c);
    }

    public MATreeSet(ReplacementRule replacementRule, Collection<? extends E> c) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        replacementRule,
                        null,
                        null
                )
        );
        this.addAll(c);
    }

    public MATreeSet(Comparator<? super E> comparator, Collection<? extends E> c) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        comparator,
                        null
                )
        );
        this.addAll(c);
    }

    public MATreeSet(
            ReplacementRule replacementRule,
            Comparator<? super E> comparator,
            Collection<? extends E> c) {
        super(
                new RedBlackTreeEntries.TransientValue<E, Object>(
                        replacementRule,
                        comparator,
                        null
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

        private Comparator<? super E> comparator = null;

        public Builder<E> setReplacementRule(ReplacementRule replacementRule) {
            this.replacementRule = replacementRule != null ? replacementRule : ReplacementRule.NEW_REFERENCE_WIN;
            return this;
        }

        public Builder<E> setComparator(Comparator<? super E> comparator) {
            this.comparator = comparator;
            return this;
        }

        public MATreeSet<E> build() {
            return new MATreeSet<E>(
                    this.replacementRule,
                    this.comparator
            );
        }
    }
}
