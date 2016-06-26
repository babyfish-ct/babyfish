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
package org.babyfish.persistence.criteria.expression;

import java.util.Collection;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractSelection;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class CompoundSelectionImpl<X> extends AbstractSelection<X> implements CompoundSelection<X> {

    private static final long serialVersionUID = -1187997163043476706L;
    
    private Class<? extends X> javaType;
    
    private List<Selection<?>> compoundSelectionItems;
    
    public CompoundSelectionImpl(
            XCriteriaBuilder criteriaBuilder,
            Class<? extends X> javaType,
            Iterable<Selection<?>> compoundSelectionItems) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("javaType", javaType);
        List<Selection<?>> list;
        if (compoundSelectionItems != null) {
            if (compoundSelectionItems instanceof Collection<?>) {
                list = new ArrayList<>(((Collection<?>)compoundSelectionItems).size());
            } else {
                list = new ArrayList<>();
            }
            int index = 0;
            for (Selection<?> selection : compoundSelectionItems) {
                if (selection != null) {
                    Class<?> subJavaType = selection.getJavaType();
                    Arguments.mustNotBeArray("compoundSelectionItems[" + index + ']', subJavaType);
                    Arguments.mustNotBeCompatibleWithValue("compoundSelectionItems[" + index + ']', subJavaType, Tuple.class);
                    this.mustUnderSameCriteriaBuilder("compoundSelectionItems[" + index + ']', selection);
                    list.add(selection);
                }
                index++;
            }
        } else {
            list = MACollections.emptyList();
        }
        this.javaType = javaType;
        this.compoundSelectionItems = MACollections.unmodifiable(list);
    }

    @Override
    public boolean isCompoundSelection() {
        return true;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return this.compoundSelectionItems;
    }

    @Override
    public Class<? extends X> getJavaType() {
        return this.javaType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCompoundSelection(this);
    }

}
