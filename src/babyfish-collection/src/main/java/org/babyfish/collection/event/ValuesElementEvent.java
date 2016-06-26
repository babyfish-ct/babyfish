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
public class ValuesElementEvent<K, V> extends ElementEvent<V> {
   
    private static final long serialVersionUID = 7262149985799563283L;
    
    private K key;
    
    protected ValuesElementEvent(
            Object source, 
            Modification<V> modification,
            V detachedElement, 
            K key) {
        super(source, modification, ModificationType.DETACH, detachedElement, null);
        this.key = key;
    }
    
    public ValuesElementEvent(
            Object source, 
            Cause cause, 
            PropertyBubbler<V> elementBubbler,
            SharedPropertyBubbler<K> keyBubbler) {
        super(source, cause, elementBubbler);
        if (keyBubbler == null) {
            ValuesElementEvent<K, V> viewEvent = cause.getViewEvent();
            this.key = viewEvent.key;
        } else {
            this.key = keyBubbler.bubble();
        }
    }

    protected ValuesElementEvent(Object source, ValuesElementEvent<K, V> target) {
        super(source, target);
        this.key = target.key;
    }
    
    public K getKey() {
        return this.key;
    }
    
    @Override
    public ValuesElementEvent<K, V> dispatch(Object source) {
        return new ValuesElementEvent<>(source, this);
    }

    public static <K, V> ValuesElementEvent<K, V> createDetachEvent(
            Object source, Modification<V> modification, V element, K key) {
        return new ValuesElementEvent<>(source, modification, element, key);
    }
}
