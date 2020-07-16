package controller;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import model.UserDAO;
import model.hibernate.UserHibernateDAO;
import model.hibernate.UserRoleHibernateDAO;
import model.pojo.Role;
import model.pojo.UserPojo;
import model.pojo.UserRole;

import org.apache.commons.codec.binary.Hex;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

public class ModifyUserController extends AbstractController {

	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;
	private UserRoleHibernateDAO userRoleDAO;
	
	public ModifyUserController() {
		userDAO = new UserHibernateDAO();
		userRoleDAO = new UserRoleHibernateDAO();
	}

	@Wire
    private Textbox username;
	@Wire
    private Textbox password;
	@Wire
    private Combobox role;
	@Wire
	private Textbox email;
	@Wire
	private Textbox confirmPassword;
	
	private UserPojo user;
    
    @Listen("onClick = #saveButton")
    public void save() throws Exception {
    	user = (UserPojo)super.getObject();
    	user.setUsername(username.getValue());
    	user.setEmail(this.email.getValue());
    	byte[] bytesOfPassword = password.getValue().getBytes();
    	MessageDigest md = MessageDigest.getInstance("MD5");
    	byte[] digest = md.digest(bytesOfPassword);
    	final String result = new String(Hex.encodeHex(digest));
    	user.setPassword(result);
    	UserRole userRole = null;
    	if (user.getId()==null){
    		userRole = new UserRole();
    	} else {
    		userRole = user.getUserRoles().iterator().next();
    	}
    	
    	userRole.setRole((Role)role.getSelectedItem().getValue());
    	userRole.setUser(user);
    	Set<UserRole> userRoles = new HashSet<UserRole>();
    	userRoles.add(userRole);
    	user.setUserRoles(userRoles);
    	if (user.getId()==null){
        	user.setCreateTime(new Timestamp(new Date().getTime()));
    		userDAO.save(user);
    		userRoleDAO.save(userRole);
    		Messagebox.show("Пользователь "+user.getUsername()+" успешно создан");
    	} else {
    		userDAO.update(user);
    		userRoleDAO.update(userRole);
    		Messagebox.show("Пользователь "+user.getUsername()+" успешно изменён");
    	}
    	close();
    	reload(user);
    }
}
