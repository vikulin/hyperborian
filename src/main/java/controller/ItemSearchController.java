package controller;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;

public class ItemSearchController extends PojoListboxController {
	
	private static final long serialVersionUID = -234752997622256070L;
	
	@Wire
    private Listbox itemListbox;

	@Override
	protected Listbox getListbox() {
		return itemListbox;
	}

	@Listen("onClick = #searchButton")
	public void search() {
		final Desktop desktop = Executions.getCurrent().getDesktop();
        if (desktop.isServerPushEnabled()) {
            Messagebox.show("Already started");
        } else {
            desktop.enableServerPush(true);
            /**
             * Start parsing
             */
            
        }
		
	}
	
}
