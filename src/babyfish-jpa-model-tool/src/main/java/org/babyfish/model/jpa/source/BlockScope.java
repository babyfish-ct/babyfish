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
package org.babyfish.model.jpa.source;

/**
 * @author Tao Chen
 */
public class BlockScope implements AutoCloseable {
    
    private CodeBuilder builder;
    
    private boolean endsWithNewLine;
    
    private String endText;
    
    public BlockScope(CodeBuilder builder) {
        this(builder, true, " {", "}");
    }
    
    public BlockScope(CodeBuilder builder, boolean endsWithNewLine) {
        this(builder, endsWithNewLine, " {", "}");
    }
    
    public BlockScope(CodeBuilder builder, String beginText, String endText) {
        this(builder, true, beginText, endText);
    }
    
    public BlockScope(CodeBuilder builder, boolean endsWithNewLine, String beginText, String endText) {
        (this.builder = builder).appendBeginBlock(beginText);
        this.endsWithNewLine = endsWithNewLine;
        this.endText = endText;
    }

    @Override
    public void close() {
        CodeBuilder builder = this.builder;
        if (builder != null) {
            this.builder = null;
            builder.appendEndBlock(this.endsWithNewLine, this.endText);
        }
    }
}
