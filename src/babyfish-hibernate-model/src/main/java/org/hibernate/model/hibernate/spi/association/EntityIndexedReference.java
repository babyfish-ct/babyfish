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
package org.hibernate.model.hibernate.spi.association;

import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.association.AssociatedIndexedReference;
import org.hibernate.proxy.HibernateProxy;

public class EntityIndexedReference<T> extends AssociatedIndexedReference<T> {
    
    private static final long serialVersionUID = -6887571814035728746L;

    public EntityIndexedReference(ObjectModel objectModel, int propertyId) {
        super(objectModel, propertyId);
    }

    @Override
    public boolean isLoaded() {
        if (this.value instanceof HibernateProxy &&
                ((HibernateProxy)this.value).getHibernateLazyInitializer().isUninitialized()) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isLoadable() {
        if (this.value instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)this.value;
            return proxy.getHibernateLazyInitializer().getSession().isConnected();
        }
        return true;
    }

    @Override
    public void load() {
        if (this.value instanceof HibernateProxy) {
            ((HibernateProxy)this.value).getHibernateLazyInitializer().initialize();
        }
    }
    
    int hibernateGetIndex() {
        return this.index;
    }
    
    void hibernateSetIndex(Object value) {
        
        this.index = value instanceof Integer ? (Integer)value : INVALID_INDEX;
    }
    
    T hibernateGet() {
        return this.index == INVALID_INDEX ? null : this.value;
    }
    
    @SuppressWarnings("unchecked")
    void hibernateSet(Object value) {
        this.value = (T)value;
    }
}
