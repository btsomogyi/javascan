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
//import java.util.concurrent.TimeUnit;

public class Probe implements Callable<ResultValue> {

	// Constants
	private static final int SOCKET_TIMEOUT_SEC = 1;

	// Fields
	private InetAddress Target;
	private int port;

	// Constructors
	public Probe() {
	}

	public Probe(InetAddress target, int port) throws IllegalArgumentException {
		this.Target = target;
		if (port < 1 || port > AddrScan.MAXPORT) {
			IllegalArgumentException e = new IllegalArgumentException("Invalid port argument: [port: " + port + "]");
			throw e;
		} else {
			this.port = port;
		}
	}

	// Initialize object
	public Probe SetTarget(InetAddress target, int port) throws IllegalArgumentException {
		this.Target = target;
		if (port < 1 || port > AddrScan.MAXPORT) {
			IllegalArgumentException e = new IllegalArgumentException("Invalid port argument: [port: " + port + "]");
			throw e;
		} else {
			this.port = port;
		}
		return this;
	}

	// Getters
	public InetAddress GetTarget() {
		return this.Target;
	}

	public int GetPort() {
		return this.port;
	}

	// Runnable
	@Override
	public ResultValue call() {
		// TODO Auto-generated method stub
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
			// System.err.println(e);
			ProbeResult = ResultValue.CLOSED;
		} catch (UnknownHostException e) {
			//System.err.println(e);
			ProbeResult = ResultValue.ERROR;
		} catch (SocketTimeoutException e) {
			// System.err.println(e);
			ProbeResult = ResultValue.FILTERED;
		} catch (IOException e) {
			// System.err.println(e);
			ProbeResult = ResultValue.CLOSED;
		}

		return ProbeResult;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
