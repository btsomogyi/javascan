/**
 * 
 */
package javascan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import org.apache.commons.cli.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.net.util.SubnetUtils;

/**
 * @author bluethunder
 *
 */
public class Javascan {

	/**
	 * Display usage and exit
	 * 
	 * @param cmd
	 */
	private static void help(String cmd) {
		// This prints out some help
		System.out.println("usage: " + cmd + " <<host | ip | cidr>[:port[-port]]>...");
		System.out.println();
		System.out.println("\t" + cmd + " hostname");
		System.out.println("\t" + cmd + " a.b.c.d:10");
		System.out.println("\t" + cmd + " a.b.c.d/x:10-100");
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			help(args[0]);
		}

		try {
			// Initialize thread pool
			AddrScan.init();

			// Initialize ArrayList of scan targets
			ArrayList<Scannable> Targets = new ArrayList<>();

			// Load target arguments into Scannable ArrayList
			TargetSpec t;
			for (int x = 1; x < args.length; x++) {
				try {
					t = new TargetSpec(args[x]);
				} catch (IllegalArgumentException | UnknownHostException e) {
					e.printStackTrace();
					continue;
				}

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
					e.printStackTrace();
					continue;
				}
			}

			// Process scanning on Scannable ArrayList
			for (Scannable s : Targets) {
				try {
					s.scan();
				} catch (IllegalArgumentException | UnknownHostException e) {

					e.printStackTrace();
				}
			}

			// Output results from Scannable ArrayList
			for (Scannable s : Targets) {
				try {
					s.output();
				} catch (IllegalArgumentException e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
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
		String[] targetParts = targetSpecs.split(":", 0);
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
		if (targetParts[0].contains("/")) {
			this.Target = new SubnetUtils(targetParts[0]);
		} else {
			this.Target = InetAddress.getByName(targetParts[0]);
		}
	} // End TargetSpec constructor

	public InetAddress getInetTarget() {
		return (InetAddress) this.Target;
	}

	public SubnetUtils getSubnetTarget() {
		return (SubnetUtils) this.Target;
	}

} // End class TargetSpec

/*
 * class Cli { private String[] args = null; private Options options = new
 * Options();
 * 
 * public Cli(String[] args) {
 * 
 * this.args = args;
 * 
 * options.addOption("h", "help", false, "show help."); options.addOption("v",
 * "var", true, "Here you can set parameter .");
 * 
 * }
 * 
 * public void parse() { CommandLineParser parser = new BasicParser();
 * 
 * CommandLine cmd = null; try { cmd = parser.parse(options, args);
 * 
 * if (cmd.hasOption("h")) help();
 * 
 * if (cmd.hasOption("v")) { // Whatever you want to do with the setting goes
 * here } else { help(); }
 * 
 * } catch (ParseException e) { help(); } }
 * 
 * private void help() { // This prints out some help HelpFormatter formater =
 * new HelpFormatter();
 * 
 * formater.printHelp("Main", options); System.exit(0); } }
 */
