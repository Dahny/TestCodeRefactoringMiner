package db;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class DatabaseOperations {
    private final Session session;

    public DatabaseOperations(Session session) {
        this.session = session;
    }

    // TODO make this transaction cleaner
    public void makeTransaction(Object obj) {
        try {
            if(obj != null) {
                Transaction t = session.beginTransaction();
                session.persist(obj);
                t.commit();
            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }

}
