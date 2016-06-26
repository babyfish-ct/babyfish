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
package org.babyfish.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class AttributeContext implements Serializable {
    
    private static final long serialVersionUID = 2676175611804129096L;

    private static final int ARRAY_SOTRE_MAX_ATTRIBUTE_COUNT = 4;
    
    private Object data;

    public boolean hasAttribute(Object key) {
        Arguments.mustNotBeNull("key", key);
        Object data = this.data;
        if (data == null) {
            return false;
        }
        if (data.getClass().isArray()) {
            Object[] arr = (Object[])data;
            int len = arr.length;
            for (int i = 0; i < len; i += 2) {
                if (arr[i] == null) {
                    break;
                }
                if (arr[i].equals(key)) {
                    return true;
                }
            }
            return false;
        } 
        return ((Map<?, ?>)data).containsKey(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Object key) {
        Arguments.mustNotBeNull("key", key);
        Object data = this.data;
        if (data == null) {
            return null;
        }
        if (data.getClass().isArray()) {
            Object[] arr = (Object[])data;
            int len = arr.length;
            for (int i = 0; i < len; i += 2) {
                Object existingKey = arr[i];
                if (existingKey == null) {
                    break;
                }
                if (key.equals(existingKey)) {
                    return (T)arr[i + 1];
                }
            }
            return null;
        }
        Map<Object, Object> map = (Map<Object, Object>)data;
        return (T)map.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public void addAttribute(Object key, Object value) {
        Arguments.mustNotBeNull("key", key);
        Object data = this.data;
        if (data == null) {
            this.data = new Object[] { key, value };
            return;
        } 
        if (data.getClass().isArray()) {
            Object[] arr = (Object[])data;
            int len = arr.length;
            for (int i = 0; i < len; i += 2) {
                Object existingKey = arr[i];
                if (existingKey == null) {
                    arr[i] = key;
                    arr[i + 1] = value;
                    return;
                }
                if (key.equals(existingKey)) {
                    throw new IllegalStateException(keyAleadyExists(key));
                }
            }
            if (len < ARRAY_SOTRE_MAX_ATTRIBUTE_COUNT * 2) {
                Object[] newArr = new Object[len << 1];
                System.arraycopy(arr, 0, newArr, 0, len);
                newArr[len] = key;
                newArr[len + 1] = value;
                this.data = newArr;
                return;
            }
            Map<Object, Object> map = new HashMap<>();
            for (int i = 0; i < len; i += 2) {
                Object existingKey = arr[i];
                if (existingKey == null) {
                    break;
                }
                map.put(existingKey, arr[i + 1]);
            }
            this.data = data = map;
        }
        Map<Object, Object> map = (Map<Object, Object>)data;
        if (map.containsKey(key)) {
            throw new IllegalStateException(keyAleadyExists(key));
        }
        map.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public Object setAttribute(Object key, Object value) {
        Arguments.mustNotBeNull("key", key);
        Object data = this.data;
        if (data == null) {
            this.data = new Object[] { key, value };
            return null;
        } 
        if (data.getClass().isArray()) {
            Object[] arr = (Object[])data;
            int len = arr.length;
            for (int i = 0; i < len; i += 2) {
                Object existingKey = arr[i];
                if (existingKey == null) {
                    arr[i] = key;
                    arr[i + 1] = value;
                    return null;
                }
                if (key.equals(existingKey)) {
                    Object oldValue = arr[i + 1];
                    arr[i + 1] = value;
                    return oldValue;
                }
            }
            if (len < ARRAY_SOTRE_MAX_ATTRIBUTE_COUNT * 2) {
                Object[] newArr = new Object[len << 1];
                System.arraycopy(arr, 0, newArr, 0, len);
                newArr[len] = key;
                newArr[len + 1] = value;
                this.data = newArr;
                return null;
            }
            Map<Object, Object> map = new HashMap<>();
            for (int i = 0; i < len; i += 2) {
                Object existingKey = arr[i];
                if (existingKey == null) {
                    break;
                }
                map.put(existingKey, arr[i + 1]);
            }
            this.data = data = map;
        }
        Map<Object, Object> map = (Map<Object, Object>)data;
        return map.put(key, value);
    }
    
    public Object removeAttribute(Object key) {
        Arguments.mustNotBeNull("key", key);
        Object data = this.data;
        if (data == null) {
            return null;
        }
        if (data.getClass().isArray()) {
            Object[] arr = (Object[])data;
            int len = arr.length;
            for (int i = 0; i < len; i += 2) {
                Object existingKey = arr[i];
                if (existingKey == null) {
                    break;
                }
                if (key.equals(existingKey)) {
                    Object oldValue = arr[i + 1];
                    if (len == 2) {
                        this.data = null;
                    } else if (i * 2 <= len) {
                        Object[] newArr = new Object[len >> 1];
                        System.arraycopy(arr, 0, newArr, 0, i);
                        System.arraycopy(arr, i + 2, newArr, i, (len >> 1) - i);
                        this.data = newArr;
                    } else {
                        System.arraycopy(arr, i + 2, arr, i, len - i - 2);
                        if (i == len - 2) {
                            arr[i] = null;
                            arr[i + 1] = null;
                        }
                    }
                    return oldValue;
                }
            }
            return null;
        }
        Map<?, ?> map = (Map<?, ?>)data;
        Object oldValue = map.remove(key);
        if (map.size() <= ARRAY_SOTRE_MAX_ATTRIBUTE_COUNT) {
            Object[] arr = new Object[ARRAY_SOTRE_MAX_ATTRIBUTE_COUNT * 2];
            int index = 0;
            for (Entry<?, ?> e : map.entrySet()) {
                arr[index++] = e.getKey();
                arr[index++] = e.getValue();
            }
            this.data = arr;
        }
        return oldValue;
    }
    
    @I18N
    private static native String keyAleadyExists(Object key);
}
