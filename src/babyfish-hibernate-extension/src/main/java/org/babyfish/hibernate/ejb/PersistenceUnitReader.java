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
package org.babyfish.hibernate.ejb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.PersistenceException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.I18N;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Tao Chen
 */
public class PersistenceUnitReader {
    
    public interface Resource {
        
        String getLocation();
        
        InputStream getInputStream();
    }
    
    public interface DataSourceLookup {
        DataSource getDataSource(String dataSourceName);
    }
    
    private static final String PERSISTENCE_VERSION = "version";

    private static final String PERSISTENCE_UNIT = "persistence-unit";

    private static final String UNIT_NAME = "name";

    private static final String MAPPING_FILE_NAME = "mapping-file";

    private static final String JAR_FILE_URL = "jar-file";

    private static final String MANAGED_CLASS_NAME = "class";

    private static final String PROPERTIES = "properties";

    private static final String PROVIDER = "provider";

    private static final String TRANSACTION_TYPE = "transaction-type";

    private static final String JTA_DATA_SOURCE = "jta-data-source";

    private static final String NON_JTA_DATA_SOURCE = "non-jta-data-source";

    private static final String EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";

    private static final String SHARED_CACHE_MODE = "shared-cache-mode";

    private static final String VALIDATION_MODE = "validation-mode";

    //private static final String META_INF = "META-INF";

    public XOrderedMap<String, PersistenceUnitInfo> read(String ... resources) {
        Resource[] resourceObjs = new Resource[resources.length];
        for (int i = 0; i < resources.length; i++) {
            final String location = resources[i];
            resourceObjs[i] = new Resource() {

                @Override
                public String getLocation() {
                    return location;
                }

                @Override
                public InputStream getInputStream() {
                    InputStream inputStream = 
                            PersistenceUnitInfo
                            .class
                            .getClassLoader()
                            .getResourceAsStream(location);
                    if (inputStream == null) {
                        throw new PersistenceException(resourceIsNotExists(location));
                    }
                    return inputStream;
                }
            };
        }
        return this.read(resourceObjs);
    }

    public XOrderedMap<String, PersistenceUnitInfo> read(Resource ... resources) {
        ErrorHandler handler = new ErrorHandler() {

            @Override
            public void warning(SAXParseException exception) throws SAXException {
                
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                
            }
            
        };
        XOrderedMap<String, PersistenceUnitInfo> infos = new LinkedHashMap<>();
        for (Resource resource : resources) {
            try {
                InputStream stream = resource.getInputStream();
                try {
                    Document document = buildDocument(handler, stream);
                    parseDocument(resource, document, infos);
                }
                finally {
                    stream.close();
                }
            }
            catch (IOException ex) {
                throw new IllegalArgumentException("Cannot parse persistence unit from " + resource.getLocation(), ex);
            }
            catch (SAXException ex) {
                throw new IllegalArgumentException("Invalid XML in persistence unit from " + resource.getLocation(), ex);
            }
            catch (ParserConfigurationException ex) {
                throw new IllegalArgumentException("Internal error parsing persistence unit from " + resource.getLocation(), ex);
            }
        }
        return infos;
    }

    protected Document buildDocument(ErrorHandler handler, InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setErrorHandler(handler);
        return parser.parse(stream);
    }

    protected void parseDocument(
            Resource resource, Document document, Map<String, PersistenceUnitInfo> infos) throws IOException {
        Element persistence = document.getDocumentElement();
        String version = persistence.getAttribute(PERSISTENCE_VERSION);
        URL rootUrl = determinePersistenceUnitRootUrl(resource);
        for (Element element : getChildElements(persistence, PERSISTENCE_UNIT)) {
            PersistenceUnitInfo info = parsePersistenceUnitInfo(element, version, rootUrl);
            if (infos.put(info.getPersistenceUnitName(), info) != null) {
                throw new PersistenceException(
                        duplicatePersistenceUnit(info.getPersistenceUnitName())
                );
            }
        }
    }
    
    protected DataSourceLookup getDataSourceLookup() {
        return null;
    }

    protected URL determinePersistenceUnitRootUrl(Resource resource) throws IOException {
//      URL originalURL = resource.getURL();
//
//      // If we get an archive, simply return the jar URL (section 6.2 from the JPA spec)
//      if (ResourceUtils.isJarURL(originalURL)) {
//          return ResourceUtils.extractJarFileURL(originalURL);
//      }
//
//      // check META-INF folder
//      String urlToString = originalURL.toExternalForm();
//      if (!urlToString.contains(META_INF)) {
//          if (logger.isInfoEnabled()) {
//              logger.info(resource.getFilename() +
//                      " should be located inside META-INF directory; cannot determine persistence unit root URL for " +
//                      resource);
//          }
//          return null;
//      }
//      if (urlToString.lastIndexOf(META_INF) == urlToString.lastIndexOf('/') - (1 + META_INF.length())) {
//          if (logger.isInfoEnabled()) {
//              logger.info(resource.getFilename() +
//                      " is not located in the root of META-INF directory; cannot determine persistence unit root URL for " +
//                      resource);
//          }
//          return null;
//      }
//
//      String persistenceUnitRoot = urlToString.substring(0, urlToString.lastIndexOf(META_INF));
//      if (persistenceUnitRoot.endsWith("/")) {
//          persistenceUnitRoot = persistenceUnitRoot.substring(0, persistenceUnitRoot.length() - 1);
//      }
//      return new URL(persistenceUnitRoot);
        return null;
    }

    protected PersistenceUnitInfo parsePersistenceUnitInfo(Element persistenceUnit, String version, URL rootUrl)
            throws IOException {

        PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl();

        // set JPA version (1.0 or 2.0)
        unitInfo.persistenceXMLSchemaVersion = version;
        unitInfo.persistenceUnitRootUrl = rootUrl;
        unitInfo.persistenceUnitName = nullOrTrim(persistenceUnit.getAttribute(UNIT_NAME));
        String txType = nullOrTrim(persistenceUnit.getAttribute(TRANSACTION_TYPE));
        if (txType != null) {
            unitInfo.transactionType = PersistenceUnitTransactionType.valueOf(txType);
        }

        // data-source
        DataSourceLookup dataSourceLookup = this.getDataSourceLookup();
        String jtaDataSource = nullOrTrim(getChildElementValue(persistenceUnit, JTA_DATA_SOURCE));
        if (jtaDataSource != null) {
            if (dataSourceLookup == null) {
                throw new IllegalStateException(noDataSourceLookup(DataSourceLookup.class));
            }
            unitInfo.jtaDataSource = dataSourceLookup.getDataSource(jtaDataSource);
        }

        String nonJtaDataSource = nullOrTrim(getChildElementValue(persistenceUnit, NON_JTA_DATA_SOURCE));
        if (nonJtaDataSource != null) {
            if (dataSourceLookup == null) {
                throw new IllegalStateException(noDataSourceLookup(DataSourceLookup.class));
            }
            unitInfo.nonJtaDataSource = dataSourceLookup.getDataSource(nonJtaDataSource);
        }

        // provider
        String provider = nullOrTrim(getChildElementValue(persistenceUnit, PROVIDER));
        if (provider != null) {
            unitInfo.persistenceProviderClassName = provider;
        }

        // exclude unlisted classes
        Element excludeUnlistedClasses = getChildElement(persistenceUnit, EXCLUDE_UNLISTED_CLASSES);
        if (excludeUnlistedClasses != null) {
            unitInfo.excludeUnlistedClasses = true;
        }

        // set JPA 2.0 shared cache mode
        String sharedCacheMode = nullOrTrim(getChildElementValue(persistenceUnit, SHARED_CACHE_MODE));
        if (sharedCacheMode != null) {
            unitInfo.sharedCacheMode = SharedCacheMode.valueOf(sharedCacheMode);
        }

        // set JPA 2.0 validation mode
        String validationMode = nullOrTrim(getChildElementValue(persistenceUnit, VALIDATION_MODE));
        if (validationMode != null) {
            unitInfo.validationMode = ValidationMode.valueOf(validationMode);
        }

        parseProperties(persistenceUnit, unitInfo);
        parseManagedClasses(persistenceUnit, unitInfo);
        parseMappingFiles(persistenceUnit, unitInfo);
        parseJarFiles(persistenceUnit, unitInfo);

        return unitInfo;
    }

    private void parseProperties(Element persistenceUnit, PersistenceUnitInfoImpl unitInfo) {
        Element propRoot = getChildElement(persistenceUnit, PROPERTIES);
        Properties props = new Properties();
        if (propRoot != null) {
            List<Element> properties = getChildElements(propRoot, "property");
            for (Element property : properties) {
                String name = property.getAttribute("name");
                String value = property.getAttribute("value");
                props.put(name, value);
            }
        }
        //TODO: how to readonly?
        unitInfo.properties = props;
    }

    private void parseManagedClasses(Element persistenceUnit, PersistenceUnitInfoImpl unitInfo) {
        List<Element> classes = getChildElements(persistenceUnit, MANAGED_CLASS_NAME);
        List<String> managedClassNames = new ArrayList<>(classes.size());
        for (Element element : classes) {
            String value = nullOrTrim(getValue(element));
            if (value != null) {
                managedClassNames.add(value);
            }
        }
        unitInfo.managedClassNames = MACollections.unmodifiable(managedClassNames);
    }

    private void parseMappingFiles(Element persistenceUnit, PersistenceUnitInfoImpl unitInfo) {
        List<Element> files = getChildElements(persistenceUnit, MAPPING_FILE_NAME);
        List<String> mappingFileNames = new ArrayList<>(files.size());
        for (Element element : files) {
            String value = nullOrTrim(getValue(element).trim());
            if (value != null) {
                mappingFileNames.add(value);
            }
        }
        unitInfo.mappingFileNames = MACollections.unmodifiable(mappingFileNames);
    }
    
    private void parseJarFiles(Element persistenceUnit, PersistenceUnitInfoImpl unitInfo) throws IOException {
        List<Element> jars = getChildElements(persistenceUnit, JAR_FILE_URL);
        List<URL> jarFileUrls = new ArrayList<>();
        URL rootURL = unitInfo.getPersistenceUnitRootUrl();
        for (Element element : jars) {
            String value = nullOrTrim(getValue(element));
            if (value != null) {
                URL url = this.getJarFileUrl(value, rootURL);
                jarFileUrls.add(url);
            }
        }
        unitInfo.jarFileUrls = MACollections.unmodifiable(jarFileUrls);
    }
    
    protected URL getJarFileUrl(String value, URL rootURL) throws MalformedURLException {
        if (new File(value).exists()) {
            return new URL(value);
        }
        return new URL(rootURL, value);
    }
    
    private static String getChildElementValue(Element element, String childElementName) {
        Element childElement = getChildElement(element, childElementName);
        if (childElement != null) {
            return getValue(childElement);
        }
        return null;
    }
    
    private static Element getChildElement(Element element, String childElementName) {
        for (Node childNode = element.getFirstChild(); 
                childNode != null;
                childNode = childNode.getNextSibling()) {
            if (childNode instanceof Element && childNode.getLocalName().equals(childElementName)) {
                return (Element)childNode;
            }
        }
        return null;
    }
    
    private static List<Element> getChildElements(Element element, String childElementName) {
        List<Element> elements = new ArrayList<>();
        for (Node childNode = element.getFirstChild(); 
                childNode != null;
                childNode = childNode.getNextSibling()) {
            if (childNode instanceof Element && childNode.getLocalName().equals(childElementName)) {
                elements.add((Element)childNode);
            }
        }
        return elements;
    }
    
    private static String getValue(Element element) {
        StringBuilder builder = new StringBuilder();
        for (Node childNode = element.getFirstChild();
                childNode != null;
                childNode = childNode.getNextSibling()) {
            if (childNode instanceof Text || childNode instanceof EntityReference) {
                builder.append(childNode.getNodeValue());
            }
        }
        return builder.toString();
    }
    
    private static String nullOrTrim(String value) {
        if (value != null) {
            value = value.trim();
            if (value.isEmpty()) {
                return null;
            }
        }
        return value;
    }
    
    private static class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
        
        private String persistenceUnitName;

        private String persistenceProviderClassName;

        private PersistenceUnitTransactionType transactionType;

        private DataSource nonJtaDataSource;

        private DataSource jtaDataSource;

        private List<String> mappingFileNames;

        private URL persistenceUnitRootUrl;

        private List<String> managedClassNames;
        
        private List<URL> jarFileUrls;

        private boolean excludeUnlistedClasses = false;

        private Properties properties;

        private String persistenceXMLSchemaVersion = "1.0";
        
        private SharedCacheMode sharedCacheMode;
        
        private ValidationMode validationMode;

        @Override
        public String getPersistenceUnitName() {
            return this.persistenceUnitName;
        }

        @Override
        public String getPersistenceProviderClassName() {
            return this.persistenceProviderClassName;
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            if (this.transactionType != null) {
                return this.transactionType;
            }
            else {
                return this.jtaDataSource != null ?
                        PersistenceUnitTransactionType.JTA : 
                        PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }
        }

        @Override
        public DataSource getJtaDataSource() {
            return this.jtaDataSource;
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return this.nonJtaDataSource;
        }

        @Override
        public List<String> getMappingFileNames() {
            return this.mappingFileNames;
        }

        @Override
        public List<URL> getJarFileUrls() {
            return this.jarFileUrls;
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return this.persistenceUnitRootUrl;
        }

        @Override
        public List<String> getManagedClassNames() {
            return this.managedClassNames;
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return this.excludeUnlistedClasses;
        }

        @Override
        public Properties getProperties() {
            return this.properties;
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return this.persistenceXMLSchemaVersion;
        }

        @Override
        public ClassLoader getClassLoader() {
            return PersistenceUnitInfo.class.getClassLoader();
        }

        /**
         * This implementation throws an UnsupportedOperationException.
         */
        public ClassLoader getNewTempClassLoader() {
            return PersistenceUnitInfo.class.getClassLoader();
        }
        
        @Override
        public void addTransformer(ClassTransformer classTransformer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return this.sharedCacheMode;
        }

        @Override
        public ValidationMode getValidationMode() {
            return this.validationMode;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PersistenceUnitInfo: name '");
            builder.append(this.persistenceUnitName);
            builder.append("', root URL [");
            builder.append(this.persistenceUnitRootUrl);
            builder.append("]");
            return builder.toString();
        }
    }

    @I18N
    private static native String resourceIsNotExists(String location);
    
    @I18N
    private static native String duplicatePersistenceUnit(String persistenceUnitName);
    
    @I18N
    private static native String noDataSourceLookup(Class<DataSourceLookup> dataSourceLookupTypeConstant);
}

