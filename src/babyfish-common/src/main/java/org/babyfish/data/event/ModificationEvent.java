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
package org.babyfish.data.event;

import java.util.EventObject;
import java.util.IdentityHashMap;
import java.util.Map;

import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;
import org.babyfish.data.event.spi.AbstractModification;

public abstract class ModificationEvent extends EventObject {

    private static final long serialVersionUID = -2965281592601785697L;

    private EventType eventType;
    
    private ModificationType modificationType;
    
    private Modification modification;
    
    private Cause cause;
    
    private IdentityHashMap<Object, ModificationEvent> bubbledEventMap;
    
    private IdentityHashMap<Object, ModificationEvent> dispatchedEventMap;
    
    private EventAttributeContext localAttributeContext;
    
    private EventAttributeContext bubbleChainAttributeContext;

    private EventAttributeContext dispatchChainAttributeContext;
    
    private EventAttributeContext allChianAttributeContext;
    
    private EventAttributeContext globalAttributeContext;
    
    protected ModificationEvent(
            Object source, 
            Modification modification,
            ModificationType modificationType) {
        super(source);
        this.modificationType = modificationType;
        this.eventType = EventType.PROTOSOMATIC;
        this.modification = modification;
        
        this.localAttributeContext = EventAttributeContext.of(AttributeScope.LOCAL);
        this.bubbleChainAttributeContext = EventAttributeContext.of(AttributeScope.IN_BUBBLE_CHAIN);
        this.dispatchChainAttributeContext = EventAttributeContext.of(AttributeScope.IN_DISPATCH_CHAIN);
        this.allChianAttributeContext = EventAttributeContext.of(AttributeScope.IN_ALL_CHAIN);
        this.globalAttributeContext = modification.getAttributeContext();
    }
    
    protected ModificationEvent(Object source, Cause cause) {
        super(source);
        this.modificationType = cause.getViewEvent().modificationType;
        this.eventType = EventType.BUBBLED;   
        this.cause = cause;
        
        ModificationEvent target = (ModificationEvent)cause.getViewEvent();
        IdentityHashMap<Object, ModificationEvent> map = target.bubbledEventMap;
        if (map == null) {
            map = new IdentityHashMap<>();
            target.bubbledEventMap = map;
        }
        map.put(source, this);
        
        this.localAttributeContext = EventAttributeContext.of(AttributeScope.LOCAL);
        this.bubbleChainAttributeContext = target.getAttributeContext(AttributeScope.IN_BUBBLE_CHAIN);
        this.dispatchChainAttributeContext = EventAttributeContext.of(AttributeScope.IN_DISPATCH_CHAIN);
        this.allChianAttributeContext = target.getAttributeContext(AttributeScope.IN_ALL_CHAIN);
        this.globalAttributeContext = target.getAttributeContext(AttributeScope.GLOBAL);
    }
    
    protected ModificationEvent(Object source, ModificationEvent target) {
        super(source);
        Arguments.mustBeNull("target.getCause()", target.getCause());
        this.modificationType = target.modificationType;
        this.eventType = EventType.DISPATCHED;
        this.modification = target.modification;
        
        IdentityHashMap<Object, ModificationEvent> map = target.dispatchedEventMap;
        if (map == null) {
            map = new IdentityHashMap<>();
            target.dispatchedEventMap = map;
        }
        map.put(source, this);
        
        this.localAttributeContext = EventAttributeContext.of(AttributeScope.LOCAL);
        this.bubbleChainAttributeContext = EventAttributeContext.of(AttributeScope.IN_BUBBLE_CHAIN);
        this.dispatchChainAttributeContext = target.getAttributeContext(AttributeScope.IN_DISPATCH_CHAIN);
        this.allChianAttributeContext = target.getAttributeContext(AttributeScope.IN_ALL_CHAIN);
        this.globalAttributeContext = target.getAttributeContext(AttributeScope.GLOBAL);
    }
    
    public ModificationType getModificationType() {
        return this.modificationType;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public Modification getModification() {
        return this.modification;
    }
    
    @SuppressWarnings("unchecked")
    public <M extends AbstractModification> M getFinalModification() {
        ModificationEvent event = this;
        while (event != null) {
            Modification modifiation = event.modification;
            if (modifiation != null) {
                return (M)modifiation;
            }
            Cause cause = event.cause;
            event = cause != null ? cause.getViewEvent() : null;
        }
        throw new AssertionError("Internal bug");
    }

    public Cause getCause() {
        return this.cause;
    }

    @SuppressWarnings("unchecked")
    public <E extends ModificationEvent> E getBubbledEvent(Object source) {
        Map<Object, ModificationEvent> map = this.bubbledEventMap;
        if (map == null) {
            return null;
        }
        return (E)map.get(source);
    }

    @SuppressWarnings("unchecked")
    public <E extends ModificationEvent> E getDispatchedEvent(Object source) {
        Map<Object, ModificationEvent> map = this.dispatchedEventMap;
        if (map == null) {
            return null;
        }
        return (E)map.get(source);
    }

    public EventAttributeContext getAttributeContext(AttributeScope scope) {
        switch (Arguments.mustNotBeNull("scope", scope)) {
        case GLOBAL:
            return this.globalAttributeContext;
        case IN_ALL_CHAIN:
            return this.allChianAttributeContext;
        case IN_DISPATCH_CHAIN:
            return this.dispatchChainAttributeContext;
        case IN_BUBBLE_CHAIN:
            return this.bubbleChainAttributeContext;
        default:
            return this.localAttributeContext;
        }
    }

    public Throwable getPreModificationThrowable() {
        return ((InAllChainAttributeContext)this.allChianAttributeContext).getPreThrowable();
    }

    public Throwable getModificationThrowable() {
        return ((GlobalAttributeContext)this.globalAttributeContext).getThrowable();
    }

    public boolean isModificationSuccessed() {
        return ((GlobalAttributeContext)this.globalAttributeContext).isSuccessed();
    }
    
    public abstract ModificationEvent dispatch(Object source);
}
