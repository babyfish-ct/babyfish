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
package org.babyfishdemo.macollection.unstable;

import java.io.Serializable;

import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Singleton;

/**
 * @author Tao Chen
 */
public class PersonComparators {
    
    /*
     * This FrozenEqualityComparator will be demonstrated by the unit test
     */
    public static final FrozenEqualityComparator<Person> FIRST_NAME_EQUALITY_COMPARATOR =
            Singleton.getInstance(FirstNameEqualityComparator.class);
    
    /*
     * This FrozenComparator will be demonstrated by the unit test
     */
    public static final FrozenComparator<Person> FIRST_NAME_COMPARATOR =
            Singleton.getInstance(FirstNameComparator.class);
    
    // Demo is enough, so unit test does NOT demonstrate it
    public static final FrozenEqualityComparator<Person> LAST_NAME_EQUALITY_COMPARATOR =
            Singleton.getInstance(LastNameEqualityComparator.class);
    
    // Demo is enough, so unit test does NOT demonstrate it
    public static final FrozenComparator<Person> LAST_NAME_COMPARATOR =
            Singleton.getInstance(LastNameComparator.class);
    
    // Demo is enough, so unit test does NOT demonstrate it
    public static final FrozenEqualityComparator<Person> FULL_NAME_EQUALITY_COMPARATOR =
            Singleton.getInstance(FullNameEqualityComparator.class);

    // Demo is enough, so unit test does NOT demonstrate it
    public static final FrozenComparator<Person> FULL_NAME_COMPARATOR =
            Singleton.getInstance(FullNameComparator.class);
    
    private PersonComparators() {
        
    }
    
    private static class FirstNameEqualityComparator 
    extends Singleton 
    implements FrozenEqualityComparator<Person>, Serializable {

        @Override
        public boolean equals(Person o1, Person o2) {
            return Nulls.equals(o1.getFirstName(), o2.getFirstName());
        }

        @Override
        public int hashCode(Person o) {
            return Nulls.hashCode(o.getFirstName());
        }

        @Override
        public void freeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
        }

        @Override
        public void unfreeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.remove(obj.firstNameFrozenContext, ctx);
        }
    }
    
    private static class FirstNameComparator 
    extends Singleton 
    implements FrozenComparator<Person>, Serializable {

        @Override
        public int compare(Person o1, Person o2) {
            Integer preCmp = Nulls.preCompare(o1, o2);
            if (preCmp != null) {
                return preCmp;
            }
            return Nulls.compare(o1.getFirstName(), o2.getFirstName());
        }

        @Override
        public void freeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
        }

        @Override
        public void unfreeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.remove(obj.firstNameFrozenContext, ctx);
        }
    }
    
    private static class LastNameEqualityComparator 
    extends Singleton 
    implements FrozenEqualityComparator<Person>, Serializable {

        @Override
        public boolean equals(Person o1, Person o2) {
            return Nulls.equals(o1.getLastName(), o2.getLastName());
        }

        @Override
        public int hashCode(Person o) {
            return Nulls.hashCode(o.getLastName());
        }

        @Override
        public void freeze(Person obj, FrozenContext<Person> ctx) {
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }

        @Override
        public void unfreeze(Person obj, FrozenContext<Person> ctx) {
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
    
    private static class LastNameComparator 
    extends Singleton 
    implements FrozenComparator<Person>, Serializable {

        @Override
        public int compare(Person o1, Person o2) {
            Integer preCmp = Nulls.preCompare(o1, o2);
            if (preCmp != null) {
                return preCmp;
            }
            return Nulls.compare(o1.getLastName(), o2.getLastName());
        }

        @Override
        public void freeze(Person obj, FrozenContext<Person> ctx) {
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }

        @Override
        public void unfreeze(Person obj, FrozenContext<Person> ctx) {
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
    
    private static class FullNameEqualityComparator 
    extends Singleton 
    implements FrozenEqualityComparator<Person>, Serializable {

        @Override
        public boolean equals(Person o1, Person o2) {
            return Nulls.equals(o1.getFirstName(), o2.getFirstName()) &&
                    Nulls.equals(o1.getLastName(), o2.getLastName());
        }

        @Override
        public int hashCode(Person o) {
            return (Nulls.hashCode(o.getFirstName()) * 31) +
                    Nulls.hashCode(o.getLastName());
        }

        @Override
        public void freeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }

        @Override
        public void unfreeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
    
    private static class FullNameComparator 
    extends Singleton 
    implements FrozenComparator<Person>, Serializable {

        @Override
        public int compare(Person o1, Person o2) {
            Integer preCmp = Nulls.preCompare(o1, o2);
            if (preCmp != null) {
                return preCmp;
            }
            int cmp = Nulls.compare(o1.getFirstName(), o2.getFirstName());
            if (cmp != 0) {
                return cmp;
            }
            return Nulls.compare(o1.getLastName(), o2.getLastName());
        }

        @Override
        public void freeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.combine(obj.lastNameFrozenContext, ctx);
        }

        @Override
        public void unfreeze(Person obj, FrozenContext<Person> ctx) {
            obj.firstNameFrozenContext = FrozenContext.combine(obj.firstNameFrozenContext, ctx);
            obj.lastNameFrozenContext = FrozenContext.remove(obj.lastNameFrozenContext, ctx);
        }
    }
}
