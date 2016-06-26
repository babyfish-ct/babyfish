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
package org.babyfishdemo.om4java.dom;

import org.babyfish.model.Model;
import org.babyfishdemo.om4java.dom.visitor.Visitor;

/**
 * @author Tao Chen
 */
@Model // Using ObjectModel4Java, requires compilation-time byte code instrument
public class Text extends CharacterData {
    
    public Text(String data) {
        super(data);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TEXT;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitText(this);
    }
}
