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
package org.babyfish.model.spi.association;

import java.util.Map;

import org.babyfish.data.event.PropertyVersion;
import org.babyfish.model.spi.reference.event.KeyedValueEvent;

/**
 * @author Tao Chen
 */
abstract class KeyedValueEventHandler<K, T> extends AbandonableEventHandler<T> {
    
    /*
     * Specially, KeyedReference chooses to modify the opposite end-point 
     * during the "modifying" event, not "modified" event. 
     * 
     * If the new value is null. the opposite "map.remove" is called before the this.set(null)
     * so that the "visionallyRemove" in "PersistenceMA...Map" need not to load the map because 
     * the "session.flush" of visionallyRead can not flush any database changing.
     */
    @SuppressWarnings("unchecked")
    public void preHandle(KeyedValueEvent<K, T> e) {
        AssociatedEndpoint endpoint = this.getEndpoint();
        if (!endpoint.isSuspended()) {
            this.setSuspended(true);
            try {
                T detachedValue = e.getValue(PropertyVersion.DETACH);
                T attachedValue = e.getValue(PropertyVersion.ATTACH);
                K detachedKey = e.getKey(PropertyVersion.DETACH);
                K attachedKey = e.getKey(PropertyVersion.ATTACH);
                if (detachedValue == attachedValue && detachedKey == attachedKey) {
                    return;
                }
                if (detachedValue != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(detachedValue);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (detachedKey != null) {
                            boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                            try {
                                Map<?, ?> map = (Map<?, ?>)oppositeEndpoint;
                                map.remove(detachedKey);
                            } finally {
                                if (isOppositeEndpointDisabled) {
                                    oppositeEndpoint.disable();
                                }
                            }
                        }
                    }
                }
                if (attachedValue != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(attachedValue);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (attachedKey != null) {
                            boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                            try {
                                Map<Object, Object> map = (Map<Object, Object>)oppositeEndpoint;
                                map.put(attachedKey, endpoint.getOwner());
                            } finally {
                                if (isOppositeEndpointDisabled) {
                                    oppositeEndpoint.disable();
                                }
                            }
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
}

