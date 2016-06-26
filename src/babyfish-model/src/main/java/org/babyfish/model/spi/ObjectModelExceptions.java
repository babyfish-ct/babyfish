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

import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public final class ObjectModelExceptions {

    public static RuntimeException setDuringExecuting(Class<?> modelClass) {
        return new IllegalStateException(setDuringExecutingMessage(modelClass));
    }

    public static RuntimeException getNonExisting(Class<?> modelClass, int propertyId) {
        return new IllegalArgumentException(getNonExistingMessage(modelClass, propertyId));
    }
    
    public static RuntimeException setNonExisting(Class<?> modelClass, int propertyId) {
        return new IllegalArgumentException(setNonExistingMessage(modelClass, propertyId));
    }
    
    public static RuntimeException getNonAssociation(Class<?> modelClass, int propertyId, String propertyName) {
        return new IllegalArgumentException(getNonAssociationMessage(modelClass, propertyId, propertyName));
    }
    
    public static RuntimeException getNonExistingAsAssociation(Class<?> modelClass, int propertyId) {
        return new IllegalArgumentException(getNonExistingAsAssociationMessage(modelClass, propertyId));
    }
    
    public static RuntimeException getDisabled(Class<?> modelClass, int propertyId, String propertyName) {
        return new IllegalArgumentException(getDisabledMessage(modelClass, propertyId, propertyName));
    }
    
    public static RuntimeException checkDisablityForNonExisting(Class<?> modeClass, int propertyId) {
        return new IllegalArgumentException(checkDisablityForNonExistingMessage(modeClass, propertyId));
    }

    public static RuntimeException enableNonExisting(Class<?> modeClass, int propertyId) {
        return new IllegalArgumentException(enableNonExistingMessage(modeClass, propertyId));
    }

    public static RuntimeException disableNonExisting(Class<?> modeClass, int propertyId) {
        return new IllegalArgumentException(disableNonExistingMessage(modeClass, propertyId));
    }

    public static RuntimeException checkLazinessForNonExisting(Class<?> modeClass, int propertyId) {
        return new IllegalArgumentException(checkLazinessForNonExistingMessage(modeClass, propertyId));
    }
    
    public static RuntimeException unloadFrozen(Class<?> modelProperty, int propertyId, String propertyName) {
        return new IllegalArgumentException(unloadFrozenMessage(modelProperty, propertyId, propertyName));
    }
    
    public static RuntimeException unloadNonExisting(Class<?> modeClass, int propertyId) {
        return new IllegalArgumentException(unloadNonExistingMessage(modeClass, propertyId));
    }

    public static RuntimeException freezeArray(Class<?> modelClass, int propertyId, String propertyName) {
        return new IllegalArgumentException(freezeArrayMessage(modelClass, propertyId, propertyName));
    }
    
    public static RuntimeException unfreezeArray(Class<?> modelClass, int propertyId, String propertyName) {
        return new IllegalArgumentException(unfreezeArrayMessage(modelClass, propertyId, propertyName));
    }
    
    public static RuntimeException freezeNonScalar(Class<?> modelClass, int propertyId, String propertyName) {
        return new IllegalArgumentException(freezeNonScalarMessage(modelClass, propertyId, propertyName));
    }
    
    public static RuntimeException unfreezeNonScalar(Class<?> modelClass, int propertyId, String propertyName) {
        return new IllegalArgumentException(unfreezeNonScalarMessage(modelClass, propertyId, propertyName));
    }
    
    public static RuntimeException freezeNonExisting(Class<?> modelClass, int propertyId) {
        return new IllegalArgumentException(freezeNonExistingMessage(modelClass, propertyId));
    }
    
    public static RuntimeException unfreezeNonExisting(Class<?> modelClass, int propertyId) {
        return new IllegalArgumentException(unfreezeNonExistingMessage(modelClass, propertyId));
    }
    
    @I18N
    private static native String setDuringExecutingMessage(Class<?> modelClass);

    @I18N
    private static native String getNonExistingMessage(Class<?> modelClass, int propertyId);

    @I18N
    private static native String setNonExistingMessage(Class<?> modelClass, int propertyId);

    @I18N 
    private static native String getNonAssociationMessage(Class<?> modelClass, int propertyId, String propertyName);

    @I18N 
    private static native String getNonExistingAsAssociationMessage(Class<?> modelClass, int propertyId);
        
    @I18N
    private static native String getDisabledMessage(Class<?> modelClass, int propertyId, String propertyName);

    @I18N 
    private static native String checkDisablityForNonExistingMessage(Class<?> modeClass, int propertyId);

    @I18N
    private static native String enableNonExistingMessage(Class<?> modeClass, int propertyId);

    @I18N
    private static native String disableNonExistingMessage(Class<?> modeClass, int propertyId);

    @I18N
    private static native String checkLazinessForNonExistingMessage(Class<?> modeClass, int propertyId);
        
    @I18N
    private static native String unloadFrozenMessage(Class<?> modelProperty, int propertyId, String propertyName);
        
    @I18N
    private static native String unloadNonExistingMessage(Class<?> modeClass, int propertyId);

    @I18N
    private static native String freezeArrayMessage(Class<?> modelClass, int propertyId, String propertyName);
        
    @I18N
    private static native String unfreezeArrayMessage(Class<?> modelClass, int propertyId, String propertyName);
        
    @I18N
    private static native String freezeNonScalarMessage(Class<?> modelClass, int propertyId, String propertyName);
        
    @I18N
    private static native String unfreezeNonScalarMessage(Class<?> modelClass, int propertyId, String propertyName);
        
    @I18N
    private static native String freezeNonExistingMessage(Class<?> modelClass, int propertyId);
        
    @I18N
    private static native String unfreezeNonExistingMessage(Class<?> modelClass, int propertyId);
}
