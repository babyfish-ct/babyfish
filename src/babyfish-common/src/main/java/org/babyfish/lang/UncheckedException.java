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
package org.babyfish.lang;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author Tao Chen
 */
public class UncheckedException extends UndeclaredThrowableException {

    private static final long serialVersionUID = -3132246729156047132L;
    
    private UncheckedException(Throwable cause) {
        super(
                Arguments.mustNotBeInstanceOfAnyOfValue(
                        "cause", 
                        Arguments.mustNotBeNull("cause", cause), 
                        RuntimeException.class, 
                        Error.class
                )
        );
    }
    
    public static boolean isRuntimeExceptionOrError(Throwable throwable) {
        return 
            throwable instanceof RuntimeException ||
            throwable instanceof Error;
    }
    
    public static boolean isRuntimeExceptionOrError(Class<? extends Throwable> clazz) {
        return 
            RuntimeException.class.isAssignableFrom(clazz) ||
            Error.class.isAssignableFrom(clazz);
    }

    public static RuntimeException rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException)throwable;
        }
        if (throwable instanceof Error) {
            throw (Error)throwable;
        }
        throw new UncheckedException(throwable);
    }
}
