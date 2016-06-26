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
package org.babyfish.test.collection.wrapper;

import junit.framework.Assert;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXSet;
import org.babyfish.collection.spi.wrapper.ConstructOnlyRootData;
import org.babyfish.collection.spi.wrapper.SetOnceOnlyRootData;
import org.babyfish.validator.Validator;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class WrapperTest {
    
    @Test
    public void testSetOnceOnlyWithoutInitalization() {
        String result = "";
        Wrapper<String> wrapper = new SetOnceOnlyWrapper<String>(null);
        
        try {
            wrapper.setBase(new HashSet<String>());
            result += "-setOne";
        } catch (IllegalStateException ex) {
            result += "-setOne.Failed";
        }
        
        try {
            wrapper.setBase(new TreeSet<String>());
            result += "-setTwo";
        } catch (IllegalStateException ex) {
            result += "-setTwo.Failed";
        }
        
        Assert.assertEquals("-setOne-setTwo.Failed", result);
    }
    
    @Test
    public void testSetOnceOnlyWithInitalization() {
        String result = "";
        Wrapper<String> wrapper = new SetOnceOnlyWrapper<String>(new HashSet<String>());
        try {
            wrapper.setBase(new TreeSet<String>());
            result += "-set";
        } catch (IllegalStateException ex) {
            result += "-set.Failed";
        }
        Assert.assertEquals("-set.Failed", result);
    }
    
    @Test
    public void testConstructOnlyWithoutInitalization() {
        String result = "";
        try {
            new ConstructOnlyWrapper<String>(null);
            result += "-skipConstruct";
        } catch (IllegalArgumentException ex) {
            result += "-skipConstruct.Failed";
        }
        Assert.assertEquals("-skipConstruct.Failed", result);
    }
    
    @Test
    public void testConstructOnlyWithInitalization() {
        String result = "";
        Wrapper<String> wrapper = new ConstructOnlyWrapper<String>(new HashSet<String>());
        try {
            wrapper.setBase(new TreeSet<String>());
            result += "-set";
        } catch (IllegalStateException ex) {
            result += "-set.Failed";
        }
        Assert.assertEquals("-set.Failed", result);
    }
    
    @Test
    public void testValidatorForSetSeveralTimes() {
        final Wrapper<String> wrapper = new Wrapper<String>(null);
        Validator<String> validator = new Validator<String>() {
            
            @Override
            public void validate(String e) {
                if (!wrapper.isEmpty()) {
                    throw new IllegalArgumentException("{0F6A8718-F30B-4f16-A209-8FC855F09DA6}");
                }
            }
        };
        wrapper.addValidator(validator);
        String result = "";
        for (int i = 0; i < 4; i++) {
            wrapper.setBase(new HashSet<String>());
            for (int ii = 0; ii < 2; ii++) {
                try {
                    wrapper.add(Integer.toString(ii));
                } catch (IllegalArgumentException ex) {
                    if (ex.getMessage().equals("{0F6A8718-F30B-4f16-A209-8FC855F09DA6}")) {
                        result += "-" + i + '.' + ii;
                    }
                }
            }
        }
        Assert.assertEquals("-0.1-1.1-2.1-3.1", result);
    }
    
    static class Wrapper<E> extends AbstractWrapperXSet<E> {

        public Wrapper(XSet<E> base) {
            super(base);
        }

        @Override
        protected RootData<E> createRootData() {
            return new RootData<E>();
        }
        
        protected void setBase(XSet<E> base) {
            this.<RootData<E>>getRootData().setBase(base);
        }
        
        private static class RootData<E> extends AbstractWrapperXSet.RootData<E> {

            private static final long serialVersionUID = 8850330078523347237L;

            @Override
            protected void setBase(XSet<E> base) {
                super.setBase(base);
            }
        }
    }
    
    static class SetOnceOnlyWrapper<E> extends Wrapper<E> {

        public SetOnceOnlyWrapper(XSet<E> base) {
            super(base);
        }
        
        @Override
        protected RootData<E> createRootData() {
            return new RootData<E>();
        }
        
        protected static class RootData<E> extends Wrapper.RootData<E> implements SetOnceOnlyRootData {

            private static final long serialVersionUID = 7339894847397176469L;

        }
        
    }
    
    static class ConstructOnlyWrapper<E> extends Wrapper<E> {

        public ConstructOnlyWrapper(XSet<E> base) {
            super(base);
        }
        
        @Override
        protected RootData<E> createRootData() {
            return new RootData<E>();
        }
        
        protected static class RootData<E> extends Wrapper.RootData<E> implements ConstructOnlyRootData {

            private static final long serialVersionUID = 8637991962148700887L;

        }
        
    }
    
}
