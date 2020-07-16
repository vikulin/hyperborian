package controller;

import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Video;
import org.zkoss.zul.Window;

public class VideoController extends AbstractController {

	private static final long serialVersionUID = 6423681142155941482L;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		video.play();
	}
	
	@Wire
	private Video video;

}
