package db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class DatabaseOperations {

    private SessionFactory sessionFactory;
    private Session session;

    public DatabaseOperations(SessionFactory sf) {
        sessionFactory = sf;
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
        if (em.getClassCommitData() != null && em.getRefactoringClassMetrics() != null &&
                em.getRefactoringMethodMetrics() != null){
            session.save(em);
            session.save(em.getRefactoringData());

            for(ClassCommitData ccd: em.getClassCommitData())
                session.save(ccd);
            for(RefactoringMethodMetrics mmr: em.getRefactoringMethodMetrics())
                session.save(mmr);
            for(RefactoringClassMetrics mcr: em.getRefactoringClassMetrics())
                session.save(mcr);

            makeTransaction(em);
        } else {
            throw new RuntimeException("ExtractMethod Object:" + em.toString() + " is missing relevant metrics");
        }
    }

    private void refactoringDataTransaction(RefactoringData rd) {
        if(rd.getProject() != null){
            session.save(rd);
            makeTransaction(rd);
        } else {
            throw new RuntimeException("RefactoringData Object:" + rd.toString() + " is missing a project");
        }
    }

    private void projectTransaction(Project p) {
        if(p.getMetaData() != null){
            session.save(p);
            session.save(p.getMetaData());
            makeTransaction(p);
        } else {
            throw new RuntimeException("Project Object:" + p.toString() + " is missing a MetaData Object");
        }
    }

    public void databaseTransaction(Object obj){
        startSession();
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

    private void startSession() { session = sessionFactory.openSession(); }


}
