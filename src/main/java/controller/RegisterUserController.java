package controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import email.Email;
import model.RoleDAO;
import model.UserDAO;
import model.hibernate.RoleHibernateDAO;
import model.hibernate.UserHibernateDAO;
import model.pojo.Role;
import model.pojo.UserPojo;
import model.pojo.UserRole;

public class RegisterUserController extends AbstractController {
	
	public static final PassiveExpiringMap<String, UserRole> registrationTokens = new PassiveExpiringMap<String, UserRole>(15, TimeUnit.MINUTES);
	
	public static String generateId() throws NoSuchAlgorithmException {
	    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	    byte [] bytes = new byte[5];
	    random.nextBytes(bytes);
	    return byteArrayToHex(bytes);
	}

	public static String byteArrayToHex(byte[] bytes) {
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < bytes.length; i++) {
		 String theHex = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
		 sb.append(theHex.length() == 1 ? "0" + theHex : theHex);
	    }
	    return sb.toString();
	}

	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;
	private RoleDAO roleDAO;
	
	public RegisterUserController() {
		userDAO = new UserHibernateDAO();
		roleDAO = new RoleHibernateDAO();
	}

	@Wire
    private Textbox username;
	@Wire
    private Textbox password;
	@Wire
	private Textbox confirmPassword;
	@Wire
    private Combobox role;
	@Wire
	private Textbox email;
	
	private UserPojo user;
    
    @Listen("onClick = #saveButton")
    public void save() throws Exception {
    	Collection<UserPojo> userCheck = userDAO.findByUsername(username.getValue());
    	if(!userCheck.isEmpty()) {
    		throw new WrongValueException(username, "Such user has been already registered. Please choose a different username.");
    	}
    	if(!password.getText().equals(confirmPassword.getText())) {
    		throw new WrongValueException(confirmPassword, "Passwords do not mutch!");
    	}
    	Email email = new Email("vadym.vikulin@gmail.com", "nopasa123))");
    	email.setFrom("noreply@gmail.com");
    	email.setSubject("Hyperborian.org registration e-mail");
    	
        //close();
        
    	user = (UserPojo)super.getObject();
    	user.setUsername(username.getValue());
    	user.setEmail(this.email.getValue());
    	byte[] bytesOfPassword = password.getValue().getBytes();
    	MessageDigest md = MessageDigest.getInstance("MD5");
    	byte[] digest = md.digest(bytesOfPassword);
    	final String result = new String(Hex.encodeHex(digest));
    	user.setPassword(result);
    	UserRole userRole = new UserRole();
    	userRole.setRole((Role)roleDAO.findByName("ROLE_USER").iterator().next());
    	userRole.setUser(user);
    	Set<UserRole> userRoles = new HashSet<UserRole>();
    	userRoles.add(userRole);
    	user.setUserRoles(userRoles);
    	user.setCreateTime(new Timestamp(new Date().getTime()));
    	
        
    	String tokenId = generateId();
    	registrationTokens.put(tokenId, userRole);
    	String readyEmailBody = registrationEmailTemplate.replaceFirst("\\[registrationToken\\]", tokenId);
        email.setBody(readyEmailBody);
        email.setTo(new String[]{this.email.getValue()});
        try {
        	boolean emailSent = email.send();
        	if(!emailSent) {
        		Messagebox.show("Unknown error: email was not sent.", "Error", Messagebox.OK, Messagebox.ERROR);
        		return;
            }
        } catch ( javax.mail.AuthenticationFailedException e) {
        	e.printStackTrace();
        	Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        	return;
		}
        close();
        Messagebox.show("Please check your email to complete your registration.");
    	
		//userDAO.save(user);
		//userRoleDAO.save(userRole);
		//Messagebox.show("Пользователь "+user.getUsername()+" успешно создан");
    	
    	//reload(user);*/
    }
}
