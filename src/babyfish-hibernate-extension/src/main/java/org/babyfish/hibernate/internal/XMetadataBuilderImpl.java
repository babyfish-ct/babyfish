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

import javax.persistence.SharedCacheMode;

import org.babyfish.hibernate.XMetadata;
import org.babyfish.hibernate.XMetadataBuilder;
import org.babyfish.hibernate.XMetadataSources;
import org.hibernate.HibernateException;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.EJB3NamingStrategy;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.metamodel.Metadata;
import org.hibernate.metamodel.MetadataSourceProcessingOrder;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
public class XMetadataBuilderImpl implements XMetadataBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(XMetadataBuilderImpl.class);

    private final XMetadataSources sources;
    
    private final OptionsImpl options;
    
    public XMetadataBuilderImpl(XMetadataSources sources) {
        this(sources, getStandardServiceRegistry(sources.getServiceRegistry()));
    }

    public XMetadataBuilderImpl(XMetadataSources sources, StandardServiceRegistry serviceRegistry) {
        this.sources = sources;
        this.options = new OptionsImpl(serviceRegistry);
    }

    @Override
    public XMetadataBuilder with(NamingStrategy namingStrategy) {
        this.options.namingStrategy = namingStrategy;
        return this;
    }

    @Override
    public XMetadataBuilder with(MetadataSourceProcessingOrder metadataSourceProcessingOrder) {
        this.options.metadataSourceProcessingOrder = metadataSourceProcessingOrder;
        return this;
    }

    @Override
    public XMetadataBuilder with(SharedCacheMode sharedCacheMode) {
        this.options.sharedCacheMode = sharedCacheMode;
        return this;
    }

    @Override
    public XMetadataBuilder with(AccessType accessType) {
        this.options.defaultCacheAccessType = accessType;
        return this;
    }

    @Override
    public XMetadataBuilder withNewIdentifierGeneratorsEnabled(boolean enabled) {
        this.options.useNewIdentifierGenerators = enabled;
        return this;
    }

    @Override
    public XMetadata build() {
        return new XMetadataImpl(this.sources, this.options);
    }
    
    private static StandardServiceRegistry getStandardServiceRegistry(ServiceRegistry serviceRegistry) {
        if ( serviceRegistry == null ) {
            throw new HibernateException( "ServiceRegistry passed to MetadataBuilder cannot be null" );
        }

        if ( StandardServiceRegistry.class.isInstance( serviceRegistry ) ) {
            return ( StandardServiceRegistry ) serviceRegistry;
        }
        else if ( BootstrapServiceRegistry.class.isInstance( serviceRegistry ) ) {
            LOGGER.debug(
                    "ServiceRegistry passed to MetadataBuilder was a BootstrapServiceRegistry; this likely wont end well" +
                            "if attempt is made to build SessionFactory"
            );
            return new StandardServiceRegistryBuilder( (BootstrapServiceRegistry) serviceRegistry ).build();
        }
        else {
            throw new HibernateException(
                    String.format(
                            "Unexpected type of ServiceRegistry [%s] encountered in attempt to build MetadataBuilder",
                            serviceRegistry.getClass().getName()
                    )
            );
        }
    }

    private static class OptionsImpl implements Metadata.Options {
        
        private MetadataSourceProcessingOrder metadataSourceProcessingOrder = 
                MetadataSourceProcessingOrder.HBM_FIRST;
        
        private NamingStrategy namingStrategy = EJB3NamingStrategy.INSTANCE;
        
        private SharedCacheMode sharedCacheMode = SharedCacheMode.ENABLE_SELECTIVE;
        
        private StandardServiceRegistry serviceRegistry;
        
        private AccessType defaultCacheAccessType;
        
        private boolean useNewIdentifierGenerators;
        
        private boolean globallyQuotedIdentifiers;
        
        private String defaultSchemaName;
        
        private String defaultCatalogName;

        public OptionsImpl(StandardServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
            ConfigurationService configService = serviceRegistry.getService(ConfigurationService.class);

            // cache access type
            this.defaultCacheAccessType = configService.getSetting(
                    AvailableSettings.DEFAULT_CACHE_CONCURRENCY_STRATEGY,
                    new ConfigurationService.Converter<AccessType>() {
                        @Override
                        public AccessType convert(Object value) {
                            return AccessType.fromExternalName(value.toString());
                        }
                    }
            );

            this.useNewIdentifierGenerators = configService.getSetting(
                    AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS,
                    new ConfigurationService.Converter<Boolean>() {
                        @Override
                        public Boolean convert(Object value) {
                            return Boolean.parseBoolean(value.toString());
                        }
                    },
                    false
            );

            this.defaultSchemaName = configService.getSetting(
                    AvailableSettings.DEFAULT_SCHEMA,
                    new ConfigurationService.Converter<String>() {
                        @Override
                        public String convert(Object value) {
                            return value.toString();
                        }
                    },
                    null
            );

            this.defaultCatalogName = configService.getSetting(
                    AvailableSettings.DEFAULT_CATALOG,
                    new ConfigurationService.Converter<String>() {
                        @Override
                        public String convert(Object value) {
                            return value.toString();
                        }
                    },
                    null
            );

            this.globallyQuotedIdentifiers = configService.getSetting(
                    AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS,
                    new ConfigurationService.Converter<Boolean>() {
                        @Override
                        public Boolean convert(Object value) {
                            return Boolean.parseBoolean(value.toString());
                        }
                    },
                    false
           );
        }


        @Override
        public MetadataSourceProcessingOrder getMetadataSourceProcessingOrder() {
            return this.metadataSourceProcessingOrder;
        }

        @Override
        public NamingStrategy getNamingStrategy() {
            return this.namingStrategy;
        }

        @Override
        public AccessType getDefaultAccessType() {
            return this.defaultCacheAccessType;
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return this.sharedCacheMode;
        }

        @Override
        public boolean useNewIdentifierGenerators() {
            return this.useNewIdentifierGenerators;
        }

        @Override
        public boolean isGloballyQuotedIdentifiers() {
            return this.globallyQuotedIdentifiers;
        }

        @Override
        public String getDefaultSchemaName() {
            return this.defaultSchemaName;
        }

        @Override
        public String getDefaultCatalogName() {
            return this.defaultCatalogName;
        }


        @Override
        public StandardServiceRegistry getServiceRegistry() {
            return this.serviceRegistry;
        }
    }

}
