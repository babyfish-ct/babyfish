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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.hibernate.ejb.HibernateXEntityManagerFactory;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.Assignment;
import org.babyfish.persistence.criteria.XAbstractQuery;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.persistence.criteria.XSubquery;
import org.babyfish.persistence.criteria.expression.Aggregation;
import org.babyfish.persistence.criteria.expression.AsExpression;
import org.babyfish.persistence.criteria.expression.BetweenPredicate;
import org.babyfish.persistence.criteria.expression.BinaryArithmeticExpression;
import org.babyfish.persistence.criteria.expression.CoalesceExpression;
import org.babyfish.persistence.criteria.expression.ComparisonPredicate;
import org.babyfish.persistence.criteria.expression.CompoundPredicate;
import org.babyfish.persistence.criteria.expression.ConcatExpression;
import org.babyfish.persistence.criteria.expression.ConstantExpression;
import org.babyfish.persistence.criteria.expression.ConvertExpression;
import org.babyfish.persistence.criteria.expression.ExistsPredicate;
import org.babyfish.persistence.criteria.expression.InPredicate;
import org.babyfish.persistence.criteria.expression.InPredicate.Partition;
import org.babyfish.persistence.criteria.expression.IsEmptyPredicate;
import org.babyfish.persistence.criteria.expression.IsMemberPredicate;
import org.babyfish.persistence.criteria.expression.IsTruePredicate;
import org.babyfish.persistence.criteria.expression.LikePredicate;
import org.babyfish.persistence.criteria.expression.ListIndexExpression;
import org.babyfish.persistence.criteria.expression.LiteralExpression;
import org.babyfish.persistence.criteria.expression.MapEntryExpression;
import org.babyfish.persistence.criteria.expression.NullLiteralExpression;
import org.babyfish.persistence.criteria.expression.NullifExpression;
import org.babyfish.persistence.criteria.expression.NullnessPredicate;
import org.babyfish.persistence.criteria.expression.PathTypeExpression;
import org.babyfish.persistence.criteria.expression.PriorityConstants;
import org.babyfish.persistence.criteria.expression.SearchedCaseExpression;
import org.babyfish.persistence.criteria.expression.SimpleCaseExpression;
import org.babyfish.persistence.criteria.expression.SizeExpression;
import org.babyfish.persistence.criteria.expression.SubqueryComparisonModifierExpression;
import org.babyfish.persistence.criteria.expression.TrimExpression;
import org.babyfish.persistence.criteria.expression.UnaryArithmeticExpression;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.AbstractFunction;
import org.babyfish.persistence.criteria.spi.AbstractNode;
import org.babyfish.persistence.criteria.spi.AbstractQueryTemplate;
import org.babyfish.persistence.criteria.spi.MapKeyPath;
import org.babyfish.persistence.criteria.spi.PathId;
import org.babyfish.persistence.criteria.spi.QueryContext;
import org.babyfish.persistence.criteria.spi.QueryContext.Entity;
import org.babyfish.persistence.criteria.spi.QueryContext.PathNode;
import org.babyfish.persistence.criteria.spi.Visitor;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public class QueryTemplateImpl<T> extends AbstractQueryTemplate<T> {
    
    public QueryTemplateImpl(XCommonAbstractCriteria commonAbstractCriteria) {
        super(commonAbstractCriteria);
    }
    
    @Override
    protected void init(XCommonAbstractCriteria commonAbstractCriteria) {
        try (QueryContext queryContext = new QueryContext(commonAbstractCriteria)) {
            VisitorImpl visitorImpl = new VisitorImpl(queryContext);
            visitorImpl.visit(commonAbstractCriteria);
            if (!visitorImpl.pathIdAllocator.isEmpty()) {
                throw new AssertionError();
            }
            this.init(visitorImpl.toJPQL(), visitorImpl.getLiteralParameters());
        }
    }
    
    @Override
    protected void setTupleTransfromer(XTypedQuery<?> query, CompoundSelection<Tuple> tupleSelection) {
        org.hibernate.Query hqlQuery = query.unwrap(org.hibernate.Query.class);
        hqlQuery.setResultTransformer(new TupleResultTransformer(tupleSelection));
    }

    protected static class VisitorImpl implements Visitor {
        
        protected QueryContext queryContext;
        
        protected StringBuilder jpqlBuilder = new StringBuilder(256);
        
        protected XOrderedMap<Object, LiteralParameter> literalParameters;
        
        protected int currentPriority;
        
        protected PathId.Allocator pathIdAllocator;
        
        public VisitorImpl(QueryContext queryContext) {
            this.queryContext = queryContext;
            this.pathIdAllocator = queryContext.secondaryPathIdAllocator();
        }
        
        public String toJPQL() {
            return this.jpqlBuilder.toString();
        }
        
        public Collection<LiteralParameter> getLiteralParameters() {
            if (this.literalParameters == null) {
                return MACollections.emptySet();
            }
            return MACollections.unmodifiable(this.literalParameters.values());
        }
    
        @Override
        public void visitCriteriaQuery(XCriteriaQuery<?> query) {
            /*
             * It is very important to keep the visit order as:
             * selection, restriction, grouping, groupRestriction, orders
             * 
             * QueryContext must visit the tree by the same order
             * 
             * because the PathId.getAppearancePosition() is very important.
             */
            this.visitAbstractQuery(query);
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            
            this.pathIdAllocator.push(PathId.Allocator.ORDER_LIST);
            try {
                List<Order> orderList = query.getOrderList();
                if (!orderList.isEmpty()) {
                    jpqlBuilder.append(" order by ");
                    boolean addComma = false;
                    for (Order order : orderList) {
                        if (addComma) {
                            jpqlBuilder.append(", ");
                        } else {
                            addComma = true;
                        }
                        this.visit(order);
                    }
                }
            } finally {
                this.pathIdAllocator.pop();
            }
        }
    
        @Override
        public void visitSubquery(XSubquery<?> query) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder.append('(');
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                this.visitAbstractQuery(query);
            } finally {
                this.currentPriority = oldPriority;
            }
            jpqlBuilder.append(')');
        }
    
        @Override
        public void visitCriteriaUpdate(XCriteriaUpdate<?> update) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            
            jpqlBuilder.append("update ");
            this.pathIdAllocator.push(PathId.Allocator.ON_TREE);
            try {
                Entity entity = this.queryContext.getRootEntities(update).get(0);
                jpqlBuilder
                .append(entity.getManagedType().getJavaType().getName())
                .append(' ')
                .append(entity.getRenderAlias());
                this.renderJoin(entity);
            } finally {
                this.pathIdAllocator.pop();
            }
            
            jpqlBuilder.append(" set ");
            this.pathIdAllocator.push(PathId.Allocator.ASSIGNMENT_LIST);
            try {
                boolean addComma = false;
                for (Assignment assignment : update.getAssignments()) {
                    if (addComma) {
                        jpqlBuilder.append(", ");
                    } else {
                        addComma = true;
                    }
                    this.visit(assignment);
                }
            } finally {
                this.pathIdAllocator.pop();
            }
            
            this.pathIdAllocator.push(PathId.Allocator.RESTRICTION);
            try {
                Predicate restriction = update.getRestriction();
                if (restriction != null) {
                    jpqlBuilder.append(" where ");
                    this.visit(restriction);
                }
            } finally {
                this.pathIdAllocator.pop();
            }
        }

        @Override
        public void visitCriteriaDelete(XCriteriaDelete<?> delete) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            
            jpqlBuilder.append("delete from ");
            this.pathIdAllocator.push(PathId.Allocator.ON_TREE);
            try {
                Entity entity = this.queryContext.getRootEntities(delete).get(0);
                jpqlBuilder
                .append(entity.getManagedType().getJavaType().getName())
                .append(' ')
                .append(entity.getRenderAlias());
                this.renderJoin(entity);
            } finally {
                this.pathIdAllocator.pop();
            }
            
            this.pathIdAllocator.push(PathId.Allocator.RESTRICTION);
            try {
                Predicate restriction = delete.getRestriction();
                if (restriction != null) {
                    jpqlBuilder.append(" where ");
                    this.visit(restriction);
                }
            } finally {
                this.pathIdAllocator.pop();
            }
        }

        @Override
        public void visitOrder(Order order) {
            this.visit(order.getExpression());
            this.jpqlBuilder.append(order.isAscending() ? " asc" : " desc");
        }
    
        @Override
        public void visitAssignment(Assignment assignment) {
            this.visit(assignment.getPath());
            this.jpqlBuilder.append(" = ");
            this.visit(assignment.getExpression());
        }

        @Override
        public void visitPath(Path<?> path) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (path instanceof MapKeyPath<?>) {
                jpqlBuilder.append("key(");
                this.visit(path.getParentPath());
                jpqlBuilder.append(')');
            } else {
                PathId pathId = this.pathIdAllocator.allocate();
                if (pathId.getPath() != path) {
                    throw new AssertionError();
                }
                PathNode pathNode = this.queryContext.getPathNodes().get(pathId);
                if (pathNode == null) {
                    throw new AssertionError();
                }
                this.renderPathNode(pathNode);
            }
        }
    
        @Override
        public void visitCompoundSelection(CompoundSelection<?> compoundSelection) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            boolean constructor = 
                    !compoundSelection.getJavaType().isArray() &&
                    !Tuple.class.isAssignableFrom(compoundSelection.getJavaType());
            if (constructor) {
                jpqlBuilder
                .append("new ")
                .append(compoundSelection.getJavaType().getName())
                .append('(');
            }
            boolean addComma = false;
            for (Selection<?> selection : compoundSelection.getCompoundSelectionItems()) {
                if (addComma) {
                    jpqlBuilder.append(", ");
                } else {
                    addComma = true;
                }
                this.visit(selection);
            }
            if (constructor) {
                jpqlBuilder.append(')');
            }
        }
    
        @Override
        public void visitFunction(AbstractFunction<?> function) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            boolean addComma = false;
            jpqlBuilder
            .append(function.getFunctionName())
            .append('(');
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                for (Expression<?> expr : function.getArguments()) {
                    if (addComma) {
                        jpqlBuilder.append(", ");
                    } else {
                        addComma = true;
                    }
                    this.visit(expr);
                }
            } finally {
                this.currentPriority = oldPriority;
            }
            jpqlBuilder.append(')');
        }
    
        @Override
        public void visitAggregation(Aggregation<?, ?> aggregation) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (aggregation instanceof Aggregation.Count) {
                jpqlBuilder.append("count(");
                if (((Aggregation.Count)aggregation).isDistinct()) {
                    jpqlBuilder.append("distinct ");
                }
            } else if (aggregation instanceof Aggregation.Sum<?>) {
                jpqlBuilder.append("sum(");
            } else if (aggregation instanceof Aggregation.SumAsLong) {
                jpqlBuilder.append("sum(");
            } else if (aggregation instanceof Aggregation.SumAsDouble) {
                jpqlBuilder.append("sum(");
            } else if (aggregation instanceof Aggregation.Least<?>) {
                jpqlBuilder.append("min(");
            } else if (aggregation instanceof Aggregation.Greatest<?>) {
                jpqlBuilder.append("max(");
            } else if (aggregation instanceof Aggregation.Min<?>) {
                jpqlBuilder.append("min(");
            } else if (aggregation instanceof Aggregation.Max<?>) {
                jpqlBuilder.append("max(");
            } else if (aggregation instanceof Aggregation.Avg<?>) {
                jpqlBuilder.append("avg(");
            }else {
                throw new AssertionError("Internal bug");
            }
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                this.visit(aggregation.getOperand());
                jpqlBuilder.append(')');
            } finally {
                this.currentPriority = oldPriority;
            }
        }
    
        @Override
        public void visitConvertExpression(ConvertExpression<?, ?> convertExpression) {
            this.visit(convertExpression.getOperand());
        }
    
        @Override
        public void visitAsExpression(AsExpression<?> asExpression) {
            SessionFactoryImplementor factory = 
                    (SessionFactoryImplementor)
                    ((HibernateXEntityManagerFactory)asExpression.getCriteriaBuilder().getEntityManagerFactory())
                    .getSessionFactoryImplementor();
            Class<?> javaType = asExpression.getJavaType();
            Type hibernateType = factory.getTypeResolver().heuristicType(javaType.getName());
            if ( hibernateType == null ) {
                throw new IllegalArgumentException(
                        "Could not convert java type [" + javaType.getName() + "] to Hibernate type"
                );
            }
            String targetTypeName = hibernateType.getName();
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder.append("cast(");
            this.visit(asExpression.getOperand());
            jpqlBuilder
            .append(" as ")
            .append(targetTypeName)
            .append(")");
        }
    
        @Override
        public void visitLiteralExpression(LiteralExpression<?> literalExpression) {
            LiteralParameter parameter = this.getLiteralParameter(literalExpression);
            this.jpqlBuilder.append(':').append(parameter.getName());
        }
    
        @Override
        public void visitNullLiteralExpression(
                NullLiteralExpression<?> nullLiteralExpression) {
            this.jpqlBuilder.append("null");
        }
    
        @Override
        public void visitConstantExpression(ConstantExpression<?> constantExpression) {
            this.renderConstant(constantExpression.getValue());
        }
    
        @Override
        public void visitBinaryArithmeticExpression(
                BinaryArithmeticExpression<?> binaryArithmeticExpression) {
            this.visit(binaryArithmeticExpression.getLeftOperand());
            this
            .jpqlBuilder
            .append(' ')
            .append(binaryArithmeticExpression.getOperator())
            .append(' ');
            this.visit(binaryArithmeticExpression.getRightOperand());
        }
    
        @Override
        public void visitUnaryArithmeticExpression(
                UnaryArithmeticExpression<?> unaryArithmeticExpression) {
            this.jpqlBuilder.append(unaryArithmeticExpression.getOperator());
            this.visit(unaryArithmeticExpression.getOperand());
        }
    
        @Override
        public void visitTrimExpression(TrimExpression trimExpression) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder
            .append("trim(" );
            
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                jpqlBuilder
                .append(trimExpression.getTrimspec().name().toLowerCase())
                .append(' ');
                this.visit(trimExpression.getTrimCharacter());
                jpqlBuilder.append(" from ");
                this.visit(trimExpression.getTrimSource());
                jpqlBuilder.append(')');
            } finally {
                this.currentPriority = oldPriority;
            }
        }
    
        @Override
        public void visitNullifExpression(NullifExpression<?> nullifExpression) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder.append("nullif(");
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                this.visit(nullifExpression.getPrimaryOperand());
                jpqlBuilder.append(", ");
                this.visit(nullifExpression.getSecondaryOperand());
            } finally {
                this.currentPriority = oldPriority;
            }
            jpqlBuilder.append(')');
        }
    
        @Override
        public void visitSubqueryComparisonModifierExpression(
                SubqueryComparisonModifierExpression<?> subqueryComparisonModifierExpression) {
            this.jpqlBuilder.append(subqueryComparisonModifierExpression.getClass().getSimpleName().toLowerCase());
            this.visit(subqueryComparisonModifierExpression.getSubquery());
        }
    
        @Override
        public void visitSizeExpression(SizeExpression<?> sizeExpression) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder.append("size(");
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                this.visit(sizeExpression.getCollection());
                jpqlBuilder.append(')');
            } finally {
                this.currentPriority = oldPriority;
            }
        }
    
        @Override
        public void visitSimpleCaseExpression(
                SimpleCaseExpression<?, ?> simpleCaseExpression) {
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                StringBuilder jpqlBuilder = this.jpqlBuilder;
                jpqlBuilder.append("case "); //here must append a space for simple case
                this.visit(simpleCaseExpression.getExpression());
                for (SimpleCaseExpression.WhenClause<?, ?> whenClause : simpleCaseExpression.getWhenClauses()) {
                    jpqlBuilder
                    .append(" when ");
                    this.renderConstant(whenClause.getCondition());
                    jpqlBuilder
                    .append(" then ");
                    this.visit(whenClause.getResult());
                }
                jpqlBuilder.append(" else ");
                this.visit(simpleCaseExpression.getOtherwiseResult());
                jpqlBuilder.append(" end");
            } finally {
                this.currentPriority = oldPriority;
            }
        }
    
        @Override
        public void visitSearchedCaseExpression(
                SearchedCaseExpression<?> searchedCaseExpression) {
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                StringBuilder jpqlBuilder = this.jpqlBuilder;
                jpqlBuilder.append("case");  //here must not append a space for searched case
                for (SearchedCaseExpression.WhenClause<?> whenClause : searchedCaseExpression.getWhenClauses()) {
                    jpqlBuilder.append(" when ");
                    this.visit(whenClause.getCondition());
                    jpqlBuilder.append(" then ");
                    this.visit(whenClause.getResult());
                }
                jpqlBuilder.append(" else ");
                this.visit(searchedCaseExpression.getOtherwiseResult());
                jpqlBuilder.append(" end");
            } finally {
                this.currentPriority = oldPriority;
            }
        }
        
        @Override
        public void visitConcatExpression(ConcatExpression concatExpression) {
            List<Expression<String>> values = concatExpression.getValues();
            int size = values.size();
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            int oldPriority = this.currentPriority;
            this.currentPriority = PriorityConstants.LOWEST;
            try {
                if (size == 1) {
                    this.visit(values.get(0));
                } else {
                    for (int i = size - 2; i >= 0; i--) {
                        jpqlBuilder.append("concat(");
                    }
                    this.visit(values.get(0));
                    jpqlBuilder.append(", ");
                    this.visit(values.get(1));
                    jpqlBuilder.append(')');
                    for (int i = 2; i < size; i++) {
                        jpqlBuilder.append(", ");
                        this.visit(values.get(i));
                        jpqlBuilder.append(')');
                    }
                }
            } finally {
                this.currentPriority = oldPriority;
            }
        }
    
        @Override
        public void visitPathTypeExpression(PathTypeExpression<?> pathTypeExpression) {
            this.jpqlBuilder.append("type(");
            this.visit(pathTypeExpression.getPath());
            this.jpqlBuilder.append(')');
        }
    
        @Override
        public void visitParameterExpression(
                ParameterExpression<?> parameterExpression) {
            if (parameterExpression.getName() != null) {
                this.jpqlBuilder.append(':').append(parameterExpression.getName());
            } else {
                this.jpqlBuilder.append('?').append(parameterExpression.getPosition());
            }
        }
    
        @Override
        public void visitCoalesceExpression(CoalesceExpression<?> coalesceExpression) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (coalesceExpression.getExpressions().isEmpty()) {
                jpqlBuilder.append("null");
            } else {
                jpqlBuilder.append("coalesce(");
                int oldPriority = this.currentPriority;
                this.currentPriority = PriorityConstants.LOWEST;
                try {
                    boolean addComma = false;
                    for (Expression<?> expr : coalesceExpression.getExpressions()) {
                        if (addComma) {
                            jpqlBuilder.append(", ");
                        } else {
                            addComma = true;
                        }
                        this.visit(expr);
                    }
                } finally {
                    this.currentPriority = oldPriority;
                }
                
                jpqlBuilder.append(')');
            }
        }
    
        @Override
        public void visitMapEntryExpression(MapEntryExpression<?, ?> mapEntryExpression) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder.append("entry(");
            this.visit(mapEntryExpression.getMapAttributeJoin());
            jpqlBuilder.append(')');
        }

        @Override
        public void visitListIndexExpression(ListIndexExpression listIndexExpression) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            jpqlBuilder.append("index(");
            this.visit(listIndexExpression.getListAttributeJoin());
            jpqlBuilder.append(')');
        }

        @Override
        public void visitCompoundPredicate(CompoundPredicate compoundPredicate) {
            String op = compoundPredicate.getOperator().name().toLowerCase();
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            boolean addOp = false;
            for (Expression<?> expr : compoundPredicate.getExpressions()) {
                if (addOp) {
                    jpqlBuilder.append(' ').append(op).append(' ');
                } else {
                    addOp = true;
                }
                this.visit(expr);
            }
        }
    
        @Override
        public void visitComparisonPredicate(ComparisonPredicate comparisonPredicate) {
            String op;
            if (comparisonPredicate instanceof ComparisonPredicate.Equal) {
                op = "=";
            } else if (comparisonPredicate instanceof ComparisonPredicate.NotEqual) {
                op = "!=";
            } else if (comparisonPredicate instanceof ComparisonPredicate.LessThan) {
                op = "<";
            } else if (comparisonPredicate instanceof ComparisonPredicate.LessThanOrEqual) {
                op = "<=";
            } else if (comparisonPredicate instanceof ComparisonPredicate.GreaterThan) {
                op = ">";
            } else if (comparisonPredicate instanceof ComparisonPredicate.GreaterThanOrEqual) {
                op = ">=";
            } else {
                throw new AssertionError();
            }
            this.visit(comparisonPredicate.getLeftOperand());
            this.jpqlBuilder.append(' ').append(op).append(' ');
            this.visit(comparisonPredicate.getRightOperand());
        }
    
        @Override
        public void visitBetweenPredicate(BetweenPredicate<?> betweenPredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (betweenPredicate.getLowerBound() == null) {
                this.visit(betweenPredicate.getSource());
                if (betweenPredicate.isNegated()) {
                    jpqlBuilder.append(" > ");
                } else {
                    jpqlBuilder.append(" <= ");
                }
                this.visit(betweenPredicate.getUpperBound());
            } else if (betweenPredicate.getUpperBound() == null) {
                this.visit(betweenPredicate.getSource());
                if (betweenPredicate.isNegated()) {
                    jpqlBuilder.append(" < ");
                } else {
                    jpqlBuilder.append(" >= ");
                }
                this.visit(betweenPredicate.getLowerBound());
            } else {
                this.visit(betweenPredicate.getSource());
                jpqlBuilder.append(betweenPredicate.isNegated() ? " not between " : " between ");
                this.visit(betweenPredicate.getLowerBound());
                jpqlBuilder.append(" and ");
                this.visit(betweenPredicate.getUpperBound());
            }
        }
    
        @Override
        public void visitLikePredicate(LikePredicate likePredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            this.visit(likePredicate.getSource());
            if (likePredicate.isNegated()) {
                jpqlBuilder.append(" not");
            }
            jpqlBuilder.append(" like ");
            this.visit(likePredicate.getPattern());
            if (likePredicate.getEscapeCharacter() != null) {
                jpqlBuilder.append(" escape ");
                this.visit(likePredicate.getEscapeCharacter());
            }
        }
    
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void visitInPredicate(InPredicate<?> inPredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            List<Partition<?>> partitions = (List)inPredicate.getPartitions();
            String renderedExpression;
            /*
             * Disable the jpqlBuilder add visit the main expression
             * in another StringBuilder.
             * Because the main expression should not be rendered or
             * should be be rendered for several times but the path id
             * should only be allocated once.
             */
            this.jpqlBuilder = new StringBuilder();
            try {
                this.visit(inPredicate.getExpression());
                renderedExpression = this.jpqlBuilder.toString();
            } finally {
                this.jpqlBuilder = jpqlBuilder;
            }
            
            if (partitions.size() == 0) {
                jpqlBuilder.append(inPredicate.isNegated() ? "1 = 1" : "1 = 0");
            } else if (partitions.size() == 1 && partitions.get(0).getValues().size() == 1) {
                Expression<?> firstElementExpression = partitions.get(0).getValues().get(0);
                jpqlBuilder.append(renderedExpression);
                if (firstElementExpression instanceof Subquery<?>) {
                    if (inPredicate.isNegated()) {
                        jpqlBuilder.append(" not");
                    }
                    jpqlBuilder.append(" in");
                    this.visit(firstElementExpression);
                } else {
                    if (inPredicate.isNegated()) {
                        jpqlBuilder.append(" != ");
                    } else {
                        jpqlBuilder.append(" = ");
                    }
                    this.visit(firstElementExpression);
                }
            } else {
                boolean addOuterParentheses = partitions.size() > 1;
                if (addOuterParentheses) {
                    jpqlBuilder.append('(');
                }
                String op = inPredicate.isNegated() ? " and " : " or ";
                boolean addOp = false;
                for (Partition<?> partition : partitions) {
                    if (addOp) {
                        jpqlBuilder.append(op);
                    } else {
                        addOp = true;
                    }
                    jpqlBuilder.append(renderedExpression);
                    if (inPredicate.isNegated()) {
                        jpqlBuilder.append(" not");
                    }
                    List<Expression<?>> values = (List)partition.getValues();
                    jpqlBuilder.append(" in(");
                    if (!partition.isNeedExpand()) {
                        jpqlBuilder
                        .append(':')
                        .append(this.getLiteralParameter(partition).getName());
                    } else {
                        int oldPriority = this.currentPriority;
                        this.currentPriority = PriorityConstants.LOWEST;
                        try {
                            boolean addComma = false;
                            for (Expression<?> value : values) {
                                if (addComma) {
                                    jpqlBuilder.append(", ");
                                } else {
                                    addComma = true;
                                }
                                this.visit(value);
                            }
                        } finally {
                            this.currentPriority = oldPriority;
                        }
                    }
                    jpqlBuilder.append(')');
                }
                if (addOuterParentheses) {
                    jpqlBuilder.append(')');
                }
            }
        }
    
        @Override
        public void visitExistsPredicate(ExistsPredicate existsPredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (existsPredicate.isNegated()) {
                jpqlBuilder.append("not ");
            }
            jpqlBuilder.append("exists");
            this.visit(existsPredicate.getSubquery());
        }
    
        @Override
        public void visitIsEmptyPredicate(IsEmptyPredicate<?> isEmptyPredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            this.visit(isEmptyPredicate.getCollection());
            jpqlBuilder.append(" is ");
            if (isEmptyPredicate.isNegated()) {
                jpqlBuilder.append("not ");
            }
            jpqlBuilder.append("empty");
        }
    
        @Override
        public void visitIsMemberPredicate(IsMemberPredicate<?, ?> isMemberPredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            this.visit(isMemberPredicate.getElement());
            if (isMemberPredicate.isNegated()) {
                jpqlBuilder.append(" not");
            }
            jpqlBuilder.append(" member of ");
            this.visit(isMemberPredicate.getCollection());
        }
    
        @Override
        public void visitIsTruePredicate(IsTruePredicate isTruePredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            this.visit(isTruePredicate.getOperand());
            if (isTruePredicate.isNegated()) {
                jpqlBuilder.append(" = 0");
            } else {
                jpqlBuilder.append(" = 1");
            }
        }
    
        @Override
        public void visitNullnessPredicate(NullnessPredicate nullnessPredicate) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            this.visit(nullnessPredicate.getOperand());
            if (nullnessPredicate.isNegated()) {
                jpqlBuilder.append(" is not null");
            } else {
                jpqlBuilder.append(" is null");
            }
        }
        
        protected void visitAbstractQuery(XAbstractQuery<?> query) {
            final StringBuilder jpqlBuilder = this.jpqlBuilder;
            boolean addComma;
            
            /*
             * It is very important to keep the visit order as:
             * selection, restriction, grouping, groupRestriction, orders
             * 
             * QueryContext must visit the tree by the same order
             * 
             * because the PathId.getAppearancePosition() is very important.
             */
            jpqlBuilder.append("select ");
            if (query.isDistinct()) {
                jpqlBuilder.append("distinct ");
            }
            
            this.pathIdAllocator.push(PathId.Allocator.SELECTION);
            try {
                if (query.getSelection() != null) {
                    this.visit(query.getSelection());
                } else {
                    GraphTraveler<Entity> graphTraveler = new GraphTraveler<Entity>() {
                        
                        private boolean addComma;
                        
                        @Override
                        protected Iterator<Entity> getNeighborNodeIterator(Entity node) {
                            List<Entity> entities = node.getEntities();
                            return entities != null ? entities.iterator() : null;
                        }

                        @Override
                        protected void preTravelNeighborNodes(
                                GraphTravelContext<Entity> ctx,
                                GraphTravelAction<Entity> optionalGraphTravelAction) {
                            Entity entity = ctx.getNode();
                            if (entity.isUsed() && entity.isExplicit() && !entity.isFetch()) {
                                if (this.addComma) {
                                    jpqlBuilder.append(", ");
                                } else {
                                    this.addComma = true;
                                }
                                jpqlBuilder.append(entity.getRenderAlias());
                            }
                        }
                    };
                    for (Entity entity : this.queryContext.getRootEntities(query)) {
                        graphTraveler.depthFirstTravel(entity);
                    }
                }
            } finally {
                this.pathIdAllocator.pop();
            }
            
            this.pathIdAllocator.push(PathId.Allocator.ON_TREE);
            try {
                jpqlBuilder.append(" from ");
                addComma = false;
                for (Entity entity : this.queryContext.getRootEntities(query)) {
                    if (addComma) {
                        jpqlBuilder.append(", ");
                    } else {
                        addComma = true;
                    }
                    jpqlBuilder
                    .append(entity.getManagedType().getJavaType().getName())
                    .append(' ')
                    .append(entity.getRenderAlias());
                }
                for (Entity entity : this.queryContext.getRootEntities(query)) {
                    this.renderJoin(entity);
                }
            } finally {
                this.pathIdAllocator.pop();
            }
            
            this.pathIdAllocator.push(PathId.Allocator.RESTRICTION);
            try {
                Predicate restriction = query.getRestriction();
                if (restriction != null) {
                    jpqlBuilder.append(" where ");
                    this.visit(restriction);
                }
            } finally {
                this.pathIdAllocator.pop();
            }
            
            this.pathIdAllocator.push(PathId.Allocator.GROUP_LIST);
            try {
                List<Expression<?>> groupList = query.getGroupList();
                if (!groupList.isEmpty()) {
                    jpqlBuilder.append(" group by ");
                    addComma = false;
                    for (Expression<?> expr : groupList) {
                        if (addComma) {
                            jpqlBuilder.append(", ");
                        } else {
                            addComma = true;
                        }
                        this.visit(expr);
                    }
                }
            } finally {
                this.pathIdAllocator.pop();
            }
            
            this.pathIdAllocator.push(PathId.Allocator.GROUP_RESTRICTION);
            try {
            Predicate groupRestriction = query.getGroupRestriction();
                if (groupRestriction != null) {
                    jpqlBuilder.append(" having ");
                    this.visit(groupRestriction);
                } 
            } finally {
                this.pathIdAllocator.pop();
            }
        }
        
        protected void renderJoin(Entity entity) {
            if (!entity.isUsed()) {
                return;
            }
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (entity.getAttribute() != null) {
                jpqlBuilder
                .append(' ')
                .append(entity.getJoinType().name().toLowerCase())
                .append(" join ");
                if (entity.isFetch()) {
                    jpqlBuilder.append("fetch ");
                }
                jpqlBuilder
                .append(entity.getParent().getRenderAlias())
                .append('.')
                .append(entity.getAttribute().getName())
                .append(' ')
                .append(entity.getRenderAlias());
                
                Predicate on = entity.getOn();
                if (on != null) {
                    jpqlBuilder.append(" on ");
                    this.visit(entity.getOn());
                }
            }
            for (Entity childEntity : entity.getEntities()) {
                this.renderJoin(childEntity);
            }
        }
        
        protected void renderPathNode(PathNode pathNode) {
            Class<?> treatAsType = pathNode.getTreatAsType();
            if (treatAsType != null) {
                StringBuilder jpqlBuilder = this.jpqlBuilder;
                jpqlBuilder.append("treat(");
                this.renderNonTreatedPathNode(pathNode);
                jpqlBuilder.append(" as ");
                jpqlBuilder.append(treatAsType.getName());
                jpqlBuilder.append(')');
            } else {
                this.renderNonTreatedPathNode(pathNode);
            }
        }
        
        protected void renderNonTreatedPathNode(PathNode pathNode) {
            if (pathNode.getEntity() != null) {
                this.jpqlBuilder.append(pathNode.getEntity().getRenderAlias());
            } else {
                this.renderPathNode(pathNode.getParent());
                this
                .jpqlBuilder
                .append('.')
                .append(pathNode.getAttribute().getName());
            }
        }
        
        protected void renderConstant(Object constant) {
            StringBuilder jpqlBuilder = this.jpqlBuilder;
            if (constant instanceof String) {
                String str = (String)constant;
                constant = str.replaceAll("'", "''");
                jpqlBuilder.append('\'').append(constant).append('\'');
            } else if (constant instanceof Character) {
                char ch = (Character)constant;
                if (ch == '\'') {
                    jpqlBuilder.append("''''");
                } else {
                    jpqlBuilder.append('\'').append(constant).append('\'');
                }
            } else {
                jpqlBuilder.append(constant);
                if (constant instanceof Long) {
                    jpqlBuilder.append('L');
                }
                if (constant instanceof Float) {
                    jpqlBuilder.append('F');
                }
                if (constant instanceof Double) {
                    jpqlBuilder.append('D');
                }
            }
        }
        
        protected LiteralParameter getLiteralParameter(Object object) {
            XOrderedMap<Object, LiteralParameter> literalParameters = this.literalParameters;
            if (literalParameters == null) {
                this.literalParameters = literalParameters = new LinkedHashMap<>();
            }
            LiteralParameter literalParameter = literalParameters.get(object);
            if (literalParameter == null) {
                if (object instanceof LiteralExpression<?>) {
                    literalParameter = new LiteralParameter(literalParameters.size(), 
                            ((LiteralExpression<?>)object).getValue());
                } else if (object instanceof Partition<?>) {
                    Collection<Object> c = new ArrayList<>();
                    for (Expression<?> value : ((Partition<?>)object).getValues()) {
                        if (value instanceof LiteralExpression<?>) {
                            c.add(((LiteralExpression<?>)value).getValue());
                        } else {
                            c.add(((ConstantExpression<?>)value).getValue());
                        }
                    }
                    literalParameter = new LiteralParameter(literalParameters.size(), c);
                } else {
                    Arguments.mustBeInstanceOfAnyOfValue("object", object, LiteralExpression.class, Partition.class);
                }
                literalParameters.put(object, literalParameter);
            }
            return literalParameter;
        }
        
        protected void visit(Object o) {
            if (o != null) {
                Object parent = this.pathIdAllocator.peek();
                this.pathIdAllocator.push(o);
                try {
                    if (o instanceof AbstractExpression<?>) {
                        AbstractExpression<?> abstractExpression = (AbstractExpression<?>)o;
                        int priority = abstractExpression.getPriority();
                        if (priority >= this.currentPriority) {
                            int oldPriority = this.currentPriority;
                            this.currentPriority = priority;
                            try {
                                abstractExpression.accept(this);
                            } finally {
                                this.currentPriority = oldPriority;
                            }
                        } else {
                            int oldPriority = this.currentPriority;
                            this.currentPriority = priority;
                            try {
                                this.jpqlBuilder.append('(');
                                abstractExpression.accept(this);
                                this.jpqlBuilder.append(')');
                            } finally {
                                this.currentPriority = oldPriority;
                            }
                        }
                    } else {
                        ((AbstractNode)o).accept(this);
                    }
                    if (o instanceof TupleElement  && !(o instanceof CompoundSelection)) {
                        String alias = ((TupleElement<?>)o).getAlias();
                        if (!Nulls.isNullOrEmpty(alias) && !(o instanceof From<?, ?>)) {
                            if (parent == PathId.Allocator.SELECTION || parent instanceof CompoundSelection<?>) {
                                this.jpqlBuilder.append(" as ").append(alias);
                            }
                        }
                    }
                } finally {
                    this.pathIdAllocator.pop();
                }
            }
        } 
    }
    
    private static class TupleResultTransformer implements ResultTransformer {
        
        private static final long serialVersionUID = -2248010661387946493L;
        
        private CompoundSelection<Tuple> tupleSelection;

        public TupleResultTransformer(CompoundSelection<Tuple> tupleSelection) {
            this.tupleSelection = tupleSelection;
        }

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            return new TupleImpl(this.tupleSelection, tuple);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public List transformList(List collection) {
            return collection;
        }
    }
    
    private static class TupleImpl implements Tuple, Serializable {
        
        private static final long serialVersionUID = 9017189381422990789L;
        
        private Object[] values;
        
        private String[] aliases;
        
        private String[] nonNullAliases;
        
        private int size;
        
        private transient TupleElement<?>[] tupleElements;
        
        public TupleImpl(CompoundSelection<Tuple> tupleSelection, Object[] values) {
            this.size = tupleSelection.getCompoundSelectionItems().size();
            TupleElement<?>[] tupleElements = tupleSelection.getCompoundSelectionItems().toArray(new TupleElement[this.size]);
            String[] aliases = new String[this.size];
            Collection<String> nonNullList = new ArrayList<>(this.size);
            for (int i = this.size - 1; i >= 0; i--) {
                String alias = tupleElements[i].getAlias();
                aliases[i] = alias; 
                if (alias != null) {
                    nonNullList.add(alias);
                }
            }
            this.aliases = aliases;
            this.nonNullAliases = nonNullList.toArray(new String[nonNullList.size()]);
            this.tupleElements = tupleElements;
            this.values = values;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            int size = this.size;
            TupleElement<?>[] tupleElements = this.tupleElements;
            if (tupleElements != null) {
                for (int i = 0; i < size; i++) {
                    if (tupleElements[i] == tupleElement) {
                        return (X)this.values[i];
                    }
                }
            }
            String alias = tupleElement.getAlias();
            if (alias != null) {
                String[] aliases = this.aliases;
                for (int i = 0; i < size; i++) {
                    if (alias.equals(aliases[i])) {
                        return (X)this.values[i];
                    }
                }
            }
            throw new IllegalArgumentException(missTupleElementOfThisIsDeserialized());
        }

        @Override
        public Object get(String alias) {
            if (alias != null) {
                int size = this.size;
                String[] aliases = this.aliases;
                for (int i = 0; i < size; i++) {
                    if (alias.equals(aliases[i])) {
                        return this.values[i];
                    }
                }
                Arguments.mustBeAnyOfValue("alias", alias, this.nonNullAliases);
            }
            Arguments.mustNotBeNull("alias", alias);
            throw new AssertionError(/* impossible */);
        }

        @Override
        public Object get(int index) {
            try {
                return this.values[index];
            } catch (ArrayIndexOutOfBoundsException ex) {
                Arguments.indexMustBetweenValue("index", index, 0, true, this.values.length, false);
                throw new AssertionError(/* impossible */ex);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <X> X get(String alias, Class<X> type) {
            return (X)this.get(alias);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <X> X get(int index, Class<X> type) {
            return (X)this.get(index);
        }

        @Override
        public List<TupleElement<?>> getElements() {
            if (this.tupleElements == null) {
                throw new IllegalStateException(thisIsDeserialized());
            }
            return MACollections.wrap(this.tupleElements);
        }

        @Override
        public Object[] toArray() {
            return this.values;
        }
        
        @I18N
        private static native String missTupleElementOfThisIsDeserialized();
            
        @I18N
        private static native String thisIsDeserialized();
    }
}
