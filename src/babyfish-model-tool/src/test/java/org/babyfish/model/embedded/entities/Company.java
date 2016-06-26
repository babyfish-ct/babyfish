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
package org.babyfish.model.embedded.entities;

import java.util.NavigableMap;

import org.babyfish.model.Association;
import org.babyfish.model.Model;

/**
 * @author Tao Chen
 */
@Model
public class Company {

    @Association(opposite = "company")
    private NavigableMap<ContactInfo, Site> sites;

    public NavigableMap<ContactInfo, Site> getSites() {
        return sites;
    }

    public void setSites(NavigableMap<ContactInfo, Site> sites) {
        this.sites = sites;
    }
}
