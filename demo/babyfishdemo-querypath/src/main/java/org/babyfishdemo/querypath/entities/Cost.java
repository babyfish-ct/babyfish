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
package org.babyfishdemo.querypath.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.model.jpa.JPAModel;

/**
 * Note: In JPA, the class marked by @Embededable must support 
 * hashCode and equals(Object).
 * But in ObjectModel4JPA, the hashCode and equals
 * must NOT be supported.
 *
 * @author Tao Chen
 */
@JPAModel
@Embeddable
public class Cost {

    @Column(name = "MINERAL", nullable = false)
    private int mineral;
    
    @Column(name = "GAS", nullable = false)
    private int gas;
    
    public Cost() {
        
    }
    
    public Cost(int mineral, int gas) {
        this.mineral = mineral;
        this.gas = gas;
    }

    public int getMineral() {
        return mineral;
    }

    public void setMineral(int mineral) {
        this.mineral = mineral;
    }

    public int getGas() {
        return gas;
    }

    public void setGas(int gas) {
        this.gas = gas;
    }
}
