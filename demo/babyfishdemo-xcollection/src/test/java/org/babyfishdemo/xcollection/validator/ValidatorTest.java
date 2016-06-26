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
package org.babyfishdemo.xcollection.validator;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
import org.babyfish.lang.NullArgumentException;
import org.babyfish.validator.Validators;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ValidatorTest {

    @Test
    public void testNonNull() {
        XCollection<String> c = new HashSet<>();
        c.addValidator(Validators.<String>notNull());
        
        // add(null), not allowed
        try {
            c.add(null);
            Assert.fail(NullArgumentException.class.getName() + "is expected");
        } catch (NullArgumentException ex) {
            
        }
        Assert.assertTrue(c.isEmpty());
        
        // add("^w^"), allowed
        c.add("^w^");
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("[^w^]", c.toString());
    }
    
    @Test
    public void testNullOrLengthRange() {
        XCollection<String> c = new ArrayList<>();
        
        /*
         * This list can only accept the element whose length is between 5 and 10
         * or null!
         * 
         * Important Note: 
         * Except "Validators.notNull()", no validators can forbid the null elements!
         */
        c.addValidator(Validators.minLength(5));
        c.addValidator(Validators.maxLength(10));
        
        // Add null, allowed.
        c.add(null);
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("[null]", c.toString());
        
        // Add "orz."(length = 4), not allowed
        try {
            c.add("orz.");
            Assert.fail(IllegalArgumentException.class.getName() + " is expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        // Add "<.)++++++<<"(length = 11), not allowed
        try {
            c.add("<.)++++++<<");
            Assert.fail(IllegalArgumentException.class.getName() + " is expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        // Add "orz=3"(length = 5) and "<.)+++++<<"(length = 10), allowed
        c.add("orz=3");
        c.add("<.)+++++<<");
        Assert.assertEquals(3, c.size());
        Assert.assertEquals("[null, orz=3, <.)+++++<<]", c.toString());
    }
    
    @Test
    public void testNotNullAndLengthRange() {
        XCollection<String> c = new ArrayList<>();
        
        /*
         * This list can only accept the element whose is NOT null
         * and length is between 5 and 10
         * 
         * Important Note: 
         * Except Validators.notNull(), no validators can forbid the null elements!
         */
        c.addValidator(Validators.<String>notNull());
        c.addValidator(Validators.minLength(5));
        c.addValidator(Validators.maxLength(10));
        
        // add(null), not allowed
        try {
            c.add(null);
            Assert.fail(NullArgumentException.class.getName() + "is expected");
        } catch (NullArgumentException ex) {
            
        }
        Assert.assertTrue(c.isEmpty());
        
        // Add "orz."(length = 4), not allowed
        try {
            c.add("orz.");
            Assert.fail(IllegalArgumentException.class.getName() + " is expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        // Add "<.)++++++<<"(length = 11), not allowed
        try {
            c.add("<.)++++++<<");
            Assert.fail(IllegalArgumentException.class.getName() + " is expected");
        } catch (IllegalArgumentException ex) {
            
        }
        
        // Add "orz=3"(length = 5) and "<.)+++++<<"(length = 10), allowed
        c.add("orz=3");
        c.add("<.)+++++<<");
        Assert.assertEquals(2, c.size());
        Assert.assertEquals("[orz=3, <.)+++++<<]", c.toString());
    }
    
    @Test
    public void testMap() {
        XMap<String, String> map = new HashMap<>();
        map.addKeyValidator(Validators.<String>notNull()); //Forbid null key
        map.addValueValidator(Validators.<String>notNull()); //Forbid null value
        
        //put(null, null), not allowed
        try {
            map.put(null, null);
            Assert.fail(NullArgumentException.class.getName() + " is expected");
        } catch (NullArgumentException ex) {
            
        }
        Assert.assertTrue(map.isEmpty());
        
        //put(null, ""), not allowed
        try {
            map.put(null, "");
            Assert.fail(NullArgumentException.class.getName() + " is expected");
        } catch (NullArgumentException ex) {
            
        }
        Assert.assertTrue(map.isEmpty());
        
        //put("", null), not allowed
        try {
            map.put("", null);
            Assert.fail(NullArgumentException.class.getName() + " is expected");
        } catch (NullArgumentException ex) {
            
        }
        Assert.assertTrue(map.isEmpty());
        
        //put("", ""), allowed
        map.put("", "");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("{=}", map.toString());
    }
    
    @Test
    public void testCustomValidator() {
        
        StringBuilder builder = new StringBuilder();
        XCollection<Complex> c = new ArrayList<>();
        MaxComplexAbsValidator 
            validator1 = new MaxComplexAbsValidator(8, builder),
            validator2 = new MaxComplexAbsValidator(6, builder);
        
        /*
         * Add validator1
         */
        c.addValidator(validator1);
        
        /*
         * Add "6 + 7i", not allowed, 
         * because abs(6 + 7i) = 9.2195444572928873100022742817628, > 8
         */
        builder.setLength(0);
        try {
            c.add(new Complex(6, 7));
            Assert.fail(IllegalArgumentException.class.getName() + " is expected");
        } catch (IllegalArgumentException ex) {
            
        }
        Assert.assertTrue(c.isEmpty());
        Assert.assertEquals(
                "MaxComplexAbsValidator(maxAbs = 8.0) validate \"6.0 + 7.0i\";", 
                builder.toString()
        );
        
        /*
         * Add "5 + 6i", allowed, 
         * because abs(5 + 6i) = 7.8102496759066543941297227357591, < 8
         */
        builder.setLength(0);
        c.add(new Complex(5, 6));
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("[5.0 + 6.0i]", c.toString());
        Assert.assertEquals(
                "MaxComplexAbsValidator(maxAbs = 8.0) validate \"5.0 + 6.0i\";", 
                builder.toString()
        );
        
        /*
         * validator1 requires the absolute value of complex must <= 8
         * validator2 requires the absolute value of complex must <= 6
         * 
         * If a complex can be successfully validated by validator2,
         * it can also be successfully validated by validator1, absolutely!
         * 
         * So, validator2 can suppress validator1.
         */
        Assert.assertTrue(validator2.suppress(validator1));
        Assert.assertFalse(validator1.suppress(validator2));
        
        /* 
         * Add validator2.
         * 
         * From now, validator1 will NEVER be used 
         * UNTIL the validator2 is removed from the collection
         */
        c.addValidator(validator2);
        
        /*
         * Add "4 + 5i", not allowed, 
         * because abs(4 + 5i) = 6.4031242374328486864882176746218, > 6
         */
        builder.setLength(0);
        try {
            c.add(new Complex(4, 5));
            Assert.fail(IllegalArgumentException.class.getName() + " is expected");
        } catch (IllegalArgumentException ex) {
            
        }
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("[5.0 + 6.0i]", c.toString());
        Assert.assertEquals( //Only validator2 is used, validate1 is NOT used because it's suppressed by validator2
                "MaxComplexAbsValidator(maxAbs = 6.0) validate \"4.0 + 5.0i\";", 
                builder.toString()
        );
        
        /*
         * Add "3 + 4i", allowed, 
         * because abs(3 + 4i) = 5, < 6
         */
        builder.setLength(0);
        c.add(new Complex(3, 4));
        Assert.assertEquals(2, c.size());
        Assert.assertEquals("[5.0 + 6.0i, 3.0 + 4.0i]", c.toString());
        Assert.assertEquals( // Only validator2 is used, validate1 is NOT used because it's suppressed by validator2
                "MaxComplexAbsValidator(maxAbs = 6.0) validate \"3.0 + 4.0i\";", 
                builder.toString()
        );
        
        /*
         * Removed the validator2,
         * From now, the suppressed validator1 is liberated
         */
        c.removeValidator(validator2);
        
        /*
         * Add "4 + 5i", not allowed, 
         * because abs(4 + 5i) = 6.4031242374328486864882176746218, < 8
         */
        builder.setLength(0);
        c.add(new Complex(4, 5));
        Assert.assertEquals(3, c.size());
        Assert.assertEquals("[5.0 + 6.0i, 3.0 + 4.0i, 4.0 + 5.0i]", c.toString());
        Assert.assertEquals( // validator1 is used again because it has been liberated.
                "MaxComplexAbsValidator(maxAbs = 8.0) validate \"4.0 + 5.0i\";", 
                builder.toString()
        );
    }
}
