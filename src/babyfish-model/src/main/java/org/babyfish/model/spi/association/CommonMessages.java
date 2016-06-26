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
package org.babyfish.model.spi.association;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.lang.I18N;
import org.babyfish.model.metadata.ModelProperty;

/**
 * @author Tao Chen
 */
class CommonMessages {
    
    @I18N
    public static native String createFailedBecausePropertyMustBe(
            Class<?> associatedEndpointType, 
            ModelProperty modelProperty, 
            Class<?> expectedType);

    @I18N
    public static native String createFailedBecausePropertyMustNotBe(
            Class<?> associatedEndpointType, 
            ModelProperty modelProperty, 
            Class<?> excludedType);
    
    @I18N
    public static native String createFailedBecauseOfunidirectionalAssociation(
            Class<?> associatedEndpointType,
            Class<?> requireBidirectionalAssociationType,
            ModelProperty modelProperty);
    
    @I18N
    public static native String canNotAttachElementToSpecialAssociation(
            ModelProperty property,
            Class<?> type,
            ModelProperty oppositeProperty,
            Class<?> oppositeType);

    @I18N
    public static native String baseReplacementRuleMustBe(ReplacementRule newReferenceWinConstant);
    
    @I18N
    public static native String baseBidiTypeMustNotBeNone(BidiType noneConstant);
    
    @I18N
    public static native String currentReferenceIsDisabled();
}
