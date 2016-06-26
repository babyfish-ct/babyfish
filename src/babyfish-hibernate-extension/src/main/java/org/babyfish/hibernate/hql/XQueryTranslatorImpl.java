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
package org.babyfish.hibernate.hql;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Tuple;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.hibernate.cfg.SettingsFactory;
import org.babyfish.hibernate.dialect.DistinctLimitDialect;
import org.babyfish.hibernate.dialect.Oracle10gDialect;
import org.babyfish.hibernate.hql.HqlASTHelper.AliasGenerator;
import org.babyfish.hibernate.internal.AbstractHibernatePathPlanFactory;
import org.babyfish.hibernate.loader.DistinctLimitQueryLoader;
import org.babyfish.hibernate.loader.UnlimitedCountLoader;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.jpa.path.spi.JoinNode;
import org.babyfish.model.jpa.path.spi.OrderNode;
import org.babyfish.model.jpa.path.spi.PathPlan;
import org.babyfish.model.jpa.path.spi.PathPlanKey;
import org.babyfish.model.jpa.path.spi.SubPlan;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ScalarBatchLoadingExecutor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.FieldInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.InsnNode;
import org.babyfish.org.objectweb.asm.tree.JumpInsnNode;
import org.babyfish.org.objectweb.asm.tree.LabelNode;
import org.babyfish.org.objectweb.asm.tree.LdcInsnNode;
import org.babyfish.org.objectweb.asm.tree.MethodInsnNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.org.objectweb.asm.tree.TypeInsnNode;
import org.babyfish.org.objectweb.asm.tree.VarInsnNode;
import org.babyfish.persistence.Constants;
import org.babyfish.persistence.QueryType;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassEnhancer;
import org.hibernate.Filter;
import org.hibernate.QueryException;
import org.hibernate.bytecode.instrumentation.internal.FieldInterceptionHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.query.spi.EntityGraphQueryHint;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.internal.ast.SqlGenerator;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.model.hibernate.spi.scalar.HibernateScalarLoader;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.AssociationType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.collections.AST;

/**
 * @author Tao Chen
 */
public abstract class XQueryTranslatorImpl 
extends QueryTranslatorImpl 
implements XQueryTranslator, XFilterTranslator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(XQueryTranslatorImpl.class);
    
    private static final Constructor<XQueryTranslatorImpl> CONSTRUCTOR;
    
    private static final Method PARSE_METHOD;
    
    /*
     * These fields is not private because they may be access by the dynamic derived class.
     */
    SessionFactoryImplementor factory; 
    
    String hql;
    
    PathPlanKey pathPlanKey;
    
    PathPlan pathPlan;
        
    int queryType; //Hibernate's queryType: SELECT, INSERT, UPDATE, DELETE; not babyfish's queryType: DISTINCT, RESULT
    
    Map<String, AST> startFromElementASTMap;
    
    Map<String, Integer> returnedEntityColumns;
    
    String countSql;
    
    AST countSqlAst;
    
    String distinctCountSql;
    
    AST distinctCountSqlAst;
    
    QueryLoader queryLoader;
    
    DistinctLimitQueryLoader distinctLimitQueryLoader;
    
    UnlimitedCountLoader countLoader;
    
    UnlimitedCountLoader distinctCountLoader;
    
    ExceptionCreator unlimitedCountExceptionCreator;
    
    boolean usingQueryPaths;
    
    protected XQueryTranslatorImpl(
            String queryIdentifier, 
            String query,
            PathPlanKey pathPlanKey,
            Map<String, Filter> enabledFilters, 
            SessionFactoryImplementor factory,
            EntityGraphQueryHint entityGraphQueryHint) {
        super(queryIdentifier, query, enabledFilters, factory);
        this.factory = factory;
        this.hql = query;
        this.pathPlanKey = pathPlanKey;
        this.usingQueryPaths = pathPlanKey != null;
    }
    
    public static XQueryTranslatorImpl newInstance(
            String queryIdentifier,
            String queryString, 
            PathPlanKey pathPlanKey,
            Map<String, Filter> filters, 
            SessionFactoryImplementor factory,
            EntityGraphQueryHint entityGraphQueryHint) {
        try {
            return CONSTRUCTOR.newInstance(
                    new Object[] { 
                            queryIdentifier, 
                            queryString, 
                            pathPlanKey, 
                            filters, 
                            factory,
                            entityGraphQueryHint
                    }
            );
        } catch (InstantiationException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (IllegalAccessException ex) {
            throw UncheckedException.rethrow(ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    @Override
    public String getCountSQLString() {
        return this.countSql;
    }

    @Override
    public String getDistinctCountSQLString() {
        return this.distinctCountSql;
    }
    
    @Override
    public final PathPlan getPathPlan() {
        return this.pathPlan;
    }

    @Override
    public long unlimitedCount(
            SessionImplementor session,
            QueryParameters queryParameters,
            QueryType queryType) {
        if (this.unlimitedCountExceptionCreator != null) {
            throw this.unlimitedCountExceptionCreator.create();
        }
        UnlimitedCountLoader loader;
        if (queryType == QueryType.DISTINCT  && this.distinctCountLoader != null) {
            loader = this.distinctCountLoader;
        } else {
            loader = this.countLoader;
        }
        if (loader == null) {
            throw new QueryException(operationRequiresQuery("unlimitedCount"));
        }
        List<?> list = loader.list(session, queryParameters);
        Object o = list.iterator().next();
        if (o.getClass().isArray()) {
            o = ((Object[])o)[0];
        }
        if (o instanceof Long) {
            return (Long)o;
        }
        if (o instanceof Integer) {
            return (Integer)o;
        }
        return Long.parseLong(o.toString());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> List<T> list(SessionImplementor session,
            QueryParameters queryParameters,
            QueryType queryType) {
        if (queryType == QueryType.DISTINCT) {
            List<T> tmp;
            if (this.distinctLimitQueryLoader != null) {
                tmp = (List)this.distinctLimitQueryLoader.list(session, queryParameters);
            } else {
                boolean hasLimit = queryParameters.getRowSelection() != null && queryParameters.getRowSelection().definesLimits();
                if (hasLimit && this.containsCollectionFetches()) {
                    if (!SettingsFactory.isLimitInMemoryEnabled(session.getFactory().getProperties())) {
                        throw new QueryException(
                                hibernateLimitInMemoryForCollectionFetchIsNotEnabled(
                                        DistinctLimitDialect.class, 
                                        Oracle10gDialect.class, 
                                        SettingsFactory.ENABLE_LIMIT_IN_MEMORY
                                )
                        );
                    }
                }
                tmp = this.list(session, queryParameters);              
            }
            Set<T> distinction = new LinkedHashSet<T>(ReferenceEqualityComparator.getInstance());
            distinction.addAll(tmp);
            List<T> results = new ArrayList<T>(distinction.size());
            results.addAll(distinction);
            this.applyScalarEagerness(results, session);
            return results;
        } else {
            return this.queryLoader.list(session, queryParameters);
        }
    }
    
    protected final SessionFactoryImplementor getFactory() {
        return this.factory;
    }
    
    protected final boolean isUsingQueryPaths() {
        return this.usingQueryPaths;
    }

    //It is invoked by the byte code generated runtime.
    //It is always called before 
    //applyRootJoinNodeForCount and applyRootJoinNodeForDistinctCount
    final void applyRootJoinNode(AST ast) {
        this.queryType = ast.getType();
        this.initialize(ast);
        if (ast.getType() == HqlTokenTypes.QUERY) {
            logAST(ast, "Before apply query paths for generic query, the HQL-AST is");
            this.onApplyRootJoinNode(ast);
            this.preApplyScalarEagerness(ast);
            logAST(ast, "After apply query paths for generic query, the HQL-AST is");
        }
    }
    
    //It is invoked by the byte code generated runtime.
    final void applyRootJoinNodeForCount(AST ast) {
        this.initialize(ast);
        if (ast.getType() == HqlTokenTypes.QUERY && this.unlimitedCountExceptionCreator == null) {
            this.unlimitedCountExceptionCreator = this.getUnlimitedCountExceptionCreator(ast);
            if (this.unlimitedCountExceptionCreator == null) {
                logAST(ast, "Before apply query paths for count query, the HQL-AST is");
                this.onApplyRootJoinNodeForCount(ast, false);
                logAST(ast, "After apply query paths for count query, the HQL-AST is");
            }
        }
    }
    
    //It is invoked by the byte code generated runtime.
    final void applyRootJoinNodeForDistinctCount(AST ast) {
        this.initialize(ast);
        if (ast.getType() == HqlTokenTypes.QUERY && this.unlimitedCountExceptionCreator == null) {
            this.unlimitedCountExceptionCreator = this.getUnlimitedCountExceptionCreator(ast);
            if (this.unlimitedCountExceptionCreator == null) {
                logAST(ast, "Before apply query paths for distinct count query, the HQL-AST is");
                this.onApplyRootJoinNodeForCount(ast, true);
                logAST(ast, "After apply query paths for distinct count query, the HQL-AST is");
            }
        }
    }
    
    //It is invoked by the byte code generated runtime.
    final void processCountSqlAst(AST countSqlAST) {
        logAST(countSqlAST, "Before SQL-AST processing for count query, the SQL-AST is");
        this.onProcessCountSqlAST(countSqlAST, false);
        logAST(countSqlAST, "After SQL-AST processing for count query, the SQL-AST is");
    }
    
    //It is invoked by the byte code generated runtime.
    final void processDistinctCountSqlAst(AST distinctCountSqlAST) {
        logAST(distinctCountSqlAST, "Before SQL-AST processing for distinct count query, the SQL-AST is");
        this.onProcessCountSqlAST(distinctCountSqlAST, true);
        logAST(distinctCountSqlAST, "Before SQL-AST processing for distinct count query, the SQL-AST is");
    }
    
    @SuppressWarnings("unchecked")
    protected boolean shouldUseDistinctQuery() {
        if (this.pathPlan.containsCollectionJoins()) {
            return true;
        }
        QueryNode queryNode = (QueryNode)this.getSqlAST();
        boolean foundFrom = false;
        for (FromElement fromElement : (List<FromElement>)queryNode.getFromClause().getFromElements()) {
            if (fromElement.getClass() == FromElement.class ) {
                if (!fromElement.getText().contains(" join ")) {
                    if (foundFrom) {
                        return true;
                    }
                    foundFrom = true;
                } else if (fromElement.getText().startsWith("cross ")) {
                    return true;
                } else if (fromElement.getQueryableCollection() != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    protected boolean shouldUseDistinctCount() {
        if (this.pathPlan.containsCollectionInnerJoins()) {
            return true;
        }
        QueryNode queryNode = (QueryNode)this.getSqlAST();
        Set<FromElement> collectionLeftJoins = new HashSet<>(ReferenceEqualityComparator.getInstance());
        boolean foundFrom = false;
        for (FromElement fromElement : (List<FromElement>)queryNode.getFromClause().getFromElements()) {
            if (fromElement.getClass() == FromElement.class ) {
                if (!fromElement.getText().contains(" join ")) {
                    if (foundFrom) {
                        return true;
                    }
                    foundFrom = true;
                } else if (fromElement.getText().startsWith("cross ")) {
                    return true;
                } else if (fromElement.getQueryableCollection() != null) {
                    if (!fromElement.getText().startsWith("left ")) {
                        return true;
                    }
                    if (fromElement.getWithClauseFragment() != null) {
                        return true;
                    }
                    collectionLeftJoins.add(fromElement);
                }
            }
        }
        return SqlASTHelper.findJoinReferenceInWhereCaluse(queryNode, collectionLeftJoins);
    }
    
    protected void onApplyRootJoinNode(AST ast) {
        AliasGenerator aliasGenerator = new AliasGenerator();
        AST selectFromAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT_FROM);
        AST fromAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.FROM);
        
        for (SubPlan subPlan : this.pathPlan.getSubPlans().values()) {
            Map<JoinNode, AST> fromElementASTMap = new HashMap<>(
                    ReferenceEqualityComparator.<JoinNode>getInstance(),
                    ReferenceEqualityComparator.<AST>getInstance());
            AST startFromElementAst = this.startFromElementASTMap.get(subPlan.getAlias());
            fromElementASTMap.put(subPlan.getJoinNode(), startFromElementAst);
            if (!subPlan.getJoinNode().getChildNodes().isEmpty()) {
                for (JoinNode joinNode : subPlan.getJoinNode().getChildNodes().values()) {
                    HqlASTHelper.addJoinAST(
                            fromAst, 
                            startFromElementAst, 
                            joinNode, 
                            aliasGenerator, 
                            true, 
                            false, 
                            false,
                            fromElementASTMap);
                }
            }
            List<OrderNode> preOrderNodes = subPlan.getPreOrderNodes();
            List<OrderNode> postOrderNodes = subPlan.getPostOrderNodes();
            if (!preOrderNodes.isEmpty() || !postOrderNodes.isEmpty()) {
                AST orderAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.ORDER);
                if (orderAst == null) {
                    orderAst = HqlASTHelper.createAST(HqlTokenTypes.ORDER, "order");
                    ast.addChild(orderAst);
                } else {
                    HqlASTHelper.removeChildOrdersInToppestQuery(orderAst, preOrderNodes, fromElementASTMap);
                    HqlASTHelper.removeChildOrdersInToppestQuery(orderAst, postOrderNodes, fromElementASTMap);
                }
                for (int i = preOrderNodes.size() - 1; i >= 0; i--) {
                    OrderNode preOrderNode = preOrderNodes.get(i);
                    JoinNode joinNode = preOrderNode.getParentNode();
                    AST fromElementAST = fromElementASTMap.get(joinNode);
                    String alias = HqlASTHelper.getAlias(fromElementAST);
                    if (alias == null) {
                        alias = aliasGenerator.generateAlias();
                        HqlASTHelper.setAlias(fromElementAST, alias);
                    }
                    AST orderFieldAST = HqlASTHelper.createOrderFieldAST(alias, preOrderNode);
                    AST orderTypeAST = preOrderNode.isDesc() ? 
                            HqlASTHelper.createAST(HqlTokenTypes.DESCENDING, "desc") :
                            HqlASTHelper.createAST(HqlTokenTypes.ASCENDING, "asc");
                    orderTypeAST.setNextSibling(orderAst.getFirstChild());
                    orderFieldAST.setNextSibling(orderTypeAST);
                    orderAst.setFirstChild(orderFieldAST);
                }
                for (OrderNode postOrderNode : postOrderNodes) {
                    JoinNode joinNode = postOrderNode.getParentNode();
                    AST fromElementAST = fromElementASTMap.get(joinNode);
                    String alias = HqlASTHelper.getAlias(fromElementAST);
                    if (alias == null) {
                        alias = aliasGenerator.generateAlias();
                        HqlASTHelper.setAlias(fromElementAST, alias);
                    }
                    AST orderFieldAST = HqlASTHelper.createOrderFieldAST(alias, postOrderNode);
                    AST orderTypeAST = postOrderNode.isDesc() ? 
                            HqlASTHelper.createAST(HqlTokenTypes.DESCENDING, "desc") :
                            HqlASTHelper.createAST(HqlTokenTypes.ASCENDING, "asc");
                    orderAst.addChild(orderFieldAST);
                    orderAst.addChild(orderTypeAST);
                }
            }
        }
    }
    
    protected void onApplyRootJoinNodeForCount(AST ast, boolean distinct) {
        AST orderAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.ORDER);
        if (orderAst != null) {
            HqlASTHelper.removeOrderByInToppestQuery(ast);
            orderAst = null;
        }
        AST selectFromAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT_FROM);
        AST fromAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.FROM);
        AST selectAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.SELECT);
        AST defaultRangeAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.RANGE);
        AST defaultRangeAliasAst = HqlASTHelper.findFirstChildInToppestQuery(defaultRangeAst, HqlTokenTypes.ALIAS);
        AliasGenerator aliasGenerator = new AliasGenerator();
        if (defaultRangeAliasAst == null) {
            defaultRangeAliasAst = HqlASTHelper.createAST(HqlTokenTypes.ALIAS, aliasGenerator.generateAlias());
            defaultRangeAst.addChild(defaultRangeAliasAst);
        }
        AST countAst = 
                HqlASTHelper.createAST(
                        HqlTokenTypes.COUNT,
                        "count",
                        distinct && this.shouldUseDistinctCount() ? HqlASTHelper.createAST(HqlTokenTypes.DISTINCT, "distinct") : null,
                        HqlASTHelper.createAST(HqlTokenTypes.IDENT, defaultRangeAliasAst.getText()));
        if (selectAst == null) {
            selectAst = HqlASTHelper.createAST(HqlTokenTypes.SELECT, "select", countAst);
            selectFromAst.addChild(selectAst);
        } else {
            selectAst.setFirstChild(countAst);
        }
        
        for (SubPlan subPlan : this.pathPlan.getSubPlans().values()) {
            AST startFromElementAst = this.startFromElementASTMap.get(subPlan.getAlias());
            //Must add join ASTs before unset fetch, because whether the original join ASTs
            //is with fetches is very important to find out how to add new join ASTs.
            for (JoinNode joinNode : subPlan.getJoinNode().getChildNodes().values()) {
                HqlASTHelper.addJoinAST(
                        fromAst, 
                        startFromElementAst, 
                        joinNode, 
                        aliasGenerator, 
                        false,
                        true,
                        distinct,
                        null);
            }
        }
        
        //After join ASTs, unset fetch of all the joinAST
        for (AST fromElementAst = fromAst.getFirstChild();
                fromElementAst != null;
                fromElementAst = fromElementAst.getNextSibling()) {
            if (fromElementAst.getType() == HqlTokenTypes.JOIN) {
                HqlASTHelper.unsetFetch(fromElementAst);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void onProcessCountSqlAST(AST sqlAst, boolean distinct) {
        QueryNode queryNode = (QueryNode)sqlAst;
        Set<FromElement> usedFromElements = new HashSet<>(ReferenceEqualityComparator.getInstance());
        for (FromElement fromElement : (List<FromElement>)queryNode.getFromClause().getFromElements()) {
            if (fromElement.getClass() == FromElement.class && fromElement.getWithClauseFragment() == null) {
                if (distinct || fromElement.getQueryableCollection() == null) {
                    if (fromElement.getText().startsWith("left ")) {
                        continue;
                    } else if (fromElement.getRole() != null && 
                            fromElement.getQueryableCollection() == null &&
                            SettingsFactory.isDbSchemaStrict(this.factory.getProperties())) {
                        String role = fromElement.getRole();
                        int lastDotIndex = role.lastIndexOf('.');
                        String entityName = role.substring(0, lastDotIndex);
                        String propertyName = role.substring(lastDotIndex + 1);
                        EntityMetamodel entityMetamodel = this.factory.getEntityPersister(entityName).getEntityMetamodel();
                        int propertyIndex = entityMetamodel.getPropertyIndex(propertyName);
                        if (entityMetamodel.getPropertyTypes()[propertyIndex] instanceof ManyToOneType) {
                            boolean nullable = entityMetamodel.getPropertyNullability()[propertyIndex];
                            if (!nullable) {
                                continue;
                            }
                        }
                    }
                }
            }
            SqlASTHelper.addFromElementAndancestors(usedFromElements, fromElement);
        }
        SqlASTHelper.removeFromElementsExcept(queryNode, usedFromElements);
    }
    
    private void initialize(AST ast) {
        if (this.pathPlan == null) {
            if (ast.getType() != HqlTokenTypes.QUERY) {
                if (this.pathPlanKey != null) {
                    throw new QueryException(queryPathsCanNotBeApplyToNonQuery(this.hql));
                }
            } else {
                this.pathPlan = new PathPlanFactoryImpl(this.factory, ast).create(this.pathPlanKey);
                if (HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT) == null && 
                        this.pathPlan.containsNoFetchJoins()) {
                    throw new QueryException(
                            hqlMustContainsSelectCaluseWhenThereIsNoFetchJoinInQueryPaths(this.hql)
                    );
                }
                this.startFromElementASTMap = this.createStartFromElementASTMap(ast);
            }
        }
    }
    
    private ExceptionCreator getUnlimitedCountExceptionCreator(AST ast) {
        final String hql = this.hql;
        AST groupAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.GROUP);
        if (groupAst != null) {
            return new ExceptionCreator() {
                @Override
                public RuntimeException create() {
                    return new QueryException(
                            unlimitedCountIsUnsupportedBecauseOfGroupBy(hql)
                    );
                }
            };
        }
        AST selectFromAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT_FROM);
        AST selectAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.SELECT);
        if (selectAst != null) {
            AST selectionAst = selectAst.getFirstChild();
            if (selectionAst.getNextSibling() != null) {
                return new ExceptionCreator() {
                    @Override
                    public RuntimeException create() {
                        return new QueryException(
                                unlimitedCountIsUnsupportedBecauseOfTooManySelections(hql)
                        );
                    }
                };
            }
            boolean selectRootEntity = false;
            if (selectionAst.getType() == HqlTokenTypes.IDENT) {
                AST firstRangeAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.RANGE);
                String alias = HqlASTHelper.getAlias(firstRangeAst);
                selectRootEntity = selectionAst.getText().equals(alias);
            }
            if (!selectRootEntity) {
                return new ExceptionCreator() {
                    @Override
                    public RuntimeException create() {
                        return new QueryException(
                                unlimitedCountIsUnsupportedBecauseSelectionIsNotRootEntity(hql)
                        );
                    }
                };
            }
        } else {
            boolean metRange = false;
            AST fromAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.FROM);
            for (AST fromElementAst = fromAst.getFirstChild(); 
                    fromElementAst != null;
                    fromElementAst = fromElementAst.getNextSibling()) {
                if (fromElementAst.getType() == HqlTokenTypes.RANGE) {
                    if (metRange) {
                        return new ExceptionCreator() {
                            @Override
                            public RuntimeException create() {
                                return new QueryException(
                                        unlimitedCountIsUnsupportedBecauseOfTooManyRangeAndNoSelection(hql)
                                );
                            }
                        };
                    }
                    metRange = true;
                } else {
                    AST fetchAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, HqlTokenTypes.FETCH);
                    if (fetchAst == null) {
                        return new ExceptionCreator() {
                            @Override
                            public RuntimeException create() {
                                return new QueryException(
                                        unlimitedCountIsUnsupportedBecauseOfNonFetchJoinsAndNoSelection(hql)
                                );
                            }
                        };
                    }
                }
            }
        }
        return null;
    }
    
    private void preApplyScalarEagerness(AST ast) {
        if (!this.pathPlan.containsScalarEagerness()) {
            return;
        }
        this.returnedEntityColumns = new LinkedHashMap<>();
        AST selectAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT);
        if (selectAst != null) {
            for (SubPlan subPlan : this.pathPlan.getSubPlans().values()) {
                if (subPlan.getJoinNode().containsScalarEagerness()) {
                    String subPlanAlias = subPlan.getAlias();
                    int column = -1;
                    int index = 0;
                    for (AST childAst = selectAst.getFirstChild(); childAst != null; childAst = childAst.getNextSibling()) {
                        if (subPlanAlias == null) {
                            column = index;
                            break;
                        }
                        if (childAst.getType() == HqlTokenTypes.IDENT && 
                                (subPlanAlias == null || childAst.getText().equals(subPlanAlias))) {
                            column = index;
                            break;
                        }
                        index++;
                    }
                    if (column == -1) {
                        throw new QueryException(
                                noSelectedColumnAliasForQueryPath(subPlanAlias)
                        );
                    }
                    this.returnedEntityColumns.put(subPlanAlias, column);
                }
            }
        } else {
            AST fromAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.FROM);
            XOrderedMap<String, Integer> allEntityMap = new LinkedHashMap<>();
            int index = 0;
            for (AST fromElementAst = fromAst.getFirstChild(); fromElementAst != null; fromElementAst = fromElementAst.getNextSibling()) {
                int type = fromElementAst.getType();
                if (type == HqlTokenTypes.JOIN) {
                    if (HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, HqlTokenTypes.FETCH) != null) {
                        index++;
                        continue;
                    }
                }
                String fromElementAlias = HqlASTHelper.getAlias(fromElementAst);
                if (!allEntityMap.containsKey(fromElementAlias)) {
                    allEntityMap.put(fromElementAlias, index);
                }
                index++;
            }
            for (SubPlan subPlan : this.pathPlan.getSubPlans().values()) {
                if (subPlan.getJoinNode().containsScalarEagerness()) {
                    String subPlanAlias = subPlan.getAlias();
                    Integer column;
                    if (subPlanAlias != null) {
                        column = allEntityMap.get(subPlanAlias);
                        if (column == null) {
                            throw new QueryException(
                                    noSelectedColumnAliasForQueryPath(subPlanAlias)
                            );
                        }
                    } else {
                        column = allEntityMap.firstEntry().getValue();
                    }
                    this.returnedEntityColumns.put(subPlanAlias, column);
                }
            }
        }
    }
    
    private void applyScalarEagerness(List<?> results, SessionImplementor session) {
        if (this.returnedEntityColumns == null) {
            return;
        }
        ScalarEagerness scalarEagerness = new ScalarEagerness(session);
        for (Entry<String, Integer> entry : this.returnedEntityColumns.entrySet()) {
            for (Object result : results) {
                if (result == null) {
                    continue;
                }
                Object entity;
                if (result.getClass().isArray()) {
                    Object[] arr = (Object[])result;
                    entity = arr[entry.getValue()];
                } else if (result instanceof Tuple) {
                    Tuple tuple = (Tuple)result;
                    entity = tuple.get(entry.getValue());
                } else {
                    if (entry.getValue() != 0) {
                        throw new AssertionError();
                    }
                    entity = result;
                }
                JoinNode joinNode = this.pathPlan.getSubPlans().get(entry.getKey()).getJoinNode();
                scalarEagerness.prepareApply(entity, joinNode);
            }
        }
        scalarEagerness.apply();
    }

    private Map<String, AST> createStartFromElementASTMap(AST ast) {
        AST selectFromAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT_FROM);
        AST fromAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.FROM);
        AST defaultRangeAst = HqlASTHelper.findFirstChildInToppestQuery(fromAst, HqlTokenTypes.RANGE);
        
        Map<String, AST> map = new HashMap<>();
        map.put(null, defaultRangeAst);
        
        for (SubPlan subPlan : this.pathPlan.getSubPlans().values()) {
            String alias = subPlan.getAlias();          
            if (alias != null) {
                AST matchedFromElementAst = null;
                for (AST fromElementAst = fromAst.getFirstChild(); fromElementAst != null; fromElementAst = fromElementAst.getNextSibling()) {
                    AST feaAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, HqlTokenTypes.ALIAS);
                    String feaAlias = feaAst != null ? feaAst.getText() : null;
                    if (feaAlias.startsWith(Constants.NOT_SHARED_JOIN_ALIAS_PREFIX)) {
                        feaAlias = feaAlias.substring(Constants.NOT_SHARED_JOIN_ALIAS_PREFIX.length());
                    }
                    if (alias.equals(feaAlias)) {
                        matchedFromElementAst = fromElementAst;
                        break;
                    }
                }
                if (matchedFromElementAst == null) {
                    throw new IllegalArgumentException(
                            illegalSubPathAlias(
                                    alias, 
                                    Constants.NOT_SHARED_JOIN_ALIAS_PREFIX + alias,
                                    hql
                            )
                    );
                }
                map.put(alias, matchedFromElementAst);
            }
        }
        return map;
    }

    private static void logAST(AST ast, String message) {
        if (LOGGER.isDebugEnabled()) {
            ASTPrinter printer = new ASTPrinter(HqlTokenTypes.class);
            String astText = printer.showAsString(ast, message);
            LOGGER.debug(astText);
        }
    }

    private static class Enhancer extends ClassEnhancer {
        
        private static final Enhancer INSTANCE = getInstance(Enhancer.class);
        
        private int walkerVarInDoCompile;
    
        private Enhancer() {
            super(XQueryTranslatorImpl.class);
        }
        
        static Class<XQueryTranslatorImpl> getEhancedClass() {
            return INSTANCE.getResultClass();
        }
    
        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            Method method = methodSource.getMethod();
            if (method.getDeclaringClass() == QueryTranslatorImpl.class &&
                    method.getName().equals("parse") &&
                    method.getReturnType() == HqlParser.class) {
                InsnList instructions = methodSource.getInstructions();
                for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.ARETURN) {
                        InsnList tmpInstructions = new InsnList();
                        tmpInstructions.add(new InsnNode(Opcodes.DUP));
                        tmpInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        tmpInstructions.add(new InsnNode(Opcodes.SWAP));
                        tmpInstructions.add(
                                new MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL, 
                                        ASM.getInternalName(HqlParser.class), 
                                        "getAST", 
                                        "()" + ASM.getDescriptor(AST.class),
                                        false));
                        tmpInstructions.add(
                                new MethodInsnNode(
                                        Opcodes.INVOKESPECIAL, 
                                        this.getResultInternalName(), 
                                        "applyRootJoinNode", 
                                        "(" +
                                        ASM.getDescriptor(AST.class) +
                                        ")V",
                                        false));
                        instructions.insertBefore(abstractInsnNode, tmpInstructions);
                    }
                }
            } else if (method.getDeclaringClass() == QueryTranslatorImpl.class &&
                    method.getName().equals("doCompile") &&
                    Arrays.equals(method.getParameterTypes(), new Class[] { Map.class, boolean.class, String.class })) {
                InsnList instructions = methodSource.getInstructions();
                for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractInsnNode;
                        if (fieldInsnNode.name.equals("queryLoader") &&
                                fieldInsnNode.owner.equals(ASM.getInternalName(QueryTranslatorImpl.class))) {
                            
                            InsnList beforeInstructions = new InsnList();
                            beforeInstructions.add(new InsnNode(Opcodes.DUP));
                            beforeInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            beforeInstructions.add(new InsnNode(Opcodes.SWAP));
                            beforeInstructions.add(
                                    new FieldInsnNode(
                                            Opcodes.PUTFIELD, 
                                            ASM.getInternalName(XQueryTranslatorImpl.class), 
                                            "queryLoader", 
                                            ASM.getDescriptor(QueryLoader.class)));
                            instructions.insertBefore(fieldInsnNode, beforeInstructions);
                            
                            InsnList afterInstructions = new InsnList();
                            /*
                             * if (this.queryType == QUERY) {
                             */
                            LabelNode isNotQueryLabelNode = new LabelNode();
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(new FieldInsnNode(
                                    Opcodes.GETFIELD, 
                                    ASM.getInternalName(XQueryTranslatorImpl.class),
                                    "queryType",
                                    "I"));
                            afterInstructions.add(new LdcInsnNode(HqlTokenTypes.QUERY));
                            afterInstructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, isNotQueryLabelNode));
                            
                            /*
                             * this.compileForCount();
                             */
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            afterInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESPECIAL, 
                                            this.getResultInternalName(), 
                                            "compileForCount", 
                                            "(Ljava/lang/String;)V",
                                            false));
                            
                            /*
                             * this.compileForDistinctCount();
                             */
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            afterInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESPECIAL, 
                                            this.getResultInternalName(), 
                                            "compileForDistinctCount", 
                                            "(Ljava/lang/String;)V",
                                            false));
                            
                            /*
                             * if (this.shouldUseDistinctQuery() && this.factory.getDialect() instanceof DistinctLimitDialect) {
                             *      this.distinctQueryLoader = new DistinctLimitQueryLoader(this, this.factory, walker.getSelectClause());
                             * }
                             */
                            LabelNode endIfNode = new LabelNode(new Label());
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKEVIRTUAL, 
                                            this.getResultInternalName(),
                                            "shouldUseDistinctQuery",
                                            "()Z",
                                            false));
                            afterInstructions.add(new JumpInsnNode(Opcodes.IFEQ, endIfNode));
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(
                                    new FieldInsnNode(
                                            Opcodes.GETFIELD,
                                            ASM.getInternalName(XQueryTranslatorImpl.class),
                                            "factory",
                                            ASM.getDescriptor(SessionFactoryImplementor.class)));
                            afterInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKEINTERFACE,
                                            ASM.getInternalName(SessionFactoryImplementor.class),
                                            "getDialect",
                                            "()" + ASM.getDescriptor(Dialect.class),
                                            true));
                            afterInstructions.add(new TypeInsnNode(Opcodes.INSTANCEOF, ASM.getInternalName(DistinctLimitDialect.class)));
                            afterInstructions.add(new JumpInsnNode(Opcodes.IFEQ, endIfNode));
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(
                                    new TypeInsnNode(
                                            Opcodes.NEW, 
                                            ASM.getInternalName(DistinctLimitQueryLoader.class)));
                            afterInstructions.add(new InsnNode(Opcodes.DUP));
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            afterInstructions.add(
                                    new FieldInsnNode(
                                            Opcodes.GETFIELD,
                                            this.getResultInternalName(),
                                            "factory",
                                            ASM.getDescriptor(SessionFactoryImplementor.class)));
                            if (this.walkerVarInDoCompile == 0) {
                                throw new AssertionError();
                            }
                            afterInstructions.add(new VarInsnNode(Opcodes.ALOAD, this.walkerVarInDoCompile));
                            afterInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKEVIRTUAL,
                                            ASM.getInternalName(HqlSqlWalker.class),
                                            "getSelectClause",
                                            "()" + ASM.getDescriptor(SelectClause.class),
                                            false));
                            afterInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESPECIAL,
                                            ASM.getInternalName(DistinctLimitQueryLoader.class),
                                            "<init>",
                                            "(" +
                                            ASM.getDescriptor(XQueryTranslatorImpl.class) +
                                            ASM.getDescriptor(SessionFactoryImplementor.class) +
                                            ASM.getDescriptor(SelectClause.class) +
                                            ")V",
                                            false));
                            afterInstructions.add(
                                    new FieldInsnNode(
                                            Opcodes.PUTFIELD,
                                            this.getResultInternalName(),
                                            "distinctLimitQueryLoader",
                                            ASM.getDescriptor(DistinctLimitQueryLoader.class)));
                            afterInstructions.add(endIfNode);
                            
                            /*
                             * } //end if (this.queryType == QUERY)
                             */
                            afterInstructions.add(isNotQueryLabelNode);
                            
                            instructions.insert(abstractInsnNode, afterInstructions);
                        }
                    } else if (abstractInsnNode.getOpcode() == Opcodes.ASTORE &&
                            abstractInsnNode.getPrevious() instanceof MethodInsnNode) {
                        MethodInsnNode prevMethodInsnNode = (MethodInsnNode)abstractInsnNode.getPrevious();
                        if (prevMethodInsnNode.name.equals("analyze") &&
                                prevMethodInsnNode.owner.equals(ASM.getInternalName(QueryTranslatorImpl.class))) {
                            this.walkerVarInDoCompile = ((VarInsnNode)abstractInsnNode).var;
                        }
                    }
                }
            }
        }

        @Override
        protected Collection<MethodNode> getMoreMethodNodes(
                MethodSourceFactory methodSourceFactory) {
            List<MethodNode> methodNodes = new ArrayList<MethodNode>();
            methodNodes.add(this.createCompileForCount(false));
            methodNodes.add(this.createCompileForCount(true));
            methodNodes.add(this.createParseForCount(false, methodSourceFactory));
            methodNodes.add(this.createParseForCount(true, methodSourceFactory));
            return methodNodes;
        }
        
        private MethodNode createCompileForCount(boolean distinct) {
            
            /*
             * private void compileForCount(String collectionRole) {
             *      HqlSqlWalker walker = super.analyze(this.parseForCount(true), collectionRole);
             *      this.countSqlAst = walker.getAST();
             *      SqlGenerator sqlGenerator = new SqlGenerator(this.factory);
             *      sqlGenerator.statement(this.countSqlAst);
             *      this.countSql = sqlGenerator.getSQL();
             *      this.countLoader = new UnlimitedCountLoader(
             *          this,
             *          this.factory,
             *          waler.getSelectClause(),
             *          false
             *      );
             * }
             * 
             * private void compileForDistinctCount(String role) {
             *      if (!this.shouldUseDistinctQuery()) {
             *          return;
             *      }
             *      HqlSqlWalker walker = super.analyze(this.parseForDistinctCount(true), role);
             *      this.distinctCountSqlAst = walker.getAST();
             *      SqlGenerator sqlGenerator = new SqlGenerator(this.factory);
             *      sqlGenerator.statement(this.distinctCountSqlAst);
             *      this.distinctCountSql = sqlGenerator.getSQL();
             *      this.distinctCountLoader = new UnlimitedCountLoader(
             *          this,
             *          this.factory,
             *          walker.getSelectClause(),
             *          true
             *      );
             * }
             */
            MethodNode methodNode = createMethodNode(
                    Opcodes.ACC_PRIVATE, 
                    distinct ? "compileForDistinctCount" : "compileForCount", 
                    "(Ljava/lang/String;)V", 
                    null);
            
            InsnList instructions = new InsnList();
            final int walkerIndex = 2;
            final int sqlAstIndex = walkerIndex + 1;
            final int sqlGeneratorIndex = sqlAstIndex + 1;
            
            if (distinct) {
                LabelNode useDistinctLabelNode = new LabelNode();
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                this.getResultInternalName(),
                                "shouldUseDistinctQuery",
                                "()Z",
                                false
                        )
                );
                instructions.add(new JumpInsnNode(Opcodes.IFNE, useDistinctLabelNode));
                instructions.add(new InsnNode(Opcodes.RETURN));
                instructions.add(useDistinctLabelNode);
            }
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new LdcInsnNode(true));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            this.getResultInternalName(),
                            distinct ? "parseForDistinctCount" : "parseForCount",
                            "(Z)" +
                            ASM.getDescriptor(HqlParser.class),
                            false));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            ASM.getInternalName(QueryTranslatorImpl.class),
                            "analyze",
                            "(" +
                            ASM.getDescriptor(HqlParser.class) +
                            "Ljava/lang/String;)" +
                            ASM.getDescriptor(HqlSqlWalker.class),
                            false));
            instructions.add(new VarInsnNode(Opcodes.ASTORE, walkerIndex));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, walkerIndex));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            ASM.getInternalName(HqlSqlWalker.class),
                            "getAST",
                            "()" + ASM.getDescriptor(AST.class),
                            false));
            instructions.add(new VarInsnNode(Opcodes.ASTORE, sqlAstIndex));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, sqlAstIndex));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            this.getResultInternalName(),
                            distinct ? "processDistinctCountSqlAst" : "processCountSqlAst",
                            "(" + ASM.getDescriptor(AST.class) + ")V",
                            false));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, sqlAstIndex));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.PUTFIELD, 
                            this.getResultInternalName(), 
                            distinct ? "distinctCountSqlAst" : "countSqlAst", 
                            ASM.getDescriptor(AST.class)));
            
            instructions.add(new TypeInsnNode(Opcodes.NEW, ASM.getInternalName(SqlGenerator.class)));
            instructions.add(new InsnNode(Opcodes.DUP));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETFIELD, 
                            this.getResultInternalName(), 
                            "factory", 
                            ASM.getDescriptor(SessionFactoryImplementor.class)));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            ASM.getInternalName(SqlGenerator.class),
                            "<init>",
                            "(" +
                            ASM.getDescriptor(SessionFactoryImplementor.class) +
                            ")V",
                            false));
            instructions.add(new VarInsnNode(Opcodes.ASTORE, sqlGeneratorIndex));
            
            instructions.add(new VarInsnNode(Opcodes.ALOAD, sqlGeneratorIndex));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETFIELD, 
                            this.getResultInternalName(), 
                            distinct ? "distinctCountSqlAst" : "countSqlAst", 
                            ASM.getDescriptor(AST.class)));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(SqlGenerator.class), 
                            "statement", 
                            "(" +
                            ASM.getDescriptor(AST.class) +
                            ")V",
                            false));
            
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, sqlGeneratorIndex));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(SqlGenerator.class), 
                            "getSQL", 
                            "()Ljava/lang/String;",
                            false));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.PUTFIELD, 
                            this.getResultInternalName(), 
                            distinct ? "distinctCountSql" : "countSql",
                            "Ljava/lang/String;"));
            
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new TypeInsnNode(Opcodes.NEW, ASM.getInternalName(UnlimitedCountLoader.class)));
            instructions.add(new InsnNode(Opcodes.DUP));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETFIELD, 
                            this.getResultInternalName(), 
                            "factory", 
                            ASM.getDescriptor(SessionFactoryImplementor.class)));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, walkerIndex));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(HqlSqlWalker.class), 
                            "getSelectClause", 
                            "()" + ASM.getDescriptor(SelectClause.class),
                            false));
            instructions.add(new InsnNode(distinct ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(UnlimitedCountLoader.class), 
                    "<init>", 
                    "(" +
                    ASM.getDescriptor(XQueryTranslatorImpl.class) +
                    ASM.getDescriptor(SessionFactoryImplementor.class) +
                    ASM.getDescriptor(SelectClause.class) +
                    "Z)V",
                    false));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.PUTFIELD,
                            this.getResultInternalName(),
                            distinct ? "distinctCountLoader" : "countLoader",
                            ASM.getDescriptor(UnlimitedCountLoader.class)));
            
            instructions.add(new InsnNode(Opcodes.RETURN));
            
            methodNode.instructions = instructions;
            return methodNode;
        }
        
        private MethodNode createParseForCount(boolean distinct, MethodSourceFactory methodSourceFactory) {
            MethodNode methodNode = createMethodNode(
                    Opcodes.ACC_PRIVATE, 
                    distinct ? "parseForDistinctCount" : "parseForCount", 
                    "(Z)" + ASM.getDescriptor(HqlParser.class), 
                    null);
            MethodSource parseSource = methodSourceFactory.getMethodSource(PARSE_METHOD);
            methodNode.tryCatchBlocks = cloneTryCatchBlocks(parseSource.getOldTryCatchBlocks());
            InsnList instructions = cloneInsnList(parseSource.getOldInstructions());
            for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                    abstractInsnNode != null;
                    abstractInsnNode = abstractInsnNode.getNext()) {
                if (abstractInsnNode.getOpcode() == Opcodes.ARETURN) {
                    InsnList tmpInstructions = new InsnList();
                    tmpInstructions.add(new InsnNode(Opcodes.DUP));
                    tmpInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    tmpInstructions.add(new InsnNode(Opcodes.SWAP));
                    tmpInstructions.add(
                            new MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASM.getInternalName(HqlParser.class), 
                                    "getAST", 
                                    "()" + ASM.getDescriptor(AST.class),
                                    false));
                    tmpInstructions.add(
                            new MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL, 
                                    this.getResultInternalName(), 
                                    distinct ? "applyRootJoinNodeForDistinctCount" : "applyRootJoinNodeForCount", 
                                    "(" +
                                    ASM.getDescriptor(AST.class) +
                                    ")V",
                                    false));
                    instructions.insertBefore(abstractInsnNode, tmpInstructions);
                }
            }
            methodNode.instructions = instructions;
            return methodNode;
        }
    }
    
    private static class PathPlanFactoryImpl extends AbstractHibernatePathPlanFactory {
        
        private Map<String, EntityPersister> entityPersisters;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public PathPlanFactoryImpl(SessionFactoryImplementor sfi, AST ast) {
            Map<String, Object> map = new HashMap<>();
            AST selectFromAst = HqlASTHelper.findFirstChildInToppestQuery(ast, HqlTokenTypes.SELECT_FROM);
            AST fromAst = HqlASTHelper.findFirstChildInToppestQuery(selectFromAst, HqlTokenTypes.FROM);
            boolean metFirstRange = false;
            for (AST fromElementAst = fromAst.getFirstChild(); 
                    fromElementAst != null; 
                    fromElementAst = fromElementAst.getNextSibling()) {
                if (fromElementAst.getType() == HqlTokenTypes.RANGE) {
                    AST aliasAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, HqlTokenTypes.ALIAS);
                    if (!metFirstRange || aliasAst != null) {
                        String alias = aliasAst != null ? aliasAst.getText() : null;
                        String entityName = HqlASTHelper.getComplexIdentifier(fromElementAst.getFirstChild());
                        EntityPersister entityPersister = sfi.getEntityPersister(entityName);
                        if (alias != null) {
                            map.put(alias, entityPersister);
                        }
                        if (!metFirstRange) {
                            metFirstRange = true;
                            map.put(null, entityPersister);
                        }
                    }
                }
                if (fromElementAst.getType() == HqlTokenTypes.JOIN) {
                    AST aliasAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, HqlTokenTypes.ALIAS);
                    if (aliasAst != null) {
                        AST dotAST = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, HqlTokenTypes.DOT);
                        if (dotAST == null) {
                            throw new AssertionError();
                        }
                        AST parentAliasAst = dotAST.getFirstChild();
                        AST associationNameAst = parentAliasAst.getNextSibling();
                        JoinSource joinSource = new JoinSource(parentAliasAst.getText(), associationNameAst.getText());
                        map.put(aliasAst.getText(), joinSource);
                    }
                }
            }
            while (true) {
                boolean continue_ = false;
                for (Entry<String, Object> e1 : map.entrySet()) {
                    if (e1.getValue() instanceof EntityPersister) {
                        EntityPersister parentEntityPersister = (EntityPersister)e1.getValue();
                        for (Entry<String, Object> e2 : map.entrySet()) {
                            if (e2.getValue() instanceof JoinSource) {
                                JoinSource joinSource = (JoinSource)e2.getValue();
                                if (joinSource.parentAlias.equals(e1.getKey())) {
                                    String entityName = ((AssociationType)parentEntityPersister.getPropertyType(joinSource.assocationName)).getAssociatedEntityName(sfi);
                                    EntityPersister entityPersister = sfi.getEntityPersister(entityName);
                                    e2.setValue(entityPersister);
                                    continue_ = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!continue_) {
                    break;
                }
            }
            this.entityPersisters = (Map)map;
        }

        @Override
        protected EntityPersister getEntityPersister(String alias) {
            return this.entityPersisters.get(alias);
        }
        
        private static class JoinSource {
            
            String parentAlias;
            
            String assocationName;

            public JoinSource(String parentAlias, String assocationName) {
                this.parentAlias = parentAlias;
                this.assocationName = assocationName;
            }
        }
    }
    
    private static class ScalarEagerness {
        
        private SessionImplementor  session;
        
        private Map<Class<?>, ClassMetadata> classMetadatas = new HashMap<>();
        
        private ScalarBatchLoadingExecutor scalarBatchLoadingExecutor = new ScalarBatchLoadingExecutor();
        
        ScalarEagerness(SessionImplementor  session) {
            this.session = session;
        }

        @SuppressWarnings("unchecked")
        void prepareApply(Object entity, JoinNode joinNode) {
            
            if (entity == null || !joinNode.containsScalarEagerness()) {
                return;
            }
            
            ClassMetadata classMetadata = this.getClassMetadata(entity.getClass());
            EntityPersister entityPersister = 
                    this
                    .session
                    .getEntityPersister(
                            classMetadata.getEntityName(), 
                            entity
                    );
            
            XOrderedSet<String> loadedScalarNames = joinNode.getLoadedScalarNames();
            String[] names = entityPersister.getPropertyNames();
            Type[] types = entityPersister.getPropertyTypes();
            boolean[] laziness = entityPersister.getPropertyLaziness();
            XOrderedSet<String> requiredPropertyNames = new LinkedHashSet<>((loadedScalarNames.size() * 4 + 2) / 3); 
            for (int i = names.length - 1; i >= 0; i--) {
                if (!types[i].isAssociationType()) {
                    if (laziness[i] && loadedScalarNames.contains(names[i])) {
                        requiredPropertyNames.add(names[i]);
                    }
                }
            }
            if (!requiredPropertyNames.isEmpty()) {
                this.parepareLoadScalars(entity, entityPersister, requiredPropertyNames);
            }
            
            for (int i = names.length - 1; i >= 0; i--) {
                if (types[i].isAssociationType()) {
                    JoinNode childJoinNode = joinNode.getChildNodes().get(names[i]);
                    if (childJoinNode != null && childJoinNode.isFetch()) {
                        Object value = entityPersister.getPropertyValue(entity, i);
                        if (value != null) {
                            if (value.getClass().isArray()) {
                                Object[] arr = (Object[])value;
                                for (Object childEntity : arr) {
                                    this.prepareApply(childEntity, childJoinNode);
                                }
                            } else if (value instanceof Collection<?>) {
                                Collection<Object> c = (Collection<Object>)value;
                                for (Object childEntity : c) {
                                    this.prepareApply(childEntity, childJoinNode);
                                }
                            } else if (value instanceof Map<?, ?>) {
                                Map<?, Object> m = (Map<?, Object>)value;
                                for (Object childEntity : m.values()) {
                                    this.prepareApply(childEntity, childJoinNode);
                                }
                            } else {
                                this.prepareApply(value, childJoinNode);
                            }
                        }
                    }
                }
            }
        }
        
        void apply() {
            this.scalarBatchLoadingExecutor.flush();
        }
        
        private void parepareLoadScalars(Object entity, EntityPersister entityPersister, Set<String> propertyNames) {
            FieldInterceptor fieldInterceptor = FieldInterceptionHelper.extractFieldInterceptor(entity);
            if (fieldInterceptor instanceof HibernateScalarLoader) {
                HibernateScalarLoader hibernateObjectModelScalarLoader =
                        (HibernateScalarLoader)fieldInterceptor;
                ObjectModel objectModel = hibernateObjectModelScalarLoader.getObjectModel();
                ModelClass modelClass = objectModel.getModelClass();
                int[] scalarPropertyIds = new int[propertyNames.size()];
                int index = 0;
                for (String propertyName : propertyNames) {
                    scalarPropertyIds[index++] = modelClass.getProperties().get(propertyName).getId();
                }
                this.scalarBatchLoadingExecutor.prepareLoad(objectModel, scalarPropertyIds);
            } else { 
                throw new IllegalProgramException(
                        scalarFetchCanOnlyBeAppliedOnJAPObjectModel(
                                entityPersister.getEntityName()
                        )
                );
            }
        }
        
        private ClassMetadata getClassMetadata(Class<?> clazz) {
            ClassMetadata classMetadata = this.classMetadatas.get(clazz);
            if (classMetadata == null) {
                if (clazz == Object.class) {
                    throw new AssertionError();
                }
                SessionFactoryImplementor factory = session.getFactory();
                classMetadata = factory.getClassMetadata(clazz);
                if (classMetadata == null) {
                    classMetadata = this.getClassMetadata(clazz.getSuperclass());
                }
                this.classMetadatas.put(clazz, classMetadata);
            }
            return classMetadata;
        }
    }
    
    static {
        
        try {
            /*
             * PARSE_METHOD must be initialized at first,
             * because Ehancer.<clinit> depends on it.
             */
            PARSE_METHOD = QueryTranslatorImpl.class.getDeclaredMethod("parse", boolean.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("No method \"parse(boolean)\"", ex);
        }

        Constructor<XQueryTranslatorImpl> constructor;
        try {
            constructor =
                    Enhancer.getEhancedClass().getDeclaredConstructor(
                            String.class, 
                            String.class,
                            PathPlanKey.class,
                            Map.class,
                            SessionFactoryImplementor.class,
                            EntityGraphQueryHint.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("No constructor", ex);
        }
        constructor.setAccessible(true);
        CONSTRUCTOR = constructor;
    }
    
    private interface ExceptionCreator {
        RuntimeException create();
    };
    
    @I18N
    private static native String queryPathsCanNotBeApplyToNonQuery(String hql);
        
    @I18N
    private static native String hqlMustContainsSelectCaluseWhenThereIsNoFetchJoinInQueryPaths(String hql);
        
    @I18N
    private static native String operationRequiresQuery(String operataion);
        
    @I18N
    private static native String illegalSubPathAlias(
                String alias,
                String notSharedAlias,
                String hql);
        
    @I18N
    private static native String hibernateLimitInMemoryForCollectionFetchIsNotEnabled(
                Class<DistinctLimitDialect> distinctLimitDialectType,
                Class<? extends DistinctLimitDialect> exampleDistinctLimitDialectType,
                String configurationPropertyName);
        
    @I18N
    private static native String scalarFetchCanOnlyBeAppliedOnJAPObjectModel(String entityName);
        
    @I18N
    private static native String unlimitedCountIsUnsupportedBecauseOfGroupBy(String hql);
        
    @I18N
    private static native String unlimitedCountIsUnsupportedBecauseOfTooManySelections(String hql);
        
    @I18N
    private static native String unlimitedCountIsUnsupportedBecauseSelectionIsNotRootEntity(String hql);
        
    @I18N
    private static native String unlimitedCountIsUnsupportedBecauseOfTooManyRangeAndNoSelection(String hql);
        
    @I18N
    private static native String unlimitedCountIsUnsupportedBecauseOfNonFetchJoinsAndNoSelection(String hql);
    
    @I18N
    private static native String noSelectedColumnAliasForQueryPath(String alias);
}
