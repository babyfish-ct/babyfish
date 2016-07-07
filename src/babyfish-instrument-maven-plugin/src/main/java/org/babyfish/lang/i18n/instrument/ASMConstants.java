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
package org.babyfish.lang.i18n.instrument;

import java.util.ResourceBundle;

import org.babyfish.lang.internal.I18NUtils;
import org.babyfish.org.objectweb.asm.Type;
import org.teavm.jso.JSBody;

/**
 * @author Tao Chen
 */
public class ASMConstants {

    public static final String RESOURCE_BUNDLE_DESCRIPTOR = Type.getDescriptor(ResourceBundle.class);
    
    public static final String RESOURCE_BUNDLE_INTERNAL_NAME = descToInternalName(RESOURCE_BUNDLE_DESCRIPTOR);
    
    public static final String CHAR_SEQUENCE_DESCRIPTOR = Type.getDescriptor(CharSequence.class);
    
    public static final String I18N_INTERNAL_NAME = Type.getInternalName(I18NUtils.class);
    
    public static final String JS_BODY_DESCRIPTOR = Type.getDescriptor(JSBody.class);
    
    private static String descToInternalName(String desc) {
        // Tiny optimization: Make desc and internalName share same memory block.
        return desc.substring(1, desc.length() - 1);
    }
    
    private ASMConstants() {
        throw new UnsupportedOperationException();
    }
}
