package listener;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContextEvent;

import org.hyperborian.bt.Start;

public class StartTorrentListener implements javax.servlet.ServletContextListener {
	
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			Start.main(null);
		} catch (SocketException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}

}
