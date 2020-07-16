package controller;
 
import java.util.Collection;
import java.util.Set;

import model.UserDAO;
import model.UserRoleDAO;
import model.hibernate.UserHibernateDAO;
import model.hibernate.UserRoleHibernateDAO;
import model.pojo.UserPojo;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.ext.Selectable;
 
public class UserSearchController extends PojoListboxController {
 
    private static final long serialVersionUID = 1L;
    @Wire
    private Listbox listbox;
    @Wire
    private Textbox username;
    @Wire
    private Component detailBox;
    @Wire
    private Label usernameLabel;
    @Wire
    private Label createDate;
    @Wire
    private Label role;
    
	protected UserDAO userDAO;
	protected UserRoleDAO userRoleDAO;

    public UserSearchController() {
    	userDAO = new UserHibernateDAO();
		userRoleDAO = new UserRoleHibernateDAO();
	}

    @Listen("onClick = #searchButton")
    public void search(){
        String username = this.username.getValue();
        Collection<UserPojo> result = null;
        if (username==null || "".equals(username)){
			result = userDAO.findAll();
		} else {
			result = userDAO.findByUsername(username);
		}
        listbox.setModel(new ListModelList<UserPojo>(result));
    }
     
    @Listen("onSelect = #listbox")
    public void showUserDetail(){
        Set<UserPojo> selection = ((Selectable<UserPojo>)listbox.getModel()).getSelection();
        if (selection!=null && !selection.isEmpty()){
            UserPojo selected = selection.iterator().next();
            setUserDetail(selected);
        }
    }
    
    private void setUserDetail(UserPojo user){
    	usernameLabel.setValue(user.getUsername());
        createDate.setValue(user.getCreateTime().toString());
        role.setValue(userRoleDAO.findByUserId(user.getId()).iterator().next().getRole().getName());
    }
    
    /**
     * Modify part
     */
    @Listen("onClick = #createButton")
    public void create(){
    	UserPojo user = new UserPojo();
    	String page = "./user.zhtml";
    	openWindow(page, user);
    }


	protected Listbox getListbox() {
		return listbox;
	}
    
    
}