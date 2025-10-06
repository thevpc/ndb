package net.thevpc.nsql;

import net.thevpc.nuts.text.NVisitResult;

import java.sql.ResultSet;

public class NSqlQueryCallerContext<V> {
    private Object userObject;
    private NSqlConnection c;
    private ResultSet rs;
    private NSafeResultSet srs;
    private V resultValue;
    private NVisitResult visitResult;

    public NSqlQueryCallerContext(Object userObject, NSqlConnection c, ResultSet rs) {
        this.userObject = userObject;
        this.c = c;
        this.rs = rs;
        this.srs = new NSafeResultSet(rs);
    }

    public <T> T userObject() {
        return (T) userObject;
    }

    public NSqlConnection connection() {
        return c;
    }

    public ResultSet rs() {
        return rs;
    }
    public NSafeResultSet srs() {
        return srs;
    }
    public NSqlQueryCallerContext<V> terminate(V resultValue) {
        visitResult=NVisitResult.TERMINATE;
        this.resultValue=resultValue;
        return this;
    }

    public V getResultValue() {
        return resultValue;
    }

    public NVisitResult getVisitResult() {
        return visitResult;
    }
}
