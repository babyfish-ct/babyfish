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
package org.babyfish.test.collection.wrapper;

import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.spi.laziness.AbstractLazyMAMap;
import org.babyfish.test.collection.MAMapTest;

/**
 * @author Tao Chen
 */
public class MALazyMapTest extends MAMapTest {

    @Override
    protected MAMap<String, String> createMAMap() {
        
        return new AbstractLazyMAMap<String, String>(null) {

            @Override
            protected RootData<String, String> createRootData() {
                return new AbstractLazyMAMap.RootData<String, String>() {
                
                    private static final long serialVersionUID = -9087358066884280278L;

                    private boolean loaded;
                    
                    private boolean loading;

                    @Override
                    public boolean isLoaded() {
                        return this.loaded;
                    }

                    @Override
                    public boolean isLoading() {
                        return this.loading;
                    }

                    @Override
                    public boolean isLoadable() {
                        return true;
                    }

                    @Override
                    protected void setLoaded(boolean loaded) {
                        this.loaded = loaded;
                    }

                    @Override
                    protected void setLoading(boolean loading) {
                        this.loading = loading;
                    }

                    @Override
                    protected void onLoad() {
                        this.setBase(new MAHashMap<String, String>());
                    }
                    
                };
            }
        };
    }

}
