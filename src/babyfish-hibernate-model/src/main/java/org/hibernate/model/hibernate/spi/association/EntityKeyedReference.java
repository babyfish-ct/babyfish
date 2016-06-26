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

import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.association.AssociatedKeyedReference;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;

public class EntityKeyedReference<K, T> extends AssociatedKeyedReference<K, T> {
    
    private static final long serialVersionUID = 5615409514519933877L;
    
    private boolean inverse;

    public EntityKeyedReference(ObjectModel objectModel, int propertyId) {
        super(objectModel, propertyId);
        this.inverse = ((JPAModelProperty)objectModel.getModelClass().getProperty(propertyId)).isInverse();
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
    
    @Override
    protected boolean isLoadedValue(T value) {
        return Hibernate.isInitialized(value);
    }

    @Override
    protected boolean isAbandonableValue(T value) {
        //This endpoint is not inverse means the opposite end point is inverse.
        if (!this.inverse && value instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)value;
            SessionImplementor session = proxy.getHibernateLazyInitializer().getSession();
            return session == null ||
                    !session.isOpen() ||
                    !session.isConnected();
        }
        return false;
    }

    @Override
    protected void loadValue(T value) {
        Hibernate.initialize(value);
    }
    
    K hibernateGetKey() {
        return this.key;
    }
    
    @SuppressWarnings("unchecked")
    void hibernateSetKey(Object key) {
        this.key = (K)key;
    }
    
    T hibernateGet() {
        return this.value; 
    }
    
    @SuppressWarnings("unchecked")
    void hibernateSet(Object value) {
        this.value = (T)value;
    }
}
