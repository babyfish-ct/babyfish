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
package org.babyfish.hibernate.cfg;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

import org.babyfish.hibernate.collection.type.AbstractMACollectionType;
import org.babyfish.hibernate.collection.type.MACollectionProperties;
import org.babyfish.hibernate.collection.type.MAListType;
import org.babyfish.hibernate.collection.type.MANavigableMapType;
import org.babyfish.hibernate.collection.type.MANavigableSetType;
import org.babyfish.hibernate.collection.type.MAOrderedMapType;
import org.babyfish.hibernate.collection.type.MAOrderedSetType;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.jpa.JPAModel;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.hibernate.MappingException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;
import org.hibernate.model.hibernate.spi.association.ObjectModel4HibernatePropertyAccessor;

public class PersistentClassProcessor {

    private static final String MODEL_EXPECTED_POM_SECTION =
            "<plugin>" +
            "  <groupId>org.babyfish</groupId>"+
            "  <artifactId>babyfish-instrument-maven-plugin</artifactId>" +
            "  <version>${babyfish.version}</version>" +
            "  <executions>" +
            "    <execution>" +
            "      <goals>" +
            "        <goal>instrument</goal>" +
            "        <goal>instrument-test</goal>" +
            "      </goals>" +
            "    </execution>" +
            "  </executions>" +
            "  <dependencies>" +
            "    <dependency>" +
            "      <groupId>org.babyfish</groupId>" +
            "      <artifactId>babyfish-hibernate-model-tool</artifactId>" +
            "      <version>${babyfish.version}</version>" +
            "    </dependency>" +
            "  </dependencies>" +
            "</plugin>";
    
    public static void process(PersistentClass persistentClass) {
        Class<?> mappedClass = persistentClass.getMappedClass();
        if (mappedClass == null || !mappedClass.isAnnotationPresent(JPAModel.class)) {
            return;
        }
        
        ModelClass modelClass;
        try {
            modelClass = ModelClass.of(mappedClass);
        } catch (RuntimeException | Error ex) {
            throw new IllegalProgramException(
                    missJPAModelInstumenter(
                            mappedClass, 
                            JPAModel.class, 
                            MODEL_EXPECTED_POM_SECTION
                    ),
                    ex
            );
        }
        
        String propertyAccessorName = ObjectModel4HibernatePropertyAccessor.class.getName();
        for (ModelProperty modelProperty : modelClass.getDeclaredProperties().values()) {
            if (modelProperty.getConvarianceProperty() != null) {
                continue;
            }
            Property hibernateProperty = persistentClass.getProperty(modelProperty.getName());
            hibernateProperty.setPropertyAccessorName(propertyAccessorName);
            Class<?> standardCollectionType = modelProperty.getStandardCollectionType();
            if (standardCollectionType == null) {
                continue;
            }
            if (NavigableMap.class.isAssignableFrom(standardCollectionType)) {
                replaceUserCollectionType(
                        hibernateProperty, 
                        org.hibernate.mapping.Map.class, 
                        MANavigableMapType.class);
            } else if (Map.class.isAssignableFrom(standardCollectionType)) {
                replaceUserCollectionType(
                        hibernateProperty, 
                        org.hibernate.mapping.Map.class, 
                        MAOrderedMapType.class);
            } else if (List.class.isAssignableFrom(standardCollectionType)) {
                replaceUserCollectionType(
                        hibernateProperty, 
                        org.hibernate.mapping.List.class, 
                        MAListType.class);
            } else if (NavigableSet.class.isAssignableFrom(standardCollectionType)) {
                replaceUserCollectionType(
                        hibernateProperty, 
                        org.hibernate.mapping.Set.class, 
                        MANavigableSetType.class);
            } else if (Set.class.isAssignableFrom(standardCollectionType)) {
                replaceUserCollectionType(
                        hibernateProperty, 
                        org.hibernate.mapping.Set.class, 
                        MAOrderedSetType.class);
            }  else {
                replaceUserCollectionType(
                        hibernateProperty, 
                        org.hibernate.mapping.Bag.class, 
                        MAOrderedSetType.class);
            }
            
            org.hibernate.mapping.Collection mappingColletion =
                    ((org.hibernate.mapping.Collection)hibernateProperty.getValue());
            mappingColletion.setTypeParameters(
                    new MACollectionProperties(
                                (JPAModelProperty)modelProperty, 
                                mappingColletion.getTypeParameters()
                    )
            );
        }
    }
    
    private static void replaceUserCollectionType(
            Property hibernateProperty,
            Class<? extends org.hibernate.mapping.Collection> hibernateCollectionType,
            Class<? extends AbstractMACollectionType> babyfishCollectionType) {
        /*
         * Don't invoke property.getType() or property.getValue().getType()
         * that will cause the creating of original collection-type before the replacement.
         * that is is slow
         */
        Value value = hibernateProperty.getValue();
        if (!(value instanceof org.hibernate.mapping.Collection)) {
            throw new MappingException(
                    '"' +
                    hibernateProperty.getPersistentClass().getMappedClass().getName() +
                    '.' +
                    hibernateProperty.getName() +
                    "\" must be mapped as collection.");
        }
        org.hibernate.mapping.Collection collection = (org.hibernate.mapping.Collection)value;
        String typeName = collection.getTypeName();
        if (typeName == null) {
            if (!hibernateCollectionType.isAssignableFrom(value.getClass())) {
                throw new MappingException(
                        '"' +
                        hibernateProperty.getPersistentClass().getEntityName() +
                        '.' +
                        hibernateProperty.getName() +
                        "\" must be mapped collection whose hibernate type is \"" +
                        hibernateCollectionType.getName() +
                        "\".");
            }
            collection.setTypeName(babyfishCollectionType.getName());
        } else {
            Class<?> userCollctionType;
            try {
                userCollctionType = ReflectHelper.classForName(typeName);
            } catch (ClassNotFoundException ex) {
                throw new MappingException(
                        '"' +
                        hibernateProperty.getPersistentClass().getEntityName() +
                        '.' +
                        hibernateProperty.getName() +
                        "\" must be mapped as collection whose attribute \"collection-type\" is \"" +
                        typeName +
                        "\", but the there is no java type names\"" +
                        typeName +
                        "\".",
                        ex
                );
            }
            if (!babyfishCollectionType.isAssignableFrom(userCollctionType)) {
                throw new MappingException(
                        '"' +
                        hibernateProperty.getPersistentClass().getEntityName() +
                        '.' +
                        hibernateProperty.getName() +
                        "\" must be mapped as collection whose attribut \"collection-type\" is \"" +
                        typeName +
                        "\", but the there class \"" +
                        typeName +
                        "\" is not \"" +
                        babyfishCollectionType.getName() +
                        "\" or its derived class.");
            }
        }
    }
    
    @I18N
    private static native String missJPAModelInstumenter(
            Class<?> mappedClass, 
            Class<JPAModel> jpaModelTypeConstant, 
            String modelExpectedPomSection
    );
    
    private PersistentClassProcessor() {}
}
