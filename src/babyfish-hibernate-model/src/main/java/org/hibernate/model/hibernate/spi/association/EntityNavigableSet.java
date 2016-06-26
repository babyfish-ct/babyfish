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

import java.util.Set;

import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.association.AssociatedNavigableSet;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;

public class EntityNavigableSet<E> extends AssociatedNavigableSet<E> implements ModelPersistentCollection {

    private static final long serialVersionUID = -1746674470736894533L;
    
    private boolean inverse;
    
    public EntityNavigableSet(ObjectModel objectModel, int propertyId) {
        super(objectModel, propertyId);
        this.inverse = ((JPAModelProperty)objectModel.getModelClass().getProperty(propertyId)).isInverse();
    }

    @Override
    protected boolean isLoadedElement(E element) {
        return Hibernate.isInitialized(element);
    }

    @Override
    protected boolean isAbandonableElement(E element) {
        //This endpoint is not inverse means the opposite end point is inverse.
        if (!this.inverse && element instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)element;
            SessionImplementor session = proxy.getHibernateLazyInitializer().getSession();
            return session == null ||
                    !session.isOpen() ||
                    !session.isConnected();
        }
        return false;
    }

    @Override
    protected void loadElement(E element) {
        Hibernate.initialize(element);
    }
    
    Set<E> hibenrateGet() {
        return this.getBase();
    }

    void hibernateSet(Object value) {
        this.replace(value);
    }
    
    @Override
    public final boolean wasInitialized() {
        return this.isLoaded();
    }

    @Override
    public final void forceInitialization() throws HibernateException {
        this.load();
    }
}
