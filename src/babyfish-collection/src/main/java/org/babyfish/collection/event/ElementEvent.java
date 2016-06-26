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
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class ElementEvent<E> extends ModificationEvent {

    private static final long serialVersionUID = 586078236109295681L;

    private E detachedElement;
    
    private E attachedElement;

    protected ElementEvent(
            Object source, 
            Modification<E> modification, 
            ModificationType modificationType,
            E detachedElement,
            E attachedElement) {
        super(source, modification, modificationType);
        this.detachedElement = detachedElement;
        this.attachedElement = attachedElement;
    }
    
    public ElementEvent(
            Object source, 
            Cause cause, 
            PropertyBubbler<E> elementBubbler) {
        super(source, cause);
        if (elementBubbler == null) {
            ElementEvent<E> viewEvent = cause.getViewEvent();
            this.detachedElement = viewEvent.detachedElement;
            this.attachedElement = viewEvent.attachedElement;
        } else {
            ModificationType modificationType = this.getModificationType();
            if (modificationType.contains(PropertyVersion.DETACH)) {
                this.detachedElement = elementBubbler.bubble(PropertyVersion.DETACH);
            }
            if (modificationType.contains(PropertyVersion.ATTACH)) {
                this.attachedElement = elementBubbler.bubble(PropertyVersion.ATTACH);
            }
        }
    }
    
    protected ElementEvent(Object source, ElementEvent<E> target) {
        super(source, target);
        this.detachedElement = target.detachedElement;
        this.attachedElement = target.attachedElement;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Modification<E> getModification() {
        return (Modification<E>)super.getModification();
    }
    
    public E getElement(PropertyVersion version) {
        switch (Arguments.mustNotBeNull("version", version)) {
        case DETACH:
            return this.detachedElement;
        case ATTACH:
            return this.attachedElement;
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    @Override
    public ElementEvent<E> dispatch(Object source) {
        return new ElementEvent<>(source, this);
    }

    public static <E> ElementEvent<E> createDetachEvent(
            Object source,
            Modification<E> modification,
            E element) {
        return new ElementEvent<>(
                source, 
                modification, 
                ModificationType.DETACH,
                element,
                null
        );
    }
    
    public static <E> ElementEvent<E> createAttachEvent(
            Object source,
            Modification<E> modification,
            E element) {
        return new ElementEvent<>(
                source, 
                modification, 
                ModificationType.ATTACH,
                null,
                element
        );
    }
    
    public static <E> ElementEvent<E> createReplaceEvent(
            Object source,
            Modification<E> modification,
            E detachedElement,
            E attachedElement) {
        return new ElementEvent<>(
                source, 
                modification, 
                ModificationType.REPLACE,
                detachedElement,
                attachedElement
        );
    }
    
    public interface Modification<E> extends org.babyfish.data.event.Modification {}
}
