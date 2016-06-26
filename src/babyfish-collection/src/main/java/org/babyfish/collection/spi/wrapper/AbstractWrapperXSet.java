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

import org.babyfish.collection.HashSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XSet;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperXSet<E> extends AbstractWrapperXCollection<E> implements XSet<E> {

    protected AbstractWrapperXSet(XSet<E> base) {
        super(base);
    }

    protected AbstractWrapperXSet(AbstractWrapperXSet<E> parent, ViewInfo viewInfo) {
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
    protected AbstractWrapperXSet() {
        
    }

    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }

    @Deprecated
    @Override
    protected final XCollection<E> createBaseView(
            XCollection<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((XSet<E>)parentBase, viewInfo);
    }

    protected XSet<E> createBaseView(XSet<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }

    @Override
    public ReplacementRule replacementRule() {
        return this.<XSet<E>>getBase().replacementRule();
    }
    
    protected static class RootData<E> extends AbstractWrapperXCollection.RootData<E> {

        private static final long serialVersionUID = -2437466386186805762L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XCollection<E> base) {
            this.setBase((XSet<E>)base);
        }
        
        protected void setBase(XSet<E> base) {
            super.setBase(base);
        }

        @Override
        protected XSet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            Comparator<? super E> comparator = unifiedComparator.comparator();
            if (comparator != null) {
                return new TreeSet<E>(comparator);
            }
            return new HashSet<E>(unifiedComparator.equalityComparator());
        }
        
    }
    
}
