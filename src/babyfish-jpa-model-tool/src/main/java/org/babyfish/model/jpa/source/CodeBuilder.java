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
public class CodeBuilder {
    
    private static final String LINE_SPERATOR = System.getProperty("line.separator", "\n");
    
    private boolean newLine;
    
    private int tabCount;
    
    private StringBuilder builder = new StringBuilder();
    
    public CodeBuilder(int tabCount) {
        this.tabCount = tabCount;
        this.newLine = true;
    }
    
    public CodeBuilder append(String code) {
        this.appendTabsIfNecessary();
        this.builder.append(code);
        return this;
    }
    
    public CodeBuilder appendLine(String code) {
        this.append(code);
        return this.appendLine();
    }
    
    public CodeBuilder appendLine() {
        this.builder.append(LINE_SPERATOR);
        this.newLine = true;
        return this;
    }
    
    public CodeBuilder appendBeginBlock() {
        return this.appendBeginBlock(" {");
    }
    
    public CodeBuilder appendBeginBlock(String beginText) {
        if (beginText != null) {
            this.append(beginText);
        }
        this.appendLine();
        this.tabCount++;
        return this;
    }
    
    public CodeBuilder appendEndBlock() {
        return this.appendEndBlock(true, "}");
    }
    
    public CodeBuilder appendEndBlock(boolean newLine) {
        return this.appendEndBlock(newLine, "}");
    }
    
    public CodeBuilder appendEndBlock(String endText) {
        return this.appendEndBlock(true, endText);
    }
    
    public CodeBuilder appendEndBlock(boolean newLine, String endText) {
        this.tabCount--;
        if (endText != null) {
            this.append(endText);
        }
        if (newLine) {
            this.appendLine();
        }
        return this;
    }

    private CodeBuilder appendTabsIfNecessary() {
        if (this.newLine) {
            this.newLine = false;
            for (int i = this.tabCount - 1; i >= 0; i--) {
                builder.append('\t');
            } 
        }
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
    
}
