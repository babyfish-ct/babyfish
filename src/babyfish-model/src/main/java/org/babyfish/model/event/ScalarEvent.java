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
package org.babyfish.model.event;

import org.babyfish.data.event.Cause;
import org.babyfish.data.event.ModificationEvent;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyBubbler;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.data.event.SharedPropertyBubbler;
import org.babyfish.lang.Arguments;
import org.babyfish.model.event.modification.ObjectModelModifications;

/**
 * @author Tao Chen
 */
public class ScalarEvent extends ModificationEvent {

    private static final long serialVersionUID = -2232135624989814067L;

    private int scalarPropertyId;
    
    private Object detachedValue;
    
    private Object attachedValue;
    
    protected ScalarEvent(
            Object source, 
            ObjectModelModifications.SetByScalarPropertyIdAndValue modification, 
            int scalarPropertyId,
            Object detachedValue,
            Object attachedValue) {
        super(source, modification, ModificationType.REPLACE);
        this.scalarPropertyId = scalarPropertyId;
        this.detachedValue = detachedValue;
        this.attachedValue = attachedValue;
    }
    
    public ScalarEvent(
            Object source, 
            Cause cause, 
            SharedPropertyBubbler<Integer> scalarPropertyIdBubbler, 
            PropertyBubbler<Object> valueBubbler) {
        super(source, cause);
        if (scalarPropertyIdBubbler == null) {
            ScalarEvent viewEvent = cause.getViewEvent();
            this.scalarPropertyId = viewEvent.scalarPropertyId;
        } else {
            this.scalarPropertyId = scalarPropertyIdBubbler.bubble();
        }
        if (valueBubbler == null) {
            ScalarEvent viewEvent = cause.getViewEvent();
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

    protected ScalarEvent(Object source, ScalarEvent target) {
        super(source, target);
        this.scalarPropertyId = target.scalarPropertyId;
        this.detachedValue = target.detachedValue;
        this.attachedValue = target.attachedValue;
    }
    
    public int getScalarPropertyId() {
        return this.scalarPropertyId;
    }
    
    public Object getValue(PropertyVersion version) {
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
    public ScalarEvent dispatch(Object source) {
        return new ScalarEvent(source, this);
    }
    
    public static ScalarEvent createReplaceEvent(
            Object source, 
            ObjectModelModifications.SetByScalarPropertyIdAndValue modification,
            int scalarPropertyId,
            Object detachedValue,
            Object attachedValue) {
        return new ScalarEvent(
                source, 
                modification, 
                scalarPropertyId, 
                detachedValue, 
                attachedValue
        );
    }
}

