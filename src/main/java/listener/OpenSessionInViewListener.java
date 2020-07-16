package listener;

import java.util.List;

import model.hibernate.HibernateUtil;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.util.ExecutionCleanup;
import org.zkoss.zk.ui.util.ExecutionInit;

public class OpenSessionInViewListener implements ExecutionInit, ExecutionCleanup {

	static Logger log = Logger.getLogger(OpenSessionInViewListener.class.getName());
 
    public void init(Execution exec, Execution parent) {
        if (parent == null) { //the root execution of a servlet request
            log.debug("Starting a database transaction: "+exec);
            Transaction tr = HibernateUtil.getSessionFactory().getCurrentSession().getTransaction();
            if(!tr.isActive()) {
            	HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            }
        }
    }
 
    public void cleanup(Execution exec, Execution parent, List<Throwable> errs) throws Exception{
        if (parent == null) { //the root execution of a servlet request
            if (errs == null || errs.isEmpty()) {
                log.debug("Committing the database transaction: "+exec);
                Transaction tr = HibernateUtil.getSessionFactory().getCurrentSession().getTransaction();
                if (tr.isActive() && !tr.wasCommitted()){
                	tr.commit();
                }
            } else {
                final Throwable ex = (Throwable) errs.get(0);
                rollback(exec, ex);
            }
        }
    }
 
    private void rollback(Execution exec, Throwable ex) {
        try {
            if (HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive()) {
                log.debug("Trying to rollback database transaction after exception:"+ex);
                ex.printStackTrace();
                HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            }
        } catch (Throwable rbEx) {
            log.error("Could not rollback transaction after exception! Original Exception:\n"+ex, rbEx);
        }
    }

}