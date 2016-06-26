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

import java.util.Set;

import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.FromReferenceNode;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.ast.util.ASTUtil;

import antlr.collections.AST;

/**
 * @author Tao Chen
 */
class SqlASTHelper implements HqlSqlTokenTypes {

    private SqlASTHelper() {
        
    }
    
    public static boolean findJoinReferenceInWhereCaluse(QueryNode queryNode, Set<FromElement> joins) {
        if (joins.isEmpty()) {
            return false;
        }
        AST whereAST = ASTUtil.findTypeInChildren(queryNode, WHERE);
        if (whereAST == null) {
            return false;
        }
        return findJoinReferenceImpl(whereAST, joins);
    }
    
    private static boolean findJoinReferenceImpl(AST sqlAst, Set<FromElement> joins) {
        if (sqlAst instanceof FromReferenceNode) {
            FromElement referencedFromElement = ((FromReferenceNode)sqlAst).getFromElement();
            if (joins.contains(referencedFromElement)) {
                return true;
            }
        }
        for (AST childSqlAst = sqlAst.getFirstChild(); childSqlAst != null; childSqlAst = childSqlAst.getNextSibling()) {
            if (findJoinReferenceImpl(childSqlAst, joins)) {
                return true;
            }
        }
        return false;
    }
    
    public static void addFromElementAndancestors(Set<FromElement> fromElements, FromElement fromElement) {
        while (fromElement != null) {
            if (!fromElements.add(fromElement)) {
                return;
            }
            fromElement = fromElement.getOrigin();
        }
    }
    
    public static void removeFromElementsExcept(QueryNode queryNode, Set<FromElement> usedFromElements) {
        AST whereAst = ASTUtil.findTypeInChildren(queryNode, WHERE);
        if (whereAst != null) {
            updateUsedFromElements(whereAst, usedFromElements);
        }
        removeFromElementsExceptImpl(queryNode.getFromClause(), usedFromElements);
    }
    
    private static void updateUsedFromElements(AST sqlAst, Set<FromElement> usedFromElements) {
        if (sqlAst instanceof FromReferenceNode) {
            FromElement referencedFromElement = ((FromReferenceNode)sqlAst).getFromElement();
            if (referencedFromElement != null) {
                addFromElementAndancestors(usedFromElements, referencedFromElement);
            }
        }
        for (AST childSqlAst = sqlAst.getFirstChild(); childSqlAst != null; childSqlAst = childSqlAst.getNextSibling()) {
            updateUsedFromElements(childSqlAst, usedFromElements);
        }
    }
    
    private static void removeFromElementsExceptImpl(AST sqlAst, Set<FromElement> usedFromElements) {
        AST firstChild = sqlAst.getFirstChild();
        if (firstChild instanceof FromElement && !usedFromElements.contains(firstChild)) {
            firstChild = firstChild.getNextSibling();
            sqlAst.setFirstChild(firstChild);
        }
        
        AST nextSibling = sqlAst.getNextSibling();
        if (nextSibling instanceof FromElement && !usedFromElements.contains(nextSibling)) {
            nextSibling = nextSibling.getNextSibling();
            sqlAst.setNextSibling(nextSibling);
        }
        
        if (firstChild instanceof FromElement) {
            removeFromElementsExceptImpl(firstChild, usedFromElements);
        }
        if (nextSibling instanceof FromElement) {
            removeFromElementsExceptImpl(nextSibling, usedFromElements);
        }
    }
}
