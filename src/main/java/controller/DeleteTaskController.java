package controller;

import org.zkoss.zk.ui.select.annotation.Listen;

public class DeleteTaskController extends AbstractController {

	private static final long serialVersionUID = 3031513248715349621L;

    @Listen("onClick = #yes")
    public void delete() throws Exception {
    	Object obj = getObject();
    	if (obj!=null){
			delete(obj);
			close();
    	}
    }
}
