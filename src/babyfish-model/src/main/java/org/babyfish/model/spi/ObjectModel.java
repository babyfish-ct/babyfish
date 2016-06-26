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
package org.babyfish.model.spi;

import org.babyfish.collection.FrozenContext;
import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.event.ScalarModificationAware;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.association.AssociatedEndpoint;

/**
 * @author Tao Chen
 */
public interface ObjectModel extends ScalarModificationAware {
    
    ModelClass getModelClass();
    
    Object getOwner();

    Object get(int propertyId);
    
    void set(int propertyId, Object value);
    
    AssociatedEndpoint getAssociatedEndpoint(int propertyId);
    
    boolean isLoaded(int propertyId);
    
    default boolean isUnloaded(int propertyId) {
        return !this.isLoaded(propertyId);
    }
    
    void load(int propertyId);
    
    void load(int ... propertyIds);
    
    default void loadScalars() {}
    
    void batchLoad(Iterable<ObjectModel> objectModels, int[] propertyIds);
    
    void unload(int propertyId);
    
    default boolean isEnabed(int propertyId) {
        return !this.isDisabled(propertyId);
    }
    
    boolean isDisabled(int propertyId);
    
    void enable(int propertyId);
    
    void disable(int propertyId);
    
    ScalarLoader getScalarLoader();
    
    void setScalarLoader(ScalarLoader scalarLoader);
    
    int hashCodeScalar(int scalarPropertyId, StringComparatorType stringComparatorType);
    
    boolean equalsScalar(int scalarPropertyId, StringComparatorType stringComparatorType, ObjectModel other);
    
    int compareScalar(
            int scalarPropertyId, 
            StringComparatorType stringComparatorType, 
            NullComparatorType nullComparatorType, 
            ObjectModel other);
    
    void freezeScalar(int scalarPropertyId, FrozenContext<?> ctx);
    
    void unfreezeScalar(int scalarPropertyId, FrozenContext<?> ctx);
}
