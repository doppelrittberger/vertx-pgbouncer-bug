package org.example;

import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;

public class JdbiWrapper {
    private final Jdbi jdbi;

    JdbiWrapper(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public <R, X extends Exception> R inTransaction(final HandleCallback<R, X> callback) throws X {
        return jdbi.inTransaction(callback);
    }

    public <R, X extends Exception> R withHandle(final HandleCallback<R, X> callback) throws X {
        return jdbi.withHandle(callback);
    }
}
