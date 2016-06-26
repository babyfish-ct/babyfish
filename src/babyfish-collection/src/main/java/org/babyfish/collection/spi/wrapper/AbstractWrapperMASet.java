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
package org.babyfish.collection.spi.wrapper;

import java.util.Comparator;

import org.babyfish.collection.MACollection;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperMASet<E> extends AbstractWrapperMACollection<E> implements MASet<E> {

    protected AbstractWrapperMASet(MASet<E> base) {
        super(base);
    }

    protected AbstractWrapperMASet(
            AbstractWrapperMASet<E> parent, ViewInfo viewInfo) {
        super(parent, viewInfo);
    }
    
    /**
     * This method should not be invoked by the customer immediately.
     * 
     * <p>
     * It is used to create the instance during the when 
     * {@link java.io.ObjectInputStream} reads this object from a stream.
     * Although the derived classes of this class may implement {@link java.io.Serializable},
     * but this abstract super class does not implement {@link java.io.Serializable}
     * because it have some derived class that implements {@link View} which can 
     * not be implement {@link java.io.Serializable}
     * </p>
     * 
     * <p>
     * If the derived class is still a class does not implement {@link java.io.Serializable},
     * please support a no arguments constructor and mark it with {@link Deprecated}  too, 
     * like this method.
     * </p>
     */
    @Deprecated
    protected AbstractWrapperMASet() {
        
    }

    @Override
    public ReplacementRule replacementRule() {
        return this.<MASet<E>>getBase().replacementRule();
    }

    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }

    @Deprecated
    @Override
    protected final MACollection<E> createBaseView(
            MACollection<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((MASet<E>)parentBase, viewInfo);
    }
    
    protected MASet<E> createBaseView(MASet<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractWrapperMACollection.AbstractIteratorImpl<E> {

        public AbstractIteratorImpl(
                AbstractWrapperMASet<E> owner,
                ViewInfo viewInfo) {
            super(owner, viewInfo);
        }

        @Deprecated
        @Override
        protected final MAIterator<E> createBaseView(
                MACollection<E> baseParent, 
                ViewInfo viewInfo) {
            return this.createBaseView(
                    (MASet<E>)baseParent, 
                    viewInfo);
        }
        
        protected abstract MAIterator<E> createBaseView(
                MASet<E> baseParent, 
                ViewInfo viewInfo);
        
    }
    
    protected static class RootData<E> extends AbstractWrapperMACollection.RootData<E> {

        private static final long serialVersionUID = 8872386927417070796L;
        
        public RootData() {
            
        }

        @Override
        @Deprecated
        protected final void setBase(MACollection<E> base) {
            this.setBase((MASet<E>)base);
        }
        
        protected void setBase(MASet<E> base) {
            super.setBase(base);
        }

        @Override
        protected MASet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            Comparator<? super E> comparator = unifiedComparator.comparator();
            if (comparator != null) {
                return new MATreeSet<E>(comparator);
            }
            return new MAHashSet<E>(unifiedComparator.equalityComparator());
        }
        
    }
}
