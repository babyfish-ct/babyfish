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
import org.babyfish.collection.event.ListElementEvent;

/**
 * @author Tao Chen
 */
public abstract class AbstractListElementEventDispatcher<E> extends AbstractElementEventDispatcher<E> {

    protected AbstractListElementEventDispatcher(Object owner) {
        super(owner);
    }

    @Deprecated
    @Override
    protected final void executePreDispatchedEvent(ElementEvent<E> dispatchedEvent) {
        this.executePreDispatchedEvent((ListElementEvent<E>)dispatchedEvent);
    }

    @Deprecated
    @Override
    protected final void executePostDispatchedEvent(ElementEvent<E> dispatchedEvent) {
        this.executePostDispatchedEvent((ListElementEvent<E>)dispatchedEvent);
    }
    
    protected abstract void executePreDispatchedEvent(ListElementEvent<E> dispatchedEvent);

    protected abstract void executePostDispatchedEvent(ListElementEvent<E> dispatchedEvent);

}
