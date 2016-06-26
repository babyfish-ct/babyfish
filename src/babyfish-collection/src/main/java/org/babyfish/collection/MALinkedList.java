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

import org.babyfish.collection.spi.AbstractMAList;
import org.babyfish.collection.spi.base.LinkedElements;

/**
 * @author Tao Chen
 */
public class MALinkedList<E> extends AbstractMAList<E> implements Serializable {

    private static final long serialVersionUID = -4678112481619491763L;

    public MALinkedList() {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        null
                )
        );
    }

    public MALinkedList(ReaderOptimizationType readerOptimizationType) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        null
                )
        );
    }

    public MALinkedList(EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        equalityComparator
                )
        );
    }

    public MALinkedList(Comparator<? super E> comparator) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        comparator
                )
        );
    }

    public MALinkedList(UnifiedComparator<? super E> unifiedComparator) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        unifiedComparator
                )
        );
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType, 
            EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        equalityComparator
                )
        );
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType, 
            Comparator<? super E> comparator) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        comparator
                )
        );
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType, 
            UnifiedComparator<? super E> unifiedComparator) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        unifiedComparator
                )
        );
    }

    public MALinkedList(Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        null
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType, 
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        null
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            EqualityComparator<? super E> equalityComparator, 
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        equalityComparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(Comparator<? super E> comparator, Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        comparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            UnifiedComparator<? super E> unifiedComparator, 
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        unifiedComparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType,
            EqualityComparator<? super E> equalityComparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        equalityComparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType,
            Comparator<? super E> comparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        comparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            ReaderOptimizationType readerOptimizationType,
            UnifiedComparator<? super E> unifiedComparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        BidiType.NONE,
                        readerOptimizationType,
                        unifiedComparator
                )
        );
        this.addAll(c);
    }
    
    public MALinkedList(BidiType bidiType) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        null
                )
        );
    }

    public MALinkedList(BidiType bidiType, ReaderOptimizationType readerOptimizationType) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        null
                )
        );
    }

    public MALinkedList(BidiType bidiType, EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        equalityComparator
                )
        );
    }

    public MALinkedList(BidiType bidiType, Comparator<? super E> comparator) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        comparator
                )
        );
    }

    public MALinkedList(BidiType bidiType, UnifiedComparator<? super E> unifiedComparator) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        unifiedComparator
                )
        );
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType, 
            EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        equalityComparator
                )
        );
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType, 
            Comparator<? super E> comparator) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        comparator
                )
        );
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType, 
            UnifiedComparator<? super E> unifiedComparator) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        unifiedComparator
                )
        );
    }

    public MALinkedList(BidiType bidiType, Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        null
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType, 
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        null
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            BidiType bidiType, 
            EqualityComparator<? super E> equalityComparator, 
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        equalityComparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(BidiType bidiType, Comparator<? super E> comparator, Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        comparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            BidiType bidiType, 
            UnifiedComparator<? super E> unifiedComparator, 
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        ReaderOptimizationType.OPTIMIZE_READING,
                        unifiedComparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType,
            EqualityComparator<? super E> equalityComparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        equalityComparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType,
            Comparator<? super E> comparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        comparator
                )
        );
        this.addAll(c);
    }

    public MALinkedList(
            BidiType bidiType, 
            ReaderOptimizationType readerOptimizationType,
            UnifiedComparator<? super E> unifiedComparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<>(
                        bidiType,
                        readerOptimizationType,
                        unifiedComparator
                )
        );
        this.addAll(c);
    }

    public ReaderOptimizationType readerOptimizationType() {
        return this.<LinkedElements<E>>getBaseElements().readerOptimizationType();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }
    
    public static class Builder<E> {
        
        private BidiType bidiType = BidiType.NONE;

        private ReaderOptimizationType readerOptimizationType = ReaderOptimizationType.OPTIMIZE_READING;

        private EqualityComparator<? super E> equalityComparator = null;

        private Comparator<? super E> comparator = null;

        private UnifiedComparator<? super E> unifiedComparator = null;

        public Builder<E> setBidiType(BidiType bidiType) {
            this.bidiType = bidiType != null ? bidiType : BidiType.NONE;
            return this;
        }
        
        public Builder<E> setReaderOptimizationType(ReaderOptimizationType readerOptimizationType) {
            this.readerOptimizationType = readerOptimizationType != null ? readerOptimizationType : ReaderOptimizationType.OPTIMIZE_READING;
            return this;
        }

        public Builder<E> setEqualityComparator(EqualityComparator<? super E> equalityComparator) {
            if (equalityComparator != null) {
                if (this.comparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "comparator",
                                    "equalityComparator"
                            )
                    );
                }
                if (this.unifiedComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "unifiedComparator",
                                    "equalityComparator"
                            )
                    );
                }
            }
            this.equalityComparator = equalityComparator;
            return this;
        }

        public Builder<E> setComparator(Comparator<? super E> comparator) {
            if (comparator != null) {
                if (this.equalityComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "equalityComparator",
                                    "comparator"
                            )
                    );
                }
                if (this.unifiedComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "unifiedComparator",
                                    "comparator"
                            )
                    );
                }
            }
            this.comparator = comparator;
            return this;
        }

        public Builder<E> setUnifiedComparator(UnifiedComparator<? super E> unifiedComparator) {
            if (unifiedComparator != null) {
                if (this.equalityComparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "equalityComparator",
                                    "unifiedComparator"
                            )
                    );
                }
                if (this.comparator != null) {
                    throw new IllegalStateException(
                            BuilderMessages.conflictProperties(
                                    Builder.class,
                                    "comparator",
                                    "unifiedComparator"
                            )
                    );
                }
            }
            this.unifiedComparator = unifiedComparator;
            return this;
        }

        public MALinkedList<E> build() {
            if (this.equalityComparator != null) {
                return new MALinkedList<>(
                        this.bidiType,
                        this.readerOptimizationType,
                        this.equalityComparator
                );
            } else if (this.comparator != null) {
                return new MALinkedList<>(
                        this.bidiType,
                        this.readerOptimizationType,
                        this.comparator
                );
            } else {
                return new MALinkedList<>(
                        this.bidiType,
                        this.readerOptimizationType,
                        this.unifiedComparator
                );
            }
        }
    }
}
