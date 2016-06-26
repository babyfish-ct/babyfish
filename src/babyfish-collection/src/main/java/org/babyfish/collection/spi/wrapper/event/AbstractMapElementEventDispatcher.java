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

import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class AbstractMapElementEventDispatcher<K, V> implements MapElementListener<K, V> {

    private Object owner;
    
    protected AbstractMapElementEventDispatcher(Object owner) {
        Arguments.mustNotBeNull("owner", owner);
        this.owner = owner;
    }
    
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        return (T)this.owner;
    }
    
    protected abstract boolean isDispatchable();
    
    protected abstract void executePreDispatchedEvent(MapElementEvent<K, V> dispatchedEvent);
    
    protected abstract void executePostDispatchedEvent(MapElementEvent<K, V> dispatchedEvent);
    
    @Override
    public final void modifying(MapElementEvent<K, V> e) throws Throwable {
        //bubble event can not be dispatched.
        if (e.getCause() == null && this.isDispatchable()) {
            this.executePreDispatchedEvent(e.dispatch(this.owner)); 
        }
    }

    @Override
    public final void modified(MapElementEvent<K, V> e) throws Throwable {
        MapElementEvent<K, V> dispatchedEvent = e.getDispatchedEvent(this.owner);
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
        return this.owner == ((AbstractMapElementEventDispatcher<?, ?>)obj).owner;
    }

}
