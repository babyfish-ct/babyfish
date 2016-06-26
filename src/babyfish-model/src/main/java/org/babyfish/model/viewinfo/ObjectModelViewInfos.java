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
package org.babyfish.model.viewinfo;

import org.babyfish.data.spi.AbstractViewInfo;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class ObjectModelViewInfos {
    
    public static Scalar scalar(int scalarPropertyId) {
        return new Scalar(scalarPropertyId);
    }
    
    public static class Scalar extends AbstractViewInfo {
   
        private static final long serialVersionUID = 905169321671453485L;
        
        private int scalarPropertyId;

        Scalar(int scalarPropertyId) {
            super();
            this.scalarPropertyId = scalarPropertyId;
        }

        public int getScalarPropertyId() {
            return this.scalarPropertyId;
        }
        
        @Override
        public int hashCode() {
            return this.scalarPropertyId;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            Scalar other = (Scalar)obj;
            return this.scalarPropertyId == other.scalarPropertyId;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("scalarPropertyId", this.scalarPropertyId);
        }
    }
    
    @Deprecated
    protected ObjectModelViewInfos() {
        throw new UnsupportedOperationException();
    }
}
