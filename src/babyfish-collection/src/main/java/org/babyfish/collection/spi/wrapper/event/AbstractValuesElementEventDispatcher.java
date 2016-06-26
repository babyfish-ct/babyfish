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
import org.babyfish.collection.event.ValuesElementEvent;

/**
 * @author Tao Chen
 */
public abstract class AbstractValuesElementEventDispatcher<K, V> extends AbstractElementEventDispatcher<V> {

    protected AbstractValuesElementEventDispatcher(Object owner) {
        super(owner);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void executePreDispatchedEvent(ElementEvent<V> dispatchedEvent) {
        this.executePreDispatchedEvent((ValuesElementEvent<K, V>)dispatchedEvent);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void executePostDispatchedEvent(ElementEvent<V> dispatchedEvent) {
        this.executePostDispatchedEvent((ValuesElementEvent<K, V>)dispatchedEvent);
    }
    
    protected abstract void executePreDispatchedEvent(ValuesElementEvent<K, V> dispatchedEvent);

    protected abstract void executePostDispatchedEvent(ValuesElementEvent<K, V> dispatchedEvent);

}
