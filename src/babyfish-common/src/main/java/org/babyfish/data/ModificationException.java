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
package org.babyfish.data;

import org.babyfish.data.event.ModificationEvent;

/**
 * @author Tao Chen
 */
public class ModificationException extends RuntimeException {

    private static final long serialVersionUID = -5592538389707038810L;

    private boolean modified;
    
    private ModificationEvent modificationEvent;

    public ModificationException(
            boolean modified, ModificationEvent modificationEvent, Throwable cause) {
        super(cause);
        this.modified = modified;
        this.modificationEvent = modificationEvent;
    }
    
    public boolean isModified() {
        return this.modified;
    }
    
    public ModificationEvent getModificationEvent() {
        return this.modificationEvent;
    }
}
