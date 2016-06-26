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
package org.babyfishdemo.xcollection.validator;

import org.babyfish.lang.Arguments;
import org.babyfish.validator.SuppressibleValidator;
import org.babyfish.validator.Validator;

/*
 * For customer validator, 
 * (1) Validator<T> is required interface
 * (2) SuppressibleValidator<T> that inherits Validator<T> is optional interface
 * 
 * In order to make our demo to be more interesting, 
 * this class choose to implement "SuppressibleValidator<T>"
 */
/**
 * @author Tao Chen
 */
public class MaxComplexAbsValidator implements SuppressibleValidator<Complex> {
    
    private double maxAbs;
    
    private StringBuilder builder;
    
    public MaxComplexAbsValidator(double maxAbs, StringBuilder builder) {
        this.maxAbs = Arguments.mustBeGreaterThanValue("maxAbs", maxAbs, 0);
        this.builder = builder;
    }

    @Override
    public void validate(Complex e) {
        /*
         * Need not to check whether the argument "e" is null.
         * Except Validators.notNull(), no validators have chance to check null elements.
         */
        if (this.builder != null) {
            this.builder.append(this).append(" validate \"").append(e).append("\";");
        }
        Arguments.mustBeLessThanOrEqualToValue("e.getAbs()", e.getAbs(), this.maxAbs);
    }

    @Override
    public boolean suppress(Validator<Complex> other) {
        if (other instanceof MaxComplexAbsValidator) {
            return this.maxAbs <= ((MaxComplexAbsValidator)other).maxAbs;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "MaxComplexAbsValidator(maxAbs = " + this.maxAbs + ')';
    }
}
