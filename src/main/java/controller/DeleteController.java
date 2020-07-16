package controller;

import model.hibernate.HibernateUtil;

import org.hibernate.Session;
import org.zkoss.zk.ui.select.annotation.Listen;

public class DeleteController extends AbstractController {

	private static final long serialVersionUID = 3031513248715349621L;

    @Listen("onClick = #yes")
    public void delete() throws Exception {
    	Object obj = getObject();
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    	if (obj!=null){
			session.delete(obj);
			super.delete(obj);
			close();
    	}
    }

}
