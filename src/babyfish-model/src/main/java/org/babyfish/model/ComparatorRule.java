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
package org.babyfish.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * THis annotation can be used on
 * <ul>
 * 	<li>The association field whose type is set or map(Note, for map, it specifies the value comparator, not key comparator)</li>
 *  <li>The ObjectModel4Java class whose type is {@link ModelType#EMBEDDABLE}</li>
 *  <li>The ObjectModel4JPA class that's marked by the annotation "javax.persistence.Embeddable"</li>
 * </ul>
 * @author Tao Chen
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface ComparatorRule {

    ComparatorProperty[] properties();
}
