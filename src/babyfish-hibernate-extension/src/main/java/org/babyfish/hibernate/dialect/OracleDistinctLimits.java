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
package org.babyfish.hibernate.dialect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Pattern;

import org.babyfish.collection.ArrayList;
import org.babyfish.hibernate.cfg.SettingsFactory;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Strings;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.StatelessSession;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
class OracleDistinctLimits {
    
    private static final String DISTINCT_ROWID_COUNTER_INTERNAL_NAME = 
            "org/babyfish/hibernate/dialect/oracle/DistinctRankContext";
    
    private static final String DISTINCT_RANK = "DISTINCT_RANK";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDistinctLimits.class);
    
    protected OracleDistinctLimits() {
        throw new UnsupportedOperationException();
    }
    
    public static String getOracleDistinctLimitString(String sql, boolean hasOffset) {
        
        boolean isForUpdate = false;
        if (sql.endsWith(" for update") ) {
            sql = sql.substring( 0, sql.length() - 11);
            isForUpdate = true;
        }
        
        int fromIndex = indexOfToppestStatement(sql, "from", true);
        String rootTableAlias = getRootTableAlias(sql, fromIndex);
        int orderByIndex =indexOfToppestStatement(sql, "order by", true);
        
        String orderBy;
        boolean useDistinctRank = false;
        String rankAlias = "dense_rank____";
        if (orderByIndex == -1) {
            orderBy = " order by " + rootTableAlias + ".rowid asc";
        } else {
            orderBy = sql.substring(orderByIndex);
            sql = sql.substring(0, orderByIndex);       
            String rootTableAliasPrefix = rootTableAlias + '.';
            for (String orderByColumn : splitOrderByCluase(orderBy)) {
                if (!orderByColumn.startsWith(rootTableAliasPrefix)) {
                    useDistinctRank = true;
                    break;
                }
            }
            if (!useDistinctRank) {
                orderBy += ", " + rootTableAlias + ".rowid asc";
            } else {
                rankAlias = "distinct_rank____";
            }
        }
        
        StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
        pagingSelect
        .append("select * from (")
        .append(sql.substring(0, fromIndex))
        .append(", ");
        
        if (useDistinctRank) {
            pagingSelect
            .append("distinct_rank(")
            .append(rootTableAlias)
            .append(".rowid)");
        } else {
            pagingSelect.append("dense_rank()");
        }
        
        pagingSelect
        .append(" over(")
        .append(orderBy)
        .append(") ")
        .append(rankAlias)
        .append(' ')
        .append(sql.substring(fromIndex))
        .append(") where ")
        .append(rankAlias)
        .append(" <= ?");
        if (hasOffset) {
            pagingSelect
            .append(" and ")
            .append(rankAlias)
            .append(" > ?");
        }
        if ( isForUpdate ) {
            pagingSelect.append( " for update" );
        }
        
        return pagingSelect.toString();
    }
    
    private static String getRootTableAlias(String sql, int fromIndex) {
        int index = fromIndex + 4;
        int whitespaceCount = 0;
        boolean isPreviousWhitespace = false;
        StringBuilder builder = new StringBuilder();
        while (true) {
            char c = sql.charAt(index++);
            if (Character.isWhitespace(c)) {
                if (!isPreviousWhitespace) {
                    if (builder.length() != 0) {
                        String value = builder.toString();
                        if (!value.equals("as")) {
                            return value;
                        }
                        builder = new StringBuilder();
                    }
                    isPreviousWhitespace = true;
                    whitespaceCount++;
                }
            } else {
                isPreviousWhitespace = false;
                if (whitespaceCount > 1) {
                    builder.append(c);
                }
            }
        }
    }
    
    private static int indexOfToppestStatement(String sql, String search, boolean mustBeWord) {
        char searchFirst = search.charAt(0);
        int searchLen = search.length();
        int statementDepth = 0;
        int len = sql.length();
        boolean inComment = false;
        boolean inQName = false;
        for (int i = 0; i < len; i++) {
            char c = sql.charAt(i);
            if (inComment) {
                if (c == '*' && i + 1 < len && sql.charAt(i + 1) == '/') {
                    inComment = false;
                    i++;
                }
                continue;
            } else if (c == '/' && i + 1 < len && sql.charAt(i + 1) == '*') {
                inComment = true;
                i++;
                continue;
            }
            
            if (inQName) {
                if (c == '"') {
                    inQName = false;
                }
                continue;
            } else if (c == '"') {
                inQName = true;
                continue;
            }
            
            if (c == '(') {
                statementDepth++;
                continue;
            }
            if (c == ')') {
                statementDepth--;
                continue;
            }
            
            if (statementDepth == 0 && c == searchFirst && i + searchLen <= len) {
                int ii = 0;
                while(ii < searchLen) {
                    if (sql.charAt(i + ii) != search.charAt(ii)) {
                        break;
                    }
                    ii++;
                }
                if (ii == searchLen) {
                    if (mustBeWord &&
                            (i != 0 && isValidIdentifierChar(sql.charAt(i - 1))) ||
                            (i + searchLen >= len && isValidIdentifierChar(sql.charAt(i + searchLen)))) {
                        i += searchLen - 1;
                        continue;
                    }
                    return i;
                }
            }
        }
        return -1;
    }
    
    private static List<String> splitOrderByCluase(String orderBy) {
        
        List<String> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        orderBy = orderBy.substring(9);
        
        int statementDepth = 0;
        int len = orderBy.length();
        boolean inComment = false;
        boolean inQName = false;
        for (int i = 0; i < len; i++) {
            char c = orderBy.charAt(i);
            if (inComment) {
                if (c == '*' && i + 1 < len && orderBy.charAt(i + 1) == '/') {
                    inComment = false;
                    i++;
                }
                continue;
            } else if (c == '/' && i + 1 < len && orderBy.charAt(i + 1) == '*') {
                inComment = true;
                i++;
                continue;
            }
            
            if (inQName) {
                if (c == '"') {
                    inQName = false;
                }
                builder.append(c);
                continue;
            } else if (c == '"') {
                inQName = true;
                builder.append(c);
                continue;
            }
            
            if (c == '(') {
                statementDepth++;
                builder.append(c);
                continue;
            }
            if (c == ')') {
                statementDepth--;
                builder.append(c);
                continue;
            }
            
            if (statementDepth == 0 && c == ',') {
                list.add(builder.toString().trim());
                builder = new StringBuilder();
            } else {
                builder.append(c);
            }
        }
        list.add(builder.toString().trim());
        return list;
    }
    
    private static boolean isValidIdentifierChar(char c) {
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c == '_') {
            return true;
        }
        if (c == '$') {
            return true;
        }
        return false;
    }

    public static void install(SessionFactoryImplementor sfi) {
        if (!SettingsFactory.isDistinctRankCreateable(sfi.getProperties())) {
            return;
        }
        LOGGER.info(
                tryToCreateAnalyticFunction(
                        SettingsFactory.CREATE_ORACLE_DISTINCT_RANK, 
                        "true", 
                        DISTINCT_RANK
                )
        );
        StatelessSession sls = sfi.openStatelessSession();
        try {
            Connection con = ((SessionImplementor)sls).connection();
            installPLSQLWrapper(con);
        } catch (SQLException ex) {
            throw new QueryException(ex);
        } catch (IOException ex) {
            throw new HibernateException("Can not install the installable dialect", ex);
        } finally {
            sls.close();
        }
    }
    
    private static void installPLSQLWrapper(Connection con) throws SQLException, IOException {
        
        boolean existing;
        
        //Don't use try(...) because lower version Oracle driver may not implement java7
        String queryFunctionSql = 
                "select object_name "
                + "from user_objects "
                + "where object_name = ? and object_type = ? "
                + "union "
                + "select synonym_name "
                + "from all_synonyms sy "
                + "inner join all_objects o "
                + "on sy.table_name = o.object_name "
                + "and sy.table_owner = o.owner "
                + "where sy.synonym_name = ? and o.object_type = ?";
        PreparedStatement pstmt = con.prepareStatement(queryFunctionSql);
        try {
            pstmt.setString(1, DISTINCT_RANK);
            pstmt.setString(2, "FUNCTION");
            pstmt.setString(3, DISTINCT_RANK);
            pstmt.setString(4, "FUNCTION");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        queryFunctionSql
                        + " with parameters: "
                        + Strings.join(new String[] { 
                                DISTINCT_RANK,
                                "FUNCTION",
                                DISTINCT_RANK,
                                "FUNCTION"
                        })
                );
            }
            ResultSet rs = pstmt.executeQuery();
            try {
                existing = rs.next(); 
            } finally {
                rs.close();
            }
        } finally {
            pstmt.close(); 
        }
        
        if (existing) {
            LOGGER.info(analyticFunctionDoesExists(DISTINCT_RANK));
            return;
        }
        
        installJavaImpl(con);
        
        LOGGER.info(analyticFunctionDoesNotExists(DISTINCT_RANK));
        
        List<String> sqls = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Oracle10gDialect.class.getResourceAsStream("oracle_distinct_rank.sql")
                )
            )
        ) {
            while (true) {
                String line = reader.readLine();
                String trimedLine = line;
                if (line != null) {
                    trimedLine = line.trim();
                }
                if (line == null || trimedLine.equals("/")) {
                    if (builder.length() != 0) {
                        sqls.add(builder.toString());
                        builder = new StringBuilder();
                    }
                }
                if (line == null) {
                    break;
                }
                if (trimedLine.isEmpty() || trimedLine.equals("/")) {
                    continue;
                }
                builder.append(line).append('\n');
            }
        }
        for (String sql : sqls) {
            Statement stmt = con.createStatement();
            try {
                LOGGER.info(sql);
                stmt.execute(sql);
            } finally {
                stmt.close(); 
            }
        }
    }

    private static void installJavaImpl(Connection con) throws SQLException, IOException {
        
        boolean existing;
        
        //Don't use try(...) because lower version Oracle driver may not implement java7
        String queryJavaClassSql = 
                "select name "
                + "from all_java_classes "
                + "where name = ? "
                + "union "
                + "select synonym_name "
                + "from all_synonyms s "
                + "inner join all_java_classes jc "
                + "on s.table_name = jc.name "
                + "and s.table_owner = jc.owner "
                + "where synonym_name = ?";
        PreparedStatement pstmt = con.prepareStatement(queryJavaClassSql);
        try {
            pstmt.setString(1, DISTINCT_ROWID_COUNTER_INTERNAL_NAME);
            pstmt.setString(2, DISTINCT_ROWID_COUNTER_INTERNAL_NAME);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        queryJavaClassSql
                        + " with parameters: "
                        + Strings.join(new String[] { 
                                DISTINCT_ROWID_COUNTER_INTERNAL_NAME,
                                DISTINCT_ROWID_COUNTER_INTERNAL_NAME
                        })
                );
            }
            ResultSet rs = pstmt.executeQuery();
            try {
                existing = rs.next(); 
            } finally {
                rs.close();
            }
        } finally {
            pstmt.close(); 
        }
        
        if (existing) {
            LOGGER.info(javaClassDoesExists(DISTINCT_ROWID_COUNTER_INTERNAL_NAME));
            return;
        }
        
        LOGGER.info(javaClassDoesNotExists(DISTINCT_ROWID_COUNTER_INTERNAL_NAME));
        
        // Oracle JDBC driver has a bug, it can not execute the sql like this
        //      CREATE OR REPLACE AND COMPILE JAVA SOURCE NAMED ${name} AS
        //          ${java source code};
        //
        // Fortunately, Oracle supports dynamic SQL, so the embedded sql resource 
        // "oracle_distinct_rank_java.sql" can be execute as dyanmic sql in pl/sql block
        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN\n");
        builder.append("\tEXECUTE IMMEDIATE\n\t\t");
        try (BufferedReader reader = 
                new BufferedReader(
                        new InputStreamReader(
                                Oracle10gDialect.class.getResourceAsStream("oracle_distinct_rank_java.sql")
                        )
                )
        ) {
            boolean addConcat = false;
            Pattern singleQuotePattern = Pattern.compile("'", Pattern.LITERAL);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                line = singleQuotePattern.matcher(line).replaceAll("''");
                if (addConcat) {
                    builder.append("\n\t\t|| chr(10) || ");
                }
                builder.append('\'').append(line).append('\'');
                addConcat = true;
            }
            builder.append(";\nEND;\n");
        }
        
        //Don't use try(...) because lower version Oracle driver may not implement java7
        Statement stmt = con.createStatement();
        try {
            String sql = builder.toString();
            LOGGER.info(sql);
            stmt.execute(sql);
        } finally {
            stmt.close(); 
        }
    }
    
    @I18N
    private static native String tryToCreateAnalyticFunction(
                String babyfishHiberantePropertyName,
                String babyfishHiberantePropertyValue,
                String analyticFunctionName);
        
    @I18N
    private static native String javaClassDoesNotExists(String javaClassName);
        
    @I18N
    private static native String javaClassDoesExists(String javaClassName);
        
    @I18N
    private static native String analyticFunctionDoesNotExists(String analyticFunctionName);
        
    @I18N
    private static native String analyticFunctionDoesExists(String analyticFunctionName);
}
