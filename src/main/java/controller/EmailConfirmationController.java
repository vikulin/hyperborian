package controller;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

import model.UserDAO;
import model.UserRoleDAO;
import model.hibernate.UserHibernateDAO;
import model.hibernate.UserRoleHibernateDAO;
import model.pojo.UserPojo;
import model.pojo.UserRole;

public class EmailConfirmationController extends AbstractController {
	
	private static final long serialVersionUID = 2477189674166282440L;
	
	private UserDAO userDAO;
	private UserRoleDAO userRoleDAO;
	
	public EmailConfirmationController() {
		userDAO = new UserHibernateDAO();
		userRoleDAO = new UserRoleHibernateDAO();
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Execution exec = Executions.getCurrent();
		
		String token = exec.getParameter("token");
		if(token!=null) {
			UserRole userRole = RegisterUserController.registrationTokens.remove(token);
			if(userRole!=null) {
				UserPojo user = userRole.getUser();
				userDAO.save(user);
				userRoleDAO.save(userRole);
				String success = "Your registration has been completed!";
				message.setValue(success);
				Executions.sendRedirect("/");
			} else {
				String error = "registration token not found or has been expired";
				message.setValue(error);
			}
		} else {
			String error = "registration token is missing";
			message.setValue(error);
		}
	}
	
	@Wire
	private Label message;

}
