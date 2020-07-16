package viewmodel;

import java.util.Collection;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.ToServerCommand;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.util.Clients;

import model.RoleDAO;
import model.hibernate.RoleHibernateDAO;
import model.pojo.Role;

@ToServerCommand("verify")
public class UserViewModel extends SelectorComposer<Component>{
	
	private static final long serialVersionUID = 3754482657024685961L;

	@Init
	public void init(@BindingParam("userPrincipal") Object userPrincipal){
		this.userPrincipal = userPrincipal;
		this.roleDAO = new RoleHibernateDAO();
		this.roleList = roleDAO.findAll();
	}
	
	private Role role;
	private RoleDAO roleDAO;
	private Collection<Role> roleList;
	
	private Object userPrincipal;
    
	public Collection<Role> getRoles() {
		return roleList;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}
	
	/**
	 * reCaptcha code part
	 */
	private boolean disabled = true;
	
	final String SECRET = "6Ld_QnkUAAAAADs1TUeDIvZ9ubhzzd44tiU-M6HS"; //from reCaptcha
	
	@Command @NotifyChange("disabled")
	public void verify(@BindingParam("response")String response) throws Exception{
		JSONObject result = RecaptchaVerifier.verifyResponse(SECRET, response);
		if (Boolean.parseBoolean(result.get("success").toString())){
			disabled = false;
		}else{
			String errorCode = result.get("error-codes").toString();
			//log or show error
			Clients.showNotification(errorCode);
		}
	}
	
	@Command
	public void submit(){
		//Clients.showNotification("submitted");
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	/**
	 * reCaptcha code part
	 */
	
}
