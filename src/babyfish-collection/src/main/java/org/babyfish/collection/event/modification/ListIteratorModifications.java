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

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.data.event.spi.AbstractModification;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class ListIteratorModifications extends IteratorModifications {

    public static <E> AddByElement<E> add(E element) {
        return new AddByElement<>(element);
    }
    
    public static <E> SetByElement<E> set(E element) {
        return new SetByElement<>(element);
    }
    
    public static class AddByElement<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = 498460831531894960L;
        
        private E element;
        
        public AddByElement(E element) {
            this.element = element;
        }
        
        public E getElement() {
            return this.element;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("element", element);
        }
    }
    
    public static class SetByElement<E> extends AbstractModification implements Modification<E> {
     
        private static final long serialVersionUID = 4788681420653624685L;
        
        private E element;
        
        public SetByElement(E element) {
            this.element = element;
        }
        
        public E getElement() {
            return this.element;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("element", element);
        }
    }
    
    @Deprecated
    protected ListIteratorModifications() {}
}
