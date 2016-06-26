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
package org.babyfish.test.util.reflect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import junit.framework.Assert;

import org.babyfish.util.reflect.Modifier;
import org.babyfish.util.reflect.ModifierSet;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ModifierSetTest {
    
    private static final int JDK_MODIFIER_VARARGS = 0x00000080;
    
    private static final int JDK_MODIFIER_BRIDGE = 0x00000040;
    
    private static final int JDK_MODIFIER_SYNTHETIC = 0x00001000;
    
    @Test
    public void testCompareVisibility() {
        Assert.assertEquals(
                0, 
                ModifierSet.forType(Modifier.PUBLIC).compareVisibility(
                        ModifierSet.forType(Modifier.PUBLIC)));
        Assert.assertEquals(
                +1, 
                ModifierSet.forType(Modifier.PUBLIC).compareVisibility(
                        ModifierSet.forType(Modifier.PROTECTED)));
        Assert.assertEquals(
                +1, 
                ModifierSet.forType(Modifier.PUBLIC).compareVisibility(
                        ModifierSet.forType()));
        Assert.assertEquals(
                +1, 
                ModifierSet.forType(Modifier.PUBLIC).compareVisibility(
                        ModifierSet.forType(Modifier.PRIVATE)));
        
        Assert.assertEquals(
                -1, 
                ModifierSet.forType(Modifier.PROTECTED).compareVisibility(
                        ModifierSet.forType(Modifier.PUBLIC)));
        Assert.assertEquals(
                0, 
                ModifierSet.forType(Modifier.PROTECTED).compareVisibility(
                        ModifierSet.forType(Modifier.PROTECTED)));
        Assert.assertEquals(
                +1, 
                ModifierSet.forType(Modifier.PROTECTED).compareVisibility(
                        ModifierSet.forType()));
        Assert.assertEquals(
                +1, 
                ModifierSet.forType(Modifier.PROTECTED).compareVisibility(
                        ModifierSet.forType(Modifier.PRIVATE)));
        
        Assert.assertEquals(
                -1, 
                ModifierSet.forType().compareVisibility(
                        ModifierSet.forType(Modifier.PUBLIC)));
        Assert.assertEquals(
                -1, 
                ModifierSet.forType().compareVisibility(
                        ModifierSet.forType(Modifier.PROTECTED)));
        Assert.assertEquals(
                0, 
                ModifierSet.forType().compareVisibility(
                        ModifierSet.forType()));
        Assert.assertEquals(
                +1, 
                ModifierSet.forType().compareVisibility(
                        ModifierSet.forType(Modifier.PRIVATE)));
        
        Assert.assertEquals(
                -1, 
                ModifierSet.forType(Modifier.PRIVATE).compareVisibility(
                        ModifierSet.forType(Modifier.PUBLIC)));
        Assert.assertEquals(
                -1, 
                ModifierSet.forType(Modifier.PRIVATE).compareVisibility(
                        ModifierSet.forType(Modifier.PROTECTED)));
        Assert.assertEquals(
                -1, 
                ModifierSet.forType(Modifier.PRIVATE).compareVisibility(
                        ModifierSet.forType()));
        Assert.assertEquals(
                0, 
                ModifierSet.forType(Modifier.PRIVATE).compareVisibility(
                        ModifierSet.forType(Modifier.PRIVATE)));
    }

    @Test
    public void testConflicts() {
        Assert.assertEquals(
                "types-fields-methods-", 
                getConflictReason(Modifier.PUBLIC, Modifier.PROTECTED));
        Assert.assertEquals(
                "types-fields-methods-", 
                getConflictReason(Modifier.PROTECTED, Modifier.PUBLIC));
        
        Assert.assertEquals(
                "types-fields-methods-", 
                getConflictReason(Modifier.PROTECTED, Modifier.PRIVATE));
        Assert.assertEquals(
                "types-fields-methods-", 
                getConflictReason(Modifier.PRIVATE, Modifier.PROTECTED));
        
        Assert.assertEquals(
                "types-fields-methods-", 
                getConflictReason(Modifier.PRIVATE, Modifier.PUBLIC));
        Assert.assertEquals(
                "types-fields-methods-", 
                getConflictReason(Modifier.PUBLIC, Modifier.PRIVATE));

        Assert.assertEquals(
                "types-methods-", 
                getConflictReason(Modifier.FINAL, Modifier.ABSTRACT));
        Assert.assertEquals(
                "types-methods-", 
                getConflictReason(Modifier.ABSTRACT, Modifier.FINAL));
        
        Assert.assertEquals(
                "fields-", 
                getConflictReason(Modifier.FINAL, Modifier.VOLATILE));
        Assert.assertEquals(
                "fields-", 
                getConflictReason(Modifier.VOLATILE, Modifier.FINAL));

        Assert.assertEquals("methods-", getConflictReason(Modifier.STATIC, Modifier.ABSTRACT));
        Assert.assertEquals("methods-", getConflictReason(Modifier.ABSTRACT, Modifier.STATIC));
        
        Assert.assertEquals("methods-", getConflictReason(Modifier.ABSTRACT, Modifier.NATIVE));
        Assert.assertEquals("methods-", getConflictReason(Modifier.NATIVE, Modifier.ABSTRACT));
    }
    
    @Test
    public void testAddAndAddAll() {
        ModifierSet modifierSet = ModifierSet.forMethod();
        ModCountAssert mcAssert = new ModCountAssert(modifierSet);
        Assert.assertTrue(modifierSet.add(Modifier.PUBLIC));
        mcAssert.assertModCountChanged();
        Assert.assertTrue(modifierSet.add(Modifier.ABSTRACT));
        mcAssert.assertModCountChanged();
        Assert.assertTrue(modifierSet.add(Modifier.STRICT));
        mcAssert.assertModCountChanged();
        Assert.assertTrue(modifierSet.add(Modifier.VARARGS));
        mcAssert.assertModCountChanged();
        Assert.assertTrue(
                modifierSet.addAll(
                        ModifierSet.forMethod(
                                Modifier.SYNTHETIC, 
                                Modifier.BRIDGE)));
        mcAssert.assertModCountChanged();
        Assert.assertEquals(6, modifierSet.size());
        Assert.assertEquals(
                java.lang.reflect.Modifier.PUBLIC |
                java.lang.reflect.Modifier.ABSTRACT |
                java.lang.reflect.Modifier.STRICT |
                JDK_MODIFIER_VARARGS |
                JDK_MODIFIER_BRIDGE |
                JDK_MODIFIER_SYNTHETIC, 
                modifierSet.toJDKModifiers());
        
        Assert.assertFalse(
                modifierSet.addAll(
                        ModifierSet.forMethod(
                                Modifier.SYNTHETIC, 
                                Modifier.BRIDGE)));
        mcAssert.assertModCountNotChanged();
        Assert.assertEquals(6, modifierSet.size());
        Assert.assertEquals(
                java.lang.reflect.Modifier.PUBLIC |
                java.lang.reflect.Modifier.ABSTRACT |
                java.lang.reflect.Modifier.STRICT |
                JDK_MODIFIER_VARARGS |
                JDK_MODIFIER_BRIDGE |
                JDK_MODIFIER_SYNTHETIC, 
                modifierSet.toJDKModifiers());
    }
    
    @Test
    public void testClear() {
        ModifierSet modifierSet = ModifierSet.forMethod(
                java.lang.reflect.Modifier.PUBLIC |
                java.lang.reflect.Modifier.FINAL);
        ModCountAssert mcAssert = new ModCountAssert(modifierSet);
        Assert.assertFalse(modifierSet.isEmpty());
        modifierSet.clear();
        mcAssert.assertModCountChanged();
        Assert.assertTrue(modifierSet.isEmpty());
    }
    
    @Test
    public void testRemove() {
        ModifierSet modifierSet = ModifierSet.forMethod(
                java.lang.reflect.Modifier.PUBLIC |
                java.lang.reflect.Modifier.FINAL);
        ModCountAssert mcAssert = new ModCountAssert(modifierSet);
        Assert.assertTrue(modifierSet.contains(Modifier.PUBLIC));
        Assert.assertTrue(modifierSet.contains(Modifier.FINAL));
        
        Assert.assertTrue(modifierSet.remove(Modifier.PUBLIC));
        mcAssert.assertModCountChanged();
        Assert.assertFalse(modifierSet.contains(Modifier.PUBLIC));
        Assert.assertTrue(modifierSet.contains(Modifier.FINAL));
        
        Assert.assertFalse(modifierSet.remove(Modifier.PUBLIC));
        mcAssert.assertModCountNotChanged();
        Assert.assertFalse(modifierSet.contains(Modifier.PUBLIC));
        Assert.assertTrue(modifierSet.contains(Modifier.FINAL));
        
        Assert.assertTrue(modifierSet.remove(Modifier.FINAL));
        mcAssert.assertModCountChanged();
        Assert.assertFalse(modifierSet.contains(Modifier.PUBLIC));
        Assert.assertFalse(modifierSet.contains(Modifier.FINAL));
    }
    
    @Test
    public void testRemoveAll() {
        ModifierSet modifierSet = ModifierSet.forMethod(
                java.lang.reflect.Modifier.PUBLIC |
                java.lang.reflect.Modifier.FINAL |
                java.lang.reflect.Modifier.SYNCHRONIZED |
                java.lang.reflect.Modifier.STRICT |
                JDK_MODIFIER_VARARGS |
                JDK_MODIFIER_BRIDGE |
                JDK_MODIFIER_SYNTHETIC);
        ModCountAssert mcAssert = new ModCountAssert(modifierSet);
        
        Assert.assertFalse(
                modifierSet.removeAll(
                    ModifierSet.forMethod(
                            Modifier.ABSTRACT,
                            Modifier.PROTECTED)));
        Assert.assertEquals(7, modifierSet.size());
        mcAssert.assertModCountNotChanged();
        
        Assert.assertTrue(
                modifierSet.removeAll(
                    ModifierSet.forMethod(
                            Modifier.VARARGS,
                            Modifier.BRIDGE,
                            Modifier.SYNTHETIC)));
        Assert.assertEquals(4, modifierSet.size());
        mcAssert.assertModCountChanged();
        
        Assert.assertTrue(
                modifierSet.removeAll(
                    ModifierSet.forMethod(
                            Modifier.PUBLIC,
                            Modifier.FINAL,
                            Modifier.SYNCHRONIZED,
                            Modifier.STRICT)));
        Assert.assertTrue(modifierSet.isEmpty());
        mcAssert.assertModCountChanged();
    }
    
    @Test
    public void testRetainAll() {
        ModifierSet modifierSet = ModifierSet.forMethod(
                java.lang.reflect.Modifier.PUBLIC |
                java.lang.reflect.Modifier.FINAL |
                java.lang.reflect.Modifier.SYNCHRONIZED |
                java.lang.reflect.Modifier.STRICT |
                JDK_MODIFIER_VARARGS |
                JDK_MODIFIER_BRIDGE |
                JDK_MODIFIER_SYNTHETIC);
        ModCountAssert mcAssert = new ModCountAssert(modifierSet);
        
        Assert.assertFalse(
                modifierSet.retainAll(
                    modifierSet.clone()));
        Assert.assertEquals(7, modifierSet.size());
        mcAssert.assertModCountNotChanged();
        
        Assert.assertTrue(
                modifierSet.retainAll(
                    ModifierSet.forMethod(
                            Modifier.PUBLIC,
                            Modifier.FINAL,
                            Modifier.SYNCHRONIZED,
                            Modifier.STRICT)));
        Assert.assertEquals(4, modifierSet.size());
        mcAssert.assertModCountChanged();
        
        Assert.assertTrue(
                modifierSet.retainAll(
                    ModifierSet.forMethod()));
        Assert.assertTrue(modifierSet.isEmpty());
        mcAssert.assertModCountChanged();
    }
    
    @Test
    public void testIterator() {
        ModifierSet modifierSet = ModifierSet.forMethod(
                Modifier.PUBLIC,
                Modifier.FINAL,
                Modifier.NATIVE,
                Modifier.VARARGS,
                Modifier.BRIDGE,
                Modifier.SYNTHETIC);
        ModCountAssert mcAssert = new ModCountAssert(modifierSet);
        StringBuilder builder = new StringBuilder();
        Iterator<Modifier> iterator = modifierSet.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Modifier modifier = iterator.next();
            builder.append(modifier.getModifierName());
            builder.append('-');
            if (index != 0) {
                iterator.remove();
                mcAssert.assertModCountChanged();
            }
            index = (index + 1) % 2;
        }
        Assert.assertEquals(
                "public-final-native-varargs-bridge-synthetic-", 
                builder.toString());
        
        builder = new StringBuilder();
        iterator = modifierSet.iterator();
        index = 0;
        while (iterator.hasNext()) {
            Modifier modifier = iterator.next();
            builder.append(modifier.getModifierName());
            builder.append('-');
            if (index == 0) {
                iterator.remove();
                mcAssert.assertModCountChanged();
            }
            index = (index + 1) % 2;
        }
        Assert.assertEquals(
                "public-native-bridge-", 
                builder.toString());
        Assert.assertEquals(1, modifierSet.size());
        Assert.assertTrue(
                modifierSet.containsAll(
                        ModifierSet.forMethod(
                                java.lang.reflect.Modifier.NATIVE)));
    }
    
    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        ModifierSet modifierSet = ModifierSet.forMethod(
                Modifier.PUBLIC,
                Modifier.FINAL,
                Modifier.NATIVE,
                Modifier.VARARGS,
                Modifier.BRIDGE,
                Modifier.SYNTHETIC);
        
        byte[] bytes;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(outStream);
            try {
                out.writeObject(modifierSet);
                bytes = outStream.toByteArray();
            } finally {
                out.close();
            }
        } finally {
            outStream.close();
        }
        
        ModifierSet deserializedModifierSet;
        ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInput in = new ObjectInputStream(inStream);
            try {
                deserializedModifierSet = (ModifierSet)in.readObject();
            } finally {
                in.close();
            }
        } finally {
            inStream.close();
        }
        
        Assert.assertNotSame(modifierSet, deserializedModifierSet);
        Assert.assertEquals(modifierSet, deserializedModifierSet);
        Assert.assertEquals(modifierSet.hashCode(), deserializedModifierSet.hashCode());
        Assert.assertEquals(modifierSet.toJDKModifiers(), deserializedModifierSet.toJDKModifiers());
        Assert.assertEquals(modifierSet.size(), deserializedModifierSet.size());
        
        Iterator<Modifier> itr1 = modifierSet.iterator();
        Iterator<Modifier> itr2 = deserializedModifierSet.iterator();
        while (itr1.hasNext()) {
            Assert.assertSame(itr1.next(), itr2.next());
        }
    }
    
    private static String getConflictReason(Modifier modifier1, Modifier modifier2) {
        StringBuilder builder = new StringBuilder();
        if (modifier1.isForTypes() && modifier2.isForTypes()) {
            ModifierSet set = ModifierSet.forType();
            set.add(modifier1);
            try {
                set.add(modifier2);
            } catch (IllegalArgumentException ex) {
                builder.append("types-");
            }
        }
        if (modifier1.isForFields() && modifier2.isForFields()) {
            ModifierSet set = ModifierSet.forField();
            set.add(modifier1);
            try {
                set.add(modifier2);
            } catch (IllegalArgumentException ex) {
                builder.append("fields-");
            }
        } 
        if (modifier1.isForMethods() && modifier2.isForMethods()) {
            ModifierSet set = ModifierSet.forMethod();
            set.add(modifier1);
            try {
                set.add(modifier2);
            } catch (IllegalArgumentException ex) {
                builder.append("methods-");
            }
        }
        return builder.toString();
    }
    
    private static class ModCountAssert {
        
        private static final Field MODCOUNT_FIELD;
        
        private final ModifierSet modifierSet;
        
        private int modCount;
        
        public ModCountAssert(ModifierSet modifierSet) {
            this.modifierSet = modifierSet;
            this.modCount = this.getModCount();
        }
        
        public void assertModCountChanged() {
            int newModCount = this.getModCount();
            int oldModCount = this.modCount;
            this.modCount = newModCount;
            Assert.assertTrue(newModCount > oldModCount);
        }
        
        public void assertModCountNotChanged() {
            int newModCount = this.getModCount();
            int oldModCount = this.modCount;
            Assert.assertTrue(newModCount == oldModCount);
        }
        
        private int getModCount() {
            try {
                return MODCOUNT_FIELD.getInt(this.modifierSet);
            } catch (IllegalArgumentException ex) {
                throw new AssertionError(ex);
            } catch (IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
        }
        
        static {
            Field modCountField;
            try {
                modCountField = ModifierSet.class.getDeclaredField("modCount");
                modCountField.setAccessible(true);
                MODCOUNT_FIELD = modCountField;
            } catch (NoSuchFieldException ex) {
                throw new AssertionError(ex);
            }
        }
    }
    
}

