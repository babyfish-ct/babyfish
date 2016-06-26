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

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.babyfish.persistence.criteria.Assignment;
import org.babyfish.persistence.criteria.XAbstractQuery;
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
public abstract class AbstractVisitor implements Visitor {

    @Override
    public void visitCriteriaQuery(XCriteriaQuery<?> query) {
        this.visit(query.getSelection());
        this.visitOnExpressions(query);
        this.visit(query.getRestriction());
        for (Expression<?> expression : query.getGroupList()) {
            this.visit(expression);
        }
        this.visit(query.getGroupRestriction());
        for (Order order : query.getOrderList()) {
            this.visit(order);
        }
    }

    @Override
    public void visitSubquery(XSubquery<?> query) {
        this.visit(query.getSelection());
        this.visitOnExpressions(query);
        this.visit(query.getRestriction());
        for (Expression<?> expression : query.getGroupList()) {
            this.visit(expression);
        }
        this.visit(query.getGroupRestriction());
    }

    @Override
    public void visitCriteriaUpdate(XCriteriaUpdate<?> update) {
        this.visitOnExpressions(update);
        for (Assignment assignment : update.getAssignments()) {
            this.visit(assignment);
        }
        this.visit(update.getRestriction());
    }

    @Override
    public void visitCriteriaDelete(XCriteriaDelete<?> delete) {
        this.visitOnExpressions(delete);
        this.visit(delete.getRestriction());
    }

    @Override
    public void visitOrder(Order order) {
        this.visit(order.getExpression());
    }

    @Override
    public void visitAssignment(Assignment assignment) {
        this.visit(assignment.getPath());
        this.visit(assignment.getExpression());
    }

    @Override
    public void visitCompoundSelection(CompoundSelection<?> compoundSelection) {
        for (Selection<?> selection : compoundSelection.getCompoundSelectionItems()) {
            this.visit(selection);
        }
    }

    @Override
    public void visitFunction(AbstractFunction<?> function) {
        for (Expression<?> expression : function.getArguments()) {
            this.visit(expression);
        }
    }

    @Override
    public void visitAggregation(Aggregation<?, ?> aggregation) {
        this.visit(aggregation.getOperand());
    }

    @Override
    public void visitConvertExpression(ConvertExpression<?, ?> convertExpression) {
        this.visit(convertExpression.getOperand());
    }

    @Override
    public void visitAsExpression(AsExpression<?> asExpression) {
        this.visit(asExpression.getOperand());
    }
    
    @Override
    public void visitPath(Path<?> path) {
        //Do nothing
    }
    
    @Override
    public void visitParameterExpression(ParameterExpression<?> parameterExpression) {
        //Do nothing
    }

    @Override
    public void visitLiteralExpression(LiteralExpression<?> literalExpression) {
        //Do nothing
    }

    @Override
    public void visitNullLiteralExpression(NullLiteralExpression<?> nullLiteralExpression) {
        //Do nothing
    }

    @Override
    public void visitConstantExpression(ConstantExpression<?> constantExpression) {
        //Do nothing
    }

    @Override
    public void visitBinaryArithmeticExpression(BinaryArithmeticExpression<?> binaryArithmeticExpression) {
        this.visit(binaryArithmeticExpression.getLeftOperand());
        this.visit(binaryArithmeticExpression.getRightOperand());
    }

    @Override
    public void visitUnaryArithmeticExpression(UnaryArithmeticExpression<?> unaryArithmeticExpression) {
        this.visit(unaryArithmeticExpression.getOperand());
    }

    @Override
    public void visitTrimExpression(TrimExpression trimExpression) {
        this.visit(trimExpression.getTrimCharacter());
        this.visit(trimExpression.getTrimSource());
    }

    @Override
    public void visitNullifExpression(NullifExpression<?> nullifExpression) {
        this.visit(nullifExpression.getPrimaryOperand());
        this.visit(nullifExpression.getSecondaryOperand());
    }

    @Override
    public void visitSubqueryComparisonModifierExpression(
            SubqueryComparisonModifierExpression<?> subqueryComparisonModifierExpression) {
        this.visit(subqueryComparisonModifierExpression.getSubquery());
    }

    @Override
    public void visitSizeExpression(SizeExpression<?> sizeExpression) {
        this.visit(sizeExpression.getCollection());
    }

    @Override
    public void visitSimpleCaseExpression(SimpleCaseExpression<?, ?> simpleCaseExpression) {
        this.visit(simpleCaseExpression.getExpression());
        for (SimpleCaseExpression.WhenClause<?, ?> whenClause : simpleCaseExpression.getWhenClauses()) {
            this.visit(whenClause.getResult());
        }
        this.visit(simpleCaseExpression.getOtherwiseResult());
    }

    @Override
    public void visitSearchedCaseExpression(SearchedCaseExpression<?> searchedCaseExpression) {
        for (SearchedCaseExpression.WhenClause<?> whenClause : searchedCaseExpression.getWhenClauses()) {
            this.visit(whenClause.getCondition());
            this.visit(whenClause.getResult());
        }
        this.visit(searchedCaseExpression.getOtherwiseResult());
    }

    @Override
    public void visitConcatExpression(ConcatExpression concatExpression) {
        for (Expression<String> value : concatExpression.getValues()) {
            this.visit(value);
        }
    }

    @Override
    public void visitPathTypeExpression(
            PathTypeExpression<?> pathTypeExpression) {
        this.visit(pathTypeExpression.getPath());
    }

    @Override
    public void visitCoalesceExpression(CoalesceExpression<?> coalesceExpression) {
        for (Expression<?> expression : coalesceExpression.getExpressions()) {
            this.visit(expression);
        }
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression<?, ?> mapEntryExpression) {
        this.visit(mapEntryExpression.getMapAttributeJoin());
    }

    @Override
    public void visitListIndexExpression(ListIndexExpression listIndexExpression) {
        this.visit(listIndexExpression.getListAttributeJoin());
    }

    @Override
    public void visitCompoundPredicate(CompoundPredicate compoundPredicate) {
        for (Expression<?> expression : compoundPredicate.getExpressions()) {
            this.visit(expression);
        }
    }

    @Override
    public void visitComparisonPredicate(ComparisonPredicate comparisonPredicate) {
        this.visit(comparisonPredicate.getLeftOperand());
        this.visit(comparisonPredicate.getRightOperand());
    }

    @Override
    public void visitBetweenPredicate(BetweenPredicate<?> betweenPredicate) {
        this.visit(betweenPredicate.getSource());
        this.visit(betweenPredicate.getLowerBound());
        this.visit(betweenPredicate.getUpperBound());
    }

    @Override
    public void visitLikePredicate(LikePredicate likePredicate) {
        this.visit(likePredicate.getSource());
        this.visit(likePredicate.getPattern());
        this.visit(likePredicate.getEscapeCharacter());
    }

    @Override
    public void visitInPredicate(InPredicate<?> inPredicate) {
        this.visit(inPredicate.getExpression());
        for (Expression<?> value : inPredicate.getValues()) {
            this.visit(value);
        }
    }

    @Override
    public void visitExistsPredicate(ExistsPredicate existsPredicate) {
        this.visit(existsPredicate.getSubquery());
    }

    @Override
    public void visitIsEmptyPredicate(IsEmptyPredicate<?> isEmptyPredicate) {
        this.visit(isEmptyPredicate.getCollection());
    }

    @Override
    public void visitIsMemberPredicate(IsMemberPredicate<?, ?> isMemberPredicate) {
        this.visit(isMemberPredicate.getElement());
        this.visit(isMemberPredicate.getCollection());
    }

    @Override
    public void visitIsTruePredicate(IsTruePredicate isTruePredicate) {
        this.visit(isTruePredicate.getOperand());
    }

    @Override
    public void visitNullnessPredicate(NullnessPredicate nullnessPredicate) {
        this.visit(nullnessPredicate.getOperand());
    }
    
    protected void visit(Object o) {
        if (o != null) {
            ((AbstractNode)o).accept(this);
        }
    }
    
    protected void visitOnExpressions(XAbstractQuery<?> abstractQuery) {
        for (Root<?> root : abstractQuery.getRoots()) {
            for (Join<?, ?> join : root.getJoins()) {
                this.visitOnExpressions(join);
            }
        }
    }
    
    protected void visitOnExpressions(XCriteriaUpdate<?> update) {
        for (Join<?, ?> join : update.getRoot().getJoins()) {
            this.visitOnExpressions(join);
        }
    }
    
    protected void visitOnExpressions(XCriteriaDelete<?> delete) {
        for (Join<?, ?> join : delete.getRoot().getJoins()) {
            this.visitOnExpressions(join);
        }
    }
    
    private void visitOnExpressions(Join<?, ?> join) {
        this.visit(join.getOn());
        for (Join<?, ?> subJoin : join.getJoins()) {
            this.visitOnExpressions(subJoin);
        }
    }
}
