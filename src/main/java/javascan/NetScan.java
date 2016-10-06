/**
 * @author Blue Thunder Somogyi
 *
 * Copyright (c) 2016 Blue Thunder Somogyi
 */
package javascan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.Validate;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

// Netscan is a wrapper around AddrScan providing the ability to scan contiguous subnets
// in a single object.  Maintains order of AddrScan objects to generate output in order
// of submission.  Implements Scannable to allow comingling of Addr and Net objects in
// single ordered list of results.
public class NetScan implements Scannable {

	// Class Fields
	private SubnetUtils targets;
	private SubnetInfo targetInfo;
	private int portLow;
	private int portHigh;
	ArrayList<AddrScan> scans;

	// Constructor
	public NetScan(SubnetUtils Cidr, int low, int high) throws IllegalArgumentException, NullPointerException {
		Validate.notNull(Cidr, "SubnetUtils Cidr cannot be null");
		this.targets = Cidr;
		this.targetInfo = targets.getInfo();
		scans = new ArrayList<AddrScan>();

		AddrScan.validatePorts(low, high);
		this.portLow = low;
		this.portHigh = high;
	}
	
	// Getters
	public SubnetUtils getTargets() {
		return this.targets;
	}

	public int getPortLow() {
		return this.portLow;
	}

	public int getPortHigh() {
		return this.portHigh;
	}
	
	// Scan target ports (Scannable interface)
	@Override
	public void scan() throws UnknownHostException {
		
		for ( InetAddress target = InetAddress.getByName(targetInfo.getLowAddress()); 
				targetInfo.isInRange(target.getHostAddress()); target = incrementIp(target)) {
			
			//System.out.println(target.getHostAddress() + " " + portlow + " " + porthigh);
			scans.add(new AddrScan(target, portLow, portHigh));
		}
		
		for (AddrScan s: scans) {
			//System.out.println(s.GetTarget().getHostAddress() + " " + portlow + " " + porthigh);
			s.scan(); 
		}
	}

	// Output all results, in submitted order (Scannable interface)
	@Override
	public void output() throws InterruptedException, ExecutionException {
		
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
			e.printStackTrace();
		}
		return result;
	}

} // End NetScan class
