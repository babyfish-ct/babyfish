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
package org.babyfish.test.collection.equalitycomparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;

/**
 * @author Tao Chen
 */
class Helper {
    
    private Helper() {
        throw new UnsupportedOperationException();
    }
    
    public static final EqualityComparator<Element> ELEMENT_CODE_EQUALITY_COMPARATOR =
        new EqualityComparator<Element>() {

            @Override
            public int hashCode(Element o) {
                String code = o.getCode();
                return code == null ? 0 : code.hashCode();
            }
    
            @Override
            public boolean equals(Element o1, Element o2) {
                return o1.getCode().equals(o2.getCode());
            }
            
        };
        
    public static final EqualityComparator<Element> ELEMENT_NAME_EQUALITY_COMPARATOR =
        new EqualityComparator<Element>() {
    
            @Override
            public int hashCode(Element o) {
                String name = o.getName();
                return name == null ? 0 : name.hashCode();
            }
    
            @Override
            public boolean equals(Element o1, Element o2) {
                return o1.getName().equals(o2.getName());
            }
            
        };
        
    public static final Comparator<Element> ELEMENT_CODE_COMPARATOR =
        new Comparator<Element>() {

            @Override
            public int compare(Element o1, Element o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        
        };
        
    public static final Comparator<Element> ELEMENT_NAME_COMPARATOR =
        new Comparator<Element>() {

            @Override
            public int compare(Element o1, Element o2) {
                return o1.getName().compareTo(o2.getName());
            }
        
        };
        
    public static XList<Element> createElementListManagedByCodeByCodes(String ... codes) {
        XList<Element> xList = new ArrayList<Element>(ELEMENT_CODE_EQUALITY_COMPARATOR);
        for (String code : codes) {
            xList.add(new Element(code, "invalidName"));
        }
        return xList;
    }
    
    public static XList<Element> createElementListManagedByNameByCodes(String ... codes) {
        XList<Element> xList = new ArrayList<Element>(ELEMENT_NAME_EQUALITY_COMPARATOR);
        for (String code : codes) {
            xList.add(new Element(code, "invalidName"));
        }
        return xList;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> XList<E> createList(
            UnifiedComparator<? super E> unifiedComparator, 
            E ... elements) {
        XList<E> xList = new ArrayList<E>(unifiedComparator);
        for (E e : elements) {
            xList.add(e);
        }
        return xList;
    }
    
    public static Entry<Element, Element> createEntry(
            final String keyCode, 
            final String keyName, 
            final String valueCode, 
            final String valueName) {
        return new Entry<Element, Element>() {

            @Override
            public Element getKey() {
                return new Element(keyCode, keyName);
            }

            @Override
            public Element getValue() {
                return new Element(valueCode, valueName);
            }

            @Override
            public Element setValue(Element value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean equals(Object obj) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return this.getKey() + "=" + this.getValue();
            }
            
        };
    }
    
    public static void assertElementCodes(Collection<Element> elements, String ... codes) {
        Assert.assertEquals(codes.length, elements.size());
        boolean ordered = 
            elements instanceof List<?> ||
            elements instanceof SortedSet<?> ||
            elements instanceof XOrderedSet<?>;
        if (ordered) {
            int index = 0;
            for (Element element : elements) {
                Assert.assertEquals(codes[index++], element.getCode());
            }
        } else {
            List<String> codeList = new ArrayList<String>(codes.length);
            for (String code : codes) {
                codeList.add(code);
            }
            for (Element element : elements) {
                Assert.assertTrue(codeList.remove(element.getCode()));
            }
            Assert.assertTrue(codeList.isEmpty());
        }
    }
    
    public static void assertElementNames(Collection<Element> elements, String ... names) {
        Assert.assertEquals(names.length, elements.size());
        boolean ordered = 
            elements instanceof List<?> ||
            elements instanceof SortedSet<?> ||
            elements instanceof XOrderedSet<?>;
        if (ordered) {
            int index = 0;
            for (Element element : elements) {
                Assert.assertEquals(names[index++], element.getName());
            }
        } else {
            List<String> nameList = new ArrayList<String>(names.length);
            for (String name : names) {
                nameList.add(name);
            }
            for (Element element : elements) {
                Assert.assertTrue(nameList.remove(element.getName()));
            }
            Assert.assertTrue(nameList.isEmpty());
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <E> void assertCollection(boolean ordered, Collection<E> c, E ... elements) {
        Assert.assertEquals(elements.length, c.size());
        if (ordered) {
            int index = 0;
            for (E e : c) {
                Assert.assertEquals(elements[index++], e);
            }
        } else {
            List<E> list = new ArrayList<E>(elements.length);
            for (E e : elements) {
                list.add(e);
            }
            for (E e : c) {
                Assert.assertTrue(list.remove(e));
            }
            Assert.assertTrue(list.isEmpty());
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <E> XList<E> prepareXList(
            EqualityComparator<? super E> equalityComparator, 
            E ... elements) {
        XList<E> xList = new ArrayList<E>(equalityComparator);
        for (E element : elements) {
            xList.add(element);
        }
        return xList;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> XSet<E> prepareXSet(
            UnifiedComparator<? super E> unifiedComparator, E ... elements) {
        return 
            unifiedComparator.comparator() != null ?
                    prepareXNavigableSet(unifiedComparator.comparator(), elements) :
                    prepareXOrderedSet(unifiedComparator.equalityComparator(), elements);
    }
    
    public static <K, V> XMap<K, V> prepareXMap(
            UnifiedComparator<? super K> keyUnifiedComparator, K[] ks, V[] vs) {
        Assert.assertEquals(ks.length, vs.length);
        XMap<K, V> xMap = 
            keyUnifiedComparator.comparator() != null ?
                    new TreeMap<K, V>(keyUnifiedComparator.comparator()) :
                    new LinkedHashMap<K, V>(keyUnifiedComparator.equalityComparator());
        for (int i = 0; i < ks.length; i++) {
            xMap.put(ks[i], vs[i]);
        }
        return xMap;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> XOrderedSet<E> prepareXOrderedSet(
            EqualityComparator<? super E> equalityComparator,
            E ... elements) {
        XOrderedSet<E> xOrderedSet = new LinkedHashSet<E>(equalityComparator);
        for (E element : elements) {
            xOrderedSet.add(element);
        }
        return xOrderedSet;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> XNavigableSet<E> prepareXNavigableSet(
            Comparator<? super E> comparator,
            E ... elements) {
        XNavigableSet<E> xNavigableSet = new TreeSet<E>(comparator);
        for (E element : elements) {
            xNavigableSet.add(element);
        }
        return xNavigableSet;
    }
    
}
