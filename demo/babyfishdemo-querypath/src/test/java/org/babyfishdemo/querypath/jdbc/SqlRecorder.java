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
package org.babyfishdemo.querypath.jdbc;

/**
 * @author Tao Chen
 */
public abstract class SqlRecorder implements AutoCloseable {

    private static final ThreadLocal<SqlRecorder> RECORDER_LOCAL = new ThreadLocal<>();

    private SqlRecorder prev;
    
    public SqlRecorder() {
        this.prev = RECORDER_LOCAL.get();
        RECORDER_LOCAL.set(this);
    }
    
    @Override
    public void close() {
        if (RECORDER_LOCAL.get() != this) {
            throw new IllegalStateException(
                    "The current recorder is not the youngest recorder of current thread"
            );
        }
        if (this.prev != null) {
            RECORDER_LOCAL.set(this.prev);
        } else {
            RECORDER_LOCAL.remove();
        }
    }

    protected abstract void prepareStatement(String sql);
    
    static void prepare(String sql) {
        SqlRecorder youngest = RECORDER_LOCAL.get();
        if (youngest != null) {
            youngest.prepareStatement(sql);
        }
    }
}
