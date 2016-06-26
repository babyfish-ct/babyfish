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
package org.babyfish.data.event.spi;

import java.io.Serializable;

import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.EventAttributeContext;
import org.babyfish.data.event.Modification;
import org.babyfish.data.spi.Appender;

public abstract class AbstractModification implements Modification, Serializable {

    private static final long serialVersionUID = -8406843393303491613L;
    
    private EventAttributeContext attributeContext;
    
    protected AbstractModification() {
        this.attributeContext = EventAttributeContext.of(AttributeScope.GLOBAL);
    }
    
    public EventAttributeContext getAttributeContext() {
        return this.attributeContext;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.appendTo(builder);
        return builder.toString();
    }
    
    public void appendTo(StringBuilder builder) {
        builder.append(this.getClass().getName()).append('{');
        Appender appender = new Appender(builder);
        this.appendTo(appender);
        if (!appender.isEmpty()) {
            builder.append(' ');
        }
        builder.append('}');
    }
    
    protected void appendTo(Appender appender) {}
}
