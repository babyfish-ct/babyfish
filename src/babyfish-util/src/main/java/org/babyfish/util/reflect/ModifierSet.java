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
package org.babyfish.util.reflect;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.babyfish.collection.spi.base.BasicAlgorithms;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class ModifierSet implements Set<Modifier>, Serializable, Cloneable {
    
    private static final long serialVersionUID = -4478556790448217066L;
    
    private static final int MAX_SIZE = Modifier.ALL_MODIFIERS.length;
    
    private static final int ALL_ACCESS_MODIFIERS = 
        java.lang.reflect.Modifier.PUBLIC |
        java.lang.reflect.Modifier.PROTECTED |
        java.lang.reflect.Modifier.PRIVATE;
    
    private static final Conflict[] CONFLICTS;
    
    private static final Conflict[] CONFLICTS_FOR_TYPES;
    
    private static final Conflict[] CONFLICTS_FOR_FIELDS;
    
    private static final Conflict[] CONFLICTS_FOR_METHODS;
    
    private int modifierOrdinals;
    
    private transient int jdkModifiers;
    
    private transient int size;
    
    private transient int modCount;

    private ModifierSet() {
        
    }
    
    public static ModifierSet forType() {
        return new ForType();
    }
    
    public static ModifierSet forType(Modifier ... modifiers) {
        ModifierSet set = new ForType();
        for (Modifier modifier : modifiers) {
            set.add(modifier);
        }
        return set;
    }
    
    public static ModifierSet forType(int jdkModifiers) {
        ModifierSet set = new ForType();
        for (Modifier modifier : Modifier.ALL_MODIFIERS) {
            if (modifier.forTypes && (jdkModifiers & modifier.jdkModifier) != 0) {
                set.add(modifier);
            }
        }
        return set;
    }
    
    public static ModifierSet forField() {
        return new ForField();
    }
    
    public static ModifierSet forField(Modifier ... modifiers) {
        ModifierSet set = new ForField();
        for (Modifier modifier : modifiers) {
            set.add(modifier);
        }
        return set;
    }
    
    public static ModifierSet forField(int jdkModifiers) {
        ModifierSet set = new ForField();
        for (Modifier modifier : Modifier.ALL_MODIFIERS) {
            if (modifier.forFields && (jdkModifiers & modifier.jdkModifier) != 0) {
                set.add(modifier);
            }
        }
        return set;
    }
    
    public static ModifierSet forMethod() {
        return new ForMethod();
    }
    
    public static ModifierSet forMethod(Modifier ... modifiers) {
        ModifierSet set = new ForMethod();
        for (Modifier modifier : modifiers) {
            set.add(modifier);
        }
        return set;
    }
    
    public static ModifierSet forMethod(int jdkModifiers) {
        ModifierSet set = new ForMethod();
        for (Modifier modifier : Modifier.ALL_MODIFIERS) {
            if (modifier.forMethods && (jdkModifiers & modifier.jdkModifier) != 0) {
                set.add(modifier);
            }
        }
        return set;
    }
    
    public abstract boolean isForType();
    
    public abstract boolean isForField();

    public abstract boolean isForMethod();
    
    abstract void validateModifier(Modifier e);

    public int toJDKModifiers() {
        return this.jdkModifiers;
    }
    
    public int compareVisibility(ModifierSet modifiers) {
        if (this == modifiers) {
            return 0;
        }
        int level1 =
            this.contains(Modifier.PUBLIC) ?
                    3 :
                    (this.contains(Modifier.PROTECTED) ? 
                        2 : 
                        (this.contains(Modifier.PRIVATE) ? 0 : 1));
        int level2 =
            modifiers.contains(Modifier.PUBLIC) ?
                    3 :
                    (modifiers.contains(Modifier.PROTECTED) ? 
                        2 : 
                        (modifiers.contains(Modifier.PRIVATE) ? 0 : 1));
        if (level1 == level2) {
            return 0;
        }
        return level1 < level2 ? -1 : +1;
    }

    public boolean isPublic() {
        return this.contains(Modifier.PUBLIC);
    }
    
    public boolean isProtected() {
        return this.contains(Modifier.PROTECTED);
    }
    
    public boolean isPrivate() {
        return this.contains(Modifier.PRIVATE);
    }
    
    public boolean isDefault() {
        return (this.jdkModifiers & ALL_ACCESS_MODIFIERS) == 0;
    }
    
    public boolean isStatic() {
        return this.contains(Modifier.STATIC);
    }
    
    public boolean isFinal() {
        return this.contains(Modifier.FINAL);
    }
    
    public boolean isAbstract() {
        return this.contains(Modifier.ABSTRACT);
    }
    
    public boolean isSynchronzied() {
        return this.contains(Modifier.SYNCHRONIZED);
    }
    
    public boolean isNative() {
        return this.contains(Modifier.NATIVE);
    }
    
    public boolean isTransient() {
        return this.contains(Modifier.TRANSIENT);
    }
    
    public boolean isVolatile() {
        return this.contains(Modifier.VOLATILE);
    }
    
    public boolean isStrict() {
        return this.contains(Modifier.STRICT);
    }
    
    public boolean isVarArgs() {
        return this.contains(Modifier.VARARGS);
    }
    
    public boolean isBridge() {
        return this.contains(Modifier.BRIDGE);
    }
    
    public boolean isSynthetic() {
        return this.contains(Modifier.SYNTHETIC);
    }
    
    public boolean isInterface() {
        return this.contains(Modifier.INTERFACE);
    }
    
    public boolean isAnnotation() {
        return this.contains(Modifier.ANNOTATION);
    }
    
    public boolean isEnum() {
        return this.contains(Modifier.ENUM);
    }

    /*
     * Invoked by JVM automatically.
     */
    private void readObject(
            java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        
        in.defaultReadObject();
        
        int mos = this.modifierOrdinals;
        int jms = 0;
        int sz = 0;
        for (int ordinal = MAX_SIZE - 1; ordinal >= 0; ordinal--) {
            if ((mos & (1 << ordinal)) != 0) {
                jms |= Modifier.ALL_MODIFIERS[ordinal].jdkModifier;
                sz++;
            }
        }
        this.jdkModifiers = jms;
        this.size = sz;
    }

    @Override
    public boolean isEmpty() {
        return this.modifierOrdinals == 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Modifier) {
            int ordinal = ((Modifier)o).ordinal();
            return (this.modifierOrdinals & 1 << ordinal) != 0;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c instanceof ModifierSet) {
            int ordinalsOfC = ((ModifierSet)c).modifierOrdinals;
            return (this.modifierOrdinals & ordinalsOfC) == ordinalsOfC;
        }
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object[] toArray() {
        return BasicAlgorithms.collectionToArray(this);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return BasicAlgorithms.collectionToArray(this, a);
    }

    @Override
    public boolean add(Modifier e) {
        /*
         * Notice, don't write the code like this:
         * 
         * int jms = this.jdkModifiers | modifier.jdkModifier;
         * if (jms != this.jdkModifiers) {
         *      ... ...
         * }
         * 
         * The reason is the problem such as
         * Modifier.BRIDGE and Modifier.VOLATILE 
         * have same jdk modifier.
         */
        int mos = this.modifierOrdinals | (1 << e.ordinal());
        if (mos != this.modifierOrdinals) {
            this.validateModifier(e);
            this.modifierOrdinals = mos;
            this.jdkModifiers |= e.jdkModifier;
            this.size++;
            this.modCount++;
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Modifier> c) {
        boolean retval = false;
        for (Modifier modifier : c) {
            retval |= this.add(modifier);
        }
        return retval;
    }

    @Override
    public void clear() {
        this.modifierOrdinals = 0;
        this.jdkModifiers = 0;
        this.size = 0;
        this.modCount++;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Modifier) {
            Modifier modifier = (Modifier)o;
            /*
             * Notice, don't write the code like this:
             * 
             * int jms = this.jdkModifiers & ~modifier.jdkModifier;
             * if (jms != this.jdkModifiers) {
             *      ... ...
             * }
             * 
             * The reason is the problem such as
             * Modifier.BRIDGE and Modifier.VOLATILE 
             * have same jdk modifier.
             */
            int mos = this.modifierOrdinals & ~(1 << modifier.ordinal());
            if (mos != this.modifierOrdinals) {
                this.modifierOrdinals = mos;
                this.jdkModifiers &= ~modifier.jdkModifier;
                this.size--;
                this.modCount++;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c instanceof ModifierSet) {
            int ordinals = this.modifierOrdinals;
            int common = ordinals & ((ModifierSet)c).modifierOrdinals;
            if (common == 0) {
                return false;
            } else if (common == ordinals && ordinals != 0) {
                this.modifierOrdinals = 0;
                this.jdkModifiers = 0;
                this.size = 0;
                this.modCount++;
                return true;
            }
        }
        
        boolean retval = false;
        if (c.size() < this.size) {
            for (Object o : c) {
                retval |= this.remove(o);
            }
        } else {
            Iterator<Modifier> iterator = this.iterator();
            while (iterator.hasNext()) {
                if (c.contains(iterator.next())) {
                    iterator.remove();
                    retval = true;
                }
            }
        }
        return retval;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (c instanceof ModifierSet) {
            int ordinals = this.modifierOrdinals;
            int common = ordinals & ((ModifierSet)c).modifierOrdinals;
            if (common == 0 && ordinals != 0) {
                this.modifierOrdinals = 0;
                this.jdkModifiers = 0;
                this.size = 0;
                this.modCount++;
                return true;
            } else if (ordinals == common) {
                return false;
            }
        }
        
        boolean retval = false;
        Iterator<Modifier> iterator = this.iterator();
        while (iterator.hasNext()) {
            if (!c.contains(iterator.next())) {
                iterator.remove();
                retval = true;
            }
        }
        return retval;
    }

    @Override
    public Iterator<Modifier> iterator() {
        return this.new IteratorImpl();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean addSpace = false;
        for (Modifier modifier : this) {
            if (modifier.keyword) {
                if (addSpace) {
                    builder.append(' ');
                } else {
                    addSpace = true;
                }
                builder.append(modifier.getModifierName());
            }
        }
        return builder.toString();
    }

    public String toFullString() {
        StringBuilder builder = new StringBuilder();
        boolean addSpace = false;
        for (Modifier modifier : this) {
            if (addSpace) {
                builder.append(' ');
            } else {
                addSpace = true;
            }
            builder.append(modifier.getModifierName());
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return this.modifierOrdinals;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModifierSet)) {
            return false;
        }
        return this.modifierOrdinals == ((ModifierSet)obj).modifierOrdinals;
    }
    
    @Override
    public ModifierSet clone() {
        try {
            return (ModifierSet)super.clone();
        } catch (CloneNotSupportedException canNotHappen) {
            throw new AssertionError("Internal bug", canNotHappen);
        }
    }

    void validateNoConflicts(Modifier newModifier, Conflict[] conflicts) {
        for (Conflict conflict : conflicts) {
            if (conflict.modifier1 == newModifier && this.contains(conflict.modifier2)) {
                throw new IllegalArgumentException(
                        conflictModifier(ModifierSet.class, newModifier, conflict.modifier2)
                );
            }
        }
    }

    private class IteratorImpl implements Iterator<Modifier> {
        
        private int expectModCount;
        
        private int preparedOrdinal;
        
        private int lastRetOrdinal;
        
        public IteratorImpl() {
            this.expectModCount = ModifierSet.this.modCount;
            this.preparedOrdinal = -1;
            this.lastRetOrdinal = -1;
            this.prepare();
        }
        
        private void prepare() {
            int mos = ModifierSet.this.modifierOrdinals;
            int po = this.preparedOrdinal + 1;
            while (po < MAX_SIZE) {
                if ((mos & (1 << po)) != 0) {
                    break;
                }
                po++;
            }
            this.preparedOrdinal = po;
        }

        @Override
        public boolean hasNext() {
            return this.preparedOrdinal < MAX_SIZE;
        }

        @Override
        public Modifier next() {
            if (this.expectModCount != ModifierSet.this.modCount) {
                throw new ConcurrentModificationException(concurrentModifcation());
            }
            if (this.preparedOrdinal >= MAX_SIZE) {
                throw new NoSuchElementException(noSuchElement());
            }
            this.lastRetOrdinal = this.preparedOrdinal;
            this.prepare();
            return Modifier.ALL_MODIFIERS[this.lastRetOrdinal];
        }

        @Override
        public void remove() {
            if (this.expectModCount != ModifierSet.this.modCount) {
                throw new ConcurrentModificationException(concurrentModifcation());
            }
            int lro = this.lastRetOrdinal;
            if (this.lastRetOrdinal == -1) {
                throw new IllegalStateException(removeNoExtractedElement());
            }
            ModifierSet owner = ModifierSet.this;
            owner.modifierOrdinals &= ~(1 << lro);
            owner.jdkModifiers &= ~Modifier.ALL_MODIFIERS[lro].jdkModifier;
            owner.size--;
            this.expectModCount = ++owner.modCount;
            this.lastRetOrdinal = -1;
        }
        
    }
    
    private static class ForType extends ModifierSet {

        private static final long serialVersionUID = 5531533386217994L;

        @Override
        public boolean isForType() {
            return true;
        }
        
        @Override
        public boolean isForField() {
            return false;
        }

        @Override
        public boolean isForMethod() {
            return false;
        }

        @Override
        void validateModifier(Modifier e) {
            if (!e.forTypes) {
                throw new IllegalArgumentException(invalidTypeModifier(e));
            }
            this.validateNoConflicts(e, CONFLICTS);
            this.validateNoConflicts(e, CONFLICTS_FOR_TYPES);
        }
        
    }
    
    private static class ForField extends ModifierSet {

        private static final long serialVersionUID = 4217682436361761548L;

        @Override
        public boolean isForType() {
            return false;
        }
        
        @Override
        public boolean isForField() {
            return true;
        }

        @Override
        public boolean isForMethod() {
            return false;
        }

        @Override
        void validateModifier(Modifier e) {
            if (!e.forFields) {
                throw new IllegalArgumentException(invalidFieldModifier(e));
            }
            this.validateNoConflicts(e, CONFLICTS);
            this.validateNoConflicts(e, CONFLICTS_FOR_FIELDS);
        }
        
    }
    
    private static class ForMethod extends ModifierSet {

        private static final long serialVersionUID = 4945344517280325898L;
        
        @Override
        public boolean isForType() {
            return false;
        }

        @Override
        public boolean isForField() {
            return false;
        }

        @Override
        public boolean isForMethod() {
            return true;
        }

        @Override
        void validateModifier(Modifier e) {
            if (!e.forMethods) {
                throw new IllegalArgumentException(invalidMethodModifier(e));
            }
            this.validateNoConflicts(e, CONFLICTS);
            this.validateNoConflicts(e, CONFLICTS_FOR_METHODS);
        }
        
    }
    
    private static class Conflict {
        
        public final Modifier modifier1;
        
        public final Modifier modifier2;

        public Conflict(Modifier modifier1, Modifier modifier2) {
            if (modifier1 == modifier2) {
                throw new AssertionError();
            }
            this.modifier1 = modifier1;
            this.modifier2 = modifier2;
        }
        
    }
    
    @I18N
    private static native String conflictModifier(
            Class<ModifierSet> modifierSetType,
            Modifier newModifier,
            Modifier existingModifier);
    
    @I18N
    private static native String concurrentModifcation();
    
    @I18N
    private static native String noSuchElement();
    
    @I18N
    private static native String removeNoExtractedElement();
    
    @I18N
    private static native String invalidTypeModifier(Modifier modifier);
    
    @I18N
    private static native String invalidFieldModifier(Modifier modifier);
    
    @I18N
    private static native String invalidMethodModifier(Modifier modifier);

    static {
        
        Conflict[] conflicts = new Conflict[] {
                new Conflict(Modifier.PUBLIC, Modifier.PROTECTED),
                new Conflict(Modifier.PROTECTED, Modifier.PUBLIC),
                
                new Conflict(Modifier.PROTECTED, Modifier.PRIVATE),
                new Conflict(Modifier.PRIVATE, Modifier.PROTECTED),
                
                new Conflict(Modifier.PRIVATE, Modifier.PUBLIC),
                new Conflict(Modifier.PUBLIC, Modifier.PRIVATE),
        };
        Conflict[] conflictsForTypes = new Conflict[] {
                new Conflict(Modifier.FINAL, Modifier.ABSTRACT),
                new Conflict(Modifier.ABSTRACT, Modifier.FINAL),
        };
        Conflict[] conflictsForFields = new Conflict[] {
                new Conflict(Modifier.FINAL, Modifier.VOLATILE),
                new Conflict(Modifier.VOLATILE, Modifier.FINAL),
        };
        Conflict[] conflictsForMethods = new Conflict[] {
                new Conflict(Modifier.STATIC, Modifier.ABSTRACT),
                new Conflict(Modifier.ABSTRACT, Modifier.STATIC),
                
                new Conflict(Modifier.FINAL, Modifier.ABSTRACT),
                new Conflict(Modifier.ABSTRACT, Modifier.FINAL),
                
                new Conflict(Modifier.ABSTRACT, Modifier.NATIVE),
                new Conflict(Modifier.NATIVE, Modifier.ABSTRACT),
        };
        CONFLICTS = conflicts;
        CONFLICTS_FOR_TYPES = conflictsForTypes;
        CONFLICTS_FOR_FIELDS = conflictsForFields;
        CONFLICTS_FOR_METHODS = conflictsForMethods;
    }
    
}
