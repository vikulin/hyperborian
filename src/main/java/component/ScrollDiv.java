package component;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;

public class ScrollDiv extends Div {
  static {
    addClientEvent(ScrollDiv.class, Events.ON_SCROLL, CE_DUPLICATE_IGNORE);
  }
}
