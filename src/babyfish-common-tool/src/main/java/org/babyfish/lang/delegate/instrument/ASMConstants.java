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
package org.babyfish.lang.delegate.instrument;

import java.util.function.BiFunction;

import org.babyfish.lang.internal.AbstractCombinedDelegate;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
class ASMConstants {

    public static final String ABSTRACT_COMBINED_DELEGATE_DESCRIPTOR = 
            Type.getDescriptor(AbstractCombinedDelegate.class);
    
    public static final String ABSTRACT_COMBINED_DELEGATE_INTERNAL_NAME =
            descToInternalName(ABSTRACT_COMBINED_DELEGATE_DESCRIPTOR);
    
    public static final String BI_FUNCTION_DESCRIPTOR =
            Type.getDescriptor(BiFunction.class);
    
    public static final String BI_FUNCTION_INTERNAL_NAME =
            descToInternalName(BI_FUNCTION_DESCRIPTOR);
    
    protected static String descToInternalName(String desc) {
        // Small optimization, let internal name share the memory with descriptor.
        return desc.substring(1, desc.length() - 1);
    }
    
    private ASMConstants() {}
}
