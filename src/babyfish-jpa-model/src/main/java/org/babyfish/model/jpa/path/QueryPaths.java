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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.path.FetchPath.Builder;
import org.babyfish.model.jpa.path.spi.QueryPathBaseVisitor;
import org.babyfish.model.jpa.path.spi.QueryPathLexer;
import org.babyfish.model.jpa.path.spi.QueryPathParser;
import org.babyfish.model.jpa.path.spi.QueryPathParser.FetchNodeContext;
import org.babyfish.model.jpa.path.spi.QueryPathParser.FetchPathContext;
import org.babyfish.model.jpa.path.spi.QueryPathParser.MainContext;
import org.babyfish.model.jpa.path.spi.QueryPathParser.OrderNodeContext;
import org.babyfish.model.jpa.path.spi.QueryPathParser.QueryPathContext;
import org.babyfish.model.jpa.path.spi.QueryPathParser.SimpleOrderPathContext;
import org.babyfish.model.jpa.path.spi.QueryPathParser.SingleOrderPathContext;

/**
 * @author Tao Chen
 */
public class QueryPaths {
    
    private static final Map<String, List<QueryPath>> LEVEL_ONE_CACHE = new WeakHashMap<>();
    
    private static final int LEVEL_2_CACHE_MAX_SIZE = 512;
    
    private static final QueryPath[] EMPTY_QUERY_PATHS = new QueryPath[0];
    
    private static final XOrderedMap<String, List<QueryPath>> LEVEL_TWO_CACHE = 
            new LinkedHashMap<>(
                    (LEVEL_2_CACHE_MAX_SIZE * 4 + 2) / 3, 
                    .75F, 
                    false, 
                    OrderAdjustMode.NEXT, 
                    OrderAdjustMode.NEXT
            );
            
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();

    protected QueryPaths() {
        throw new UnsupportedOperationException();
    }
    
    public static FetchPath.Builder begin() {
        return new FetchPathBuilderImpl();
    }
    
    public static SimpleOrderPath.Builder preOrderBy() {
        return new SimpleOrderPathBuilderImpl(false);
    }
    
    public static SimpleOrderPath.Builder postOrderBy() {
        return new SimpleOrderPathBuilderImpl(true);
    }
    
    public static QueryPath[] compile(String queryPath) {
        return compile(new String[] { queryPath });
    }
    
    public static QueryPath[] compile(String ... queryPaths) {
        List<QueryPath> list = new ArrayList<>();
        if (queryPaths != null) {
            for (String queryPath : queryPaths) {
                if (queryPath != null && !queryPath.isEmpty()) {
                    list.addAll(compileViaCache(queryPath));
                }
            }
        }
        if (list.isEmpty()) {
            return EMPTY_QUERY_PATHS;
        }
        return list.toArray(new QueryPath[list.size()]);
    }
    
    public static QueryPath[] combine(QueryPath[] pathArr1, QueryPath ... pathArr2) {
        if (Nulls.isNullOrEmpty(pathArr1)) {
            return pathArr2 != null ? pathArr2 : EMPTY_QUERY_PATHS;
        }
        if (Nulls.isNullOrEmpty(pathArr2)) {
            return pathArr1 != null ? pathArr1 : EMPTY_QUERY_PATHS;
        }
        QueryPath[] arr = new QueryPath[pathArr1.length + pathArr2.length];
        System.arraycopy(pathArr1, 0, arr, 0, pathArr1.length);
        System.arraycopy(pathArr2, 0, arr, pathArr1.length, pathArr2.length);
        return arr;
    }
    
    public static QueryPath[] combine(QueryPath path, QueryPath ... pathArr) {
        if (path == null) {
            return pathArr;
        }
        if (Nulls.isNullOrEmpty(pathArr)) {
            return new QueryPath[] { path };
        }
        QueryPath[] arr = new QueryPath[pathArr.length + 1];
        arr[0] = path;
        System.arraycopy(pathArr, 0, arr, 1, pathArr.length);
        return arr;
    }
    
    public static QueryPath[] fetchPaths(QueryPath ... pathArr) {
        if (Nulls.isNullOrEmpty(pathArr)) {
            return pathArr;
        }
        int len = 0;
        for (QueryPath queryPath : pathArr) {
            if (queryPath instanceof FetchPath) {
                len++;
            }
        }
        if (len == pathArr.length) {
            return pathArr;
        }
        QueryPath[] arr = new QueryPath[len];
        len = 0;
        for (QueryPath queryPath : pathArr) {
            if (queryPath instanceof FetchPath) {
                arr[len++] = queryPath;
            }
        }
        return arr;
    }
    
    public static QueryPath[] simpleOrderPaths(QueryPath ... pathArr) {
        if (Nulls.isNullOrEmpty(pathArr)) {
            return pathArr;
        }
        int len = 0;
        for (QueryPath queryPath : pathArr) {
            if (queryPath instanceof SimpleOrderPath) {
                len++;
            }
        }
        if (len == pathArr.length) {
            return pathArr;
        }
        QueryPath[] arr = new QueryPath[len];
        len = 0;
        for (QueryPath queryPath : pathArr) {
            if (queryPath instanceof SimpleOrderPath) {
                arr[len++] = queryPath;
            }
        }
        return arr;
    }
    
    private static List<QueryPath> compileViaCache(String queryPath) {
        
        Lock lock;
        List<QueryPath> compileResult;
        
        (lock = CACHE_LOCK.readLock()).lock(); //1st locking
        try {
            compileResult = LEVEL_ONE_CACHE.get(queryPath); //1st level-1 reading
            if (compileResult == null) { //1st level-1 checking
                compileResult = LEVEL_TWO_CACHE.access(queryPath); //1st level-2 reading
            }
        } finally {
            lock.unlock();
        }
        
        if (compileResult == null) { //1st level-2 checking
            (lock = CACHE_LOCK.writeLock()).lock(); //2nd locking
            try {
                compileResult = LEVEL_ONE_CACHE.get(queryPath); //2nd level-1 reading
                if (compileResult == null) { //2nd level-1 checking
                    compileResult = LEVEL_TWO_CACHE.access(queryPath); //2nd level-2 reading
                    if (compileResult == null) { //2nd level-2 checking
                        ANTLRInputStream input = new ANTLRInputStream(queryPath);
                        QueryPathLexer lexer = new QueryPathLexer(input);
                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                        QueryPathParser parser = new QueryPathParser(tokens);
                        QueryPathErrorListener queryPathErrorListener = new QueryPathErrorListener();
                        lexer.removeErrorListeners();
                        lexer.addErrorListener(queryPathErrorListener);
                        parser.removeErrorListeners();
                        parser.addErrorListener(queryPathErrorListener);
                        VisitorImpl visitor = new VisitorImpl();
                        parser.main().accept(visitor);
                        compileResult = visitor.getQueryPaths();
                        
                        //save to level-2 cache
                        for (int i = LEVEL_TWO_CACHE.size() - LEVEL_2_CACHE_MAX_SIZE; i >= 0; i--) {
                            LEVEL_TWO_CACHE.pollFirstEntry();
                        }
                        LEVEL_TWO_CACHE.put(queryPath, compileResult);
                    }
                    //save to level-1 cache
                    LEVEL_ONE_CACHE.put(queryPath, compileResult);
                }
            } finally {
                lock.unlock();
            }
        }
        return compileResult;
    }

    public static boolean isStandard(QueryPath queryPath) {
        if (queryPath == null) {
            return true;
        }
        Class<?> type = queryPath.getClass();
        return type == FetchPathImpl.class || type == SimpleOrderPathImpl.class;
    }
    
    public static QueryPath toStandard(QueryPath queryPath) {
        if (isStandard(queryPath)) {
            return queryPath;
        }
        if (queryPath instanceof QueryPathWrapper) {
            return toStandard(((QueryPathWrapper)queryPath).unwrap());
        }
        if (queryPath instanceof FetchPath) {
            FetchPath fetchPath = (FetchPath)queryPath;
            FetchPath.Builder builder = new FetchPathBuilderImpl();
            for (FetchPath.Node node = fetchPath.getFirstNode(); node != null; node = node.getNextNode()) {
                builder.get(node.getName(), node.getGetterType(), node.getCollectionFetchType());
            }
            return builder.end();
        }
        SimpleOrderPath simpleOrderPath = (SimpleOrderPath)queryPath;
        SimpleOrderPath.Builder builder = new SimpleOrderPathBuilderImpl(simpleOrderPath.isPost());
        for (SimpleOrderPath.Node node = simpleOrderPath.getFirstNode(); node != null; node = node.getNextNode()) {
            builder.get(node.getName(), node.getGetterType());
        }
        return simpleOrderPath.isDesc() ? builder.desc() : builder.asc();
    }
    
    private final static class FetchPathImpl implements FetchPath {
        
        private static final long serialVersionUID = -3235830851625359618L;
        
        private FetchPathNodeImpl firstNode;
        
        private transient String toString;
        
        FetchPathImpl(FetchPathNodeImpl firstNode) {
            this.firstNode = firstNode;
        }

        @Override
        public Node getFirstNode() {
            return this.firstNode;
        }
        
        @Override
        public int hashCode() {
            return this.firstNode.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FetchPath)) {
                return false;
            }
            FetchPath other = (FetchPath)obj;
            return this.firstNode.equals(other.getFirstNode());
        }

        @Override
        public String toString() {
            String toString = this.toString;
            if (toString == null) {
                StringBuilder builder = new StringBuilder("this");
                this.firstNode.toString(builder);
                this.toString = toString = builder.toString(); 
            }
            return toString;
        }
    }
    
    private static final class FetchPathNodeImpl implements FetchPath.Node, Serializable {

        private static final long serialVersionUID = -1545661515013667415L;

        private String name;
        
        private GetterType getterType;
        
        private CollectionFetchType collectionFetchType;
        
        private FetchPathNodeImpl nextNode;
        
        private int hash;
        
        private transient String toString;

        FetchPathNodeImpl(
                String name, 
                GetterType getterType, 
                CollectionFetchType collectionFetchType,
                FetchPathNodeImpl nextNode) {
            this.name = name;
            this.getterType = getterType;
            this.collectionFetchType = collectionFetchType;
            this.nextNode = nextNode;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public GetterType getGetterType() {
            return this.getterType;
        }

        @Override
        public CollectionFetchType getCollectionFetchType() {
            return this.collectionFetchType;
        }
        
        @Override
        public FetchPath.Node getNextNode() {
            return this.nextNode;
        }

        @Override
        public int hashCode() {
            int hash = this.hash;
            if (hash == 0) {
                hash = Nulls.hashCode(this.name);
                hash = 31 * hash + this.getterType.hashCode();
                hash = 31 * hash + this.collectionFetchType.hashCode();
                if (this.nextNode != null) {
                    hash = 31 * hash + this.nextNode.hashCode();
                }
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
            if (obj instanceof FetchPathNodeImpl) {
                return this.fastEquals((FetchPathNodeImpl)obj);
            }
            if (!(obj instanceof FetchPath.Node)) {
                return false;
            }
            FetchPath.Node other = (FetchPath.Node)obj;
            return Nulls.equals(this.name, other.getName()) &&
                    this.getterType == other.getGetterType() &&
                    this.collectionFetchType == other.getCollectionFetchType() &&
                    (this.nextNode == null ? other.getNextNode() == null : this.nextNode.equals(other.getNextNode()));
        }
        
        private boolean fastEquals(FetchPathNodeImpl other) {
            return Nulls.equals(this.name, other.name) &&
                    this.getterType == other.getterType &&
                    this.collectionFetchType == other.collectionFetchType &&
                    (this.nextNode == null ? other.nextNode == null : this.nextNode.fastEquals(other.nextNode));
        }

        @Override
        public String toString() {
            String toString = this.toString;
            if (toString == null) {
                StringBuilder builder = new StringBuilder();
                this.toString(builder);
                this.toString = toString = builder.toString(); 
            }
            return toString;
        }
        
        void toString(StringBuilder builder) {
            if (this.name == null) {
                builder.append("this");
            }
            else {
                if (this.getterType == GetterType.REQUIRED) {
                    builder.append("..");
                } else {
                    builder.append('.');
                }
                if (this.collectionFetchType == CollectionFetchType.PARTIAL) {
                    builder.append("partial(");
                    builder.append(this.name);
                    builder.append(')');
                } else {
                    builder.append(this.name);
                }
            }
            if (this.nextNode != null) {
                this.nextNode.toString(builder);
            }
        }
    }
    
    private static final class FetchPathBuilderImpl implements FetchPath.Builder {
        
        // name, getterType, collectionFetchType, 
        // name, getterType, collectionFetchType, 
        // ... 
        private Object[] arr;
        
        private int len;
        
        public FetchPathBuilderImpl() {
            this.arr = new Object[3 * 4];
        }
        
        @Override
        public FetchPath.Builder get(String nodeName) {
            return this.get(nodeName, GetterType.OPTIONAL, CollectionFetchType.ALL);
        }

        @Override
        public FetchPath.Builder get(String nodeName, GetterType getterType) {
            return this.get(nodeName, getterType, CollectionFetchType.ALL);
        }

        @Override
        public FetchPath.Builder get(String nodeName, CollectionFetchType collectionFetchType) {
            return this.get(nodeName, GetterType.OPTIONAL, collectionFetchType);
        }

        @Override
        public Builder get(
                String nodeName, 
                GetterType getterType,
                CollectionFetchType collectionFetchType) {
            Arguments.mustNotBeEmpty("nodeName", Arguments.mustNotBeNull("nodeName", nodeName));
            Arguments.mustNotBeNull("getterType", getterType);
            Arguments.mustNotBeNull("collectionFetchType", collectionFetchType);
            Object[] arr = this.arr;
            int len = this.len;
            if (len + 3 > arr.length) {
                Object[] newArr = new Object[len << 1];
                System.arraycopy(arr, 0, newArr, 0, len);
                this.arr = arr = newArr;
            }
            arr[len] = nodeName;
            arr[len + 1] = getterType;
            arr[len + 2] = collectionFetchType;
            this.len += 3;
            return this;
        }

        @Override
        public FetchPath end() {
            FetchPathNodeImpl firstNode = null;
            Object[] arr = this.arr;
            for (int i = this.len - 3; i >= 0; i -= 3) {
                firstNode = new FetchPathNodeImpl(
                        (String)arr[i], 
                        (GetterType)arr[i + 1],
                        (CollectionFetchType)arr[i + 2],
                        firstNode);
            }
            return new FetchPathImpl(firstNode);
        }
    }
    
    private static class SimpleOrderPathImpl implements SimpleOrderPath {
    
        private static final long serialVersionUID = 4218621135114779440L;
        
        private boolean post;
        
        private boolean desc;
        
        private SimpleOrderPathNodeImpl firstNode;
        
        private int hash;
        
        private transient String toString;
        
        SimpleOrderPathImpl(boolean post, boolean desc, SimpleOrderPathNodeImpl firstNode) {
            this.post = post;
            this.desc = desc;
            this.firstNode = firstNode;
            int hash = firstNode != null ? firstNode.hashCode() : 0;
            hash = 31 * hash + (post ? 1231 : 1237);
            hash = 31 * hash + (desc ? 1231 : 1237);
            this.hash = hash;
        }
        
        @Override
        public boolean isPost() {
            return this.post;
        }
    
        @Override
        public boolean isDesc() {
            return this.desc;
        }
    
        @Override
        public Node getFirstNode() {
            return this.firstNode;
        }
        
        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SimpleOrderPath)) {
                return false;
            }
            SimpleOrderPath other = (SimpleOrderPath)obj;
            return this.firstNode == null ? other.getFirstNode() == null : this.firstNode.equals(other.getFirstNode());
        }

        @Override
        public String toString() {
            String toString = this.toString;
            if (toString == null) {
                StringBuilder builder = new StringBuilder();
                if (this.post) {
                    builder.append("post order by this");
                } else {
                    builder.append("pre order by this");
                }
                if (this.firstNode != null) {
                    this.firstNode.toString(builder);
                }
                if (this.desc) {
                    builder.append(" desc");
                } else {
                    builder.append(" asc");
                }
                this.toString = toString = builder.toString();
            }
            return toString;
        }
    }
    
    private static final class SimpleOrderPathNodeImpl implements SimpleOrderPath.Node, Serializable {
        
        private static final long serialVersionUID = -4710765418728981017L;
        
        private String name;
        
        private GetterType getterType;
        
        private SimpleOrderPathNodeImpl nextNode;
        
        private int hash;
        
        private transient String toString;

        SimpleOrderPathNodeImpl(String name, GetterType getterType, SimpleOrderPathNodeImpl nextNode) {
            this.name = name;
            this.getterType = getterType;
            this.nextNode = nextNode;
            int hash = name.hashCode();
            hash = 31 * hash + getterType.hashCode();
            if (nextNode != null) {
                hash = 31 * hash + nextNode.hash;
            }
            this.hash = hash;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public GetterType getGetterType() {
            return this.getterType;
        }

        @Override
        public SimpleOrderPath.Node getNextNode() {
            return this.nextNode;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof SimpleOrderPathNodeImpl) {
                return this.fastEquals((SimpleOrderPathNodeImpl)obj);
            }
            if (!(obj instanceof SimpleOrderPath.Node)) {
                return false;
            }
            SimpleOrderPath.Node other = (SimpleOrderPath.Node)obj;
            return this.name.equals(other.getName()) &&
                    this.getterType == other.getGetterType() &&
                    (this.nextNode == null ? other.getNextNode() == null : this.nextNode.equals(other.getNextNode()));
        }
        
        private boolean fastEquals(SimpleOrderPathNodeImpl other) {
            return this.name.equals(other.name) &&
                    this.getterType == other.getterType &&
                    (this.nextNode == null ? other.nextNode == null : this.nextNode.fastEquals(other.nextNode));
        }

        @Override
        public String toString() {
            String toString = this.toString;
            if (toString == null) {
                StringBuilder builder = new StringBuilder();
                this.toString(builder);
                this.toString = toString = builder.toString(); 
            }
            return toString;
        }
        
        void toString(StringBuilder builder) {
            if (this.getterType == GetterType.REQUIRED) {
                builder.append("..");
            } else {
                builder.append('.');
            }
            builder.append(this.name);
            if (this.nextNode != null) {
                this.nextNode.toString(builder);
            }
        }
    }
    
    static class SimpleOrderPathBuilderImpl implements SimpleOrderPath.Builder {
        
        private boolean post;
        
        //name, getterType, name, getterType ... 
        private Object[] arr = new Object[6];
        
        private int len;
        
        protected SimpleOrderPathBuilderImpl(boolean post) {
            this.post = post;
        }
        
        @Override
        public SimpleOrderPath.Builder get(String nodeName) {
            return this.get(nodeName, GetterType.OPTIONAL);
        }
        
        @Override
        public SimpleOrderPath.Builder get(String nodeName, GetterType getterType) {
            Arguments.mustNotBeEmpty("nodeName", Arguments.mustNotBeNull("nodeName", nodeName));
            Arguments.mustNotBeNull("getterType", getterType);
            Object[] arr = this.arr;
            int len = this.len;
            if (len + 2 > arr.length) {
                Object[] newArr = new Object[len << 1];
                System.arraycopy(arr, 0, newArr, 0, len);
                this.arr = arr = newArr;
            }
            arr[len] = nodeName;
            arr[len + 1] = getterType;
            this.len += 2;
            return this;
        }
        
        @Override
        public SimpleOrderPath asc() {
            return this.end(false);
        }
        
        @Override
        public SimpleOrderPath desc() {
            return this.end(true);
        }
        
        private SimpleOrderPath end(boolean desc) {
            SimpleOrderPathNodeImpl simpleOrderPathNode = null;
            Object[] arr = this.arr;
            for (int i = this.len - 2; i >= 0; i -= 2) {
                simpleOrderPathNode = new SimpleOrderPathNodeImpl(
                        (String)arr[i], 
                        (GetterType)arr[i + 1], 
                        simpleOrderPathNode);
            }
            return new SimpleOrderPathImpl(this.post, desc, simpleOrderPathNode);
        }
    }
    
    private static class VisitorImpl extends QueryPathBaseVisitor<Void> {
        
        private List<QueryPath> queryPaths = new ArrayList<>();
        
        private FetchPath.Builder fetchPathBuilder;
        
        private SimpleOrderPath.Builder simpleOrderPathBuilder;
        
        private boolean postOrder;
        
        List<QueryPath> getQueryPaths() {
            List<QueryPath> queryPaths = this.queryPaths;
            return MACollections.wrap(queryPaths.toArray(new QueryPath[queryPaths.size()]));
        }

        @Override
        public Void visitQueryPath(QueryPathContext ctx) {
            return super.visitQueryPath(ctx);
        }

        @Override
        public Void visitFetchPath(FetchPathContext ctx) {
            this.fetchPathBuilder = QueryPaths.begin();
            super.visitFetchPath(ctx);
            this.queryPaths.add(this.fetchPathBuilder.end());
            return null;
        }

        @Override
        public Void visitFetchNode(FetchNodeContext ctx) {
            String nodeName = ctx.name.getText();
            GetterType getterType = 
                    is(ctx.getterType, QueryPathParser.REQUIRED_DOT) ? 
                            GetterType.REQUIRED : 
                            GetterType.OPTIONAL;
            CollectionFetchType collectionFetchType =
                    is(ctx.collectionFetchType, QueryPathParser.PARTIAL) ?
                            CollectionFetchType.PARTIAL :
                            CollectionFetchType.ALL;
            this.fetchPathBuilder.get(nodeName, getterType, collectionFetchType);
            return super.visitFetchNode(ctx);
        }

        @Override
        public Void visitSimpleOrderPath(SimpleOrderPathContext ctx) {
            this.postOrder = is(ctx.orderOpportunity, QueryPathParser.POST); 
            return super.visitSimpleOrderPath(ctx);
        }
        
        @Override
        public Void visitSingleOrderPath(SingleOrderPathContext ctx) {
            this.simpleOrderPathBuilder = this.postOrder ? QueryPaths.postOrderBy() : QueryPaths.preOrderBy();
            super.visitSingleOrderPath(ctx);
            if (is(ctx.sortMode, QueryPathParser.DESC)) {
                this.queryPaths.add(this.simpleOrderPathBuilder.desc());
            } else {
                this.queryPaths.add(this.simpleOrderPathBuilder.asc());
            }
            return null;
        }

        @Override
        public Void visitOrderNode(OrderNodeContext ctx) {
            String nodeName = ctx.name.getText();
            GetterType getterType = 
                    is(ctx.getterType, QueryPathParser.REQUIRED_DOT) ? 
                            GetterType.REQUIRED : 
                            GetterType.OPTIONAL;
            this.simpleOrderPathBuilder.get(nodeName, getterType);
            return null;
        }

        @Override
        public Void visitMain(MainContext ctx) {
            return super.visitMain(ctx);
        }
        
        private static boolean is(Token token, int type) {
            return token != null && token.getType() == type;
        }
    }
    
    private static class QueryPathErrorListener extends BaseErrorListener {

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol, 
                int line, 
                int charPositionInLine,
                String msg, 
                RecognitionException ex) {
            throw new QueryPathCompilationException(msg, ex, recognizer, offendingSymbol, line, charPositionInLine);
        }
    }
}
