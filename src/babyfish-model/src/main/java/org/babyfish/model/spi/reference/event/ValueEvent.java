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

import org.babyfish.data.event.ModificationEvent;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class ValueEvent<T> extends ModificationEvent {

    private static final long serialVersionUID = -9518741641336670L;

    private T detachedValue;
    
    private T attachedValue;

    protected ValueEvent(
            Object source, 
            Modification<T> modification, 
            T detachedValue,
            T attachedValue) {
        super(source, modification, ModificationType.REPLACE);
        this.detachedValue = detachedValue;
        this.attachedValue = attachedValue;
    }
    
    protected ValueEvent(Object source, ValueEvent<T> target) {
        super(source, target);
        this.detachedValue = target.detachedValue;
        this.attachedValue = target.attachedValue;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Modification<T> getModification() {
        return (Modification<T>)super.getModification();
    }
    
    public T getValue(PropertyVersion version) {
        switch (Arguments.mustNotBeNull("version", version)) {
        case DETACH:
            return this.detachedValue;
        case ATTACH:
            return this.attachedValue;
        default:
            throw new AssertionError("Internal bug");
        }
    }

    @Override
    public ValueEvent<T> dispatch(Object source) {
        return new ValueEvent<>(source, this);
    }
    
    public static <T> ValueEvent<T> createReplaceEvent(
            Object source, 
            Modification<T> modification, 
            T detachedValue,
            T attachedValue) {
        return new ValueEvent<>(source, modification, detachedValue, attachedValue);
    }
    
    public interface Modification<T> extends org.babyfish.data.event.Modification {}
}