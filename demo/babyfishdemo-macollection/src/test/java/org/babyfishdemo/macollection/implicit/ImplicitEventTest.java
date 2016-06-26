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
package org.babyfishdemo.macollection.implicit;

import java.util.Objects;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MASet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Test;

import junit.framework.Assert;

public class ImplicitEventTest {

    @Test
    public void implictEventCausedByBidiMap() {
        
        MAMap<String, String> bidiMap = new MAHashMap<>(BidiType.ALL_VALUES);
        bidiMap.put("A", "value");
        
        MapElementListenerImpl<String, String> impl = new MapElementListenerImpl<>();
        bidiMap.addMapElementListener(impl);
        
        bidiMap.put("B", "value");
        Assert.assertEquals(
                "detached(key = A, value = value)" // Implicit event caused by bidiMap because duplicate values are not allowed
                + "attached(key = B, value = value)", // Explicit event caused by you
                impl.getAndClear()
        );
    }
    
    @Test
    public void implictEventCausedByBidiList() {
        
        MAList<String> bidiList = new MAArrayList<>(BidiType.ALL_VALUES);
        bidiList.addAll(MACollections.wrap("A", "B"));
        
        ListElementListenerImpl<String> impl = new ListElementListenerImpl<>();
        bidiList.addListElementListener(impl);
        
        bidiList.add("A");
        Assert.assertEquals(
                "detached(index = 0, element = A)" // Implicit event caused by bidiList because duplicate elements are not allowed
                + "attached(index = 1, element = A)", // Explicit event caused by you
                impl.getAndClear()
        );
    }
    
    @Test
    public void implicitEventCausedByUnstableElements() {
        
        MASet<Unstable> set = new MAHashSet<>(Unstable.EQUALITY_COMPARATOR);
        Unstable a = new Unstable("a");
        set.add(a);
        
        ElementListenerImpl<Unstable> impl = new ElementListenerImpl<>();
        set.addElementListener(impl);
        
        a.set("b");
        Assert.assertEquals(
                "detached(element = unstable(a))"
                + "attached(element = unstable(b))", 
                impl.getAndClear()
        );
    }
    
    @Test
    public void implicitEventCausedByUnstableKeys() {
        
        MAMap<Unstable, Object> map = new MAHashMap<>(Unstable.EQUALITY_COMPARATOR, (EqualityComparator<Object>)null);
        Unstable a = new Unstable("a");
        map.put(a, null);
        
        MapElementListenerImpl<Unstable, Object> impl = new MapElementListenerImpl<>();
        map.addMapElementListener(impl);
        
        a.set("b");
        Assert.assertEquals(
                "detached(key = unstable(a), value = null)"
                + "attached(key = unstable(b), value = null)", 
                impl.getAndClear()
        );
    }
    
    @Test
    public void implicitEventCausedByUnstabledBidiValues() {
        
        MAMap<String, Unstable> map = new MAHashMap<>(
                BidiType.ALL_VALUES,
                (EqualityComparator<Object>)null, 
                Unstable.EQUALITY_COMPARATOR);
        Unstable a = new Unstable("a");
        map.put("key", a);
        
        MapElementListenerImpl<Object, Unstable> impl = new MapElementListenerImpl<>();
        map.addMapElementListener(impl);
        
        a.set("b");
        Assert.assertEquals(
                "detached(key = key, value = unstable(a))"
                + "attached(key = key, value = unstable(b))", 
                impl.getAndClear()
        );
    }
    
    @Test
    public void implicitEventCausedByUnstableBidiElements() {
        
        MAList<Unstable> bidiList = new MAArrayList<>(BidiType.ALL_VALUES, Unstable.EQUALITY_COMPARATOR);
        Unstable a = new Unstable("a");
        bidiList.add(a);
        
        ListElementListenerImpl<Unstable> impl = new ListElementListenerImpl<>();
        bidiList.addListElementListener(impl);
        
        a.set("b");
        Assert.assertEquals(
                "detached(index = 0, element = unstable(a))"
                + "attached(index = 0, element = unstable(b))", 
                impl.getAndClear()
        );
    }
    
    private static class MapElementListenerImpl<K, V> implements MapElementListener<K, V> {

        private StringBuilder builder = new StringBuilder();
        
        @Override
        public void modified(MapElementEvent<K, V> e) throws Throwable {
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this
                .builder
                .append("detached(key = ")
                .append(e.getKey(PropertyVersion.DETACH))
                .append(", value = ")
                .append(e.getValue(PropertyVersion.DETACH))
                .append(')');
            } 
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this
                .builder
                .append("attached(key = ")
                .append(e.getKey(PropertyVersion.ATTACH))
                .append(", value = ")
                .append(e.getValue(PropertyVersion.ATTACH))
                .append(')');
            }
        }
        
        public String getAndClear() {
            String str = this.builder.toString();
            this.builder.setLength(0);
            return str;
        }
    }
    
    private static class ListElementListenerImpl<E> implements ListElementListener<E> {

        private StringBuilder builder = new StringBuilder();
        
        @Override
        public void modified(ListElementEvent<E> e) throws Throwable {
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this
                .builder
                .append("detached(index = ")
                .append(e.getIndex(PropertyVersion.DETACH))
                .append(", element = ")
                .append(e.getElement(PropertyVersion.DETACH))
                .append(')');
            } 
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this
                .builder
                .append("attached(index = ")
                .append(e.getIndex(PropertyVersion.ATTACH))
                .append(", element = ")
                .append(e.getElement(PropertyVersion.ATTACH))
                .append(')');
            }
        }
        
        public String getAndClear() {
            String str = this.builder.toString();
            this.builder.setLength(0);
            return str;
        }
    }
    
    private static class ElementListenerImpl<E> implements ElementListener<E> {

        private StringBuilder builder = new StringBuilder();
        
        @Override
        public void modified(ElementEvent<E> e) throws Throwable {
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this
                .builder
                .append("detached(element = ")
                .append(e.getElement(PropertyVersion.DETACH))
                .append(')');
            } 
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this
                .builder
                .append("attached(element = ")
                .append(e.getElement(PropertyVersion.ATTACH))
                .append(')');
            }
        }
        
        public String getAndClear() {
            String str = this.builder.toString();
            this.builder.setLength(0);
            return str;
        }
    }
    
    private static class Unstable {
        
        public static final FrozenEqualityComparator<Unstable> EQUALITY_COMPARATOR = new FECImpl();
        
        private String val;
        
        private transient FrozenContext<Unstable> valFrozenContext;
        
        public Unstable(String val) {
            this.val = val;
        }

        @SuppressWarnings("unused")
        public String get() {
            return val;
        }

        public void set(String val) {
            FrozenContext<Unstable> ctx = valFrozenContext;
            FrozenContext.suspendFreezing(ctx, this);
            this.val = val;
            FrozenContext.resumeFreezing(ctx);
        }
        
        @Override
        public String toString() {
            return "unstable(" + this.val + ')';
        }
        
        private static class FECImpl implements FrozenEqualityComparator<Unstable> {

            @Override
            public int hashCode(Unstable o) {
                return Objects.hashCode(o.val);
            }
            
            @Override
            public boolean equals(Unstable o1, Unstable o2) {
                return Objects.equals(o1.val, o2.val);
            }

            @Override
            public void freeze(Unstable obj, FrozenContext<Unstable> ctx) {
                obj.valFrozenContext = FrozenContext.combine(obj.valFrozenContext, ctx);
            }

            @Override
            public void unfreeze(Unstable obj, FrozenContext<Unstable> ctx) {
                obj.valFrozenContext = FrozenContext.remove(obj.valFrozenContext, ctx);
            }
        } 
    }
}
