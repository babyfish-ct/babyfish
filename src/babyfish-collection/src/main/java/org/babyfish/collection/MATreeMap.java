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

import org.babyfish.collection.spi.AbstractMANavigableMap;
import org.babyfish.collection.spi.base.RedBlackTreeEntries;

/**
 * @author Tao Chen
 */
public class MATreeMap<K, V> extends AbstractMANavigableMap<K, V> implements Serializable {
    
    private static final long serialVersionUID = -8835235183099301793L;
    
    public MATreeMap() {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null
                )
        );
    }

    public MATreeMap(ReplacementRule keyReplacementRule) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null
                )
        );
    }

    public MATreeMap(Comparator<? super K> keyComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        null
                )
        );
    }

    public MATreeMap(Comparator<? super K> keyComparator, EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueEqualityComparator
                )
        );
    }

    public MATreeMap(Comparator<? super K> keyComparator, Comparator<? super V> valueComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueComparator
                )
        );
    }

    public MATreeMap(Comparator<? super K> keyComparator, UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
    }

    public MATreeMap(ReplacementRule keyReplacementRule, Comparator<? super K> keyComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        null
                )
        );
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        valueEqualityComparator
                )
        );
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            Comparator<? super V> valueComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        valueComparator
                )
        );
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
    }

    public MATreeMap(Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(ReplacementRule keyReplacementRule, Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        null,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(Comparator<? super K> keyComparator, Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            Comparator<? super K> keyComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueEqualityComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            Comparator<? super K> keyComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            Comparator<? super K> keyComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        valueEqualityComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        valueComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        BidiType.NONE,
                        keyReplacementRule,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
        this.putAll(m);
    }
    
    public MATreeMap(BidiType bidiType) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        null
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator, 
            EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueEqualityComparator
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator, 
            Comparator<? super V> valueComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueComparator
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator, 
            UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule, 
            Comparator<? super K> keyComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        null
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            EqualityComparator<? super V> valueEqualityComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        valueEqualityComparator
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            Comparator<? super V> valueComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        valueComparator
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            UnifiedComparator<? super V> valueUnifiedComparator) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
    }

    public MATreeMap(
            BidiType bidiType, 
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        null,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule, 
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        null,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator, 
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueEqualityComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            Comparator<? super K> keyComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        ReplacementRule.NEW_REFERENCE_WIN,
                        keyComparator,
                        valueUnifiedComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        null
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            EqualityComparator<? super V> valueEqualityComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        valueEqualityComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            Comparator<? super V> valueComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        valueComparator
                )
        );
        this.putAll(m);
    }

    public MATreeMap(
            BidiType bidiType, 
            ReplacementRule keyReplacementRule,
            Comparator<? super K> keyComparator,
            UnifiedComparator<? super V> valueUnifiedComparator,
            Map<? extends K, ? extends V> m) {
        super(
                new RedBlackTreeEntries<K, V>(
                        bidiType,
                        keyReplacementRule,
                        keyComparator,
                        valueUnifiedComparator
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

        private Comparator<? super K> keyComparator = null;

        private EqualityComparator<? super V> valueEqualityComparator = null;

        private Comparator<? super V> valueComparator = null;

        private UnifiedComparator<? super V> valueUnifiedComparator = null;

        public Builder<K, V> setBidiInfo(BidiType bidiType) {
            this.bidiType = bidiType != null ? bidiType : BidiType.NONE;
            return this;
        }
        
        public Builder<K, V> setKeyReplacementRule(ReplacementRule keyReplacementRule) {
            this.keyReplacementRule = keyReplacementRule != null ? keyReplacementRule : ReplacementRule.NEW_REFERENCE_WIN;
            return this;
        }

        public Builder<K, V> setKeyComparator(Comparator<? super K> keyComparator) {
            this.keyComparator = keyComparator;
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

        public MATreeMap<K, V> build() {
            if (this.valueEqualityComparator != null) {
                return new MATreeMap<K, V>(
                        this.bidiType,
                        this.keyReplacementRule,
                        this.keyComparator,
                        this.valueEqualityComparator
                );
            } else if (this.valueComparator != null) {
                return new MATreeMap<K, V>(
                        this.bidiType,
                        this.keyReplacementRule,
                        this.keyComparator,
                        this.valueComparator
                );
            } else {
                return new MATreeMap<K, V>(
                        this.bidiType,
                        this.keyReplacementRule,
                        this.keyComparator,
                        this.valueUnifiedComparator
                );
            }
        }
    }
}
