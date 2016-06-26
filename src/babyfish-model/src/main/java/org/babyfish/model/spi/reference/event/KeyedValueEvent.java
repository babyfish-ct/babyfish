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
package org.babyfish.model.spi.reference.event;

import org.babyfish.data.event.PropertyVersion;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class KeyedValueEvent<K, T> extends ValueEvent<T> {

    private static final long serialVersionUID = -4967212094010341525L;

    private K detachedKey;
    
    private K attachedKey;
    
    public KeyedValueEvent(
            Object source,
            KeyedModification<K, T> modification, 
            T detachedValue,
            T attachedValue,
            K detachedKey,
            K attachedKey) {
        super(source, modification, detachedValue, attachedValue);
        this.detachedKey = detachedKey;
        this.attachedKey = attachedKey;
    }

    public KeyedValueEvent(Object source, KeyedValueEvent<K, T> target) {
        super(source, target);
        this.detachedKey = target.detachedKey;
        this.attachedKey = target.attachedKey;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public KeyedModification<K, T> getModification() {
        return (KeyedModification<K, T>)super.getModification();
    }
    
    public K getKey(PropertyVersion version) {
        switch (Arguments.mustNotBeNull("version", version)) {
        case DETACH:
            return this.detachedKey;
        case ATTACH:
            return this.attachedKey;
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    @Override
    public KeyedValueEvent<K, T> dispatch(Object source) {
        return new KeyedValueEvent<>(source, this);
    }

    public static <K, T> KeyedValueEvent<K, T> createReplaceEvent(
            Object source,
            KeyedModification<K, T> modification, 
            T detachedValue,
            T attachedValue,
            K detachedKey,
            K attachedKey) {
        return new KeyedValueEvent<K, T>(
                source, 
                modification, 
                detachedValue, 
                attachedValue, 
                detachedKey, 
                attachedKey
        );
    }
    
    public interface KeyedModification<K, T> extends Modification<T> {}
}
