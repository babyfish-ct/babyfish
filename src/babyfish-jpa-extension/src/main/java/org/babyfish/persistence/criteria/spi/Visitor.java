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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;

import org.babyfish.persistence.criteria.Assignment;
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
public interface Visitor {

    void visitCriteriaQuery(XCriteriaQuery<?> query);
    
    void visitSubquery(XSubquery<?> query);
    
    void visitCriteriaUpdate(XCriteriaUpdate<?> update);
    
    void visitCriteriaDelete(XCriteriaDelete<?> delete);
    
    void visitOrder(Order order);
    
    void visitAssignment(Assignment assignment);
    
    void visitPath(Path<?> path);
    
    void visitCompoundSelection(CompoundSelection<?> compoundSelection);
    
    void visitFunction(AbstractFunction<?> function);
    
    void visitAggregation(Aggregation<?, ?> aggregation);
    
    void visitConvertExpression(ConvertExpression<?, ?> convertExpression);
    
    void visitAsExpression(AsExpression<?> asExpression);
    
    void visitLiteralExpression(LiteralExpression<?> literalExpression);
    
    void visitNullLiteralExpression(NullLiteralExpression<?> nullLiteralExpression);
    
    void visitConstantExpression(ConstantExpression<?> constantExpression);
    
    void visitBinaryArithmeticExpression(BinaryArithmeticExpression<?> binaryArithmeticExpression);
    
    void visitUnaryArithmeticExpression(UnaryArithmeticExpression<?> unaryArithmeticExpression);
    
    void visitTrimExpression(TrimExpression trimExpression);
    
    void visitNullifExpression(NullifExpression<?> nullifExpression);
    
    void visitSubqueryComparisonModifierExpression(SubqueryComparisonModifierExpression<?> subqueryComparisonModifierExpression);
    
    void visitSizeExpression(SizeExpression<?> sizeExpression);
    
    void visitSimpleCaseExpression(SimpleCaseExpression<?, ?> simpleCaseExpression);
    
    void visitSearchedCaseExpression(SearchedCaseExpression<?> searchedCaseExpression);
    
    void visitConcatExpression(ConcatExpression concatExpression);
    
    void visitPathTypeExpression(PathTypeExpression<?> pathTypeExpression);
    
    void visitParameterExpression(ParameterExpression<?> parameterExpression);
    
    void visitCoalesceExpression(CoalesceExpression<?> coalesceExpression);
    
    void visitMapEntryExpression(MapEntryExpression<?, ?> mapEntryExpression);
    
    void visitListIndexExpression(ListIndexExpression listIndexExpression);
    
    void visitCompoundPredicate(CompoundPredicate compoundPredicate);
    
    void visitComparisonPredicate(ComparisonPredicate comparisonPredicate);
    
    void visitBetweenPredicate(BetweenPredicate<?> betweenPredicate);
    
    void visitLikePredicate(LikePredicate likePredicate);
    
    void visitInPredicate(InPredicate<?> inPredicate);
    
    void visitExistsPredicate(ExistsPredicate existsPredicate);
    
    void visitIsEmptyPredicate(IsEmptyPredicate<?> isEmptyPredicate);
    
    void visitIsMemberPredicate(IsMemberPredicate<?, ?> isMemberPredicate);
    
    void visitIsTruePredicate(IsTruePredicate isTruePredicate);
    
    void visitNullnessPredicate(NullnessPredicate nullnessPredicate);
    
}
