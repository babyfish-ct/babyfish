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
package org.babyfish.collection.event;

import org.babyfish.data.event.Cause;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyBubbler;
import org.babyfish.data.event.SharedPropertyBubbler;

/**
 * @author Tao Chen
 */
public class KeySetElementEvent<K, V> extends ElementEvent<K> {
  
    private static final long serialVersionUID = 5768984637255552521L;
    
    private V value;
    
    protected KeySetElementEvent(
            Object source, 
            Modification<K> modification,
            K detachedElement,
            V value) {
        super(source, modification, ModificationType.DETACH, detachedElement, null);
        this.value = value;
    }
    
    public KeySetElementEvent(
            Object source, 
            Cause cause, 
            PropertyBubbler<K> elementBubbler,
            SharedPropertyBubbler<V> valueBubbler) {
        super(source, cause, elementBubbler);
        if (valueBubbler == null) {
            KeySetElementEvent<K, V> viewEvent = cause.getViewEvent();
            this.value = viewEvent.value;
        } else {
            this.value = valueBubbler.bubble();
        }
    }

    protected KeySetElementEvent(Object source, KeySetElementEvent<K, V> target) {
        super(source, target);
        this.value = target.value;
    }
    
    public V getValue() {
        return this.value;
    }
    
    @Override
    public KeySetElementEvent<K, V> dispatch(Object source) {
        return new KeySetElementEvent<>(source, this);
    }

    public static <K, V> KeySetElementEvent<K, V> createDetachEvent(
            Object source, Modification<K> modification, K element, V value) {
        return new KeySetElementEvent<>(source, modification, element, value);
    }
}
