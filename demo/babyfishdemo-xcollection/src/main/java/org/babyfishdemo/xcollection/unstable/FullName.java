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
package org.babyfishdemo.xcollection.unstable;

import java.io.Serializable;
import java.util.Comparator;
 
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Singleton;
 
/**
 * @author Tao Chen
 */
public class FullName {
 
    public static final Comparator<FullName> FIRST_NAME_COMPARATOR = 
            Singleton.getInstance(FirstNameComparator.class);
    
    public static final EqualityComparator<FullName> FIRST_NAME_EQUALITY_COMPARATOR = 
            Singleton.getInstance(FirstNameEqualityComparator.class);
    
    public static final Comparator<FullName> LAST_NAME_COMPARATOR = 
            Singleton.getInstance(LastNameComparator.class);
    
    public static final EqualityComparator<FullName> LAST_NAME_EQUALITY_COMPARATOR = 
            Singleton.getInstance(LastNameEqualityComparator.class);
    
    public static final Comparator<FullName> FULL_NAME_COMPARATOR = 
            Singleton.getInstance(FullNameComparator.class);
    
    public static final EqualityComparator<FullName> FULL_NAME_EQUALITY_COMPARATOR = 
            Singleton.getInstance(FullNameEqualityComparator.class);
 
    private String firstName;
    
    /*
     * FrozenContext uses java.lang.WeakReference<T> to retain
     * all the hash-tables and red-black-trees whose keys use the
     * org.babyfish.collection.FrozenEqualityComparator<T> and
     * org.babyfish.collection.FrozenComparator<T>
     * whose comparison algorithm depends on "this.firstName".
    */
    private FrozenContext<FullName> firstNameFrozenContext;
    
    private String lastName;
    
    /*
     * FrozenContext uses java.lang.WeakReference<T> to retain
     * all the hash-tables and red-black-trees whose keys use the
     * org.babyfish.collection.FrozenEqualityComparator<T> and
     * org.babyfish.collection.FrozenComparator<T>
     * whose comparison algorithm depends on "this.lastName".
    */
    private FrozenContext<FullName> lastNameFrozenContext;
    
    public FullName() {
    }
    
    public FullName(String firstName, String lastName) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }
    
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        
        FrozenContext<FullName> ctx = this.firstNameFrozenContext;
        
        /*
         * Before modification, remove the current object from the all the
         * hash-tables and red-black-trees retained by this.firstNameFrozenContext
        */
        FrozenContext.suspendFreezing(ctx, this);
        try {
            this.firstName = firstName;
        } finally {
            /*
             * After modification, add the current object into the all the
             * hash-tables and red-black-trees retained by this.firstNameFrozenContext
             * AGAIN!
            */
            FrozenContext.resumeFreezing(ctx);
        }
    }
    
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        
        FrozenContext<FullName> ctx = this.lastNameFrozenContext;
        
        /*
         * Before modification, remove the current object from the all the
         * hash-tables and red-black-trees retained by this.lastNameFrozenContext
        */
        FrozenContext.suspendFreezing(ctx, this);
        try {
            this.lastName = lastName;
        } finally {
            /*
             * After modification, add the current object into the all the
             * hash-tables and red-black-trees retained by this.lastNameFrozenContext
             * AGAIN!
            */
            FrozenContext.resumeFreezing(ctx);
        }
    }
    
    @Override
    public String toString() {
        return "{ firstName: '" + this.firstName + "', lastName: '" + this.lastName + "' }";
    }
    
    private static class FirstNameComparator extends Singleton 
    implements FrozenComparator<FullName>, Serializable {
        
        private static final long serialVersionUID = 3457648054782417419L;
        
        @Override
        public int compare(FullName o1, FullName o2) {
            Integer preCmp = Nulls.preCompare(o1, o2);
            if (preCmp != null) {
                return preCmp;
            }
            return Nulls.compare(o1.firstName, o2.firstName);
        }
        
        @Override
        public void freeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
        }
        
        @Override
        public void unfreeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.remove(obj.firstNameFrozenContext, ctx);
        }
    }
    
    private static class FirstNameEqualityComparator extends Singleton 
    implements FrozenEqualityComparator<FullName>, Serializable {
        
        private static final long serialVersionUID = 3457648054782417419L;
        
        @Override
        public int hashCode(FullName o) {
            /*
             * For EqualityComparator, you need NOT to check whether
             * "o == null" because BabyFish X Collection Framework can handle it automatically.
             */
            return Nulls.hashCode(o.firstName);
        }
 
        @Override
        public boolean equals(FullName o1, FullName o2) {
            /*
             * For EqualityComparator, you need NOT to check whether
             * "o1 == null" and "o2 == null" because BabyFish X Collection Framework can handle it automatically.
             */
            return Nulls.equals(o1.firstName, o2.firstName);
        }
        
        @Override
        public void freeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
        }
        
        @Override
        public void unfreeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.remove(obj.firstNameFrozenContext, ctx);
        }
    }
    
    private static class LastNameComparator extends Singleton 
    implements FrozenComparator<FullName>, Serializable {
        
        private static final long serialVersionUID = 3457648054782417419L;
        
        @Override
        public int compare(FullName o1, FullName o2) {
            Integer preCmp = Nulls.preCompare(o1, o2);
            if (preCmp != null) {
                return preCmp;
            }
            return Nulls.compare(o1.lastName, o2.lastName);
        }
        
        @Override
        public void freeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }
        
        @Override
        public void unfreeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
    
    private static class LastNameEqualityComparator extends Singleton 
    implements FrozenEqualityComparator<FullName>, Serializable {
        
        private static final long serialVersionUID = 3457648054782417419L;
        
        @Override
        public int hashCode(FullName o) {
            /*
             * For EqualityComparator, you need NOT to check whether
             * "o == null" because BabyFish X Collection Framework can handle it automatically.
             */
            return Nulls.hashCode(o.lastName);
        }
 
        @Override
        public boolean equals(FullName o1, FullName o2) {
            /*
             * For EqualityComparator, you need NOT to check whether
             * "o1 == null" and "o2 == null" because BabyFish X Collection Framework can handle it automatically.
             */
            return Nulls.equals(o1.lastName, o2.lastName);
        }
        
        @Override
        public void freeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }
        
        @Override
        public void unfreeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
    
    private static class FullNameComparator extends Singleton 
    implements FrozenComparator<FullName>, Serializable {
        
        private static final long serialVersionUID = 3457648054782417419L;
        
        @Override
        public int compare(FullName o1, FullName o2) {
            Integer preCmp = Nulls.preCompare(o1, o2);
            if (preCmp != null) {
                return preCmp;
            }
            int cmp = Nulls.compare(o1.firstName, o2.firstName);
            if (cmp != 0) {
                return cmp;
            }
            return Nulls.compare(o1.lastName, o2.lastName);
        }
        
        @Override
        public void freeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }
        
        @Override
        public void unfreeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.remove(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
    
    private static class FullNameEqualityComparator extends Singleton 
    implements FrozenEqualityComparator<FullName>, Serializable {
        
        private static final long serialVersionUID = 3457648054782417419L;
        
        @Override
        public int hashCode(FullName o) {
            /*
             * For EqualityComparator, you need NOT to check whether
             * "o == null" because BabyFish X Collection Framework can handle it automatically.
             */
            return 31 * Nulls.hashCode(o.firstName) + Nulls.hashCode(o.lastName);
        }
 
        @Override
        public boolean equals(FullName o1, FullName o2) {
            /*
             * For EqualityComparator, you need NOT to check whether
             * "o1 == null" and "o2 == null" because BabyFish X Collection Framework can handle it automatically.
             */
            return Nulls.equals(o1.firstName, o2.firstName) && Nulls.equals(o1.lastName, o2.lastName);
        }
        
        @Override
        public void freeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }
        
        @Override
        public void unfreeze(FullName obj, FrozenContext<FullName> ctx) {
            obj.firstNameFrozenContext = FrozenContext.remove(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
}
