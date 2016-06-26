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
package org.babyfish.model.ui.entities;

import org.babyfish.model.Contravariance;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
@Model
public class TabPage extends Composite {

    private static final long serialVersionUID = 3761212880822589213L;

    @Scalar
    private String title;
    
    @Scalar
    private boolean closeable;
    
    @Contravariance
    private Tab parent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    @Override
    public Tab getParent() {
        return parent;
    }
    
    @Deprecated
    @Override
    public final void setParent(Composite parent) {
        this.setParent((Tab)parent);
    }

    public void setParent(Tab parent) {
        this.parent = parent;
    }
}
