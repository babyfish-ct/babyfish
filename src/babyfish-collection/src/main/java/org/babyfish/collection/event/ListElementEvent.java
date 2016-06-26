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

import java.util.NavigableSet;

import org.babyfish.collection.event.spi.ConflictAbsoluteIndexes;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyBubbler;
import org.babyfish.data.event.PropertyVersion;

/**
 * @author Tao Chen
 */
public class ListElementEvent<E> extends ElementEvent<E> {

    private static final long serialVersionUID = -7258366329397177042L;

    private int detachedIndex;
    
    private int attachedIndex;
    
    protected ListElementEvent(
            Object source, 
            Modification<E> modification,
            ModificationType modificationType,
            E detachedElement,
            E attachedElement,
            int detachedIndex,
            int attachedIndex) {
        super(source, modification, modificationType, detachedElement, attachedElement);
        this.detachedIndex = detachedIndex;
        this.attachedIndex = attachedIndex;
    }
    
    public ListElementEvent(
            Object source, 
            Cause cause, 
            PropertyBubbler<E> elementBubbler,
            PropertyBubbler<Integer> indexBubbler) {
        super(source, cause, elementBubbler);
        if (indexBubbler == null) {
            ListElementEvent<E> viewEvent = cause.getViewEvent();
            this.detachedIndex = viewEvent.detachedIndex;
            this.attachedIndex = viewEvent.attachedIndex;
        } else {
            ModificationType modificationType = this.getModificationType();
            if (modificationType.contains(PropertyVersion.DETACH)) {
                this.detachedIndex = indexBubbler.bubble(PropertyVersion.DETACH);
            } else {
                this.detachedIndex = -1;
            }
            if (modificationType.contains(PropertyVersion.ATTACH)) {
                this.attachedIndex = indexBubbler.bubble(PropertyVersion.ATTACH);
            } else {
                this.attachedIndex = -1;
            }
        }
    }

    protected ListElementEvent(Object source, ListElementEvent<E> target) {
        super(source, target);
        this.detachedIndex = target.detachedIndex;
        this.attachedIndex = target.attachedIndex;
    }

    public int getIndex(PropertyVersion version) {
        switch (version) {
        case DETACH:
            return this.detachedIndex;
        case ATTACH:
            return this.attachedIndex;
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    public NavigableSet<Integer> getConflictAbsoluteIndexes() {
        return ConflictAbsoluteIndexes.get(this);
    }
    
    @Override
    public ListElementEvent<E> dispatch(Object source) {
        return new ListElementEvent<>(source, this);
    }

    public static <E> ListElementEvent<E> createDetachEvent(
            Object source, Modification<E> modification, E element, int index) {
        return new ListElementEvent<>(
                source, 
                modification, 
                ModificationType.DETACH,
                element, 
                null,
                index,
                -1
        );
    }
    
    public static <E> ListElementEvent<E> createAttachEvent(
            Object source, Modification<E> modification, E element, int index) {
        return new ListElementEvent<>(
                source, 
                modification, 
                ModificationType.ATTACH,
                null,
                element,
                -1,
                index
        );
    }
    
    public static <E> ListElementEvent<E> createReplaceEvent(
            Object source, 
            Modification<E> modification, 
            E detachedElement, 
            E attachedElement, 
            int detachedIndex,
            int attachedIndex) {
        return new ListElementEvent<>(
                source, 
                modification, 
                ModificationType.REPLACE,
                detachedElement,
                attachedElement,
                detachedIndex,
                attachedIndex
        );
    }
}
