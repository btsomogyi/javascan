package javascan;
/**
 * @author Blue Thunder Somogyi
 *
 * Copyright (c) 2016 Blue Thunder Somogyi
 */


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.apache.commons.lang3.Validate;
import org.apache.commons.net.util.SubnetUtils;

// Primary class for javascan project, containing main().  Parses command-line
// parameters, then creates AddrScan and NetScan objects as appropriate with input
// parameters.  Once all inputs are instantiated, executes scan() method across all
// Scannable objects, which asynchronously execute against a single thread pool.  As
// soon as Scannable objects are queued with thread pool, begins to output finished 
// results, blocking on any results that are not yet completed until they are (providing
// in-order output of results concurrently with ongoing scanning).
public class Javascan {

	// Used to separate host/ip/network from port range.
	// Need to use character not used as shell metacharacter
	// Also need to escape for Java Regex
	public static final String PORTDELIM = "@";

	// Display usage and exit
	private static void help() {
		String cmd = Javascan.class.getSimpleName();
		System.out.println("usage: " + cmd + " <<host | ip | cidr>[" + PORTDELIM + "port[-port]]>...");
		System.out.println();
		System.out.println("\t" + cmd + " hostname");
		System.out.println("\t" + cmd + " a.b.c.d" + PORTDELIM + "10");
		System.out.println("\t" + cmd + " a.b.c.d/x" + PORTDELIM + "10-100");
		System.exit(0);
	}

	// Project main()
	public static void main(String[] args) {
		if (args.length < 1) {
			help();
		}

		try {
			// Initialize thread pool
			AddrScan.init();

			// Initialize ArrayList of scan targets
			ArrayList<Scannable> Targets = new ArrayList<>();

			// Load target arguments into Scannable ArrayList
			TargetSpec t;
			for (int x = 0; x < args.length; x++) {
				// For each input parameter, parse and store as TargetSpec
				// Print Exception on invalid input parameter and continue
				// with remaining input parameters.
				try {
					t = new TargetSpec(args[x]);
				} catch (IllegalArgumentException | UnknownHostException e) {
					System.out.println(e.getMessage());
					continue;
				}

				// For each TargetSpec, call getScannable(). getScannable()
				// examines target type (InetAddress or SubnetUtils) and creates
				// appropriate Scannable instance to be added to Targets
				// ArrayList. Print Exception on target value validation error
				// and continue with remaining input parameters (without
				// creating Targets entry.
				try {
					Targets.add(t.getScannable());
				} catch (IllegalArgumentException | NullPointerException e) {
					System.out.println(e.getMessage());
					continue;
				}
			}

			// Process scanning on Scannable ArrayList
			for (Scannable s : Targets) {
				try {
					s.scan();
				} catch (IllegalArgumentException | UnknownHostException e) {
					System.out.println(e.getMessage());
				}
			}

			// Output results from Scannable ArrayList (releasing object after
			// output)
			if (Targets.size() > 0) {
				System.out.println("target\t\t\tport\tresult");
			}
			Scannable target = null;
			while (!Targets.isEmpty()) {
				target = Targets.remove(0);
				try {
					target.output();
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			}

		} catch (Exception e) {
			// Abort on uncaught exception
			e.printStackTrace();
		} finally {
			// Tear down thread pool
			AddrScan.shutdown();
		}
	}
} // End class Javascan

// Utility class for storing input parameters destined for AddrScan and NetScan
// objects. Utilizes a single Object reference to store either AddrScan or
// NetScan (Scannable) objects and provides convenience functions to return
// appropriate object type.
class TargetSpec {
	Object Target;
	int portLow;
	int portHigh;

	// Constructor
	public TargetSpec(String targetSpecs) throws IllegalArgumentException, UnknownHostException {
		// Validate non null input
		Validate.notNull(targetSpecs, "String targetSpecs cannot be null");

		// Parse input parameter using PORTDELIM as delimiter
		String[] targetParts = targetSpecs.split(Javascan.PORTDELIM, 0);

		// If no ports specified, default to full range
		if (targetParts.length == 1) {
			this.portLow = 1;
			this.portHigh = Probe.MAXPORT;
		} else if (targetParts.length == 2) {
			// Ports specified, parse and validate
			String[] portParts = targetParts[1].split("-", 0);
			if (portParts.length == 1) {
				// If single port value provided, use for low and high (single
				// port scan)
				try {
					this.portLow = Integer.parseInt(portParts[0]);
					this.portHigh = portLow;
				} catch (NumberFormatException e) {
					IllegalArgumentException f = new IllegalArgumentException(
							"Invalid port specifier '" + targetParts[1] + "' in parameter " + targetSpecs);
					throw f;
				}
			} else if (portParts.length == 2) {
				// Range of ports provided
				try {
					this.portLow = Integer.parseInt(portParts[0]);
					this.portHigh = Integer.parseInt(portParts[1]);
				} catch (NumberFormatException e) {
					IllegalArgumentException f = new IllegalArgumentException(
							"Invalid port specifier '" + targetParts[1] + "' in parameter " + targetSpecs);
					throw f;
				}
			} else {
				// Does not parse properly, throw exception
				IllegalArgumentException e = new IllegalArgumentException(
						"Invalid port specifier '" + targetParts[1] + "' in parameter " + targetSpecs);
				throw e;
			}
		} else {
			// Unable to parse this input parameter, throw exception
			IllegalArgumentException e = new IllegalArgumentException("Invalid target specification: " + targetSpecs);
			throw e;
		}

		// Parse hostname/address/subnet - throw exception on parse failure or
		// hostname unknown
		try {
			if (targetParts[0].contains("/")) {
				this.Target = new SubnetUtils(targetParts[0]);
			} else {
				this.Target = InetAddress.getByName(targetParts[0]);
			}
		} catch (UnknownHostException e) {
			UnknownHostException f = new UnknownHostException("Unresolvable target: " + targetParts[0]);
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // End TargetSpec constructor

	// Utility method to return Scannable object of type appropriate to
	// TargetSpec Target type
	public Scannable getScannable() throws IllegalArgumentException, NullPointerException {
		Scannable target = null;

		if (this.Target instanceof InetAddress) {
			target = new AddrScan(this.getInetTarget(), this.portLow, this.portHigh);
		} else if (this.Target instanceof SubnetUtils) {
			target = new NetScan(this.getSubnetTarget(), this.portLow, this.portHigh);
		} else {
			IllegalArgumentException e = new IllegalArgumentException(
					"Unexpected object type: " + this.Target.getClass().getSimpleName());
			throw e;
		}
		return target;
	}

	// Utility methods to return Target is desired Object type
	public InetAddress getInetTarget() {
		return (InetAddress) this.Target;
	}

	public SubnetUtils getSubnetTarget() {
		return (SubnetUtils) this.Target;
	}

} // End class TargetSpec
