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
package org.babyfish.model.nonready;

import java.util.Arrays;
import java.util.Collection;

import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.nonready.entities.Person;
import org.babyfish.model.spi.DirtinessAwareScalarLoader;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.ScalarBatchLoadingExecutor;
import org.babyfish.model.spi.ScalarLoader;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class DeferralTest {
    
    private static final int ADDRESS_ID = ModelClass.of(Person.class).getProperties().get("address").getId();
    
    private static final int IMAGE_ID = ModelClass.of(Person.class).getProperties().get("image").getId();

    @Test(expected = IllegalStateException.class)
    public void testUnload() {
        Person person = new Person();
        unloadAddress(person);
        Assert.assertTrue(isAddressUnloaded(person));
        person.getAddress();
    }
    
    @Test
    public void testImplicitlyLoad() {
        Person person = new Person();
        unloadAddress(person);
        setScalarLoader(person, new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        Assert.assertTrue(isAddressUnloaded(person));
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
    }
    
    @Test
    public void testExplicitlyLoad() {
        Person person = new Person();
        unloadAddress(person);
        setScalarLoader(person, new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        Assert.assertTrue(isAddressUnloaded(person));
        loadAddress(person);
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testUnloadByOMAPI() {
        Person person = new Person();
        unloadAddress(person);
        Assert.assertTrue(isAddressUnloaded(person));
        objectModel(person).get(ADDRESS_ID);
    }
    
    @Test
    public void testImplicitlyLoadByOMAPI() {
        Person person = new Person();
        unloadAddress(person);
        setScalarLoader(person, new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        Assert.assertTrue(isAddressUnloaded(person));
        Assert.assertEquals("addressLoadedByLoader", objectModel(person).get(ADDRESS_ID));
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
    }
    
    @Test
    public void testExplicitlyLoadByOMAPI() {
        Person person = new Person();
        unloadAddress(person);
        setScalarLoader(person, new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        Assert.assertTrue(isAddressUnloaded(person));
        objectModel(person).load(ADDRESS_ID);
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertEquals("addressLoadedByLoader", objectModel(person).get(ADDRESS_ID));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
    }
    
    @Test
    public void testLoadAddressFirst() {
        Person person = new Person();
        unloadAddress(person);
        unloadImage(person);
        setScalarLoader(person, new LoaderImpl());
        Assert.assertTrue(isAddressUnloaded(person));
        Assert.assertTrue(isImageUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
        
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertFalse(isImageUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, person.getImage()));
    }
    
    @Test
    public void testLoadImageFirst() {
        Person person = new Person();
        unloadAddress(person);
        unloadImage(person);
        setScalarLoader(person, new LoaderImpl());
        Assert.assertTrue(isAddressUnloaded(person));
        Assert.assertTrue(isImageUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, person.getImage()));
        
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertFalse(isImageUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
    }
    
    @Test
    public void testChangeDirty() {
        Person person = new Person();
        unloadAddress(person);
        unloadImage(person);
        setScalarLoader(person, new LoaderImpl());
        
        Assert.assertTrue(isAddressUnloaded(person));
        Assert.assertTrue(isImageUnloaded(person));
        Assert.assertEquals(0, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        
        person.setAddress("Unknown");
        Assert.assertEquals("Unknown", person.getAddress());
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertTrue(isImageUnloaded(person));
        Assert.assertEquals(1, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
        
        person.setImage(new byte[] {1, 4, 9 });
        Assert.assertEquals("Unknown", person.getAddress());
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 4, 9 }, person.getImage()));
        Assert.assertFalse(isAddressUnloaded(person));
        Assert.assertFalse(isImageUnloaded(person));
        Assert.assertEquals(2, ((LoaderImpl)getScalarLoader(person)).getDirtyCount());
    }
    
    @Test
    public void testBatchLoad() {
        Person person1 = new Person();
        Person person2 = new Person();
        LoaderImpl loader = new LoaderImpl();
        setScalarLoader(person1, loader);
        setScalarLoader(person2, loader);
        unloadAddress(person1);
        unloadImage(person1);
        unloadAddress(person2);
        unloadImage(person2);
        
        Assert.assertTrue(isAddressUnloaded(person1));
        Assert.assertTrue(isImageUnloaded(person1));
        Assert.assertTrue(isAddressUnloaded(person2));
        Assert.assertTrue(isImageUnloaded(person2));
        
        ScalarBatchLoadingExecutor executor = new ScalarBatchLoadingExecutor();
        executor.prepareLoad(objectModel(person1), ADDRESS_ID, IMAGE_ID);
        executor.prepareLoad(objectModel(person2), ADDRESS_ID, IMAGE_ID);
        
        Assert.assertTrue(isAddressUnloaded(person1));
        Assert.assertTrue(isImageUnloaded(person1));
        Assert.assertTrue(isAddressUnloaded(person2));
        Assert.assertTrue(isImageUnloaded(person2));
        
        executor.flush();
        
        Assert.assertFalse(isAddressUnloaded(person1));
        Assert.assertFalse(isImageUnloaded(person1));
        Assert.assertFalse(isAddressUnloaded(person2));
        Assert.assertFalse(isImageUnloaded(person2));
        Assert.assertEquals("addressLoadedByLoader[0]", person1.getAddress());
        Assert.assertTrue(Arrays.equals(new byte[] {1, 2, 3 }, person1.getImage()));
        Assert.assertEquals("addressLoadedByLoader[1]", person2.getAddress());
        Assert.assertTrue(Arrays.equals(new byte[] {2, 3, 4 }, person2.getImage()));
    }
    
    private static ObjectModel objectModel(Person person) {
        return ((ObjectModelProvider)person).objectModel();
    }
    
    private static boolean isAddressUnloaded(Person person) {
        return objectModel(person).isUnloaded(ADDRESS_ID);
    }
    
    private static void unloadAddress(Person person) {
        objectModel(person).unload(ADDRESS_ID);
    }
    
    private static void loadAddress(Person person) {
        objectModel(person).load(ADDRESS_ID);
    }
    
    private static boolean isImageUnloaded(Person person) {
        return objectModel(person).isUnloaded(IMAGE_ID);
    }
    
    private static void unloadImage(Person person) {
        objectModel(person).unload(IMAGE_ID);
    }
    
    private static ScalarLoader getScalarLoader(Person person) {
        return objectModel(person).getScalarLoader();
    }
    
    private static void setScalarLoader(Person person, ScalarLoader scalarLoader) {
        objectModel(person).setScalarLoader(scalarLoader);
    }
    
    static class LoaderImpl implements ScalarLoader, DirtinessAwareScalarLoader {
            
        private int dirtyCount;
        
        @Override
        public void load(Collection<ObjectModel> objectModels, int[] scalarPropertyIds) {
            boolean batch = objectModels.size() > 1;
            int index = 0;
            for (ObjectModel objectModel : objectModels) {
                for (int scalarPropertyId : scalarPropertyIds) {
                    if (scalarPropertyId == ADDRESS_ID) {
                        objectModel.set(
                                scalarPropertyId, 
                                "addressLoadedByLoader" + (batch ? "[" + index + ']' : "")
                        );
                    } else if (scalarPropertyId == IMAGE_ID) {
                        objectModel.set(
                                scalarPropertyId, 
                                new byte[] { (byte)(index + 1), (byte)(index + 2), (byte)(index + 3) }
                        );
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                index++;
            }
        }

        @Override
        public void dirty() {
            this.dirtyCount++;
        }
        
        public int getDirtyCount() {
            return dirtyCount;
        }
    }
}
