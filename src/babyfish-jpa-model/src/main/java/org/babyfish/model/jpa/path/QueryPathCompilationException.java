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
package org.babyfish.model.jpa.path;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * @author Tao Chen
 */
public class QueryPathCompilationException extends IllegalArgumentException {

    private static final long serialVersionUID = -1630149836390514269L;

    private Recognizer<?, ?> recognizer;
    
    private Object offendingSymbol;
    
    private int line;
    
    private int charPositionInLine;

    public QueryPathCompilationException(
            String message, 
            RecognitionException cause,
            Recognizer<?, ?> recognizer,
            Object offendingSymbol, 
            int line, 
            int charPositionInLine) {
        super(message, cause);
    }

    public Recognizer<?, ?> getRecognizer() {
        return this.recognizer;
    }

    public Object getOffendingSymbol() {
        return this.offendingSymbol;
    }

    public int getLine() {
        return this.line;
    }

    public int getCharPositionInLine() {
        return this.charPositionInLine;
    }
}
