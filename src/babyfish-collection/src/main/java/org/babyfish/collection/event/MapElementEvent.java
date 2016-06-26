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
import org.babyfish.data.event.ModificationEvent;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyBubbler;
import org.babyfish.data.event.PropertyVersion;

/**
 * @author Tao Chen
 */
public class MapElementEvent<K, V> extends ModificationEvent {

    private static final long serialVersionUID = 5456891248940047423L;

    private K detachedKey;
    
    private K attachedKey;
    
    private V detachedValue;
    
    private V attachedValue;

    protected MapElementEvent(
            Object source, 
            MapModification<K, V> modification, 
            ModificationType modificationType,
            K detachedKey,
            K attachedKey,
            V detachedValue,
            V attachedValue) {
        super(source, modification, modificationType);
        this.detachedKey = detachedKey;
        this.attachedKey = attachedKey;
        this.detachedValue = detachedValue;
        this.attachedValue = attachedValue;
    }

    public MapElementEvent(
            Object source, 
            Cause cause,
            PropertyBubbler<K> keyBubbler,
            PropertyBubbler<V> valueBubbler) {
        super(source, cause);
        if (keyBubbler == null) {
            MapElementEvent<K, V> viewEvent = cause.getViewEvent();
            this.detachedKey = viewEvent.detachedKey;
            this.attachedKey = viewEvent.attachedKey;
        } else {
            ModificationType modificationType = this.getModificationType();
            if (modificationType.contains(PropertyVersion.DETACH)) {
                this.detachedKey = keyBubbler.bubble(PropertyVersion.DETACH);
            }
            if (modificationType.contains(PropertyVersion.ATTACH)) {
                this.attachedKey = keyBubbler.bubble(PropertyVersion.ATTACH);
            }
        }
        if (valueBubbler == null) {
            MapElementEvent<K, V> viewEvent = cause.getViewEvent();
            this.detachedValue = viewEvent.detachedValue;
            this.attachedValue = viewEvent.attachedValue;
        } else {
            ModificationType modificationType = this.getModificationType();
            if (modificationType.contains(PropertyVersion.DETACH)) {
                this.detachedValue = valueBubbler.bubble(PropertyVersion.DETACH);
            }
            if (modificationType.contains(PropertyVersion.ATTACH)) {
                this.attachedValue = valueBubbler.bubble(PropertyVersion.ATTACH);
            }
        }
    }
    
    protected MapElementEvent(Object source, MapElementEvent<K, V> target) {
        super(source, target);
        this.detachedKey = target.detachedKey;
        this.attachedKey = target.attachedKey;
        this.detachedValue = target.detachedValue;
        this.attachedValue = target.attachedValue;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public MapModification<K, V> getModification() {
        return (MapModification<K, V>)super.getModification();
    }
    
    public K getKey(PropertyVersion version) {
        switch (version) {
        case DETACH:
            return this.detachedKey;
        case ATTACH:
            return this.attachedKey;
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    public V getValue(PropertyVersion version) {
        switch (version) {
        case DETACH:
            return this.detachedValue;
        case ATTACH:
            return this.attachedValue;
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    @Override
    public MapElementEvent<K, V> dispatch(Object source) {
        return new MapElementEvent<>(source, this);
    }

    public static <K, V> MapElementEvent<K, V> createDetachEvent(
            Object source, 
            MapModification<K, V> modification,
            K key,
            V value) {
        return new MapElementEvent<>(
                source,
                modification,
                ModificationType.DETACH,
                key,
                null,
                value,
                null
        );
    }
    
    public static <K, V> MapElementEvent<K, V> createAttachEvent(
            Object source, 
            MapModification<K, V> modification,
            K key,
            V value) {
        return new MapElementEvent<>(
                source,
                modification,
                ModificationType.ATTACH,
                null,
                key,
                null,
                value
        );
    }
    
    public static <K, V> MapElementEvent<K, V> createReplaceEvent(
            Object source, 
            MapModification<K, V> modification,
            K detachedKey,
            K attachedkey,
            V detachedValue,
            V attachedValue) {
        return new MapElementEvent<>(
                source,
                modification,
                ModificationType.REPLACE,
                detachedKey,
                attachedkey,
                detachedValue,
                attachedValue
        );
    }
    
    public interface MapModification<K, V> extends org.babyfish.data.event.Modification {}
}
