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
package org.babyfish.model.hibernate.entities.ui;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.babyfish.model.IndexOf;
import org.babyfish.model.jpa.JPAModel;

@JPAModel
@Entity
@Table(name = "COMPONENT")
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("COMPONENT")
public class Component implements Serializable {

    private static final long serialVersionUID = 5300030428437542278L;

    @Id
    @Column(name = "COMPONENT_ID")
    private Long id;
    
    @Embedded
    private Color color;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "alpha", column = @Column(name = "BG_ALPHA", nullable = false)),
        @AttributeOverride(name = "red", column = @Column(name = "BG_RED", nullable = false)),
        @AttributeOverride(name = "green", column = @Column(name = "BG_GREEN", nullable = false)),
        @AttributeOverride(name = "blue", column = @Column(name = "BG_BLUE", nullable = false))
    })
    private Color backgroundColor;
    
    @IndexOf("parent")
    @Column(name = "INDEX", nullable = false)
    private int index;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private Composite parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Composite getParent() {
        return parent;
    }

    public void setParent(Composite parent) {
        this.parent = parent;
    }
}
