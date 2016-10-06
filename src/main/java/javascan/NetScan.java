package javascan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.Validate;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

public class NetScan implements Scannable {

	// Class Fields
	private SubnetUtils targets;
	private SubnetInfo targetInfo;
	private int portlow;
	private int porthigh;
	ArrayList<AddrScan> scans;

	// Constructor
	public NetScan(SubnetUtils Cidr, int low, int high) throws IllegalArgumentException, NullPointerException {
		Validate.notNull(Cidr, "SubnetUtils Cidr cannot be null");
		this.targets = Cidr;
		this.targetInfo = targets.getInfo();
		scans = new ArrayList<AddrScan>();

		AddrScan.validatePorts(low, high);
		this.portlow = low;
		this.porthigh = high;
	}
	
	@Override
	public void scan() throws UnknownHostException {
		// TODO Auto-generated method stub
		
		for ( InetAddress target = InetAddress.getByName(targetInfo.getLowAddress()); 
				targetInfo.isInRange(target.getHostAddress()); target = incrementIp(target)) {
			
			//System.out.println(target.getHostAddress() + " " + portlow + " " + porthigh);
			scans.add(new AddrScan(target, portlow, porthigh));
		}
		
		for (AddrScan s: scans) {
			//System.out.println(s.GetTarget().getHostAddress() + " " + portlow + " " + porthigh);
			s.scan(); 
		}
	}

	@Override
	public void output() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		//for (AddrScan s: scans) s.output(); 
		
		AddrScan result = null;
		while (!scans.isEmpty()) {
			result = scans.remove(0);
			result.output();
		}
	}

	// Increment IP address to next sequential IP address and return result
	static InetAddress incrementIp(InetAddress ip) {
		byte[] addr = ip.getAddress();
		InetAddress result = null;
		for (int j = addr.length - 1; j >= 0; j--) {
			addr[j]++;
			if (addr[j] > 0) {
				break;
			}
		}
		try {
			result = InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String subnet = "192.168.10.0/28";
		SubnetUtils targets = new SubnetUtils(subnet);
		//SubnetInfo info = targets.getInfo();
		NetScan scan;
		
		AddrScan.init();
		//System.out.println(info.getLowAddress());
		try {
			scan = new NetScan(targets, 10, 15);
			scan.scan();
			scan.output();
		} catch (UnknownHostException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AddrScan.shutdown();
	}

}
