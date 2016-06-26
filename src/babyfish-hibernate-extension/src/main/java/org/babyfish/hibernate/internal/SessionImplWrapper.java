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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.hibernate.XQuery;
import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.hql.XQueryImpl;
import org.babyfish.hibernate.hql.XQueryPlan;
import org.babyfish.hibernate.hql.XQueryPlanCache;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.QueryPaths;
import org.babyfish.model.jpa.path.TypedQueryPath;
import org.babyfish.model.jpa.path.spi.PathPlanKey;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.persistence.QueryType;
import org.babyfish.util.reflect.MethodDescriptor;
import org.babyfish.util.reflect.MethodImplementation;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassWrapper;
import org.babyfish.util.reflect.runtime.XMethodVisitor;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.model.hibernate.spi.association.EntityReferenceComparator;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Tao Chen
 */
public class SessionImplWrapper extends ClassWrapper {
    
    private static final SessionImplWrapper INSTANCE = getInstance(SessionImplWrapper.class);
    
    private static final Method ERROR_IF_CLOSED_METHOD;
    
    private static final Method CHECK_TRANSACTION_SYNCH_STATUS_METHOD;
    
    private static final Method AUTO_FLUSH_If_REQUIRED;
    
    private static final Method AFTER_OPERATION_METHOD;
    
    private static final Method INIT_QUERY_METHOD;
    
    private static final Field DONT_FLUSH_FROM_FIND_FIELD;
    
    protected SessionImplWrapper() {
        super(SessionImpl.class, XSessionFactory.class);
    }
    
    public static XSession wrap(Session session, XSessionFactory factory) {
        if (session instanceof XSession) {
            return (XSession)session;
        }
        return (XSession)INSTANCE.createProxy((SessionImpl)session, factory);
    }

    @Override
    protected Class<?>[] onGetInterfaceTypes() {
        Class<?>[] interfaceTypes = SessionImpl.class.getInterfaces();
        Class<?>[] retval = new Class[interfaceTypes.length + 2];
        System.arraycopy(interfaceTypes, 0, retval, 2, interfaceTypes.length);
        retval[0] = org.babyfish.hibernate.classic.XSession.class;
        retval[1] = XSessionImplementor.class;
        return retval;
    }

    @Override
    protected boolean useWrapperLoader() {
        return true;
    }
    
    @Override
    protected void generateMethodCode(XMethodVisitor mv, MethodImplementation methodImplementation) {
        MethodDescriptor descriptor = methodImplementation.getDescriptor();
        if (SharedSessionBuilder.class.isAssignableFrom(descriptor.getReturnType())) {
            this.generateInvokeRawMethodCode(mv, methodImplementation);
            this.generateGetArgument(mv, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SharedSessionBuilderWrapper.class), 
                    "wrap", 
                    '(' +
                    ASM.getDescriptor(SharedSessionBuilder.class) +
                    ASM.getDescriptor(XSessionFactory.class) +
                    ')' +
                    ASM.getDescriptor(XSharedSessionBuilderImplementor.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (SessionBuilder.class.isAssignableFrom(descriptor.getReturnType())) {
            this.generateInvokeRawMethodCode(mv, methodImplementation);
            this.generateGetArgument(mv, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionBuilderWrapper.class), 
                    "wrap", 
                    '(' +
                    ASM.getDescriptor(SessionBuilder.class) +
                    ASM.getDescriptor(XSessionFactory.class) +
                    ')' +
                    ASM.getDescriptor(XSessionBuilderImplementor.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("getRawSessionImpl")) {
            this.generateGetRaw(mv);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("getSessionFactory")) {
            this.generateGetArgument(mv, 0);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("getFactory")) {
            this.generateGetArgument(mv, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(XSessionFactoryImplementor.class));
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("delete", Object.class)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class), 
                    "delete",
                    '(' +
                    ASM.getDescriptor(XSessionImplementor.class) +
                    "Ljava/lang/Object;)V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
        } else if (descriptor.match("createQuery", String.class)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class),
                    "createQuery",
                    '(' +
                    ASM.getDescriptor(XSessionImplementor.class) +
                    ASM.getDescriptor(String.class) +
                    ')' +
                    ASM.getDescriptor(XQuery.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("getNamedQuery", String.class)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class),
                    "getNamedQuery",
                    '(' +
                    ASM.getDescriptor(XSessionImplementor.class) +
                    ASM.getDescriptor(String.class) +
                    ')' +
                    ASM.getDescriptor(XQuery.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("createQuery", NamedQueryDefinition.class)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class),
                    "createQuery",
                    '(' +
                    ASM.getDescriptor(XSessionImplementor.class) +
                    ASM.getDescriptor(NamedQueryDefinition.class) +
                    ')' +
                    ASM.getDescriptor(XQuery.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("list", String.class, QueryParameters.class, QueryType.class, PathPlanKey.class)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class),
                    "list",
                    '(' +
                    ASM.getDescriptor(XSessionImplementor.class) +
                    ASM.getDescriptor(String.class) +
                    ASM.getDescriptor(QueryParameters.class) +
                    ASM.getDescriptor(QueryType.class) +
                    ASM.getDescriptor(PathPlanKey.class) +
                    ")Ljava/util/List;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("unlimitedCount", String.class, QueryParameters.class, QueryType.class, PathPlanKey.class)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class),
                    "unlimitedCount",
                    '(' +
                    ASM.getDescriptor(XSessionImplementor.class) +
                    ASM.getDescriptor(String.class) +
                    ASM.getDescriptor(QueryParameters.class) +
                    ASM.getDescriptor(QueryType.class) +
                    ASM.getDescriptor(PathPlanKey.class) +
                    ")J",
                    false);
            mv.visitInsn(Opcodes.LRETURN);
        } else if ("get".equals(descriptor.getName()) &&
                !descriptor.getParameterTypes().isEmpty() && (
                        descriptor.getParameterTypes().get(descriptor.getParameterTypes().size() - 1).isArray() ||
                        descriptor.getParameterTypes().get(1) == Iterable.class
                )
        ) {
            List<Class<?>> parameterTypes = descriptor.getParameterTypes();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            int slotIndex = 1;
            StringBuilder builder = new StringBuilder();
            for (Class<?> parameterType : parameterTypes) {
                mv.visitVarInsn(ASM.getLoadCode(parameterType), slotIndex);
                slotIndex += ASM.getSlotCount(parameterType);
                builder.append(ASM.getDescriptor(parameterType));
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class), 
                    descriptor.getName(), 
                    '(' +
                    ASM.getDescriptor(XSession.class) +
                    builder.toString() +
                    ')' +
                    ASM.getDescriptor(descriptor.getReturnType()),
                    false);
            mv.visitInsn(ASM.getReturnCode(descriptor.getReturnType()));
        } else {
            super.generateMethodCode(mv, methodImplementation);
        }
    }

    @SuppressWarnings("unchecked")
    protected static void delete(XSessionImplementor sessionProxy, Object object) {
        if (object != null) {
            SessionFactory sessionFactory = sessionProxy.getRawSessionImpl().getSessionFactory();
            PersistenceContext persistenceContext = 
                ((org.hibernate.internal.SessionImpl)sessionProxy.getRawSessionImpl()).getPersistenceContext();
            Map<PersistentCollection, CollectionEntry> collectionEntries = 
                persistenceContext.getCollectionEntries();
            for (Entry<PersistentCollection, CollectionEntry> entry : collectionEntries.entrySet()) {
                PersistentCollection persistentCollection = entry.getKey();
                if (persistentCollection.wasInitialized()) {
                    CollectionMetadata collectionMetadata = 
                        sessionFactory.getCollectionMetadata(persistentCollection.getRole());
                    Class<?> elementClass = collectionMetadata.getElementType().getReturnedClass();
                    if (elementClass.isAssignableFrom(object.getClass())) {
                        if (persistentCollection instanceof Map<?, ?>) {
                            ((Map<?, ?>)persistentCollection).values().remove(object);
                        } else if (persistentCollection instanceof Collection<?>) {
                            ((Collection<?>)persistentCollection).remove(object);
                        }
                    }
                }
            }
            Class<?> clazz = object.getClass();
            Collection<HbmReference> hbmReferences = null;
            NavigableMap<Class<?>, Collection<HbmReference>> hbmReferencesByTargetClass =
                    (NavigableMap<Class<?>, Collection<HbmReference>>)
                    ((XSessionFactoryImplementor)sessionProxy.getFactory()).getInternalData(
                            SessionFactoryImplWrapper.IDK_HBM_REFERENCES_BY_TARGET_CLASS);
            for (Entry<Class<?>, Collection<HbmReference>> entry : hbmReferencesByTargetClass.descendingMap().entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    hbmReferences = entry.getValue();
                    break;
                }
            }
            if (hbmReferences != null) {
                EntityReferenceComparator<? super Object> referenceComparator =
                    EntityReferenceComparator.getInstance();
                Entry<Object, EntityEntry>[] entityEntries =
                    persistenceContext.reentrantSafeEntityEntries();
                if (entityEntries != null) {
                    for (Entry<Object, EntityEntry> entry : entityEntries) {
                        Object entity = entry.getKey();
                        if (Hibernate.isInitialized(entity)) {
                            EntityPersister persister = entry.getValue().getPersister();
                            ClassMetadata classMetadata = 
                                persister.getClassMetadata();
                            for (HbmReference hbmReference : hbmReferences) {
                                if (hbmReference.ownerMetadata == classMetadata) {
                                    Object expectedObject = 
                                        persister.getPropertyValue(entity, hbmReference.propertyIndex);
                                    if (referenceComparator.same(expectedObject, object)) {
                                        persister.setPropertyValue(entity, hbmReference.propertyIndex, null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        sessionProxy.getRawSessionImpl().delete(object);
    }
    
    @SuppressWarnings("unchecked")
    protected static XQuery createQuery(
            XSessionImplementor sessionProxy,
            String queryString) throws HibernateException {
        errorIfClosed(sessionProxy.getRawSessionImpl());
        checkTransactionSynchStatus(sessionProxy.getRawSessionImpl());
        XQueryPlan xQueryPlan = (XQueryPlan) 
                sessionProxy
                .getFactory()
                .getQueryPlanCache()
                .getHQLQueryPlan(queryString, false, sessionProxy.getRawSessionImpl().getEnabledFilters());
        XQuery query = new XQueryImpl(
                queryString,
                sessionProxy,
                xQueryPlan.getParameterMetadata());
        query.setComment( queryString );
        return query;
    }

    @SuppressWarnings("unchecked")
    protected static XQuery getNamedQuery(
            XSessionImplementor sessionProxy, 
            String queryName) throws HibernateException {
        errorIfClosed(sessionProxy.getRawSessionImpl());
        checkTransactionSynchStatus(sessionProxy.getRawSessionImpl());
        NamedQueryDefinition nqd = sessionProxy.getFactory().getNamedQuery(queryName);
        if (nqd != null) {
            String queryString = nqd.getQueryString();
            XQueryPlan xQueryPlan = (XQueryPlan) 
                    sessionProxy
                    .getFactory()
                    .getQueryPlanCache()
                    .getHQLQueryPlan(queryString, false, sessionProxy.getRawSessionImpl().getEnabledFilters()
            );
            XQuery query = new XQueryImpl(
                    queryString,
                    nqd.getFlushMode(),
                    sessionProxy,
                    xQueryPlan.getParameterMetadata()
            );
            query.setComment( "named HQL query " + queryName );
            initQuery(sessionProxy.getRawSessionImpl(), query, nqd);
            return query;
        }
        NamedSQLQueryDefinition nsqlqd = sessionProxy.getFactory().getNamedSQLQuery(queryName);
        if (nsqlqd != null) {
            throw new MappingException(
                    "Name query \"" +
                    queryName +
                    "\" is a native query");
        }
        throw new MappingException( "Named query not known: " + queryName );
    }
    
    @SuppressWarnings("unchecked")
    protected static XQuery createQuery(
            XSessionImplementor sessionProxy, 
            NamedQueryDefinition namedQueryDefinition) throws HibernateException {
        String queryString = namedQueryDefinition.getQueryString();
        XQueryPlan xQueryPlan = (XQueryPlan) 
                sessionProxy
                .getFactory()
                .getQueryPlanCache()
                .getHQLQueryPlan(queryString, false, sessionProxy.getRawSessionImpl().getEnabledFilters()
        );
        XQuery query = new XQueryImpl(
                queryString,
                namedQueryDefinition.getFlushMode(),
                sessionProxy,
                xQueryPlan.getParameterMetadata()
        );
        query.setComment( "named HQL query " + namedQueryDefinition.getName() );
        if ( namedQueryDefinition.getLockOptions() != null ) {
            query.setLockOptions( namedQueryDefinition.getLockOptions() );
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    protected static <T> List<T> list(
            XSessionImplementor sessionProxy,
            String query, 
            QueryParameters queryParameters,
            QueryType queryType,
            PathPlanKey pathPlanKey) throws HibernateException {
        errorIfClosed(sessionProxy.getRawSessionImpl());
        checkTransactionSynchStatus(sessionProxy.getRawSessionImpl());
        queryParameters.validateParameters();
        XQueryPlanCache queryPlanCache = (XQueryPlanCache)sessionProxy.getFactory().getQueryPlanCache();
        XQueryPlan plan = queryPlanCache.getHQLQueryPlan(query, pathPlanKey, false, sessionProxy.getRawSessionImpl().getEnabledFilters());
        autoFlushIfRequired(sessionProxy.getRawSessionImpl(), plan.getQuerySpaces());
    
        List<T> results;
        boolean success = false;
    
        //stops flush being called multiple times if this method is recursively called
        increaseDontFlushFromFind(sessionProxy.getRawSessionImpl());
        try {
            results = plan.performList(sessionProxy.getRawSessionImpl(), queryParameters, queryType);
            success = true;
        }
        finally {
            decreaseDontFlushFromFind(sessionProxy.getRawSessionImpl());
            afterOperation(sessionProxy.getRawSessionImpl(), success);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    protected static long unlimitedCount(
            XSessionImplementor sessionProxy,
            String query, 
            QueryParameters queryParameters,
            QueryType queryType,
            PathPlanKey pathPlanKey) throws HibernateException {
        errorIfClosed(sessionProxy.getRawSessionImpl());
        checkTransactionSynchStatus(sessionProxy.getRawSessionImpl());
        queryParameters.validateParameters();
        XQueryPlanCache queryPlanCache = (XQueryPlanCache)sessionProxy.getFactory().getQueryPlanCache();
        XQueryPlan plan = queryPlanCache.getHQLQueryPlan(query, pathPlanKey, false, sessionProxy.getRawSessionImpl().getEnabledFilters());
        autoFlushIfRequired(sessionProxy.getRawSessionImpl(), plan.getQuerySpaces());
    
        long count = 0;
        boolean success = false;
    
        //stops flush being called multiple times if this method is recursively called
        increaseDontFlushFromFind(sessionProxy.getRawSessionImpl());
        try {
            count = plan.performUnlimitedCount(sessionProxy.getRawSessionImpl(), queryParameters, queryType);
            success = true;
        }
        finally {
            decreaseDontFlushFromFind(sessionProxy.getRawSessionImpl());
            afterOperation(sessionProxy.getRawSessionImpl(), success);
        }
        return count;
    }
    
    protected static Object get(XSession session, String entityName, Serializable id, String[] queryPaths) {
        return getByQueryPaths(session, entityName, id, null, QueryPaths.compile(queryPaths));
    }
    
    protected static Object get(XSession session, String entityName, Serializable id, LockOptions lockOptions, String[] queryPaths) {
        return getByQueryPaths(session, entityName, id, lockOptions, QueryPaths.compile(queryPaths));
    }
    
    protected static Object get(XSession session, String entityName, Serializable id, QueryPath[] queryPaths) {
        return getByQueryPaths(session, entityName, id, null, queryPaths);
    }
    
    @SuppressWarnings("unchecked")
    protected static <E> E get(XSession session, Class<E> entityClass, Serializable id, TypedQueryPath<E>[] queryPaths) { 
        return (E)getByQueryPaths(session, entityClass, id, null, queryPaths);
    }
    
    protected static Object get(XSession session, String entityName, Serializable id, LockOptions lockOptions, QueryPath[] queryPaths) {
        return getByQueryPaths(session, entityName, id, lockOptions, queryPaths);
    }
    
    @SuppressWarnings("unchecked")
    protected static <E> E get(XSession session, Class<E> entityClass, Serializable id, LockOptions lockOptions, TypedQueryPath<E>[] queryPaths) { 
        return (E)getByQueryPaths(session, entityClass, id, lockOptions, queryPaths);
    }
    
    @SuppressWarnings("unchecked")
    protected static List<Object> get(XSession session, String entityName, Iterable<? extends Serializable> ids, String[] queryPaths) {
        return getByQueryPaths(session, entityName, ids, null, QueryPaths.compile(queryPaths));
    }
    
    @SuppressWarnings("unchecked")
    protected static List<Object> get(XSession session, String entityName, Iterable<? extends Serializable> ids, LockOptions lockOptions, String[] queryPaths) {
        return getByQueryPaths(session, entityName, ids, lockOptions, QueryPaths.compile(queryPaths));
    }
    
    @SuppressWarnings("unchecked")
    protected static List<Object> get(XSession session, String entityName, Iterable<? extends Serializable> ids, QueryPath[] queryPaths) {
        return getByQueryPaths(session, entityName, ids, null, queryPaths);
    }
    
    @SuppressWarnings("unchecked")
    protected static <E> List<E> get(XSession session, Class<E> entityClass, Iterable<? extends Serializable> ids, TypedQueryPath<E>[] queryPaths) { 
        return getByQueryPaths(session, entityClass, ids, null, queryPaths);
    }
    
    @SuppressWarnings("unchecked")
    protected static List<Object> get(XSession session, String entityName, Iterable<? extends Serializable> ids, LockOptions lockOptions, QueryPath[] queryPaths) {
        return getByQueryPaths(session, entityName, ids, lockOptions, queryPaths);
    }
    
    @SuppressWarnings("unchecked")
    protected static <E> List<E> get(XSession session, Class<E> entityClass, Iterable<? extends Serializable> ids, LockOptions lockOptions, TypedQueryPath<E>[] queryPaths) { 
        return getByQueryPaths(session, entityClass, ids, lockOptions, queryPaths);
    }
    
    private static Object getByQueryPaths(
            XSession session, 
            Object entityNameOrClass, 
            Serializable id, 
            LockOptions nullableLockOptions, //can not null 
            QueryPath[] queryPaths) {
        String entityName = entityNameOrClass instanceof String ? (String)entityNameOrClass : ((Class<?>)entityNameOrClass).getName();
        if (queryPaths == null || queryPaths.length == 0) {
            if (nullableLockOptions != null) {
                return session.get(entityName, id, nullableLockOptions);
            }
            return session.get(entityName, id);
        }
        XSessionFactoryImplementor factory = ((XSessionImplementor)session).getFactory();
        Class<?> entityClass;
        if (entityNameOrClass instanceof Class<?>) {
            entityClass = (Class<?>)entityNameOrClass;
        } else {
            entityClass = factory.getEntityPersister(entityName).getMappedClass();
        }
        String idPropertyName = factory.getEntityPersister(entityClass.getName()).getIdentifierPropertyName();
        XQuery query =
                session
                .createQuery("from " + entityName + " where " + idPropertyName + " = ?")
                .setQueryPaths(queryPaths)
                .setParameter(0, id);
        if (nullableLockOptions != null) {
            query.setLockOptions(nullableLockOptions);
        }
        return query.uniqueResult();
    }
    
    @SuppressWarnings("rawtypes")
    private static List getByQueryPaths(
            XSession session, 
            Object entityNameOrClass, 
            Iterable<? extends Serializable> ids, 
            LockOptions nullableLockOptions, //can not null 
            QueryPath[] queryPaths) {
        Set<Serializable> idSet; 
        if (ids instanceof Collection<?>) {
            idSet = new LinkedHashSet<>((((Collection<?>)ids).size() * 4 + 2) / 3);
        } else {
            idSet = new LinkedHashSet<>();
        }
        for (Serializable id : ids) {
            if (id != null) {
                idSet.add(id);
            }
        }
        if (idSet.isEmpty()) {
            //Let the returned list can be modified, so don't let it return MACollections.emptyList();
            return new ArrayList<>();
        }
                
        String entityName = entityNameOrClass instanceof String ? (String)entityNameOrClass : ((Class<?>)entityNameOrClass).getName();
        XSessionFactoryImplementor factory = ((XSessionImplementor)session).getFactory();
        Class<?> entityClass;
        if (entityNameOrClass instanceof Class<?>) {
            entityClass = (Class<?>)entityNameOrClass;
        } else {
            entityClass = factory.getEntityPersister(entityName).getMappedClass();
        }
        String idPropertyName = factory.getEntityPersister(entityClass.getName()).getIdentifierPropertyName();
        XQuery query;
        if (idSet.size() == 1) {
            query =
                    session
                    .createQuery("from " + entityName + " where " + idPropertyName + " = ?")
                    .setParameter(0, idSet.iterator().next());
        } else {
            query =
                    session
                    .createQuery("from " + entityName + " where " + idPropertyName + " in (?)")
                    .setParameter(0, idSet);
        }
        query.setQueryPaths(queryPaths);
        if (nullableLockOptions != null) {
            query.setLockOptions(nullableLockOptions);
        }
        return query.list();
    }

    protected static void errorIfClosed(SessionImpl rawSession) {
        try {
            ERROR_IF_CLOSED_METHOD.invoke(rawSession);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    protected static void checkTransactionSynchStatus(SessionImpl rawSession) {
        try {
            CHECK_TRANSACTION_SYNCH_STATUS_METHOD.invoke(rawSession);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    protected static boolean autoFlushIfRequired(SessionImpl rawSession, Set<Serializable> querySpaces) {
        try {
            return (Boolean)AUTO_FLUSH_If_REQUIRED.invoke(rawSession, querySpaces);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    protected static void afterOperation(SessionImpl rawSession, boolean success) {
        try {
            AFTER_OPERATION_METHOD.invoke(rawSession, success);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    protected static void initQuery(SessionImpl rawSession, Query query, NamedQueryDefinition nqd) {
        try {
            INIT_QUERY_METHOD.invoke(rawSession, query, nqd);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    protected static void increaseDontFlushFromFind(SessionImpl rawSession) {
        try {
            DONT_FLUSH_FROM_FIND_FIELD.setInt(
                    rawSession, 
                    DONT_FLUSH_FROM_FIND_FIELD.getInt(rawSession) + 1);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }
    
    protected static void decreaseDontFlushFromFind(SessionImpl rawSession) {
        try {
            DONT_FLUSH_FROM_FIND_FIELD.setInt(
                    rawSession, 
                    DONT_FLUSH_FROM_FIND_FIELD.getInt(rawSession) - 1);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }
    
    static {
        
        Method errorIFClosedMethod;
        try {
            errorIFClosedMethod = AbstractSessionImpl.class.getDeclaredMethod("errorIfClosed");
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        errorIFClosedMethod.setAccessible(true);
        
        Method checkTransactionSynchStatusMethod;
        try {
            checkTransactionSynchStatusMethod = SessionImpl.class.getDeclaredMethod("checkTransactionSynchStatus");
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        checkTransactionSynchStatusMethod.setAccessible(true);
        
        Method autoFlushIfRequiredMethod;
        try {
            autoFlushIfRequiredMethod = SessionImpl.class.getDeclaredMethod("autoFlushIfRequired", java.util.Set.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        autoFlushIfRequiredMethod.setAccessible(true);
        
        Method afterOperationMethod;
        try {
            afterOperationMethod = SessionImpl.class.getDeclaredMethod("afterOperation", boolean.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        afterOperationMethod.setAccessible(true);
        
        Method initQueryMethod;
        try {
            initQueryMethod = AbstractSessionImpl.class.getDeclaredMethod("initQuery", Query.class, NamedQueryDefinition.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        initQueryMethod.setAccessible(true);
        
        Field dontFlushFromFindField;
        try {
            dontFlushFromFindField = SessionImpl.class.getDeclaredField("dontFlushFromFind");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        dontFlushFromFindField.setAccessible(true);
        
        ERROR_IF_CLOSED_METHOD = errorIFClosedMethod;
        CHECK_TRANSACTION_SYNCH_STATUS_METHOD = checkTransactionSynchStatusMethod;
        AUTO_FLUSH_If_REQUIRED = autoFlushIfRequiredMethod;
        AFTER_OPERATION_METHOD = afterOperationMethod;
        INIT_QUERY_METHOD = initQueryMethod;
        DONT_FLUSH_FROM_FIND_FIELD = dontFlushFromFindField;
    }
}
