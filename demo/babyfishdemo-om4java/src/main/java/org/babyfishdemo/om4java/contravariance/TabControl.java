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
package org.babyfishdemo.om4java.contravariance;

import java.util.List;

import org.babyfish.model.Contravariance;
import org.babyfish.model.Model;
 
/**
 * @author Tao Chen
 */
@Model // Using ObjectModel4Java, requires compilation-time byte code instrument
public class TabControl extends Container {
 
    /*
     * The annotation "@Contravariance" means that
     * this field shares the data with "Container.components"
     * 
     * "convariance & contravariance" is an interesting functionality invented by C#4.0
     * (1) Collection<DerivedType> => Collection<SuperType>, this calls "convariance"
     * (2) Collection<SuperType> => Collection<DerivedType>, this calles "contravariance"
     */
    @Contravariance(from = "components")
    private List<TabPage> tabPages;

    public List<TabPage> getTabPages() {
        return tabPages;
    }

    public void setTabPages(List<TabPage> tabPages) {
        this.tabPages = tabPages;
    }
}
