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
package org.babyfish.springframework.orm.hibernate;

import java.io.IOException;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.babyfish.hibernate.XSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.transaction.internal.jta.CMTTransactionFactory;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.hibernate.engine.transaction.jta.platform.internal.WebSphereExtendedJtaPlatform;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.orm.hibernate4.SpringSessionContext;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.jta.UserTransactionAdapter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Tao Chen
 */
public class LocalXSessionFactoryBuilder extends org.babyfish.hibernate.cfg.Configuration {

    private static final long serialVersionUID = 1853981674507731778L;

    private static final String RESOURCE_PATTERN = "/**/*.class";

    private static final String PACKAGE_INFO_SUFFIX = ".package-info";

    private static final TypeFilter[] ENTITY_TYPE_FILTERS = new TypeFilter[] {
            new AnnotationTypeFilter(Entity.class, false),
            new AnnotationTypeFilter(Embeddable.class, false),
            new AnnotationTypeFilter(MappedSuperclass.class, false)};

    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     * @param dataSource the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     * (may be {@code null})
     */
    public LocalXSessionFactoryBuilder(DataSource dataSource) {
        this(dataSource, new PathMatchingResourcePatternResolver());
    }

    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     * @param dataSource the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     * (may be {@code null})
     * @param classLoader the ClassLoader to load application classes from
     */
    public LocalXSessionFactoryBuilder(DataSource dataSource, ClassLoader classLoader) {
        this(dataSource, new PathMatchingResourcePatternResolver(classLoader));
    }

    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     * @param dataSource the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     * (may be {@code null})
     * @param resourceLoader the ResourceLoader to load application classes from
     */
    @SuppressWarnings("deprecation")
    public LocalXSessionFactoryBuilder(DataSource dataSource, ResourceLoader resourceLoader) {
        getProperties().put(Environment.CURRENT_SESSION_CONTEXT_CLASS, SpringSessionContext.class.getName());
        if (dataSource != null) {
            getProperties().put(Environment.DATASOURCE, dataSource);
        }
        getProperties().put(AvailableSettings.APP_CLASSLOADER, resourceLoader.getClassLoader());
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }


    /**
     * Set the Spring {@link JtaTransactionManager} or the JTA {@link TransactionManager}
     * to be used with Hibernate, if any. Allows for using a Spring-managed transaction
     * manager for Hibernate 4's session and cache synchronization, with the
     * "hibernate.transaction.jta.platform" automatically set to it. Also sets
     * "hibernate.transaction.factory_class" to {@link CMTTransactionFactory},
     * instructing Hibernate to interact with externally managed transactions.
     * <p>A passed-in Spring {@link JtaTransactionManager} needs to contain a JTA
     * {@link TransactionManager} reference to be usable here, except for the WebSphere
     * case where we'll automatically set {@link WebSphereExtendedJtaPlatform} accordingly.
     * <p>Note: If this is set, the Hibernate settings should not contain a JTA platform
     * setting to avoid meaningless double configuration.
     */
    public LocalXSessionFactoryBuilder setJtaTransactionManager(Object jtaTransactionManager) {
        Assert.notNull(jtaTransactionManager, "Transaction manager reference must not be null");
        if (jtaTransactionManager instanceof JtaTransactionManager) {
            boolean webspherePresent = ClassUtils.isPresent("com.ibm.wsspi.uow.UOWManager", getClass().getClassLoader());
            if (webspherePresent) {
                getProperties().put(AvailableSettings.JTA_PLATFORM, new WebSphereExtendedJtaPlatform());
            }
            else {
                JtaTransactionManager jtaTm = (JtaTransactionManager) jtaTransactionManager;
                if (jtaTm.getTransactionManager() == null) {
                    throw new IllegalArgumentException(
                            "Can only apply JtaTransactionManager which has a TransactionManager reference set");
                }
                getProperties().put(AvailableSettings.JTA_PLATFORM,
                        new ConfigurableJtaPlatform(jtaTm.getTransactionManager(), jtaTm.getUserTransaction()));
            }
        }
        else if (jtaTransactionManager instanceof TransactionManager) {
            getProperties().put(AvailableSettings.JTA_PLATFORM,
                    new ConfigurableJtaPlatform((TransactionManager) jtaTransactionManager, null));
        }
        else {
            throw new IllegalArgumentException(
                    "Unknown transaction manager type: " + jtaTransactionManager.getClass().getName());
        }
        getProperties().put(AvailableSettings.TRANSACTION_STRATEGY, new CMTTransactionFactory());
        return this;
    }

    /**
     * Add the given annotated classes in a batch.
     * @see #addAnnotatedClass
     * @see #scanPackages
     */
    public LocalXSessionFactoryBuilder addAnnotatedClasses(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            addAnnotatedClass(annotatedClass);
        }
        return this;
    }

    /**
     * Add the given annotated packages in a batch.
     * @see #addPackage
     * @see #scanPackages
     */
    public LocalXSessionFactoryBuilder addPackages(String... annotatedPackages) {
        for (String annotatedPackage :annotatedPackages) {
            addPackage(annotatedPackage);
        }
        return this;
    }

    /**
     * Perform Spring-based scanning for entity classes, registering them
     * as annotated classes with this {@code Configuration}.
     * @param packagesToScan one or more Java package names
     * @throws HibernateException if scanning fails for any reason
     */
    public LocalXSessionFactoryBuilder scanPackages(String... packagesToScan) throws HibernateException {
        try {
            for (String pkg : packagesToScan) {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
                Resource[] resources = this.resourcePatternResolver.getResources(pattern);
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader reader = readerFactory.getMetadataReader(resource);
                        String className = reader.getClassMetadata().getClassName();
                        if (matchesEntityTypeFilter(reader, readerFactory)) {
                            addAnnotatedClass(this.resourcePatternResolver.getClassLoader().loadClass(className));
                        }
                        else if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
                            addPackage(className.substring(0, className.length() - PACKAGE_INFO_SUFFIX.length()));
                        }
                    }
                }
            }
            return this;
        }
        catch (IOException ex) {
            throw new MappingException("Failed to scan classpath for unlisted classes", ex);
        }
        catch (ClassNotFoundException ex) {
            throw new MappingException("Failed to load annotated classes from classpath", ex);
        }
    }

    /**
     * Check whether any of the configured entity type filters matches
     * the current class descriptor contained in the metadata reader.
     */
    private boolean matchesEntityTypeFilter(MetadataReader reader, MetadataReaderFactory readerFactory) throws IOException {
        for (TypeFilter filter : ENTITY_TYPE_FILTERS) {
            if (filter.match(reader, readerFactory)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Build the {@code SessionFactory}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public XSessionFactory buildSessionFactory() throws HibernateException {
        ClassLoader appClassLoader = (ClassLoader) getProperties().get(AvailableSettings.APP_CLASSLOADER);
        Thread currentThread = Thread.currentThread();
        ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
        boolean overrideClassLoader =
                (appClassLoader != null && !appClassLoader.equals(threadContextClassLoader));
        if (overrideClassLoader) {
            currentThread.setContextClassLoader(appClassLoader);
        }
        try {
            return super.buildSessionFactory();
        }
        finally {
            if (overrideClassLoader) {
                currentThread.setContextClassLoader(threadContextClassLoader);
            }
        }
    }

    private static class ConfigurableJtaPlatform extends AbstractJtaPlatform {

        private static final long serialVersionUID = 3507524228072935224L;

        private final TransactionManager transactionManager;

        private final UserTransaction userTransaction;


        /**
         * Create a new ConfigurableJtaPlatform instance with the given
         * JTA TransactionManager and optionally a given UserTransaction.
         * @param tm the JTA TransactionManager reference (required)
         * @param ut the JTA UserTransaction reference (optional)
         */
        public ConfigurableJtaPlatform(TransactionManager tm, UserTransaction ut) {
            Assert.notNull(tm, "TransactionManager reference must not be null");
            this.transactionManager = tm;
            this.userTransaction = (ut != null ? ut : new UserTransactionAdapter(tm));
        }


        @Override
        protected TransactionManager locateTransactionManager() {
            return this.transactionManager;
        }

        @Override
        protected UserTransaction locateUserTransaction() {
            return this.userTransaction;
        }

        @Override
        protected boolean canCacheTransactionManager() {
            return true;
        }

        @Override
        protected boolean canCacheUserTransaction() {
            return true;
        }

    }
}
