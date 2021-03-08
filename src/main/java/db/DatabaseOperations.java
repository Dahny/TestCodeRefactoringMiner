package db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class DatabaseOperations {

    private Session session;

    public DatabaseOperations(SessionFactory sf) {
        session = sf.openSession();
    }


    // TODO make this transaction cleaner
    public void makeTransaction(Object obj) {
        Transaction transaction = null;
        try {
            // Check if there is actually data for the transaction
            if(obj != null) {
                transaction = session.beginTransaction();
                session.persist(obj);
                transaction.commit();
            }
        } catch(Exception e){
            e.printStackTrace();
            if(transaction != null)
                transaction.rollback();
        } finally {
            // close session after the transaction is done
            closeSession();
        }
    }

    public void closeSession(){
        session.close();
    }


}
