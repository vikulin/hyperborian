package listener;
import java.util.Locale;
 
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
 
import org.zkoss.web.Attributes;
 
public class LocaleProvider implements org.zkoss.zk.ui.util.RequestInterceptor {
	
     public void request(org.zkoss.zk.ui.Session sess, Object request, Object response) {
        final Cookie[] cookies = ((HttpServletRequest)request).getCookies();
         if (cookies != null) {
             for (int j = cookies.length; --j >= 0;) {
                if (cookies[j].getName().equals("my.locale")) {
                     //determine the locale
                     String val = cookies[j].getValue();
                     Locale locale = org.zkoss.util.Locales.getLocale(val);
                     sess.setAttribute(Attributes.PREFERRED_LOCALE, locale);
                     return;
                 }
             }
         }
     }
}