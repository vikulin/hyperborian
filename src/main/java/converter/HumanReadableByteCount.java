package converter;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class HumanReadableByteCount implements Converter {

	@Override
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		if(val==null) {
			return "";
		}
		final Long size = (Long)val;
		return humanReadableByteCount(size);
	}
	
	@Override
	public Object coerceToBean(Object val, Component comp, BindContext ctx) {
		throw new UnsupportedOperationException();
	}
	
	public static String humanReadableByteCount(long bytes) {
		boolean si=true;
	    //int unit = si ? 1000 : 1024;
		int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
