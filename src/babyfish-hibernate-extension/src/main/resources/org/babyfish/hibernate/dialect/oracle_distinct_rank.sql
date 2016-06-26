CREATE OR REPLACE PACKAGE DISTINCT_RANK_UTIL
IS
    FUNCTION ALLOCATE_CONTEXT_ RETURN NUMBER;
    PROCEDURE RESET_CONTEXT_(contextId NUMBER);
    PROCEDURE FREE_CONTEXT_(contextId NUMBER);
    PROCEDURE ITERATE_(contextId NUMBER, val ROWID);
    PROCEDURE DELETE_(contextId NUMBER, val ROWID);
    PROCEDURE MERGE_(contextId1 NUMBER, contextId2 NUMBER);
    FUNCTION RANK_(contextId NUMBER) RETURN NUMBER;
END;

/

CREATE OR REPLACE PACKAGE BODY DISTINCT_RANK_UTIL
IS
    FUNCTION ALLOCATE_CONTEXT_ RETURN NUMBER
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.allocateContext() return int';
    PROCEDURE RESET_CONTEXT_(contextId NUMBER)
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.resetContext(int)';
    PROCEDURE FREE_CONTEXT_(contextId NUMBER)
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.freeContext(int)';
    PROCEDURE ITERATE_(contextId NUMBER, val ROWID)
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.iterate(int, oracle.sql.ROWID)';
    PROCEDURE DELETE_(contextId NUMBER, val ROWID)
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.delete(int, oracle.sql.ROWID)';
    PROCEDURE MERGE_(contextId1 NUMBER, contextId2 NUMBER)
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.merge(int, int)';
    FUNCTION RANK_(contextId NUMBER) RETURN NUMBER
        IS LANGUAGE JAVA NAME 'org.babyfish.hibernate.dialect.oracle.DistinctRankContext.rank(int) return int';
END;

/

CREATE OR REPLACE TYPE DISTINCT_RANK_CONTEXT AS OBJECT(

    CONTEXT_ID NUMBER,
    
    STATIC FUNCTION ODCIAggregateInitialize(ctx IN OUT DISTINCT_RANK_CONTEXT) RETURN NUMBER,
    
    MEMBER FUNCTION ODCIAggregateIterate(
        self IN OUT DISTINCT_RANK_CONTEXT, 
        val IN ROWID) RETURN NUMBER,
    
    MEMBER FUNCTION ODCIAggregateDelete(
        self IN OUT DISTINCT_RANK_CONTEXT, 
        val IN ROWID) RETURN NUMBER,
    
    MEMBER FUNCTION ODCIAggregateMerge(
        self IN OUT DISTINCT_RANK_CONTEXT, 
        ctx2 IN DISTINCT_RANK_CONTEXT) RETURN NUMBER,
    
    MEMBER FUNCTION ODCIAggregateTerminate(
        self IN DISTINCT_RANK_CONTEXT, 
        val OUT NUMBER,
        v_flags IN NUMBER) RETURN NUMBER
);

/

CREATE OR REPLACE TYPE BODY DISTINCT_RANK_CONTEXT 
IS
    STATIC FUNCTION ODCIAggregateInitialize(ctx IN OUT DISTINCT_RANK_CONTEXT) RETURN NUMBER
    IS
    BEGIN
        IF ctx IS NULL THEN
            ctx := DISTINCT_RANK_CONTEXT(DISTINCT_RANK_UTIL.ALLOCATE_CONTEXT_);
        ELSE
            DISTINCT_RANK_UTIL.RESET_CONTEXT_(ctx.CONTEXT_ID);
        END IF;
        RETURN ODCIConst.Success;
    END;
    
    MEMBER FUNCTION ODCIAggregateIterate(self IN OUT DISTINCT_RANK_CONTEXT, val IN ROWID) RETURN NUMBER
    IS
    BEGIN
        DISTINCT_RANK_UTIL.ITERATE_(self.CONTEXT_ID, val);
        RETURN ODCIConst.Success;
    END;
    
    MEMBER FUNCTION ODCIAggregateDelete(self IN OUT DISTINCT_RANK_CONTEXT, val IN ROWID) RETURN NUMBER
    IS
    BEGIN
        DISTINCT_RANK_UTIL.DELETE_(self.CONTEXT_ID, val);
        RETURN ODCIConst.Success;
    END;
    
    MEMBER FUNCTION ODCIAggregateMerge(self IN OUT DISTINCT_RANK_CONTEXT, ctx2 IN DISTINCT_RANK_CONTEXT) RETURN NUMBER
    IS
    BEGIN
        DISTINCT_RANK_UTIL.MERGE_(self.CONTEXT_ID, ctx2.CONTEXT_ID);
        RETURN ODCIConst.Success;
    END;
    
    MEMBER FUNCTION ODCIAggregateTerminate(
        self IN DISTINCT_RANK_CONTEXT, 
        val OUT NUMBER,
        v_flags IN NUMBER) RETURN NUMBER
    IS
    BEGIN
        val := DISTINCT_RANK_UTIL.RANK_(self.CONTEXT_ID);
        IF BITAND(v_flags, 1) = 0 THEN
            DISTINCT_RANK_UTIL.FREE_CONTEXT_(self.CONTEXT_ID);
        END IF;
        RETURN ODCIConst.Success;
    END;
END;

/

CREATE OR REPLACE FUNCTION DISTINCT_RANK(val ROWID) RETURN NUMBER  
    AGGREGATE USING DISTINCT_RANK_CONTEXT;
    
/
