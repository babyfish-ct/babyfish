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
package org.babyfish.model.event.modification;

import org.babyfish.data.event.spi.AbstractModification;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class ObjectModelModifications {
    
    public static SetByScalarPropertyIdAndValue set(int scalarPropertyId, Object value) {
        return new SetByScalarPropertyIdAndValue(scalarPropertyId, value);
    }
    
    public static class SetByScalarPropertyIdAndValue extends AbstractModification {
    
        private static final long serialVersionUID = -6202331174787937282L;

        private int scalarPropertyId;
        
        private Object value;

        public SetByScalarPropertyIdAndValue(int scalarPropertyId, Object value) {
            this.scalarPropertyId = scalarPropertyId;
            this.value = value;
        }

        public int getScalarPropertyId() {
            return this.scalarPropertyId;
        }

        public Object getValue() {
            return this.value;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender
            .property("scalarPropertyId", this.scalarPropertyId)
            .property("value", this.value);
        }
    }
    
    @Deprecated
    protected ObjectModelModifications() {
        throw new UnsupportedOperationException();
    }
}
