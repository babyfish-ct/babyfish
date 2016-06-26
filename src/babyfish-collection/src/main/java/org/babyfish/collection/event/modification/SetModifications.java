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
public class SetModifications extends CollectionModifications {
    
    public static <E> SuspendViaFrozenContext<E> suspendViaFrozenContext(E element) {
        return new SuspendViaFrozenContext<>(element);
    }
    
    public static <E> ResumeViaFrozenContext<E> resumeViaFrozenContext() {
        return new ResumeViaFrozenContext<>();
    }
    
    public static class SuspendViaFrozenContext<E> extends AbstractModification implements Modification<E> {
    
        private static final long serialVersionUID = -5563446841562946067L;
        
        private E element;
        
        SuspendViaFrozenContext(E element) {
            this.element = element;
        }
        
        E getElement() {
            return this.element;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("element", this.element);
        }
    }
    
    public static class ResumeViaFrozenContext<E> extends AbstractModification implements Modification<E> {
        
        private static final long serialVersionUID = -7507264983043299574L;

        ResumeViaFrozenContext() {}
    }
    
    @Deprecated
    protected SetModifications() {}
}
