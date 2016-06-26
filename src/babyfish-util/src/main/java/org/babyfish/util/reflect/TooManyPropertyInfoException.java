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
package org.babyfish.util.reflect;

/**
 * @author Tao Chen
 */
public class TooManyPropertyInfoException extends RuntimeException {

    private static final long serialVersionUID = 4657920481509999867L;

    public TooManyPropertyInfoException() {
        super();
    }

    public TooManyPropertyInfoException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyPropertyInfoException(String message) {
        super(message);
    }

    public TooManyPropertyInfoException(Throwable cause) {
        super(cause);
    }

}
