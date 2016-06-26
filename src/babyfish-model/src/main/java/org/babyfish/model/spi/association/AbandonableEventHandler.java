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

/**
 * @author Tao Chen
 */
abstract class AbandonableEventHandler<T> extends EventHandler {
    
    protected abstract boolean isLoadedObject(T opposite);
    
    protected abstract boolean isAbandonableObject(T opposite);
    
    protected abstract void loadObject(T opposite);
    
    protected final T loaded(T opposite) {
        if (opposite == null) {
            return null;
        }
        if (this.isLoadedObject(opposite)) {
            return opposite;
        }
        if (this.isAbandonableObject(opposite)) {
            return null;
        }
        this.loadObject(opposite);
        return opposite;
    }
}
