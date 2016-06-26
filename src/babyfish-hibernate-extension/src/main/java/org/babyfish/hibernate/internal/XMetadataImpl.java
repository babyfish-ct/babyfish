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

import org.babyfish.hibernate.XMetadata;
import org.babyfish.hibernate.XMetadataSources;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.XSessionFactoryBuilder;
import org.hibernate.metamodel.source.internal.MetadataImpl;

/**
 * @author Tao Chen
 */
public class XMetadataImpl extends MetadataImpl implements XMetadata {

    private static final long serialVersionUID = 2022753996073961324L;
    
    private XSessionFactoryBuilder sessionFactoryBuilder;

    public XMetadataImpl(XMetadataSources metadataSources, Options options) {
        super(metadataSources, options);
    }

    @Override
    public XSessionFactory buildSessionFactory() {
        return this.getSessionFactoryBuilder().build();
    }

    @Override
    public XSessionFactoryBuilder getSessionFactoryBuilder() {
        XSessionFactoryBuilder sessionFactoryBuilder = this.sessionFactoryBuilder;
        if (sessionFactoryBuilder == null) {
            this.sessionFactoryBuilder = 
                    sessionFactoryBuilder = 
                    new XSessionFactoryBuilderImpl(this);
        }
        return sessionFactoryBuilder;
    }

}
