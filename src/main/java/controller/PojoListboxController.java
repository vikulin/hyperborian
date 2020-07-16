package controller;

import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import model.pojo.interfaces.Pojo;

public abstract class PojoListboxController extends AbstractController {
	
	private static final long serialVersionUID = 1L;

	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		this.search();
	}
	
	public void doAfterCompose(Window comp, boolean presearch) throws Exception {
		super.doAfterCompose(comp);
		if(presearch) {
			this.search();
		}
	}
	
	public void reload(Object obj) {
		Pojo pojo = (Pojo)obj;
		ListModelList<Pojo> model = (ListModelList)getListbox().getModel();
		int index=0;
		if (model==null){
			model = new ListModelList<Pojo>();
		}
		for (Pojo u:model){
			if (u.getId()==pojo.getId()){
				model.remove(index);
				model.add(index, pojo);
				getListbox().setModel(model);
				return;
			}
			index++;
		}
	}

	public void delete(Object obj) {
		Pojo contact = (Pojo)obj;
		ListModelList<Pojo> model = (ListModelList)getListbox().getModel();
		int index=0;
		for (Pojo u:model){
			if (u.getId()==contact.getId()){
				model.remove(index);
				getListbox().setModel(model);
				return;
			}
			index++;
		}
	}
	
	protected abstract Listbox getListbox();
	
	public abstract void search();

}
