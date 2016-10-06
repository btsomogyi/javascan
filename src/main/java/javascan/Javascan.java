/**
 * 
 */
package javascan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.apache.commons.lang3.Validate;
import org.apache.commons.net.util.SubnetUtils;

/**
 * @author Blue Thunder Somogyi
 *
 */

public class Javascan {

	// Used to separate host/ip/network from port range.
	// Need to use character not used as shell metacharacter
	// Also need to escape for Java Regex
	public static final String PORTDELIM = "@";

	/**
	 * Display usage and exit
	 * 
	 */
	private static void help() {
		// This prints out some help
		String cmd = Javascan.class.getName();
		System.out.println("usage: " + cmd + " <<host | ip | cidr>[" + PORTDELIM + "port[-port]]>...");
		System.out.println();
		System.out.println("\t" + cmd + " hostname");
		System.out.println("\t" + cmd + " a.b.c.d" + PORTDELIM + "10");
		System.out.println("\t" + cmd + " a.b.c.d/x" + PORTDELIM + "10-100");
		System.exit(0);
	}

	/**
	 * @param args
	 */
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

				// For each TargetSpec, examine target type (InetAddress or
				// SubnetUtils) and create
				// appropriate Scannable instance in Targets ArrayList. Print
				// Exception on target
				// value validation error and continue with remaining input
				// parameters.
				try {
					if (t.Target instanceof InetAddress) {
						Targets.add(new AddrScan(t.getInetTarget(), t.portLow, t.portHigh));
					} else if (t.Target instanceof SubnetUtils) {
						Targets.add(new NetScan(t.getSubnetTarget(), t.portLow, t.portHigh));
					} else {
						IllegalArgumentException e = new IllegalArgumentException(
								"Unexpected object type: " + t.Target.getClass().getSimpleName());
						throw e;
					}
				} catch (IllegalArgumentException e) {
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

class TargetSpec {
	Object Target;
	int portLow;
	int portHigh;

	public TargetSpec(String targetSpecs) throws IllegalArgumentException, UnknownHostException {
		Validate.notNull(targetSpecs, "String targetSpecs cannot be null");
		String[] targetParts = targetSpecs.split(Javascan.PORTDELIM, 0);
		if (targetParts.length == 1) {
			this.portLow = 1;
			this.portHigh = Probe.MAXPORT;
		} else if (targetParts.length == 2) {
			String[] portParts = targetParts[1].split("-", 0);
			if (portParts.length == 1) {
				try {
					this.portLow = Integer.parseInt(portParts[0]);
					this.portHigh = portLow;
				} catch (NumberFormatException e) {
					IllegalArgumentException f = new IllegalArgumentException(
							"Invalid port specifier '" + targetParts[1] + "' in parameter " + targetSpecs);
					throw f;
				}
			} else if (portParts.length == 2) {
				try {
					this.portLow = Integer.parseInt(portParts[0]);
					this.portHigh = Integer.parseInt(portParts[1]);
				} catch (NumberFormatException e) {
					IllegalArgumentException f = new IllegalArgumentException(
							"Invalid port specifier '" + targetParts[1] + "' in parameter " + targetSpecs);
					throw f;
				}
			} else {
				IllegalArgumentException e = new IllegalArgumentException(
						"Invalid port specifier '" + targetParts[1] + "' in parameter " + targetSpecs);
				throw e;
			}
		} else {
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
		}
	} // End TargetSpec constructor

	public InetAddress getInetTarget() {
		return (InetAddress) this.Target;
	}

	public SubnetUtils getSubnetTarget() {
		return (SubnetUtils) this.Target;
	}

} // End class TargetSpec
