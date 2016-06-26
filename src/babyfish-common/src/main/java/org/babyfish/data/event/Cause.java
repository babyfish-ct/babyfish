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
package org.babyfish.data.event;

import java.io.Serializable;

import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.lang.Arguments;

public class Cause implements Serializable {
    
    private static final long serialVersionUID = -72367416723412L;

    private ViewInfo viewInfo;
    
    private ModificationEvent viewEvent;
    
    public Cause(ModificationEvent viewEvent) {
        Object viewSource = viewEvent.getSource();
        Arguments.mustBeInstanceOfValue("viewEvent.getSource()", viewSource, View.class);
        this.viewInfo = ((View)viewSource).viewInfo();
        this.viewEvent = Arguments.mustNotBeNull("viewEvent", viewEvent);
    }

    public Cause(ViewInfo viewInfo, ModificationEvent viewEvent) {
        this.viewInfo = Arguments.mustNotBeNull("viewInfo", viewInfo);
        this.viewEvent = Arguments.mustNotBeNull("viewEvent", viewEvent);
    }

    public ViewInfo getViewInfo() {
        return this.viewInfo;
    }

    @SuppressWarnings("unchecked")
    public <E extends ModificationEvent> E getViewEvent() {
        return (E)this.viewEvent;
    }
}
