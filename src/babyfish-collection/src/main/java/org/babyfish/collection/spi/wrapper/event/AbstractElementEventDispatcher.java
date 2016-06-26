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
package org.babyfish.collection.spi.wrapper.event;

import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class AbstractElementEventDispatcher<E> implements ElementListener<E> {
    
    private Object owner;
    
    protected AbstractElementEventDispatcher(Object owner) {
        Arguments.mustNotBeNull("owner", owner);
        this.owner = owner;
    }
    
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        return (T)this.owner;
    }
    
    protected abstract boolean isDispatchable();
    
    protected abstract void executePreDispatchedEvent(ElementEvent<E> dispatchedEvent);
    
    protected abstract void executePostDispatchedEvent(ElementEvent<E> dispatchedEvent);
    
    @Override
    public final void modifying(ElementEvent<E> e) throws Throwable {
        //bubble event can not be dispatched.
        if (e.getCause() == null && this.isDispatchable()) {
            this.executePreDispatchedEvent(e.dispatch(this.owner));
        }
    }

    @Override
    public final void modified(ElementEvent<E> e) throws Throwable {
        ElementEvent<E> dispatchedEvent = e.getDispatchedEvent(this.owner);
        if (dispatchedEvent != null) {
            this.executePostDispatchedEvent(dispatchedEvent);
        }
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this.owner);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return this.owner == ((AbstractElementEventDispatcher<?>)obj).owner;
    }

}
