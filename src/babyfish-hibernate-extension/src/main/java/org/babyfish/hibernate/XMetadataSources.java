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
package org.babyfish.hibernate;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.babyfish.hibernate.internal.XMetadataBuilderImpl;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.service.ServiceRegistry;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;

/**
 * @author Tao Chen
 */
public class XMetadataSources extends MetadataSources {
    
    private XMetadataBuilder metadataBuilder;

    public XMetadataSources(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
        this.metadataBuilder = this.createMetdataBuilder();
    }
    
    public XMetadataSources(ServiceRegistry serviceRegistry,
            EntityResolver entityResolver, NamingStrategy namingStrategy) {
        super(serviceRegistry, entityResolver, namingStrategy);
        this.metadataBuilder = this.createMetdataBuilder();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public XMetadataSources addAnnotatedClass(Class annotatedClass) {
        return (XMetadataSources)super.addAnnotatedClass(annotatedClass);
    }

    @Override
    public XMetadataSources addPackage(String packageName) {
        return (XMetadataSources)super.addPackage(packageName);
    }

    @Override
    public XMetadataSources addResource(String name) {
        return (XMetadataSources)super.addResource(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public XMetadataSources addClass(Class entityClass) {
        return (XMetadataSources)super.addClass(entityClass);
    }

    @Override
    public XMetadataSources addFile(String path) {
        return (XMetadataSources)super.addFile(path);
    }

    @Override
    public XMetadataSources addFile(File file) {
        return (XMetadataSources)super.addFile(file);
    }

    @Override
    public XMetadataSources addCacheableFile(String path) {
        return (XMetadataSources)super.addCacheableFile(path);
    }

    @Override
    public XMetadataSources addCacheableFile(File file) {
        return (XMetadataSources)super.addCacheableFile(file);
    }

    @Override
    public XMetadataSources addInputStream(InputStream xmlInputStream) {
        return (XMetadataSources)super.addInputStream(xmlInputStream);
    }

    @Override
    public XMetadataSources addURL(URL url) {
        return (XMetadataSources)super.addURL(url);
    }

    @Override
    public XMetadataSources addDocument(Document document) {
        return (XMetadataSources)super.addDocument(document);
    }

    @Override
    public XMetadataSources addJar(File jar) {
        return (XMetadataSources)super.addJar(jar);
    }

    @Override
    public XMetadataSources addDirectory(File dir) {
        return (XMetadataSources)super.addDirectory(dir);
    }

    protected XMetadataBuilder createMetdataBuilder() {
        return new XMetadataBuilderImpl(this);
    }

    @Override
    public XMetadataBuilder getMetadataBuilder() {
        return this.metadataBuilder;
    }

    @Override
    public XMetadata buildMetadata() {
        return this.getMetadataBuilder().build();
    }
    
}
