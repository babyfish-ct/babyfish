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
package org.babyfish.validator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class Validators {

    protected Validators() {
        throw new UnsupportedOperationException();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Validator<T> combine(Validator<T> validator1, Validator<T> validator2) {
        if (validator1 == null) {
            return wrap(validator2);
        }
        if (validator2 == null) {
            return wrap(validator1);
        }
        Node<T>[] arr = new Node[nodeCapacity(validator1) + nodeCapacity(validator2)];
        int len = 0;
        Iterator<Node<T>> itr = nodeIterator(validator1);
        while (itr.hasNext()) {
            arr[len++] = itr.next();
        }
        itr = nodeIterator(validator2);
        while (itr.hasNext()) {
            Node<T> node = itr.next();
            boolean matched = false;
            for (int i = 0; i < len; i++) {
                Node<T> existingNode = arr[i];
                if (existingNode != null && existingNode.validator.equals(node.validator)) {
                    existingNode.refCount += node.refCount;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                Validator<T> validator = node.validator;
                for (int i = 0; i < len; i++) {
                    Node<T> existingNode = arr[i];
                    if (existingNode != null) {
                        Validator<T> existingValidator = existingNode.validator;
                        if (validator instanceof SuppressibleValidator<?> && 
                                ((SuppressibleValidator<T>)validator).suppress(existingValidator)) {
                            existingNode.mergedBy(validator);
                        } else if (existingValidator instanceof SuppressibleValidator<?> && 
                                ((SuppressibleValidator<T>)existingValidator).suppress(validator)) {
                            node.mergedBy(existingValidator);
                        }
                    }
                }
                arr[len++] = node;
            }
        }
        return doReturn(arr, len);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Validator<T> remove(Validator<T> validator1, Validator<T> validator2) {
        if (validator1 == null) {
            return wrap(validator2);
        }
        if (validator2 == null) {
            return wrap(validator1);
        }
        Node<T>[] arr = new Node[nodeCapacity(validator1)];
        int len = 0;
        Iterator<Node<T>> itr = nodeIterator(validator1);
        while (itr.hasNext()) {
            arr[len++] = itr.next();
        }
        itr = nodeIterator(validator2);
        while (itr.hasNext()) {
            Node<T> node = itr.next();
            for (int i = 0; i < len; i++) {
                Node<T> existingNode = arr[i];
                if (existingNode != null && existingNode.validator.equals(node.validator)) {
                    if ((existingNode.refCount -= node.refCount) <= 0) {
                        for (int ii = len - 1; ii >= 0; ii--) {
                            if (i != ii) {
                                Node<T> otherExistingNode = arr[ii];
                                if (otherExistingNode != null) {
                                    otherExistingNode.unmergedBy(existingNode.validator);
                                }
                            }
                        }
                        arr[i] = null;
                        len--;
                    }
                    break;
                }
            }
        }
        return doReturn(arr, len);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Validator<T> notNull() {
        return (Validator<T>)NotNullImpl.INSTANCE;
    }
    
    public static Validator<String> notEmpty() {
        return new MinLengthImpl(0);
    }
    
    public static Validator<String> minLength(int minLength) {
        return new MinLengthImpl(minLength);
    }
    
    public static Validator<String> maxLength(int maxLength) {
        return new MaxLengthImpl(maxLength);
    }

    @SuppressWarnings("unchecked")
    public static <T> Validator<T> instanceOf(Class<? extends T> restrictionType) {
        return (Validator<T>)InstanceOfImpl.of(
                Arguments.mustNotBeNull("restrictionType", restrictionType)
        );
    }

    private static <T> Validator<T> wrap(Validator<T> validator) {
        if (validator != null && !(validator instanceof CombinedImpl)) {
            if (!(validator instanceof Serializable)) {
                if (validator instanceof SuppressibleValidator<?>) {
                    validator = new TransientSuppressibleWrapper<T>((SuppressibleValidator<T>)validator);
                } else {
                    validator = new TransientWrapper<>(validator);
                }
            }
            if (validator != NotNullImpl.INSTANCE) {
                if (validator instanceof SuppressibleValidator<?>) {
                    validator = new NullSafeSuppressibleWrapper<T>((SuppressibleValidator<T>)validator);
                } else {
                    validator = new NullSafeWrapper<>(validator);
                }
            }
        }
        return validator;
    }
    
    private static int nodeCapacity(Validator<?> validator) {
        if (validator instanceof CombinedImpl<?>) {
            CombinedImpl<?> combinedImpl = (CombinedImpl<?>)validator;
            return combinedImpl.maxIndex - combinedImpl.minIndex + 1;
        }
        return 1;
    }
    
    private static <T> Iterator<Node<T>> nodeIterator(Validator<T> validator) {
        if (validator instanceof CombinedImpl<?>) {
            CombinedImpl<T> combinedImpl = (CombinedImpl<T>)validator;
            return new NodeItr<>(combinedImpl.arr, combinedImpl.minIndex, combinedImpl.maxIndex);
        }
        return new SingleNodeItr<>(validator);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Validator<T> doReturn(Node<T>[] arr, int len) {
        if (len == 0) {
            return null;
        }
        int all = arr.length;
        int minIndex = 0;
        while (true) {
            if (arr[minIndex] != null) {
                break;
            }
            minIndex++;
        }
        if (minIndex == all) {
            throw new AssertionError();
        }
        if (len == 1 && arr[minIndex].refCount == 1) {
            return arr[minIndex].validator;
        }
        int maxIndex = all - 1;
        while (true) {
            if (arr[maxIndex] != null) {
                break;
            }
            maxIndex--;
        }
        if (maxIndex == -1) {
            throw new AssertionError();
        }
        if ((len << 1) <= arr.length) {
            Node<T>[] newArr = new Node[arr.length >> 1];
            int index = 0;
            for (int i = minIndex; i <= maxIndex; i++) {
                if (arr[i] != null) {
                    newArr[index++] = arr[i];
                }
            }
            arr = newArr;
            minIndex = 0;
            maxIndex = index - 1;
        }
        return new CombinedImpl<T>(arr, minIndex, maxIndex);
    }
    
    private static class CombinedImpl<T> implements Validator<T>, Serializable {

        private static final long serialVersionUID = -1717016732961812479L;
        
        private Node<T>[] arr;
        
        private int minIndex;
        
        private int maxIndex;
        
        CombinedImpl(Node<T>[] arr, int minIndex, int maxIndex) {
            this.arr = arr;
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
        }
        
        @Override
        public void validate(T e) {
            int maxIndex = this.maxIndex;
            Node<T>[] arr = this.arr;
            for (int i = this.minIndex; i <= maxIndex; i++) {
                Node<T> node = arr[i];
                if (node != null && node.mergedBy == null) {
                    Validator<T> validator = node.validator;
                    if (e != null || validator instanceof NotNullImpl) {
                        validator.validate(e);
                    }
                }
            }
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            Node<T>[] arr = this.arr;
            int maxIndex = this.maxIndex;
            int serializableLen = 0;
            for (int i = minIndex; i <= maxIndex; i++) {
                if (arr[i].validator instanceof Serializable) {
                    serializableLen++;
                }
            }
            out.writeInt(serializableLen);
            for (int i = this.minIndex; i <= maxIndex; i++) {
                Node<T> node = arr[i];
                if (node.validator instanceof Serializable) {
                    out.writeObject(node.validator);
                    out.writeInt(node.refCount);
                    int serializableMergeByLen = 0;
                    if (node.mergedBy != null) {
                        for (Validator<T> other : node.mergedBy) {
                            if (other instanceof Serializable) {
                                serializableMergeByLen++;
                            }
                        }
                    }
                    out.writeInt(serializableMergeByLen);
                    if (node.mergedBy != null) {
                        for (Validator<T> other : node.mergedBy) {
                            if (other instanceof Serializable) {
                                out.writeObject(other);
                            }
                        }
                    }
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            int len = in.readInt();
            Node<T>[] arr = new Node[standardCapacity(len)];
            int index = 0;
            for (int i = 0; i < len; i++) {
                Validator<T> validator = (Validator<T>)in.readObject();
                int refCount = in.readInt();
                int mergeByLen = in.readInt();
                Validator<T>[] mergeBy = mergeByLen == 0 ? null : new Validator[standardCapacity(mergeByLen)];
                int mergeByIndex = 0;
                for (int ii = 0; ii < mergeByLen; ii++) {
                    mergeBy[mergeByIndex++] = (Validator<T>)in.readObject();
                }
                arr[index++] = new Node<T>(validator, refCount, mergeBy);
            }
            this.arr = arr;
            this.minIndex = 0;
            this.maxIndex = len - 1;
        }
        
        private static int standardCapacity(int capacity) {
            int standard = 1;
            while (standard < capacity) {
                standard <<= 1;
            }
            return standard;
        }
    }
    
    private static class Node<T> {
        
        private Validator<T> validator;
        
        private int refCount;
        
        private Validator<T>[] mergedBy;
        
        Node(Validator<T> validator) {
            this.validator = validator;
            this.refCount = 1;
        }
        
        Node(Node<T> node) {
            this.validator = node.validator;
            this.refCount = node.refCount;
            if (node.mergedBy != null) {
                this.mergedBy = Arrays.copyOf(node.mergedBy, node.mergedBy.length);
            }
        }
        
        Node(Validator<T> validator, int refCount, Validator<T>[] mergedBy) {
            this.validator = validator;
            this.refCount = refCount;
            this.mergedBy = mergedBy;
        }

        @SuppressWarnings("unchecked")
        void mergedBy(Validator<T> validator) {
            Validator<?>[] arr = this.mergedBy;
            int len = arr != null ? arr.length : 0;
            for (int i = 0; i < len; i++) {
                if (arr[i] == null) {
                    arr[i] = validator;
                    return;
                }
            }
            Validator<T>[] newArr;
            if (len == 0) {
                newArr = new Validator[] { validator };
            } else {
                newArr = new Validator[len << 1];
                System.arraycopy(arr, 0, newArr, 0, len);
                newArr[len] = validator;
            }
            this.mergedBy = newArr;
        }
        
        @SuppressWarnings("unchecked")
        void unmergedBy(Validator<T> validator) {
            Validator<T>[] arr = this.mergedBy;
            if (arr == null) {
                return;
            }
            int len = arr.length;
            int nullCount = 0;
            for (int i = 0; i < len; i++) {
                if (arr[i] == validator) {
                    arr[i] = null;
                    nullCount++;
                } else if (arr[i] == null) {
                    nullCount++;
                }
            }
            if ((nullCount << 1) >= len) {
                Validator<T>[] newArr;
                if (len == 1) {
                    newArr = null; 
                } else {
                    newArr = new Validator[len >> 1];
                    int index = 0;
                    for (int i = 0; i < len; i++) {
                        if (arr[i] != null) {
                            newArr[index++] = arr[i];
                        }
                    }
                }
                this.mergedBy = newArr;
            }
        }
    }
    
    private static class NodeItr<T> implements Iterator<Node<T>> {
        
        private Node<T>[] arr;
        
        private int index;
        
        private int maxIndex;
        
        NodeItr(Node<T>[] arr, int minIndex, int maxIndex) {
            this.arr = arr;
            this.index = minIndex;
            this.maxIndex = maxIndex;
            this.skipNull();
        }

        @Override
        public boolean hasNext() {
            return this.index <= this.maxIndex;
        }

        @Override
        public Node<T> next() {
            Node<T> raw = this.arr[this.index++];
            this.skipNull();
            return new Node<T>(raw);
        }

        @Deprecated
        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
        
        private void skipNull() {
            Node<T>[] arr = this.arr;
            int maxIndex = this.maxIndex;
            int index = this.index;
            while (index <= maxIndex && arr[index] == null) {
                index++;
            }
            this.index = index;
        }
    }
    
    private static class SingleNodeItr<T> implements Iterator<Node<T>> {
        
        private Node<T> node;
        
        SingleNodeItr(Validator<T> validator) {
            if (validator instanceof NullSafeWrapper<?>) {
                validator = ((NullSafeWrapper<T>)validator).raw;
            }
            if (validator instanceof TransientWrapper<?>) {
                validator = ((TransientWrapper<T>)validator).raw;
            }
            this.node = new Node<T>(validator);
        }

        @Override
        public boolean hasNext() {
            return this.node != null;
        }

        @Override
        public Node<T> next() {
            Node<T> node = this.node;
            this.node = null;
            return node;
        }

        @Deprecated
        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class NullSafeWrapper<T> implements Validator<T>, Serializable {
    
        private static final long serialVersionUID = -6414902344735472289L;
        
        Validator<T> raw;
        
        NullSafeWrapper(Validator<T> raw) {
            this.raw = Arguments.mustNotBeInstanceOfValue(
                    "raw", 
                    Arguments.mustNotBeInstanceOfValue(
                            "raw", 
                            Arguments.mustNotBeNull("raw", raw), 
                            CombinedImpl.class
                    ), 
                    NotNullImpl.class
            );
        }
        
        @Override
        public void validate(T e) {
            if (e != null) {
                this.raw.validate(e);
            }
        }
    }
    
    private static class NullSafeSuppressibleWrapper<T> extends NullSafeWrapper<T> implements SuppressibleValidator<T> {

        private static final long serialVersionUID = -8587549995703044485L;

        NullSafeSuppressibleWrapper(SuppressibleValidator<T> raw) {
            super(raw);
        }

        @Override
        public boolean suppress(Validator<T> other) {
            return ((SuppressibleValidator<T>)this.raw).suppress(other);
        }
    }
    
    private static class TransientWrapper<T> implements Validator<T>, Serializable {
        
        private static final long serialVersionUID = 9093681616146314737L;
        
        Validator<T> raw;
        
        TransientWrapper(Validator<T> raw) {
            this.raw = Arguments.mustNotBeInstanceOfValue(
                    "raw", 
                    Arguments.mustNotBeInstanceOfValue(
                            "raw", 
                            Arguments.mustNotBeNull("raw", raw), 
                            CombinedImpl.class
                    ), 
                    Serializable.class
            );
        }
        
        @Override
        public void validate(T e) {
            this.raw.validate(e);
        }
        
        protected Object writeReplace() throws ObjectStreamException {
            return TransientWritingReplacement.INSTANCE;
        }
    }
    
    private static class TransientSuppressibleWrapper<T> extends TransientWrapper<T> implements SuppressibleValidator<T> {
        
        TransientSuppressibleWrapper(SuppressibleValidator<T> raw) {
            super(raw);
        }
        
        @Override
        public boolean suppress(Validator<T> other) {
            return ((SuppressibleValidator<T>)this.raw).suppress(other);
        }
        
        protected Object writeReplace() throws ObjectStreamException {
            return TransientWritingReplacement.INSTANCE;
        }
    }
    
    private static class TransientWritingReplacement implements Serializable {
        
        private static final TransientWritingReplacement INSTANCE =
                new TransientWritingReplacement();

        private static final long serialVersionUID = -4218073436090269860L;
        
        protected Object readResolve() throws ObjectStreamException {
            return null;
        }
    }
    
    private static class NotNullImpl implements Validator<Object>, Serializable {

        private static final long serialVersionUID = -2319848519933839339L;
        
        private static final NotNullImpl INSTANCE = new NotNullImpl();
        
        private static final Replacement REPLACEMENT = new Replacement();

        @Override
        public void validate(Object e) {
            Arguments.mustNotBeNull("e", e);
        }
        
        protected final Object writeReplace() throws ObjectStreamException {
            return REPLACEMENT;
        }
        
        private static class Replacement implements Serializable {

            private static final long serialVersionUID = -7987752472855613703L;
            
            Object readResolve() throws ObjectStreamException {
                return INSTANCE;
            }
        }
    }
    
    private static class MinLengthImpl implements SuppressibleValidator<String>, Serializable {
        
        private static final long serialVersionUID = 1990077295405842175L;
        
        private int minLength;
        
        MinLengthImpl(int minLength) {
            this.minLength = Arguments.mustBeGreaterThanOrEqualToValue("minLength", minLength, 0);
        }

        @Override
        public void validate(String e) {
            Arguments.mustBeGreaterThanOrEqualToValue("e.length()", e.length(), this.minLength);
        }

        @Override
        public boolean suppress(Validator<String> other) {
            if (other instanceof MinLengthImpl) {
                return this.minLength >= ((MinLengthImpl)other).minLength;
            }
            return false;
        }
    }
    
    private static class MaxLengthImpl implements SuppressibleValidator<String>, Serializable {
        
        private static final long serialVersionUID = 8560281346053548685L;
        
        private int maxLength;
        
        MaxLengthImpl(int maxLength) {
            this.maxLength = Arguments.mustBeGreaterThanValue("maxLength", maxLength, 0);
        }

        @Override
        public void validate(String e) {
            Arguments.mustBeLessThanOrEqualToValue("e.length()", e.length(), this.maxLength);
        }

        @Override
        public boolean suppress(Validator<String> other) {
            if (other instanceof MaxLengthImpl) {
                return this.maxLength <= ((MaxLengthImpl)other).maxLength;
            }
            return false;
        }
    }
    
    private static class InstanceOfImpl implements SuppressibleValidator<Object>, Serializable {

        private static final long serialVersionUID = -5630204740240443438L;
        
        private static final Map<Class<?>, InstanceOfImpl> CACHE = new WeakHashMap<>();
        
        private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
                
        private Class<?> restrctionType;
        
        private Replacement replacment;
        
        static InstanceOfImpl of(Class<?> restrictionType) {
            InstanceOfImpl instanceOfImpl;
            Lock lock;
            
            (lock = CACHE_LOCK.readLock()).lock(); // 1st locking
            try {
                instanceOfImpl = CACHE.get(restrictionType); // 1st reading
            } finally {
                lock.unlock();
            }
            
            if (instanceOfImpl == null) { // 1st checking
                (lock = CACHE_LOCK.writeLock()).lock(); // 2nd locking
                try {
                    instanceOfImpl = CACHE.get(restrictionType); // 2nd reading
                    if (instanceOfImpl == null) { //2nd checking
                        instanceOfImpl = new InstanceOfImpl(restrictionType);
                        CACHE.put(restrictionType, instanceOfImpl);
                    }
                } finally {
                    lock.unlock();
                }
            }
            return instanceOfImpl;
        }

        private InstanceOfImpl(Class<?> restrictionType) {
            this.restrctionType = restrictionType;
            this.replacment = new Replacement(restrictionType);
        }
        
        @Override
        public void validate(Object e) {
            if (e != null) {
                Arguments.mustBeInstanceOfValue("e", e, this.restrctionType);
            }
        }

        @Override
        public boolean suppress(Validator<Object> other) {
            if (other instanceof InstanceOfImpl) {
                return ((InstanceOfImpl)other).restrctionType.isAssignableFrom(this.restrctionType);
            }
            return false;
        }

        protected final Object writeReplace() throws ObjectStreamException {
            return this.replacment;
        }
        
        private static class Replacement implements Serializable {
            
            private static final long serialVersionUID = 691519668573134235L;
            
            private Class<?> restrictionType;
            
            Replacement(Class<?> restrictionType) {
                this.restrictionType = restrictionType;
            }
            
            Object readResolve() throws ObjectStreamException {
                return of(this.restrictionType);
            }
        }
        
    }
}
