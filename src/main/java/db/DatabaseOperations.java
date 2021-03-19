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
    private void makeTransaction(Object obj) {
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

    private void extractMethodTransaction(ExtractMethod em) {
        if (em.getClassCommitData() != null){
            session.save(em);
            for(ClassCommitData ccd: em.getClassCommitData()){
                session.save(ccd);
            }
            session.save(em.getRefactoringData());
            makeTransaction(em);
        }
    }

    private void refactoringDataTransaction(RefactoringData rd) {
        if(rd.getProject() != null){
            session.save(rd);
            makeTransaction(rd);
        }
    }

    private void projectTransaction(Project p) {
        if(!p.getProjectName().isEmpty()){
            session.save(p);
            makeTransaction(p);
        }
    }

    public void databaseTransaction(Object obj){
        if (obj instanceof ExtractMethod)
            extractMethodTransaction((ExtractMethod) obj);
        else if (obj instanceof RefactoringData)
            refactoringDataTransaction((RefactoringData) obj);
        else if (obj instanceof Project)
            projectTransaction((Project) obj);
        closeSession();
    }

    public void closeSession(){
        session.close();
    }


}
