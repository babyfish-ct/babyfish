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

import java.util.Collection;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.QueryPaths;

/**
 * @author Tao Chen
 */
public class PathPlanKeyBuilder {
    
    private XOrderedMap<String, QueryPath[]> queryPathMap = new LinkedHashMap<>();
    
    private PathPlanKey key;
    
    public PathPlanKeyBuilder setQueryPaths(QueryPath ... queryPaths) {
        return this.setQueryPaths(null, queryPaths);
    }

    public PathPlanKeyBuilder setQueryPaths(String alias, QueryPath ... queryPaths) {
        return this.setQueryPaths(alias, MACollections.wrap(queryPaths));
    }
    
    public PathPlanKeyBuilder setQueryPaths(Collection<? extends QueryPath> queryPaths) {
        return this.setQueryPaths(null, queryPaths);
    }

    public PathPlanKeyBuilder setQueryPaths(String alias, Collection<? extends QueryPath> queryPaths) {
        if (queryPaths == null || queryPaths.isEmpty()) {
            this.queryPathMap.remove(alias);
        }
        XOrderedSet<QueryPath> set = new LinkedHashSet<>((queryPaths.size() * 4 + 2) / 3);
        for (QueryPath queryPath : queryPaths) {
            if (queryPath != null) {
                set.add(QueryPaths.toStandard(queryPath));
            }
        }
        this.queryPathMap.put(alias, queryPaths.toArray(new QueryPath[set.size()]));
        this.key = null;
        return this;
    }
    
    public PathPlanKey build() {
        PathPlanKey key = this.key;
        if (key == null) {
            this.key = key = PathPlanKey.nullToNil(PathPlanKey.of(this.queryPathMap));
        }
        return PathPlanKey.nilToNull(key);
    }
}
