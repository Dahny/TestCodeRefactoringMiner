package db;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class DatabaseOperations {
    private final Session session;

    public DatabaseOperations(Session session) {
        this.session = session;
    }

    public void makeTransaction(Object obj) {
        Transaction t = session.beginTransaction();
        session.persist(obj);
        t.commit();
    }

}