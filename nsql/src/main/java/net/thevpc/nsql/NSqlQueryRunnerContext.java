package net.thevpc.nsql;

import java.sql.ResultSet;
import net.thevpc.nuts.text.NVisitResult;

public class NSqlQueryRunnerContext<T> {

    private NSqlConnection c;
    private ResultSet rs;
    private NVisitResult visitResult;

    public NSqlQueryRunnerContext(NSqlConnection c, ResultSet rs) {
        this.c = c;
        this.rs = rs;
    }

    public NVisitResult getVisitResult() {
        return visitResult;
    }

    public void setVisitResult(NVisitResult visitResult) {
        this.visitResult = visitResult;
    }

    public NSqlConnection connection() {
        return c;
    }

    public ResultSet rs() {
        return rs;
    }
}
