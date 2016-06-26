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
package org.babyfishdemo.foundation.delegate;

import java.math.BigDecimal;

import org.junit.Test;

import junit.framework.Assert;

/*
 * (1) Please learn "CombinerTest" before learn this one
 * (2) This functionality has C++ version, 
 *     please see 
 *     ${babyfish-home}/c++/src/event.h
 *     and
 *     ${babyfish-home}/c++/demo/EventTest.cpp
 */
/**
 * @author Tao Chen
 */
public class BookTest {

    @Test
    public void test() {
        
        Book book = new Book("NodeJS", new BigDecimal(40));
        
        StringBuilder builder = new StringBuilder();
        PropertyChangedListener handler1 = e -> { builder.append("[handler-1]: " + e + '\n'); };
        PropertyChangedListener handler2 = e -> { 
            builder.append("[handler-2]: " + e + '\n'); 
            book.removePropertyChangedListener(handler1);
        };
        book.addPropertyChangedListener(handler1);
        book.addPropertyChangedListener(handler2);
        
        book.setName("AngularJS");
        Assert.assertEquals( // Those two handers are triggered
                "[handler-1]: { "
                +   "propertyName: \"name\", "
                +   "oldValue: \"NodeJS\", "
                +   "newValue: \"AngularJS\" "
                + "}\n"
                + "[handler-2]: { "
                +   "propertyName: \"name\", "
                +   "oldValue: \"NodeJS\", "
                +   "newValue: \"AngularJS\" " 
                + "}\n",
                builder.toString()
        );
        builder.setLength(0); //Clear the StringBuilder
        
        book.setPrice(new BigDecimal(43));
        Assert.assertEquals( 
                // Handler1 has been removed when the handler2 is executed at the first time.
                "[handler-2]: { "
                + "propertyName: \"price\", "
                + "oldValue: \"40\", "
                + "newValue: \"43\" " 
                + "}\n",
                builder.toString()
        );
    }
}
