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
package org.babyfish.model.spi.reference.viewinfo;

import org.babyfish.data.spi.AbstractSingletonViewInfo;

/**
 * @author Tao Chen
 */
public class KeyedReferenceViewInfos {
    
    public static Key key() {
        return Key.INSTANCE;
    }
    
    private static class Key extends AbstractSingletonViewInfo {
        
        static final Key INSTANCE = getInstance(Key.class);
        
        private Key() {}
    }
    
    @Deprecated
    protected KeyedReferenceViewInfos() {
        throw new UnsupportedOperationException();
    }
}
