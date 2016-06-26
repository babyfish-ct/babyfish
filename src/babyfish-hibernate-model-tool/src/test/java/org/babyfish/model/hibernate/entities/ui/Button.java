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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import org.babyfish.model.jpa.JPAModel;

@JPAModel
@Entity
@DiscriminatorValue("BUTTON")
public class Button extends Component {

    private static final long serialVersionUID = 1685546181849042032L;

    @Column(name = "TEXT")
    private String text;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "FONT")
    private Font font;
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "IMAGE")
    private byte[] image;
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "HELP")
    private String help;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "alpha", column = @Column(name = "ACTIVE_ALPHA")),
            @AttributeOverride(name = "red", column = @Column(name = "ACTIVE_RED")),
            @AttributeOverride(name = "green", column = @Column(name = "ACTIVE_GREEN")),
            @AttributeOverride(name = "blue", column = @Column(name = "ACTIVE_BLUE")),
    })
    private Color activeColor;
    
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "alpha", column = @Column(name = "ACTIVE_BG_ALPHA")),
            @AttributeOverride(name = "red", column = @Column(name = "ACTIVE_BG_RED")),
            @AttributeOverride(name = "green", column = @Column(name = "ACTIVE_BG_GREEN")),
            @AttributeOverride(name = "blue", column = @Column(name = "ACTIVE_BG_BLUE")),
    })
    private Color activeBackgroundColor;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
    }

    public Color getActiveBackgroundColor() {
        return activeBackgroundColor;
    }

    public void setActiveBackgroundColor(Color activeBackgroundColor) {
        this.activeBackgroundColor = activeBackgroundColor;
    }
}
