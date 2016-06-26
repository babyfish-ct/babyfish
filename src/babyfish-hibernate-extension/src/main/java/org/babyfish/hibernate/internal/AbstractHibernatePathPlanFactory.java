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
package org.babyfish.hibernate.internal;

import org.babyfish.lang.Arguments;
import org.babyfish.model.jpa.path.spi.AbstractPathPlanFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public abstract class AbstractHibernatePathPlanFactory extends AbstractPathPlanFactory {

    @Override
    protected EntityDelegate getEntityDelegate(String alias) {
        return new EntityDelegateImpl(this.getEntityPersister(alias));
    }
    
    protected abstract EntityPersister getEntityPersister(String alias);
    
    private static class EntityDelegateImpl implements EntityDelegate {
        
        private EntityPersister entityPersister;
        
        private boolean containsImplicitCollectionJoins;
        
        public EntityDelegateImpl(EntityPersister entityPersister) {
            this.entityPersister = Arguments.mustNotBeNull("entityPersister", entityPersister);
            this.containsImplicitCollectionJoins =
                    ImplicitCollectionJoins
                    .getInstance(this.entityPersister.getFactory())
                    .hasImplicitCollectionJoinProperties(this.entityPersister.getEntityName());
        }

        @Override
        public String getIdPropertyName() {
            return this.entityPersister.getIdentifierPropertyName();
        }

        @Override
        public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
            return new PropertyDelegateImpl(this.entityPersister, nonIdPropertyName);
        }

        @Override
        public boolean containsImplicitCollectionJoins() {
            return this.containsImplicitCollectionJoins;
        }
        
    }
    
    private static class PropertyDelegateImpl implements PropertyDelegate {
        
        private SessionFactoryImplementor factory;
        
        private Type propertType;
        
        public PropertyDelegateImpl(EntityPersister entityPersister, String propertyName) {
            this.factory = entityPersister.getFactory();
            this.propertType = entityPersister.getPropertyType(propertyName);
        }

        @Override
        public boolean isAssociation() {
            return this.propertType.isAssociationType();
        }

        @Override
        public boolean isCollection() {
            return this.propertType.isCollectionType();
        }

        @Override
        public EntityDelegate getAssociatedEntityDelegate() {
            String entityName = ((AssociationType)this.propertType).getAssociatedEntityName(this.factory);
            return new EntityDelegateImpl(this.factory.getEntityPersister(entityName));
        }
    }
}
