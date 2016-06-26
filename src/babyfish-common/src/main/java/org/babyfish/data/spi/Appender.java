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
package org.babyfish.data.spi;

public class Appender {
    
    private StringBuilder builder;
    
    private boolean addComma;
    
    public Appender(StringBuilder builder) {
        this.builder = builder;
    }
    
    public Appender property(String name, Object value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, boolean value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, char value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, byte value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, short value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, int value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, long value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, float value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    public Appender property(String name, double value) {
        this.name(name);
        this.builder.append(value);
        return this;
    }
    
    private Appender name(String name) {
        StringBuilder builder = this.builder;
        if (this.addComma) {
            builder.append(", ");
        } else {
            builder.append(" ");
            this.addComma = true;
        }
        builder.append(name).append(": ");
        return this;
    }

    public boolean isEmpty() {
        return !this.addComma;
    }
}
