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
package org.babyfish.model.jpa.path.spi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.path.QueryPath;

/**
 * @author Tao Chen
 */
public final class PathPlanKey implements Serializable {

    private static final long serialVersionUID = 561460285188124354L;

    private static final PathPlanKey NIL = new PathPlanKey(new SubKey[0]);
    
    private SubKey[] subKeys;
    
    private transient int hash;
    
    private transient String toString;
    
    private PathPlanKey(SubKey[] subKeys) {
        this.subKeys = subKeys;
    }
    
    static PathPlanKey of(Map<String, QueryPath[]> queryPathMap) {
        if (queryPathMap.isEmpty()) {
            return null;
        }
        SubKey[] arr = new SubKey[queryPathMap.size()];
        int len = 0;
        for (Entry<String, QueryPath[]> entry : queryPathMap.entrySet()) {
            SubKey subKey = new SubKey(entry.getKey(), entry.getValue());
            if (subKey != null) {
                arr[len++] = subKey;
            }
        }
        if (len == 0) {
            return null;
        }
        if (len < arr.length) {
            SubKey[] newArr = new SubKey[len];
            System.arraycopy(newArr, 0, arr, 0, len);
            arr = newArr;
        }
        return new PathPlanKey(arr);
    }
    
    static PathPlanKey nullToNil(PathPlanKey key) {
        return key == null ? NIL : key;
    }
    
    static PathPlanKey nilToNull(PathPlanKey key) {
        return key == NIL ? null : key;
    }

    SubKey[] getSubKeys() {
        return subKeys;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = Arrays.hashCode(this.subKeys);
            if (hash == 0) {
                hash = -1;
            }
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PathPlanKey)) {
            return false;
        }
        PathPlanKey other = (PathPlanKey)obj;
        return Arrays.equals(this.subKeys, other.subKeys);
    }

    @Override
    public String toString() {
        String toString = this.toString;
        if (toString == null) {
            StringBuilder builder = new StringBuilder("{ ");
            for (SubKey subKey : this.subKeys) {
                subKey.toString(builder);
            }
            builder.append(" }");
            this.toString = toString = builder.toString();
        }
        return toString;
    }
    
    final static class SubKey implements Serializable {
        
        private static final long serialVersionUID = 927712794296956126L;

        private String alias;
        
        private QueryPath[] queryPaths;
        
        private transient int hash;
        
        private SubKey(String alias, QueryPath[] queryPaths) {
            this.alias = alias;
            this.queryPaths = queryPaths.clone();
        }

        String getAlias() {
            return this.alias;
        }

        QueryPath[] getQueryPaths() {
            return this.queryPaths;
        }

        @Override
        public int hashCode() {
            int hash = this.hash;
            if (hash == 0) {
                hash = Arrays.hashCode(this.queryPaths);
                if (this.alias != null) {
                    hash = 31 * hash + this.alias.hashCode();
                }
                if (hash == 0) {
                    hash = -1;
                }
                this.hash = hash;
            }
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SubKey)) {
                return false;
            }
            SubKey other = (SubKey)obj;
            return Nulls.equals(this.alias, other.alias) &&
                    Arrays.equals(this.queryPaths, other.queryPaths);
        }

        void toString(StringBuilder builder) {
            builder.append(this.alias).append(" : { ");
            boolean addComma = false;
            for (QueryPath queryPath : this.queryPaths) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(queryPath);
            }
            builder.append(" }");
        }
    }
}
