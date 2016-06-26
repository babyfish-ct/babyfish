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
package org.babyfish.persistence.criteria.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.persistence.criteria.Assignment;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
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
import org.babyfish.persistence.criteria.expression.SearchedCaseExpression;
import org.babyfish.persistence.criteria.expression.SimpleCaseExpression;
import org.babyfish.persistence.criteria.expression.SizeExpression;
import org.babyfish.persistence.criteria.expression.SubqueryComparisonModifierExpression;
import org.babyfish.persistence.criteria.expression.TrimExpression;
import org.babyfish.persistence.criteria.expression.UnaryArithmeticExpression;

/**
 * @author Tao Chen
 */
public abstract class AbstractNode implements Serializable {

    private static final long serialVersionUID = 8716123152374040816L;
    
    private XCriteriaBuilder criteriaBuilder;
    
    private int freezeCount;
    
    private QueryContext frozenBy;
    
    protected AbstractNode(XCriteriaBuilder criteriaBuilder) {
        this.criteriaBuilder = Arguments.mustNotBeNull("criteriaBuilder", criteriaBuilder);
    }
    
    public XCriteriaBuilder getCriteriaBuilder() {
        return this.criteriaBuilder;
    }
    
    protected <T, S extends Selection<T>> S mustUnderSameCriteriaBuilder(
            String argumentName, S argumentValue) {
        return mustUnderSameCriteriaBuilder(
                this.criteriaBuilder, 
                argumentName, 
                argumentValue);
    }
    
    protected static <T, S extends Selection<T>> S mustUnderSameCriteriaBuilder(
            CriteriaBuilder criteriaBuilder,
            String argumentName, S argumentValue) {
        if (argumentValue != null) {
            if (!(argumentValue instanceof AbstractNode)) {
                Arguments.mustBeInstanceOfValue("argumentName", argumentValue, AbstractNode.class);
            }
            if (criteriaBuilder != ((AbstractNode)argumentValue).criteriaBuilder) {
                throw new IllegalArgumentException(
                        childNodeMustUnderTheSameCriteriaBuilder(
                                argumentName,
                                XCriteriaBuilder.class
                        )
                );
            }
        }
        return argumentValue;
    }
    
    protected static <T, S extends Selection<T>> S mustHaveExplicitDataType(
            String argumentName, S argumentValue) {
        return Arguments.mustNotBeInstanceOfAnyOfValue(
                argumentName, 
                argumentValue, 
                ParameterExpression.class,
                LiteralExpression.class
        );
    }
    
    protected static <T, S extends Selection<T>> S mustHaveExplicitDataTypeWhen(
            String whenCondition, String argumentName, S argumentValue) {
        return Arguments.mustNotBeInstanceOfAnyOfValueWhen(
                whenCondition,
                argumentName, 
                argumentValue, 
                ParameterExpression.class,
                LiteralExpression.class
        );
    }
    
    protected final void checkState() {
        if (this.frozenBy != null) {
            throw new IllegalStateException(theCurrentNodeIsFrozen(QueryContext.class));
        }
    }
    
    protected void onFrozen() {
        
    }
    
    protected void onUnfrozen() {
        
    }
    
    final void freeze(QueryContext queryContext) {
        Arguments.mustNotBeNull("queryContext", queryContext);
        if (this.frozenBy != queryContext) {
            if (this.frozenBy != null) {
                throw new IllegalStateException(freezeWhenCurrentNodeIsFrozenByOther(QueryContext.class));
            }
            this.frozenBy = queryContext;
        }
        this.freezeCount++;
        this.onFrozen();
    }
    
    final void unfreeze(QueryContext queryContext) {
        Arguments.mustNotBeNull("queryContext", queryContext);
        if (this.frozenBy != queryContext) {
            throw new IllegalStateException(unfreezeWhenCurrentNodeIsFrozenByOther(QueryContext.class));
        }
        this.onUnfrozen();
        if (--this.freezeCount == 0) {
            this.frozenBy = null;
        }
    }
    
    @Override
    public String toString() {
        ToStringVisitorImpl toStringVisitorImpl = new ToStringVisitorImpl();
        this.accept(toStringVisitorImpl);
        return toStringVisitorImpl.toString();
    }
    
    public abstract void accept(Visitor visitor);
    
    private static class ToStringVisitorImpl implements Visitor {
        
        private StringBuilder builder = new StringBuilder();
        
        private int tabCount;
        
        private boolean tabsRequired = true;
        
        private Map<FetchParent<?, ?>, String> fetchParentIds = 
                new HashMap<>(
                        ReferenceEqualityComparator.getInstance(),
                        (ReferenceEqualityComparator<String>)null);
        
        @Override
        public String toString() {
            return this.builder.toString();
        }
    
        @Override
        public void visitCriteriaQuery(XCriteriaQuery<?> query) {
            this
            .append("CriteriaQuery")
            .beginMap();
            
            this.append("select: ").append(query.getSelection()).appendLine(",");
            this.append("from: ").renderFetchParents(query.getRoots()).appendLine(",");
            this.append("where: ").append(query.getRestriction()).appendLine(",");
            this.append("groupBy: ").append(query.getGroupList()).appendLine(",");
            this.append("having: ").append(query.getGroupRestriction()).appendLine(",");
            this.append("orderBy: ").append(query.getOrderList()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitSubquery(XSubquery<?> query) {
            this
            .append("Subquery")
            .beginMap();
            
            this.append("select: ").append(query.getSelection()).appendLine(",");
            this.append("from: ").renderFetchParents(query.getRoots()).appendLine(",");
            this.append("where: ").append(query.getRestriction()).appendLine(",");
            this.append("groupBy: ").append(query.getGroupList()).appendLine(",");
            this.append("having: ").append(query.getGroupRestriction()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitCriteriaUpdate(XCriteriaUpdate<?> update) {
            this
            .append("CriteriaUpdate")
            .beginMap();
            
            this.append("update: ").renderFetchParent(update.getRoot()).appendLine(",");
            this.append("set: ").append(update.getAssignments()).appendLine(",");
            this.append("where: ").append(update.getRestriction()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitCriteriaDelete(XCriteriaDelete<?> delete) {
            this
            .append("CriteriaDelete")
            .beginMap();
            
            this.append("from: ").renderFetchParent(delete.getRoot()).appendLine(",");
            this.append("where: ").append(delete.getRestriction()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitOrder(Order order) {
            this
            .append("Order")
            .beginMap();
            
            this.append("expression: ").append(order.getExpression()).appendLine(",");
            this.append("ascending: ").append(order.isAscending()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitAssignment(Assignment assignment) {
            this
            .append("Assignment")
            .beginMap();
            
            this.append("path: ").append(assignment.getPath()).appendLine(",");
            this.append("expression: ").append(assignment.getExpression()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitPath(Path<?> path) {
            this.append("path(");
            this.renderPath(path);
            this.append(")");
        }
        
        @Override
        public void visitCompoundSelection(CompoundSelection<?> compoundSelection) {
            this
            .append("CompoundSelection")
            .beginMap();
            
            this.append("items: ").append(compoundSelection.getCompoundSelectionItems()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitFunction(AbstractFunction<?> function) {
            this
            .append("Function")
            .beginMap();
            
            this.append("functionName: ").append(function.getFunctionName()).appendLine(",");
            this.append("arguments: ").append(function.getArguments()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitAggregation(Aggregation<?, ?> aggregation) {
            this
            .append("Aggregation")
            .beginMap();
            
            this.append("type: ").append(aggregation.getClass().getSimpleName().toLowerCase()).appendLine(",");
            this.append("operand: ").append(aggregation.getOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitConvertExpression(ConvertExpression<?, ?> convertExpression) {
            this
            .append("ConvertExpression")
            .beginMap();
            
            this.append("type: ").append(convertExpression.getClass().getSimpleName().substring(2).toLowerCase()).appendLine(",");
            this.append("operand: ").append(convertExpression.getOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitAsExpression(AsExpression<?> asExpression) {
            this
            .append("AsExpression ")
            .beginMap();
            
            this.append("operand: ").append(asExpression.getOperand()).appendLine(",");
            this.append("javaType: ").append(asExpression.getJavaType()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitLiteralExpression(LiteralExpression<?> literalExpression) {
            this.append("literal(").append(literalExpression.getValue()).append(")");
        }
    
        @Override
        public void visitNullLiteralExpression(NullLiteralExpression<?> nullLiteralExpression) {
            this.append("null");
        }
    
        @Override
        public void visitConstantExpression(ConstantExpression<?> constantExpression) {
            this.append("const(").append(constantExpression.getValue()).append(")");
        }
    
        @Override
        public void visitBinaryArithmeticExpression(BinaryArithmeticExpression<?> binaryArithmeticExpression) {
            this
            .append("BinaryArithmeticExpression")
            .beginMap();
            
            this.append("operator: ").append(binaryArithmeticExpression.getOperator()).appendLine(",");
            this.append("leftOperand: ").append(binaryArithmeticExpression.getLeftOperand()).appendLine(",");
            this.append("leftOperand: ").append(binaryArithmeticExpression.getLeftOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitUnaryArithmeticExpression(UnaryArithmeticExpression<?> unaryArithmeticExpression) {
            this
            .append("UnaryArithmeticExpression")
            .beginMap();
            
            this.append("operator: ").append(unaryArithmeticExpression.getOperator()).appendLine(",");
            this.append("operand: ").append(unaryArithmeticExpression.getOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitTrimExpression(TrimExpression trimExpression) {
            this
            .append("TrimExpression")
            .beginMap();
            
            this.append("operator: ").append(trimExpression.getTrimSource()).appendLine(",");
            this.append("trimCharacter: ").append(trimExpression.getTrimCharacter()).appendLine(",");
            this.append("trimSepc: ").append(trimExpression.getTrimspec().name()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitNullifExpression(NullifExpression<?> nullifExpression) {
            this
            .append("NullifExpression")
            .beginMap();
            
            this.append("primaryOperand: ").append(nullifExpression.getPrimaryOperand()).appendLine(",");
            this.append("secondaryOperand: ").append(nullifExpression.getSecondaryOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitSubqueryComparisonModifierExpression(
                SubqueryComparisonModifierExpression<?> subqueryComparisonModifierExpression) {
            this
            .append("SubqueryComparisonModifierExpression")
            .beginMap();
            
            this.append("modifier: ").append(subqueryComparisonModifierExpression.getClass().getSimpleName().toLowerCase()).appendLine(",");
            this.append("subQuery: ").append(subqueryComparisonModifierExpression.getSubquery()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitSizeExpression(SizeExpression<?> sizeExpression) {
            this
            .append("SizeExpression")
            .beginMap();
            
            this.append("collection: ").append(sizeExpression.getCollection()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitSimpleCaseExpression(
                SimpleCaseExpression<?, ?> simpleCaseExpression) {
            this
            .append("SimpleCaseExpression")
            .beginMap();
            
            this.append("expression: ").append(simpleCaseExpression.getExpression()).appendLine(",");
            this.append("whenItems: ").beginList();
            boolean addComma = false;
            for (SimpleCaseExpression.WhenClause<?, ?> whenClause : simpleCaseExpression.getWhenClauses()) {
                if (addComma) {
                    this.appendLine(",");
                } else {
                    addComma = true;
                }
                this
                .beginMap()
                .append("condition: ")
                .append(whenClause.getCondition())
                .append(", result: ")
                .append(whenClause.getResult())
                .endMap();
            }
            this.appendLine().endList();
            this.appendLine(",");
            this.append("otherwise: ").append(simpleCaseExpression.getOtherwiseResult()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitSearchedCaseExpression(
                SearchedCaseExpression<?> searchedCaseExpression) {
            this
            .append("SimpleCaseExpression")
            .beginMap();
            
            this.append("whenItems: ").beginList();
            boolean addComma = true;
            for (SearchedCaseExpression.WhenClause<?> whenClause : searchedCaseExpression.getWhenClauses()) {
                if (addComma) {
                    this.appendLine(",");
                } else {
                    addComma = true;
                }
                this
                .beginMap()
                .append("condition: ")
                .append(whenClause.getCondition())
                .append(", result: ")
                .append(whenClause.getResult())
                .endMap();
            }
            this.appendLine().endList();
            this.appendLine(",");
            this.append("otherwise: ").append(searchedCaseExpression.getOtherwiseResult()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitConcatExpression(ConcatExpression concatExpression) {
            this
            .append("ConcatExpression")
            .beginMap();
            
            this.append("values: ").append(concatExpression.getValues()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitPathTypeExpression(
                PathTypeExpression<?> pathTypeExpression) {
            this
            .append("PathTypeExpression")
            .beginMap();
            
            this.append("path: ").append(pathTypeExpression.getPath()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitParameterExpression(
                ParameterExpression<?> parameterExpression) {
            this
            .append("parameter(")
            .append(parameterExpression.getName() != null ? parameterExpression.getName() : "?")
            .append(")");
        }
    
        @Override
        public void visitCoalesceExpression(
                CoalesceExpression<?> coalesceExpression) {
            this
            .append("CoalesceExpression")
            .beginMap();
            
            this.append("expressions: ").append(coalesceExpression.getExpressions()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitMapEntryExpression(
                MapEntryExpression<?, ?> mapEntryExpression) {
            this.append("entry(").append(mapEntryExpression.getMapAttributeJoin()).append(")");
        }
    
        @Override
        public void visitListIndexExpression(
                ListIndexExpression listIndexExpression) {
            this.append("index(").append(listIndexExpression.getListAttributeJoin()).append(")");
        }
    
        @Override
        public void visitCompoundPredicate(CompoundPredicate compoundPredicate) {
            this
            .append("CompoundPridicate")
            .beginMap();
            
            this.append("operator: ").append(compoundPredicate.getOperator()).appendLine(",");
            this.append("expressions: ").append(compoundPredicate.getExpressions()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitComparisonPredicate(ComparisonPredicate comparisonPredicate) {
            this
            .append("ComparisonPredicate")
            .beginMap();
            
            this.append("comparison: ").append(comparisonPredicate.getClass().getSimpleName().toLowerCase()).appendLine(",");
            this.append("leftOperand: ").append(comparisonPredicate.getLeftOperand()).appendLine(",");
            this.append("rightOperand: ").append(comparisonPredicate.getRightOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitBetweenPredicate(BetweenPredicate<?> betweenPredicate) {
            this
            .append(betweenPredicate.isNegated() ? "NotBetweenPredicate" : "BetweenPredicate")
            .beginMap();
            
            this.append("source: ").append(betweenPredicate.getSource()).appendLine(",");
            this.append("lowerBound: ").append(Nulls.toString(betweenPredicate.getLowerBound())).appendLine(",");
            this.append("upperBound: ").append(Nulls.toString(betweenPredicate.getUpperBound())).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitLikePredicate(LikePredicate likePredicate) {
            this
            .append(likePredicate.isNegated() ? "NotLikePredicate" : "LikePridicate")
            .beginMap();
            
            this.append("source: ").append(likePredicate.getSource()).appendLine(",");
            this.append("pattern: ").append(likePredicate.getSource()).appendLine(",");
            this.append("escapeCharacter: ").append(likePredicate.getEscapeCharacter()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitInPredicate(InPredicate<?> inPredicate) {
            this
            .append(inPredicate.isNegated() ? "NotInPredicate" : "InPrdicate")
            .beginMap();
            
            this.append("expression: ").append(inPredicate.getExpression()).appendLine(",");
            this.append("values: ").append(inPredicate.getValues()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitExistsPredicate(ExistsPredicate existsPredicate) {
            this
            .append(existsPredicate.isNegated() ? "NotExistsPredicate" : "ExistsPredicate")
            .beginMap();
            
            this.append("expression: ").append(existsPredicate.getSubquery()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitIsEmptyPredicate(IsEmptyPredicate<?> isEmptyPredicate) {
            this
            .append(isEmptyPredicate.isNegated() ? "IsNotEmptyPredicate" : "IsEmptyPredicate")
            .beginMap();
            
            this.append("collection: ").append(isEmptyPredicate.getCollection()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitIsMemberPredicate(
                IsMemberPredicate<?, ?> isMemberPredicate) {
            this
            .append(isMemberPredicate.isNegated() ? "IsNotMemberPredicate" : "IsMemberPredicate")
            .beginMap();
            
            this.append("element: ").append(isMemberPredicate.getElement()).appendLine(",");
            this.append("collection: ").append(isMemberPredicate.getCollection()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitIsTruePredicate(IsTruePredicate isTruePredicate) {
            this
            .append(isTruePredicate.isNegated() ? "IsFalsePredicate" : "IsTruePredicate")
            .beginMap();
            
            this.append("operand: ").append(isTruePredicate.getOperand()).appendLine();
            
            this.endMap();
        }
    
        @Override
        public void visitNullnessPredicate(NullnessPredicate nullnessPredicate) {
            this
            .append(nullnessPredicate.isNegated() ? "NotNullnessPredicate" : "NullnessPredicate")
            .beginMap();
            
            this.append("operand: ").append(nullnessPredicate.getOperand()).appendLine();
            
            this.endMap();
        }
        
        private ToStringVisitorImpl renderFetchParents(Collection<? extends FetchParent<?, ?>> fetchParents) {
            this.beginList();
            boolean addComma = false;
            for (FetchParent<?, ?> fetchParent : fetchParents) {
                if (addComma) {
                    this.appendLine(",");
                } else {
                    addComma = true;
                }
                this.renderFetchParent(fetchParent);
            }
            this.appendLine().endList();
            return this;
        }
        
        private ToStringVisitorImpl renderFetchParent(FetchParent<?, ?> fetchParent) {
            if (fetchParent instanceof From<?, ?>) {
                From<?, ?> from = (From<?, ?>)fetchParent;
                if (from instanceof Root<?>) {
                    this
                    .append("Root")
                    .beginMap();
                } else {
                    this
                    .append("Join")
                    .beginMap();
                }
                this.appendLine("joins: ");
                this.renderFetchParents(from.getJoins());
                if (!fetchParent.getFetches().isEmpty()) {
                    this.appendLine(",");
                } else {
                    this.appendLine();
                }
            } else {
                this
                .append("Fetch")
                .beginMap();
            }
            this.append("fetches: ");
            this.renderFetchParents(fetchParent.getFetches());
            this.appendLine();
            this.endMap();
            return this;
        }
        
        private ToStringVisitorImpl renderPath(Path<?> path) {
            if (path instanceof MapKeyPath<?>) {
                this.append("key(");
                this.renderPath(path.getParentPath());
                this.append(")");
            } else if (path instanceof FetchParent<?, ?>) {
                this.append(fetchParentId((FetchParent<?, ?>)path));
            } else {
                if (path.getParentPath() != null) {
                    this.renderPath(path.getParentPath()).append(".");
                }
                else if (path instanceof PluralAttributePath<?>){
                    this.append(((PluralAttributePath<?>)path).getAttribute().getName());
                } else {
                    this.append(((SingularAttributePath<?>)path).getAttribute().getName());
                }
            }
            return this;
        }
        
        private String fetchParentId(FetchParent<?, ?> fetchParent) {
            Map<FetchParent<?, ?>, String> fetchParentIds = this.fetchParentIds;
            String id = fetchParentIds.get(fetchParent);
            if (id == null) {
                id = "#" + fetchParentIds.size();
                fetchParentIds.put(fetchParent, id);
            }
            return id;
        }
        
        private ToStringVisitorImpl beginList() {
            this.appendLine("[");
            this.tabCount++;
            return this;
        }
        
        private ToStringVisitorImpl endList() {
            this.tabCount--;
            return this.append("]");
        }
        
        private ToStringVisitorImpl beginMap() {
            this.appendLine("{");
            this.tabCount++;
            return this;
        }
        
        private ToStringVisitorImpl endMap() {
            this.tabCount--;
            return this.append("}");
        }
        
        private ToStringVisitorImpl append(String str) {
            StringBuilder builder = this.builder;
            while (str != null && !str.isEmpty()) {
                int newLineIndex = str.indexOf('\n');
                String nextStr = null;
                if (newLineIndex != -1) {
                    nextStr = newLineIndex == -1 ? null : str.substring(newLineIndex + 1);
                    str = str.substring(0, newLineIndex); 
                }
                if (this.tabsRequired) {
                    for (int i = this.tabCount - 1; i >= 0; i--) {
                        builder.append('\t');
                    }
                    this.tabsRequired = false;
                }
                builder.append(str);
                if (newLineIndex != -1) {
                    builder.append('\n');
                }
                str = nextStr;
            }
            return this;
        }
        
        private ToStringVisitorImpl appendLine() {
            this.builder.append('\n');
            this.tabsRequired = true;
            return this;
        }
        
        private ToStringVisitorImpl appendLine(String str) {
            return this.append(str).appendLine();
        }
        
        private ToStringVisitorImpl append(Collection<?> c) {
            if (c.isEmpty()) {
                this.append("[]");
            } else {
                this.beginList();
                boolean addComma = false;
                for (Object o : c) {
                    if (addComma) {
                        this.appendLine(",");
                    } else {
                        addComma = true;
                    }
                    this.append(o);
                }
                this.appendLine().endList();
            }
            return this;
        }
        
        private ToStringVisitorImpl append(Object o) {
            if (o == null) {
                this.builder.append("null");
            } else if (o instanceof AbstractNode ){
                ((AbstractNode)o).accept(this);
            } else {
                this.builder.append(o.toString());
            }
            return this;
        }
    }

    @I18N
    private static native String childNodeMustUnderTheSameCriteriaBuilder(
                String argumentName, 
                Class<XCriteriaBuilder> xCriteriaBuilderType);
        
    @I18N
    private static native String theCurrentNodeIsFrozen(Class<QueryContext> queryContextType);

    @I18N
    private static native String freezeWhenCurrentNodeIsFrozenByOther(Class<QueryContext> queryContextType);

    @I18N
    private static native String unfreezeWhenCurrentNodeIsFrozenByOther(Class<QueryContext> queryContextType);
}
