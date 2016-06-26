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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.JoinType;

import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.spi.JoinNode;
import org.babyfish.model.jpa.path.spi.OrderNode;
import org.babyfish.persistence.Constants;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.ast.tree.Node;

import antlr.collections.AST;

/**
 * @author Tao Chen
 */
class HqlASTHelper implements HqlTokenTypes {

    private HqlASTHelper() {
        
    }
    
    public static AST findFirstChildInToppestQuery(AST ast, int type) {
        for (AST childAst = ast.getFirstChild(); 
                childAst != null; 
                childAst = childAst.getNextSibling()) {
            if (childAst.getType() != QUERY) {
                if (childAst.getType() == type) {
                    return childAst;
                }
                AST retval = findFirstChildInToppestQuery(childAst, type);
                if (retval != null) {
                    return retval;
                }
            }
        }
        return null;
    }
    
    public static AST createOrderFieldAST(String alias, OrderNode orderNode) {
        Iterator<String> nameIterator = orderNode.getNames().iterator();
        AST orderFieldAst = createAST(
                DOT,
                ".",
                createAST(IDENT, alias),
                createAST(IDENT, nameIterator.next())
        );
        while (nameIterator.hasNext()) {
            orderFieldAst = createAST(
                    DOT,
                    ".",
                    orderFieldAst,
                    createAST(IDENT, nameIterator.next()));
        }
        return orderFieldAst;
    }
    
    public static AST createAST(int type, String text, AST ... childAsts) {
        AST ast = new Node();
        ast.setType(type);
        ast.setText(text);
        for (AST childAst : childAsts) {
            if (childAst != null) {
                ast.addChild(childAst);
            }
        }
        return ast;
    }
    
    public static boolean addJoinAST(
            AST fromAst, 
            AST parentFromElementAst, 
            JoinNode joinNode,
            AliasGenerator aliasGenerator,
            boolean generateFetch,
            boolean ignoreLeftReferenceJoin,
            boolean ignoreLeftJoin,
            //The key equality comparator of key must be ReferenceEqualityComparator
            Map<JoinNode, AST> outputJoiNodeASTMapForSimpleOrderPath) {
        if (!joinNode.containsInnerJoins()) {
            if (ignoreLeftReferenceJoin && !joinNode.containsCollectionJoins()) {
                return false;
            }
            if (ignoreLeftJoin) {
                return false;
            }
        }
        boolean changed = false;
        AST parentAliasAst = HqlASTHelper.findFirstChildInToppestQuery(parentFromElementAst, ALIAS);
        AST joinAst = null;
        if (parentAliasAst != null) {
            joinAst = findExistingJoinAST(fromAst, parentAliasAst, joinNode);
        }
        if (joinAst != null) {
            int astJoinType = getJoinType(joinAst);
            int nodeJoinType = joinNode.getJoinType() == JoinType.INNER ? INNER : LEFT;
            int mergedJoinType = astJoinType != nodeJoinType ? INNER : astJoinType;
            changed |= setJoinType(joinAst, mergedJoinType);
            if (generateFetch && joinNode.isFetch()) {
                changed |= setFetch(joinAst);
            }
        } else {
            if (parentAliasAst == null) {
                parentAliasAst = HqlASTHelper.createAST(ALIAS, aliasGenerator.generateAlias());
                parentFromElementAst.addChild(parentAliasAst);
                changed |= true;
            }
            joinAst =
                    HqlASTHelper.createAST(
                            JOIN,
                            "join",
                            HqlASTHelper.createAST(
                                    joinNode.getJoinType() == JoinType.INNER ? INNER : LEFT,
                                    joinNode.getJoinType() == JoinType.INNER ? "inner" : "left"
                            ),
                            generateFetch && joinNode.isFetch() ? HqlASTHelper.createAST(FETCH, "fetch") : null,
                            HqlASTHelper.createAST(
                                    DOT, 
                                    ".",
                                    HqlASTHelper.createAST(IDENT, parentAliasAst.getText()),
                                    HqlASTHelper.createAST(IDENT, joinNode.getName()))
                    );
            fromAst.addChild(joinAst);
            changed |= true;
        }
        if (outputJoiNodeASTMapForSimpleOrderPath != null) {
            outputJoiNodeASTMapForSimpleOrderPath.put(joinNode, joinAst);
        }
        
        for (JoinNode childNode : joinNode.getChildNodes().values()) {
            changed |= addJoinAST(
                    fromAst, 
                    joinAst, 
                    childNode, 
                    aliasGenerator, 
                    generateFetch, 
                    ignoreLeftReferenceJoin, 
                    ignoreLeftJoin,
                    outputJoiNodeASTMapForSimpleOrderPath);
        }
        return changed;
    }
    
    public static AST findExistingJoinAST(AST fromAst, AST parentAliasAst, JoinNode joinNode) {
        
        AST secondRetvalMatchFetch = null;
        AST thirdRetvalMatchJoinType = null;
        AST fourthRetvalMatchNeitherFetchNorJoinType = null;
        
        for (AST fromElementAst = fromAst.getFirstChild();
                fromElementAst != null;
                fromElementAst = fromElementAst.getNextSibling()) {
            if (fromElementAst.getType() == JOIN) {
                AST joinAliasAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, ALIAS);
                if (joinAliasAst == null || !joinAliasAst.getText().startsWith(Constants.NOT_SHARED_JOIN_ALIAS_PREFIX)) {
                    AST dotAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, DOT);
                    AST dotLeftAst = dotAst.getFirstChild();
                    AST dotRightAst = dotLeftAst.getNextSibling();
                    if (dotLeftAst.getType() == IDENT && 
                            dotRightAst.getType() == IDENT &&
                            dotLeftAst.getText().equals(parentAliasAst.getText()) &&
                            dotRightAst.getText().equals(joinNode.getName())) {
                        
                        AST fetchAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, FETCH);
                        AST withAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, WITH);
                        int nodeJoinType = joinNode.getJoinType() == JoinType.LEFT ? LEFT : INNER;
                        int astJoinType = getJoinType(fromElementAst);
                        boolean matchJoinType = nodeJoinType == astJoinType;
                        
                        if (joinNode.isFetch()) { //JoinNode with fetches
                            if (withAst == null) { //fetch can only be apply to the join without with-clause
                                if (fetchAst != null) {
                                    if (matchJoinType) {
                                        //Find the return value that matches both fetch and join type, return immediately
                                        return fromElementAst; 
                                    }
                                    if (secondRetvalMatchFetch == null) {
                                        //Give the suggestion for second return value that matches fetch, continue and try to find better return value
                                        secondRetvalMatchFetch = fromElementAst;
                                    }
                                } else if (!joinNode.isCollection() || joinNode.getCollectionFetchType() == CollectionFetchType.PARTIAL) {
                                    if (matchJoinType) {
                                        //Give the suggestion for third return value that matches joinType, continue and try to find better return value
                                        if (thirdRetvalMatchJoinType == null) {
                                            thirdRetvalMatchJoinType = fromElementAst;
                                        }
                                    } else if (fourthRetvalMatchNeitherFetchNorJoinType == null) {
                                        //Give the suggestion for fourth return value that matches neight fetch and join type, continue and try to find better return value
                                        fourthRetvalMatchNeitherFetchNorJoinType = fromElementAst;
                                    }
                                }
                            }
                        } else {
                            if (fetchAst == null) { //JoinNode without fetches
                                if (matchJoinType) {
                                    //Find the return value that matches both fetch and join type, return immediately
                                    return fromElementAst;
                                }
                                if (secondRetvalMatchFetch == null) {
                                    //Give the suggestion for second return value that matches fetch, continue and try to find better return value
                                    secondRetvalMatchFetch = fromElementAst;
                                }
                            } else {
                                if (matchJoinType) {
                                    //Give the suggestion for third return value that matches joinType, continue and try to find better return value
                                    if (thirdRetvalMatchJoinType == null) {
                                        thirdRetvalMatchJoinType = fromElementAst;
                                    }
                                } else if (fourthRetvalMatchNeitherFetchNorJoinType == null) {
                                    //Give the suggestion for fourth return value that matches neight fetch and join type, continue and try to find better return value
                                    fourthRetvalMatchNeitherFetchNorJoinType = fromElementAst;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (secondRetvalMatchFetch != null) {
            return secondRetvalMatchFetch;
        }
        if (thirdRetvalMatchJoinType != null) {
            return thirdRetvalMatchJoinType;
        }
        return fourthRetvalMatchNeitherFetchNorJoinType;
    }
    
    public static int getJoinType(AST joinAst) {
        if (HqlASTHelper.findFirstChildInToppestQuery(joinAst, LEFT) != null) {
            return LEFT;
        }
        if (HqlASTHelper.findFirstChildInToppestQuery(joinAst, RIGHT) != null) {
            return RIGHT;
        }
        return INNER;
    }
    
    public static String getJoinTypeString(int joinType) {
        if (joinType == LEFT) {
            return "left";
        }
        if (joinType == RIGHT) {
            return "left";
        }
        return "inner";
    }
    
    public static boolean setJoinType(AST joinAst, int joinType) {
        AST joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, INNER);
        if (joinTypeAst == null) {
            joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, LEFT);
        }
        if (joinTypeAst == null) {
            joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, RIGHT);
        }
        if (joinTypeAst != null) {
            if (joinTypeAst.getType() != joinType) {
                joinTypeAst.setType(joinType);
                joinTypeAst.setText(getJoinTypeString(joinType));
                return true;
            }
        } else if (joinType != INNER) {
            joinTypeAst = HqlASTHelper.createAST(joinType, getJoinTypeString(joinType));
            AST oldFirstChildAst = joinAst.getFirstChild();
            joinTypeAst.setFirstChild(joinTypeAst);
            joinTypeAst.setNextSibling(oldFirstChildAst);
            return true;
        }
        return false;
    }
    
    public static boolean setFetch(AST joinAst) {
        if (HqlASTHelper.findFirstChildInToppestQuery(joinAst, FETCH) == null) {
            AST joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, INNER);
            if (joinTypeAst == null) {
                joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, LEFT);
            }
            if (joinTypeAst == null) {
                joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, RIGHT);
            }
            
            AST fetchAst = HqlASTHelper.createAST(FETCH, "fetch");
            if (joinTypeAst != null) {
                AST oldNextSiblingAst = joinTypeAst.getNextSibling();
                joinTypeAst.setNextSibling(fetchAst);
                fetchAst.setNextSibling(oldNextSiblingAst);
            } else {
                AST oldFirstChildAst = joinAst.getFirstChild();
                joinAst.setFirstChild(fetchAst);
                fetchAst.setNextSibling(oldFirstChildAst);
            }
            return true;
        }
        return false;
    }
    
    public static boolean unsetFetch(AST joinAst) {
        AST fetchAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, FETCH);
        if (fetchAst != null) {
            AST joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, INNER);
            if (joinTypeAst == null) {
                joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, LEFT);
            }
            if (joinTypeAst == null) {
                joinTypeAst = HqlASTHelper.findFirstChildInToppestQuery(joinAst, RIGHT);
            }
            if (joinTypeAst != null) {
                joinTypeAst.setNextSibling(fetchAst.getNextSibling());
            } else {
                joinAst.setFirstChild(fetchAst.getNextSibling());
            }
            return true;
        }
        return false;
    }
    
    public static String getAlias(AST fromElementAst) {
        AST aliasAst = HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, ALIAS);
        return aliasAst != null ? aliasAst.getText() : null;
    }
    
    public static void setAlias(AST fromElementAst, String alias) {
        if (HqlASTHelper.findFirstChildInToppestQuery(fromElementAst, ALIAS) == null) {
            AST aliasAst = HqlASTHelper.createAST(ALIAS, alias);
            AST childAst = fromElementAst.getFirstChild(); 
            while (childAst != null) { 
                AST nextChildAst = childAst.getNextSibling();
                if (nextChildAst == null || nextChildAst.getType() == WITH) {
                    childAst.setNextSibling(aliasAst);
                    aliasAst.setNextSibling(nextChildAst);
                    return;
                }
                childAst = nextChildAst;
            }
        }
    }
    
    public static void removeOrderByInToppestQuery(AST queryAst) {
        AST childAst = queryAst.getFirstChild(); 
        while (childAst != null) {
            AST nextChildAst = childAst.getNextSibling();
            if (nextChildAst != null && nextChildAst.getType() == ORDER) {
                childAst.setNextSibling(nextChildAst.getNextSibling());
                break;
            }
            childAst = nextChildAst;
        }
    }
    
    public static boolean removeChildOrdersInToppestQuery(
            AST orderAst, 
            List<OrderNode> orderNodes, 
            Map<JoinNode, AST> fromElementASTMap) {
        boolean changed = false;
        for (OrderNode orderNode : orderNodes) {
            AST prevOrderChildAst = null;
            for (AST orderChildAst = orderAst.getFirstChild();
                    orderChildAst != null;
                    orderChildAst = orderChildAst.getNextSibling()) {
                if (orderChildAst.getType() == DOT) {
                    AST parentAst = orderChildAst.getFirstChild();
                    String field = parentAst.getNextSibling().getText();
                    for (int i = orderNode.getNames().size() - 2; i >= 0; i--) {
                        if (parentAst.getType() != DOT) {
                            field = null;
                            break;
                        }
                        field = parentAst.getFirstChild().getNextSibling().getText() + '.' + field;
                        parentAst = parentAst.getFirstChild();
                    }
                    String owner = parentAst.getText();
                    if (orderNode.getQuanifiedName().equals(field)) {
                        AST ownerAst = fromElementASTMap.get(orderNode.getParentNode());
                        if (owner.equals(HqlASTHelper.getAlias(ownerAst))) {
                            AST nextDotAst = orderChildAst.getNextSibling();
                            if (nextDotAst != null) {
                                if (nextDotAst.getType() == ASCENDING || nextDotAst.getType() == DESCENDING) {
                                    nextDotAst = nextDotAst.getNextSibling();
                                }
                            }
                            if (prevOrderChildAst == null) {
                                orderAst.setFirstChild(nextDotAst);
                            } else {
                                prevOrderChildAst.setNextSibling(nextDotAst);
                            }
                            changed = true;
                            break;
                        }
                    }
                }
                prevOrderChildAst = orderChildAst;
            }
        }
        return changed;
    }
    
    public static String getComplexIdentifier(AST ast) {
        if (ast.getType() == DOT) {
            return getComplexIdentifier(ast.getFirstChild()) + '.' + getComplexIdentifier(ast.getFirstChild().getNextSibling());
        }
        return ast.getText();
    }
    
    public static final class AliasGenerator {
        
        private int alias;
        
        public AliasGenerator() {
            
        }
        
        public String generateAlias() {
            return Constants.QUERY_PATH_JOIN_ALIAS_PREFIX + this.alias++;
        }
    }
}
