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
package org.babyfish.hibernate.jpacriteria;

import java.lang.reflect.Field;

import org.babyfish.hibernate.jpa.internal.XEntityManagerFactoryImpl;
import org.babyfish.persistence.XEntityManagerFactory;

/**
 * @author Tao Chen
 */
public class DbSchemaStrictScope implements AutoCloseable {

    private static final Field DB_SCHEMA_STRICT_FIELD;
    
    private XEntityManagerFactoryImpl entityManagerFactoryImpl;
    
    private boolean oldValue;
    
    public DbSchemaStrictScope(XEntityManagerFactory entityManagerFactory, boolean value) {
        this.entityManagerFactoryImpl = (XEntityManagerFactoryImpl)entityManagerFactory;
        this.oldValue = this.get();
        this.set(value);
    }
    
    @Override
    public void close() {
        this.set(oldValue);
    }
    
    private boolean get() {
        try {
            return (Boolean)DB_SCHEMA_STRICT_FIELD.get(this.entityManagerFactoryImpl);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private void set(boolean value) {
        try {
            DB_SCHEMA_STRICT_FIELD.set(this.entityManagerFactoryImpl, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
    
    static {
        Field field;
        try {
            field = XEntityManagerFactoryImpl.class.getDeclaredField("dbSchemaStrict");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(
                    XEntityManagerFactoryImpl.class.getName() + 
                    " does not has private field: dbSchemaStrict",
                    ex
            );
        }
        field.setAccessible(true);
        DB_SCHEMA_STRICT_FIELD = field;
    }

}
