package net.thevpc.nsql;

import java.sql.ResultSet;
import net.thevpc.nuts.format.NVisitResult;

public class NSqlQueryRunnerContext<T> {

    private T userObject;
    private NSqlConnection c;
    private ResultSet rs;
    private NVisitResult visitResult;

    public NSqlQueryRunnerContext(T userObject, NSqlConnection c, ResultSet rs) {
        this.userObject = userObject;
        this.c = c;
        this.rs = rs;
    }

    public NVisitResult getVisitResult() {
        return visitResult;
    }

    public void setVisitResult(NVisitResult visitResult) {
        this.visitResult = visitResult;
    }

    public T userObject() {
        return userObject;
    }

    public NSqlConnection connection() {
        return c;
    }

    public ResultSet rs() {
        return rs;
    }
}
