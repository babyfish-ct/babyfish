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
package org.babyfish.model.jpa.instrument.spi;

import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Contravariance;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.model.jpa.metadata.internal.JPAModelClassImpl;
import org.babyfish.model.jpa.metadata.internal.JPAModelPropertyImpl;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public class ASMConstants extends org.babyfish.model.instrument.spi.ASMConstants {

    public static final String TRANSIENT_DESCRIPTOR = "Ljavax/persistence/Transient;";
    
    public static final String ID_DESCRIPTOR = "Ljavax/persistence/Id;";
    
    public static final String VERSION_DESCRIPTOR = "Ljavax/persistence/Version;";
    
    public static final String BASIC_DESCRIPTOR = "Ljavax/persistence/Basic;";
    
    public static final String EMBEDED_DESCRIPTOR = "Ljavax/persistence/Embedded;";
    
    public static final String ONE_TO_ONE_DESCRIPTOR = "Ljavax/persistence/OneToOne;";
    
    public static final String MANY_TO_ONE_DESCRIPTOR = "Ljavax/persistence/ManyToOne;";
    
    public static final String ONE_TO_MANY_DESCRIPTOR = "Ljavax/persistence/OneToMany;";
    
    public static final String MANY_TO_MANY_DESCRIPTOR = "Ljavax/persistence/ManyToMany;";
    
    public static final String CONTRAVARIANCE_DESCRIPTOR = "Lorg/babyfish/model/Contravariance;";
    
    public static final String COMPARATOR_RULE_DESCRIPTOR = "Lorg/babyfish/model/ComparatorRule;";
    
    public static final String JPA_MODEL_CLASS_IMPL_DESCRIPTOR = Type.getDescriptor(JPAModelClassImpl.class);
    
    public static final String JPA_MODEL_PROPERTY_IMPL_DESCRIPTOR = Type.getDescriptor(JPAModelPropertyImpl.class);
    
    public static final String JPA_SCALAR_TYPE_DESCRIPTOR = Type.getDescriptor(JPAScalarType.class);
    
    @Deprecated
    protected ASMConstants() {
    }
    
    static {
        if (!CONTRAVARIANCE_DESCRIPTOR.equals(Type.getDescriptor(Contravariance.class))) {
            throw new AssertionError("Internal bug: Forgot to refactor CONTRAVARIANCE_DESCRIPTOR");
        }
        if (!COMPARATOR_RULE_DESCRIPTOR.equals(Type.getDescriptor(ComparatorRule.class))) {
            throw new AssertionError("Internal bug: Forgot to refactor COMPARATOR_RULE_DESCRIPTOR");
        }
    }
}
