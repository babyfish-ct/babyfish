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

import org.babyfish.collection.event.modification.EntryModifications;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyBubbler;
import org.babyfish.data.event.SharedPropertyBubbler;

/**
 * @author Tao Chen
 */
public class EntryElementEvent<K, V> extends ElementEvent<V> {

    private static final long serialVersionUID = -3648470473416323494L;
    
    private K key;
    
    protected EntryElementEvent(
            Object source, 
            EntryModifications.SetByValue<V> modification,
            ModificationType modificationType,
            V detachedElement, 
            V attachedElement,
            K key) {
        super(source, modification, modificationType, detachedElement, attachedElement);
        this.key = key;
    }
    
    public EntryElementEvent(
            Object source, 
            Cause cause, 
            PropertyBubbler<V> elementBubbler,
            SharedPropertyBubbler<K> keyBubbler) {
        super(source, cause, elementBubbler);
        if (keyBubbler == null) {
            EntryElementEvent<K, V> viewEvent = cause.getViewEvent();
            this.key = viewEvent.key;
        } else {
            this.key = keyBubbler.bubble();
        }
    }

    protected EntryElementEvent(Object source, EntryElementEvent<K, V> target) {
        super(source, target);
        this.key = target.key;
    }
    
    public K getKey() {
        return this.key;
    }
    
    @Override
    public EntryElementEvent<K, V> dispatch(Object source) {
        return new EntryElementEvent<>(source, this);
    }

    public static <K, V> EntryElementEvent<K, V> createDetachEvent(
            Object source, 
            EntryModifications.SetByValue<V> modification, 
            V element, 
            K key) {
        return new EntryElementEvent<>(
                source, 
                modification, 
                ModificationType.DETACH,
                element, 
                null,
                key
        );
    }
    
    public static <K, V> EntryElementEvent<K, V> createAttachEvent(
            Object source, 
            EntryModifications.SetByValue<V> modification, 
            V element, 
            K key) {
        return new EntryElementEvent<>(
                source, 
                modification, 
                ModificationType.ATTACH,
                null, 
                element,
                key
        );
    }
    
    public static <K, V> EntryElementEvent<K, V> createReplaceEvent(
            Object source, 
            EntryModifications.SetByValue<V> modification, 
            V detachedElement, 
            V attachedElement, 
            K key) {
        return new EntryElementEvent<>(
                source, 
                modification, 
                ModificationType.REPLACE,
                detachedElement, 
                attachedElement, 
                key
        );
    }
}
