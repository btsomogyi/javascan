/**
 * @author Blue Thunder Somogyi
 *
 * Copyright (c) 2016 Blue Thunder Somogyi
 */
package javascan;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.Validate;

public class Probe implements Callable<ResultValue> {

	// Constants
	private static final int SOCKET_TIMEOUT_SEC = 1;
	static final int MAXPORT = 65535;

	// Fields
	private InetAddress Target;
	private int port;

	// Constructors
	public Probe() {
	}

	public Probe(InetAddress Target, int port) throws IllegalArgumentException, NullPointerException  {
		Validate.notNull(Target, "InetAddress cannot be null");
		this.Target = Target;
		this.port = validatePort(port);
	}

	// Initialize existing object
	public Probe setTarget(InetAddress Target, int port) throws IllegalArgumentException, NullPointerException  {
		Validate.notNull(Target, "InetAddress Target cannot be null");
		this.Target = Target;
		this.port = validatePort(port);
		return this;
	}

	// Getters
	public InetAddress getTarget() {
		return this.Target;
	}

	public int getPort() {
		return this.port;
	}

	// Validate port value
	static int validatePort(int port) throws IllegalArgumentException {
		if (port < 1 || port > MAXPORT) {
			IllegalArgumentException e = new IllegalArgumentException("Invalid port argument: [port: " + port + "]");
			throw e;
		} else {
			return port;
		}
	}

	// Runnable
	@Override
	public ResultValue call() {
		ResultValue ProbeResult = ResultValue.FILTERED;

		try {
			Socket socket = new Socket();
			// fill in socket options
			SocketAddress address = new InetSocketAddress(this.Target, this.port);
			socket.connect(address, SOCKET_TIMEOUT_SEC * 1000);
			// work with the sockets...
			ProbeResult = ResultValue.OPEN;
			socket.close();
		} catch (ConnectException e) {
			if (e.getMessage().equals("Connection refused")) {
				ProbeResult = ResultValue.CLOSED;
			} else if (e.getMessage().equals("No route to host")) {
				ProbeResult = ResultValue.ERROR;
			} else {
				//System.err.println(e);
				ProbeResult = ResultValue.ERROR;
			}
		} catch (UnknownHostException e) {
			// System.err.println(e);
			ProbeResult = ResultValue.ERROR;
		} catch (SocketTimeoutException e) {
			// System.err.println(e);
			ProbeResult = ResultValue.FILTERED;
		} catch (IOException e) {
			//System.err.println(e);
			ProbeResult = ResultValue.ERROR;
		}

		return ProbeResult;
	}
} // End class Probe
