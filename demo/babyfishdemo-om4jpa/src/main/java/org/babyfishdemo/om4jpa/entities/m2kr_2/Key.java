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
package org.babyfishdemo.om4jpa.entities.m2kr_2;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Embeddable
public class Key {

    @Column(name = "PRIMARY_CODE", length = 10, nullable = false)
    private String primaryCode;
    
    @Column(name = "PRIMARY_CODE", length = 20)
    private String secondaryCode;
    
    @SuppressWarnings("unused") // Only for hibernate
    private Key() {
        
    }

    public Key(String primaryCode) {
        this.primaryCode = primaryCode;
    }

    public Key(String primaryCode, String secondaryCode) {
        this.primaryCode = primaryCode;
        this.secondaryCode = secondaryCode;
    }

    public String getPrimaryCode() {
        return primaryCode;
    }

    public void setPrimaryCode(String primaryCode) {
        this.primaryCode = primaryCode;
    }

    public String getSecondaryCode() {
        return secondaryCode;
    }

    public void setSecondaryCode(String secondaryCode) {
        this.secondaryCode = secondaryCode;
    }
}
