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
public class CollectionModifications {
    
    public static <E> AddByElement<E> add(E e) {
        return new AddByElement<>(e);
    }
    
    public static <E> AddAllByCollection<E> addAll(Collection<? extends E> c) {
        return new AddAllByCollection<>(c);
    }
    
    public static <E> Clear<E> clear() {
        return new Clear<>();
    }
    
    public static <E> RemoveByElement<E> remove(Object o) {
        return new RemoveByElement<>(o);
    }
    
    public static <E> RemoveAllByCollection<E> removeAll(Collection<?> c) {
        return new RemoveAllByCollection<>(c);
    }
    
    public static <E> RetainAllByCollection<E> retainAll(Collection<?> c) {
        return new RetainAllByCollection<>(c);
    }
    
    public static class AddByElement<E> extends AbstractModification implements Modification<E> {
      
        private static final long serialVersionUID = -3762649988679044344L;
        
        private E element;
        
        AddByElement(E element) {
            this.element = element;
        }
        
        public E getElement() {
            return this.element;
        }
        
        protected void appendTo(Appender appender) {
            appender.property("element", this.element);
        }
    }
    
    public static class AddAllByCollection<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = 7799736440367568632L;
        
        private Collection<? extends E> collection;
        
        AddAllByCollection(Collection<? extends E> collection) {
            this.collection = collection;
        }
        
        public Collection<? extends E> getCollection() {
            return this.collection;
        }
        
        protected void appendTo(Appender appender) {
            appender.property("collection", this.collection);
        }
    }
    
    public static class Clear<E> extends AbstractModification implements Modification<E> {
     
        private static final long serialVersionUID = -2300141939927261178L;

        Clear() {}
    }
    
    public static class RemoveByElement<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = -4852873471181377009L;
        
        private Object element;
        
        RemoveByElement(Object element) {
            this.element = element;
        }
        
        public Object getElement() {
            return this.element;
        }
        
        protected void appendTo(Appender appender) {
            appender.property("element", this.element);
        }
    }
    
    public static class RemoveAllByCollection<E> extends AbstractModification implements Modification<E> {
       
        private static final long serialVersionUID = 2901313716287611505L;
        
        private Collection<?> collection;
        
        RemoveAllByCollection(Collection<?> collection) {
            this.collection = collection;
        }
        
        public Collection<?> getCollection() {
            return this.collection;
        }
        
        protected void appendTo(Appender appender) {
            appender.property("collection", this.collection);
        }
    }
    
    public static class RetainAllByCollection<E> extends AbstractModification implements Modification<E> {
       
        private static final long serialVersionUID = 7179179154618739210L;
        
        private Collection<?> collection;
        
        RetainAllByCollection(Collection<?> collection) {
            this.collection = collection;
        }
        
        public Collection<?> getCollection() {
            return this.collection;
        }
        
        protected void appendTo(Appender appender) {
            appender.property("collection", this.collection);
        }
    }
    
    @Deprecated
    protected CollectionModifications() {
        throw new UnsupportedOperationException();
    }
}
