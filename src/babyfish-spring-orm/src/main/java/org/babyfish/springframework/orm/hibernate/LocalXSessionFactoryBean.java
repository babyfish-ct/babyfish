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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.babyfish.hibernate.XSessionFactory;
import org.hibernate.Interceptor;
import org.hibernate.cfg.NamingStrategy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;

/**
 * @author Tao Chen
 */
public class LocalXSessionFactoryBean extends HibernateExceptionTranslator
        implements FactoryBean<XSessionFactory>, ResourceLoaderAware, InitializingBean, DisposableBean {

    private DataSource dataSource;

    private Resource[] configLocations;

    private String[] mappingResources;

    private Resource[] mappingLocations;

    private Resource[] cacheableMappingLocations;

    private Resource[] mappingJarLocations;

    private Resource[] mappingDirectoryLocations;

    private Interceptor entityInterceptor;

    private NamingStrategy namingStrategy;

    private Properties hibernateProperties;

    private Class<?>[] annotatedClasses;

    private String[] annotatedPackages;

    private String[] packagesToScan;

    private Object jtaTransactionManager;

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private org.babyfish.hibernate.cfg.Configuration configuration;

    private XSessionFactory sessionFactory;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocations = new Resource[] {configLocation};
    }

    public void setConfigLocations(Resource[] configLocations) {
        this.configLocations = configLocations;
    }

    public void setMappingResources(String[] mappingResources) {
        this.mappingResources = mappingResources;
    }

    public void setMappingLocations(Resource[] mappingLocations) {
        this.mappingLocations = mappingLocations;
    }

    public void setCacheableMappingLocations(Resource[] cacheableMappingLocations) {
        this.cacheableMappingLocations = cacheableMappingLocations;
    }

    public void setMappingJarLocations(Resource[] mappingJarLocations) {
        this.mappingJarLocations = mappingJarLocations;
    }

    public void setMappingDirectoryLocations(Resource[] mappingDirectoryLocations) {
        this.mappingDirectoryLocations = mappingDirectoryLocations;
    }

    public void setEntityInterceptor(Interceptor entityInterceptor) {
        this.entityInterceptor = entityInterceptor;
    }

    public void setNamingStrategy(NamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public void setHibernateProperties(Properties hibernateProperties) {
        this.hibernateProperties = hibernateProperties;
    }

    public Properties getHibernateProperties() {
        if (this.hibernateProperties == null) {
            this.hibernateProperties = new Properties();
        }
        return this.hibernateProperties;
    }

    public void setAnnotatedClasses(Class<?>[] annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

    public void setAnnotatedPackages(String[] annotatedPackages) {
        this.annotatedPackages = annotatedPackages;
    }

    public void setPackagesToScan(String... packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public void setJtaTransactionManager(Object jtaTransactionManager) {
        this.jtaTransactionManager = jtaTransactionManager;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }
    
    public void afterPropertiesSet() throws IOException {
        LocalXSessionFactoryBuilder sfb = new LocalXSessionFactoryBuilder(this.dataSource, this.resourcePatternResolver);

        if (this.configLocations != null) {
            for (Resource resource : this.configLocations) {
                // Load Hibernate configuration from given location.
                sfb.configure(resource.getURL());
            }
        }

        if (this.mappingResources != null) {
            // Register given Hibernate mapping definitions, contained in resource files.
            for (String mapping : this.mappingResources) {
                Resource mr = new ClassPathResource(mapping.trim(), this.resourcePatternResolver.getClassLoader());
                sfb.addInputStream(mr.getInputStream());
            }
        }

        if (this.mappingLocations != null) {
            // Register given Hibernate mapping definitions, contained in resource files.
            for (Resource resource : this.mappingLocations) {
                sfb.addInputStream(resource.getInputStream());
            }
        }

        if (this.cacheableMappingLocations != null) {
            // Register given cacheable Hibernate mapping definitions, read from the file system.
            for (Resource resource : this.cacheableMappingLocations) {
                sfb.addCacheableFile(resource.getFile());
            }
        }

        if (this.mappingJarLocations != null) {
            // Register given Hibernate mapping definitions, contained in jar files.
            for (Resource resource : this.mappingJarLocations) {
                sfb.addJar(resource.getFile());
            }
        }

        if (this.mappingDirectoryLocations != null) {
            // Register all Hibernate mapping definitions in the given directories.
            for (Resource resource : this.mappingDirectoryLocations) {
                File file = resource.getFile();
                if (!file.isDirectory()) {
                    throw new IllegalArgumentException(
                            "Mapping directory location [" + resource + "] does not denote a directory");
                }
                sfb.addDirectory(file);
            }
        }

        if (this.entityInterceptor != null) {
            sfb.setInterceptor(this.entityInterceptor);
        }

        if (this.namingStrategy != null) {
            sfb.setNamingStrategy(this.namingStrategy);
        }

        if (this.hibernateProperties != null) {
            sfb.addProperties(this.hibernateProperties);
        }

        if (this.annotatedClasses != null) {
            sfb.addAnnotatedClasses(this.annotatedClasses);
        }

        if (this.annotatedPackages != null) {
            sfb.addPackages(this.annotatedPackages);
        }

        if (this.packagesToScan != null) {
            sfb.scanPackages(this.packagesToScan);
        }

        if (this.jtaTransactionManager != null) {
            sfb.setJtaTransactionManager(this.jtaTransactionManager);
        }

        // Build SessionFactory instance.
        this.configuration = sfb;
        this.sessionFactory = buildSessionFactory(sfb);
    }

    protected XSessionFactory buildSessionFactory(LocalXSessionFactoryBuilder sfb) {
        return sfb.buildSessionFactory();
    }

    public final org.babyfish.hibernate.cfg.Configuration getConfiguration() {
        if (this.configuration == null) {
            throw new IllegalStateException("Configuration not initialized yet");
        }
        return this.configuration;
    }

    public XSessionFactory getObject() {
        return this.sessionFactory;
    }

    public Class<?> getObjectType() {
        return (this.sessionFactory != null ? this.sessionFactory.getClass() : XSessionFactory.class);
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() {
        this.sessionFactory.close();
    }
}

