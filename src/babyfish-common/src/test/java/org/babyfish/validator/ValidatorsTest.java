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
package org.babyfish.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class ValidatorsTest {
    
    @Test
    public void testCombiningAndRemoving() throws IOException, ClassNotFoundException {
        Validator<Object> validator = this.testCombining();
        this.testRemoving(validator);
    }
    
    @Test
    public void testCombiningAndDescendingRemoving() throws IOException, ClassNotFoundException {
        Validator<Object> validator = this.testCombining();
        this.testDescendingRemoving(validator);
    }
    
    @Test
    public void testDescendingCombiningAndRemoving() throws IOException, ClassNotFoundException {
        Validator<Object> validator = this.testDescendingCombining();
        this.testRemoving(validator);
    }
    
    @Test
    public void testDescendingCombiningAndDescendingRemoving() throws IOException, ClassNotFoundException {
        Validator<Object> validator = this.testDescendingCombining();
        this.testDescendingRemoving(validator);
    }
    
    private Validator<Object> testCombining() throws IOException, ClassNotFoundException {
        SetRef ref = SetRef.INSTANCE;
        Validator<Object> validator = null;
        
        /*
         * +1
         */
        validator = Validators.combine(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        
        validator = Validators.combine(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        
        validator = Validators.combine(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        
        /*
         * +2
         */
        validator = Validators.combine(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        
        validator = Validators.combine(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        
        validator = Validators.combine(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        
        /*
         * +3
         */
        validator = Validators.combine(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        
        validator = Validators.combine(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        
        validator = Validators.combine(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        
        /*
         * +4
         */
        validator = Validators.combine(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        
        validator = Validators.combine(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        
        validator = Validators.combine(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        
        /*
         * +A
         */
        validator = Validators.combine(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        
        /*
         * +B
         */
        validator = Validators.combine(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        
        /*
         * +C
         */
        validator = Validators.combine(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        
        /*
         * +D
         */
        validator = Validators.combine(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        return validator;
    }
    
    private Validator<Object> testDescendingCombining() throws IOException, ClassNotFoundException {
        SetRef ref = SetRef.INSTANCE;
        Validator<Object> validator = null;
        
        /*
         * +D
         */
        validator = Validators.combine(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * +C
         */
        validator = Validators.combine(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * +B
         */
        validator = Validators.combine(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * +A
         */
        validator = Validators.combine(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.combine(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * +4
         */
        validator = Validators.combine(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        
        /*
         * +3
         */
        validator = Validators.combine(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        
        /*
         * +2
         */
        validator = Validators.combine(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        
        /*
         * +1
         */
        validator = Validators.combine(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.combine(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        return validator;
    }
    
    private void testRemoving(Validator<Object> validator) throws IOException, ClassNotFoundException {
        SetRef ref = SetRef.INSTANCE;
        
        /*
         * -1
         */
        validator = Validators.remove(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        
        /*
         * -2
         */
        validator = Validators.remove(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-2-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        
        /*
         * -3
         */
        validator = Validators.remove(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        
        /*
         * -4
         */
        validator = Validators.remove(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-4-D", ref.toString());
        
        validator = Validators.remove(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * -A
         */
        validator = Validators.remove(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * -B
         */
        validator = Validators.remove(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * -C
         */
        validator = Validators.remove(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        /*
         * -D
         */
        validator = Validators.remove(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorD(ref));
        Assert.assertNull(validator);
        Assert.assertNull(serializingClone(validator));
    }
    
    private void testDescendingRemoving(Validator<Object> validator) throws IOException, ClassNotFoundException {
        SetRef ref = SetRef.INSTANCE;
        
        /*
         * -D
         */
        validator = Validators.remove(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-D", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorD(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        
        /*
         * -C
         */
        validator = Validators.remove(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-C", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorC(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        
        /*
         * -B
         */
        validator = Validators.remove(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-B", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorB(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        
        /*
         * -A
         */
        validator = Validators.remove(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4-A", ref.toString());
        
        validator = Validators.remove(validator, new ValidatorA(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        
        /*
         * -4
         */
        validator = Validators.remove(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        
        validator = Validators.remove(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3-4", ref.toString());
        
        validator = Validators.remove(validator, new Validator4(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        
        /*
         * -3
         */
        validator = Validators.remove(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        
        validator = Validators.remove(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2-3", ref.toString());
        
        validator = Validators.remove(validator, new Validator3(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        
        /*
         * -2
         */
        validator = Validators.remove(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        
        validator = Validators.remove(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1-2", ref.toString());
        
        validator = Validators.remove(validator, new Validator2(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        
        /*
         * -1
         */
        validator = Validators.remove(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        
        validator = Validators.remove(validator, new Validator1(ref));
        ref.reset();
        validator.validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        ref.reset();
        serializingClone(validator).validate(new Object());
        Assert.assertEquals("-1", ref.toString());
        
        validator = Validators.remove(validator, new Validator1(ref));
        Assert.assertNull(validator);
        Assert.assertNull(serializingClone(validator));
    }
    
    @SuppressWarnings("unchecked")
    static Validator<Object> serializingClone(Validator<Object> validator) throws IOException, ClassNotFoundException {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(validator);
            oout.flush();
            buf = bout.toByteArray();
        }
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            return (Validator<Object>)oin.readObject();
        }
    }
    
    static class Validator1 implements Validator<Object>, Serializable {
        
        private static final long serialVersionUID = 8660555619431262574L;
        
        private SetRef builder;
        
        Validator1(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-1");
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class Validator2 implements Validator<Object>, Serializable {
        
        private static final long serialVersionUID = 8607538989316739916L;
        
        private SetRef builder;
        
        Validator2(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-2");
        }
        
        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class Validator3 implements Validator<Object>, Serializable {
        
        private static final long serialVersionUID = -1352523468657779071L;
        
        private SetRef builder;
        
        Validator3(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-3");
        }
        
        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class Validator4 implements Validator<Object>, Serializable {
        
        private static final long serialVersionUID = -1776036643135834577L;
        
        private SetRef builder;
        
        Validator4(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-4");
        }
        
        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class ValidatorA implements Validator<Object>, Serializable {
        
        private static final long serialVersionUID = 9108313817848819570L;
        
        private SetRef builder;
        
        ValidatorA(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-A");
        }
        
        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class ValidatorB implements SuppressibleValidator<Object>, Serializable {
        
        private static final long serialVersionUID = 3620820068965957041L;
        
        private SetRef builder;
        
        ValidatorB(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-B");
        }
        
        @Override
        public boolean suppress(Validator<Object> other) {
            return other instanceof ValidatorA;
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class ValidatorC implements SuppressibleValidator<Object>, Serializable {
        
        private static final long serialVersionUID = 8411499946134893311L;
        
        private SetRef builder;
        
        ValidatorC(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-C");
        }
        
        @Override
        public boolean suppress(Validator<Object> other) {
            return other instanceof ValidatorB || other instanceof ValidatorA;
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class ValidatorD implements SuppressibleValidator<Object>, Serializable {
        
        private static final long serialVersionUID = -5677741051780784669L;
        
        private SetRef builder;
        
        ValidatorD(SetRef builder) {
            this.builder = builder;
        }

        @Override
        public void validate(Object e) {
            builder.append("-D");
        }
        
        @Override
        public boolean suppress(Validator<Object> other) {
            return other instanceof ValidatorC || other instanceof ValidatorB || other instanceof ValidatorA;
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass() == obj.getClass();
        }
    }
    
    static class SetRef implements Serializable {
        
        private static final long serialVersionUID = 5712850331631075219L;

        public static final SetRef INSTANCE = new SetRef();
        
        private Set<String> set;
        
        private SetRef() {
            this.reset();
        }
        
        SetRef reset() {
            this.set = new TreeSet<>();
            return this;
        }
        
        SetRef append(String s) {
            this.set.add(s);
            return this;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            this.set.forEach(s -> { builder.append(s); });
            return builder.toString();
        }
        
        private Object writeReplace() throws ObjectStreamException {
            return WR.INSTANCE;
        }
        
        private static class WR implements Serializable {
            
            private static final long serialVersionUID = -1215161798002417474L;
            
            private static final WR INSTANCE = new WR();  
            
            private Object readResolve() throws ObjectStreamException {
                return SetRef.INSTANCE;
            }
        }
    }
}
