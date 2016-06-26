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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.babyfish.lang.spi.UsingInstrumenter;

/**
 * Example:
 * <pre>
 * &#64;Delegate
 * public interface Operator&lt;T&gt; {
 *    T execute(T x, T y);
 *    static native &lt;T&gt; Operator&lt;T&gt; combine(Operator&lt;T&gt; a, Operator&lt;T&gt; b);
 *    static native &lt;T&gt; Operator&lt;T&gt; remove(Operator&lt;T&gt; a, Operator&lt;T&gt; b);
 * }
 * </pre>
 * 
 * <pre>
 * Operator&lt;BigInteger&gt; a = (x, y) -&gt; { System.out.println("+"); x.add(y); }
 * Operator&lt;BigInteger&gt; b = (x, y) -&gt; { System.out.println("-"); x.subtract(y); }
 * Operator&lt;BigInteger&gt; c = (x, y) -&gt; { System.out.println("*"); x.multiply(y); }
 * Operator&lt;BigInteger&gt; d = (x, y) -&gt; { System.out.println("/"); x.divide(y); }
 * Operator&lt;BigInteger&gt; operator = 
 *    Operator.combine(
 *      Operator.combine(a, b),
 *      Operator.combine(c, d)
 *     );
 *  System.out.println(operator.execute(17, 2));
 *  operator = Operator.remove(operator, Operator.combine(c, d));
 *  System.out.println(operator.execute(17, 2));
 * </pre>
 * 
 * The result should be
 * <pre>
 * +
 * -
 * *
 * &#47;
 * 8
 * +
 * -
 * 15
 * </pre>
 * 
 * @author Tao Chen
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@UsingInstrumenter("org.babyfish.lang.delegate.instrument.DelegateInstrumenter")
public @interface Delegate {

    DelegateExceptionHandlingType value() default DelegateExceptionHandlingType.BREAK;
}
