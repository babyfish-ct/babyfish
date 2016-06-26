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

import java.util.List;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;

/**
 * @author Tao Chen
 */
public abstract class AbstractSelection<X> extends AbstractNode implements Selection<X> {

    private static final long serialVersionUID = -2074383010261851296L;
    
    private String alias;
    
    protected AbstractSelection(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
    }
    
    @Override
    public String getAlias() {
        return this.alias;
    }

    @Override
    public Selection<X> alias(String alias) {
        this.checkState();
        this.alias = alias;
        return this;
    }
    
    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        throw new UnsupportedOperationException(isNotCompoundSelection(CompoundSelection.class));
    }

    @SuppressWarnings("rawtypes")
    @I18N
    private static native String isNotCompoundSelection(Class<CompoundSelection> compoundSelectionType);
}
