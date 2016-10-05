/**
 * 
 */
package javascan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author bluethunder
 *
 */
public class Javascan {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Scannable> Targets = new ArrayList<>();
		AddrScan.init();
		
		try {
			Targets.add(new AddrScan(InetAddress.getByName("www.google.com"), 70, 90));
			Targets.add(new AddrScan(InetAddress.getByName("localhost"), 5001, 5006));
			for (Scannable s: Targets) s.scan();
			for (Scannable s: Targets) s.output();
		} catch (IllegalArgumentException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		AddrScan.shutdown();
	}

}
