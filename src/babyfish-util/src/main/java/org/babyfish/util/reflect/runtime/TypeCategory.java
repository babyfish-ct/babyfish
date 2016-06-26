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
package org.babyfish.util.reflect.runtime;

import org.babyfish.lang.Arguments;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public enum TypeCategory {

    A(1, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.AALOAD, Opcodes.AASTORE, Opcodes.ARETURN),
    I(1, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IALOAD, Opcodes.IASTORE, Opcodes.IRETURN),
    L(2, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LALOAD, Opcodes.LASTORE, Opcodes.LRETURN),
    F(1, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FALOAD, Opcodes.FASTORE, Opcodes.FRETURN),
    D(2, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DALOAD, Opcodes.DASTORE, Opcodes.DRETURN),
    
    V {

        @Override
        public int getSlotCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLoadCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStoreCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getArrayLoadCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getArrayStoreCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException();
        }
        
    }
    ;
    
    private int slotCount;
    
    private int loadCode;
    
    private int storeCode;
    
    private int arrayLoadCode;
    
    private int arrayStoreCode;
    
    private int returnCode;
    
    private TypeCategory(int slotCount, int loadCode, int storeCode, int arrayLoadCode, int arrayStoreCode, int returnCode) {
        this.slotCount = slotCount;
        this.loadCode = loadCode;
        this.storeCode = storeCode;
        this.arrayLoadCode = arrayLoadCode;
        this.arrayStoreCode = arrayStoreCode;
        this.returnCode = returnCode;
    }
    
    private TypeCategory() {
        this.returnCode = Opcodes.RETURN;
    }
    
    public int getSlotCount() {
        return this.slotCount;
    }
    
    public int getLoadCode() {
        return this.loadCode;
    }

    public int getStoreCode() {
        return this.storeCode;
    }

    public int getArrayLoadCode() {
        return this.arrayLoadCode;
    }

    public int getArrayStoreCode() {
        return this.arrayStoreCode;
    }

    public int getReturnCode() {
        return this.returnCode;
    }

    public static TypeCategory of(Class<?> clazz) {
        Arguments.mustNotBeNull("clazz", clazz);
        Arguments.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (boolean.class == clazz || char.class == clazz || 
                byte.class == clazz || short.class == clazz || int.class == clazz) {
            return I;
        }
        if (long.class == clazz) {
            return L;
        } 
        if (float.class == clazz) {
            return F;
        } 
        if (double.class == clazz) {
            return D;
        }
        return A;
    }
    
    public static TypeCategory of(String type) {
        Arguments.mustNotBeNull("type", type);
        if (type.equals("V")) {
            return V;
        }
        if (type.equals("Z")) {
            return I;
        } 
        if (type.equals("C")) {
            return I;
        } 
        if (type.equals("B")) {
            return I;
        } 
        if (type.equals("S")) {
            return I;
        } 
        if (type.equals("I")) {
            return I;
        } 
        if (type.equals("J")) {
            return L;
        } 
        if (type.equals("F")) {
            return F;
        } 
        if (type.equals("D")) {
            return D;
        }
        return A;
    }
    
    public static TypeCategory of(Type type) {
        return of(type.getDescriptor());
    }
    
}
