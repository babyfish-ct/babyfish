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
package org.babyfish.collection.spi.wrapper;

import org.babyfish.data.ModificationAware;
import org.babyfish.data.View;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
class CommonMessages {
    
    @I18N
    public static native String illegalViewInfo();
    
    @I18N
    public static native String createRootDataMustReturnNonNull(Class<?> runtimeType);
    
    @I18N
    public static native String createBaseViewMustReturnView(Class<?> runtimeType, Class<View> viewType);
    
    @I18N
    public static native String createEventDispatcherMustReturnNonNull(Class<?> runtimeType);
    
    @I18N
    public static native String createDefaultBaseMustReturnNonNull(Class<?> runtimeType);
    
    @I18N
    public static native String ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(Class<?> runtimeType);
    
    @I18N
    public static native String concurrentModification();
    
    @I18N
    public static native String invokeGetRootOwnerTooEarlySoThatTheRootOwnerIsNull();

    @I18N
    public static native String canNotSetNullBaseWhenTheRootDataIsContructorOnlyRootData(Class<ConstructOnlyRootData> constructorOnlyRootDataType);

    @I18N
    public static native String canNotSetBaseTwiceWhenTheRootDataIsSetOnceOnlyRootData(Class<SetOnceOnlyRootData> setOnceOnlyRootDataType);

    @I18N
    public static native String canNotSetBaseDuringSerializing();
    
    @I18N
    public static native String mustSetTheBaseInContstructorWhenTheRootDataImplementsRootData(Class<ConstructOnlyRootData> rootDataType);
    
    @I18N
    public static native String mustOverrideCreateEventDispatcher(Class<?> runtimeType, Class<?> illegalOperationType);
    
    @I18N
    public static native String currentCollectionIsDisabled(Class<?> thisType);
    
    @I18N
    public static native String whenThisIsModificationAware(Class<ModificationAware> modificationAwareType);
    
    @I18N
    public static native String whenDefaultUnifiedComparatorIsNotNull();
    
    @I18N
    public static native String whenDefaultKeyUnifiedComparatorIsNotNull();
    
    @I18N
    public static native String whenDefaultValueUnifiedComparatorIsNotNull();
}
