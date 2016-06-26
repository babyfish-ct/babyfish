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
package org.babyfish.collection.event.modification;

import java.util.Collection;

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.data.event.spi.AbstractModification;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class ListModifications extends CollectionModifications {

    public static <E> AddByIndexAndElement<E> add(int index, E element) {
        return new AddByIndexAndElement<>(index, element);
    }
    
    public static <E> AddAllByIndexAndCollection<E> addAll(int index, Collection<? extends E> c) {
        return new AddAllByIndexAndCollection<>(index, c);
    }
    
    public static <E> RemoveByIndex<E> remove(int index) {
        return new RemoveByIndex<>(index);
    }
    
    public static <E> SetByIndexAndElement<E> set(int index, E element) {
        return new SetByIndexAndElement<>(index, element);
    }
    
    public static <E> SuspendViaInversedFrozenContext<E> suspendViaInversedFrozenContext(E element) {
        return new SuspendViaInversedFrozenContext<>(element);
    }
    
    public static <E> ResumeViaInversedFrozenContext<E> resumeViaInversedFrozenContext() {
        return new ResumeViaInversedFrozenContext<>();
    }
    
    public static class AddByIndexAndElement<E> extends CollectionModifications.AddByElement<E> {
     
        private static final long serialVersionUID = -6880874428177919948L;
        
        private int index;
        
        AddByIndexAndElement(int index, E element) {
            super(element);
            this.index = index;
        }
        
        public int getIndex() {
            return this.index;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", this.index);
            super.appendTo(appender);
        }
    }
    
    public static class AddAllByIndexAndCollection<E> extends CollectionModifications.AddAllByCollection<E> {
      
        private static final long serialVersionUID = -8014919482066356593L;
        
        private int index;
        
        AddAllByIndexAndCollection(int index, Collection<? extends E> c) {
            super(c);
            this.index = index;
        }
        
        public int getIndex() {
            return this.index;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", this.index);
            super.appendTo(appender);
        }
    }
    
    public static class RemoveByIndex<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = 836197991706413148L;
        
        private int index;

        public RemoveByIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", this.index);
        }
    }
    
    public static class SetByIndexAndElement<E> extends AbstractModification implements Modification<E> {
      
        private static final long serialVersionUID = -5482692727962445719L;

        private int index;
        
        private E element;

        public SetByIndexAndElement(int index, E element) {
            this.index = index;
            this.element = element;
        }

        public int getIndex() {
            return this.index;
        }

        public E getElement() {
            return this.element;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", this.index).property("element", this.element);
        }
    }
    
    public static class SuspendViaInversedFrozenContext<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = 753126114118005818L;
        
        private E element;
        
        SuspendViaInversedFrozenContext(E element) {
            this.element = element;
        }
        
        public E getElement() {
            return this.element;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("element", this.element);
        }
    }
    
    public static class ResumeViaInversedFrozenContext<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = 1506205786812367570L;

        ResumeViaInversedFrozenContext() {}
    }
    
    @Deprecated
    protected ListModifications() {}
}
