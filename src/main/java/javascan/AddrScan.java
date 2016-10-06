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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;

// AddrScan is primary unit of work, representing a single target address (multiple 
// ports).  Implements Scannable interface to allow a mix of single and multiple 
// address objects in same results set.
// Responsible for all multi-threading implementation.  Client of AddrScan must call
// AddrScan.init() to initialize thread pool, and AddrScan.shutdown() to destroy
// thread pool once scanning is complete.
public class AddrScan implements Scannable {

	// Constants
	private static final int MAXTHREADS = 2048;
	private static final long THREAD_IDLE_SEC = 5;

	// Statics
	private static ThreadPoolExecutor threadPoolExecutor;

	// Class Fields
	private InetAddress target;
	private int portLow;
	private int portHigh;
	ArrayList<Future<ResultValue>> results;

	// Static Field Initialization
	public static void init(int throttle) {
		int max;

		if (throttle >= MAXTHREADS || throttle <= 0) {
			max = MAXTHREADS;
		} else {
			max = throttle;
		}

		threadPoolExecutor = new ThreadPoolExecutor(max, max, THREAD_IDLE_SEC, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	// Default Static Initialization
	public static void init() {
		init(0);
	}

	// Static cleanup
	public static void shutdown() {
		if (threadPoolExecutor != null)
			threadPoolExecutor.shutdown();
	}

	// Constructors
	public AddrScan() {
		results = new ArrayList<Future<ResultValue>>();
	}

	public AddrScan(InetAddress Target, int low, int high) throws IllegalArgumentException, NullPointerException  {
		Validate.notNull(Target, "InetAddress Target cannot be null");
		this.target = Target;
		results = new ArrayList<Future<ResultValue>>();

		validatePorts(low, high);
		this.portLow = low;
		this.portHigh = high;

	}

	// Getters
	public InetAddress getTarget() {
		return this.target;
	}

	public int getPortlow() {
		return this.portLow;
	}

	public int getPorthigh() {
		return this.portHigh;
	}

	// AddrScan.results getter - package access only for test validation
	ArrayList<Future<ResultValue>> getResults() {
		return this.results;
	}

	// Validate port values
	static void validatePorts(int low, int high) throws IllegalArgumentException {
		if (Probe.validatePort(high) < Probe.validatePort(low)) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Invalid port arguments: [low: " + low + "] [high: " + high + "]");
			throw e;
		}

	}

	// Scan target ports (Scannable interface)
	@Override
	public void scan() throws UnknownHostException {

		for (int x = this.portLow; x <= this.portHigh; x++) {
			try {
				results.add(threadPoolExecutor.submit(new Probe(this.target, x)));
			} catch (NullPointerException e) {
				NullPointerException f = new NullPointerException(
						"AddrScan results ArrayList not initialized: invoke AddrScan.init() prior to scan()");
				throw f;
			} catch (RejectedExecutionException e) {
				System.out.println(threadPoolExecutor.getPoolSize());
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				// If thread pool unable to expand further, cap thread pool size and
				// resubmit task so it becomes queued.  Subsequent submissions will
				// be queued and run as threads become idle.  This error can be thrown
				// due to max thread per process limit in OS (not just memory constraint)
				threadPoolExecutor.setCorePoolSize(threadPoolExecutor.getPoolSize() - 1);
				results.add(threadPoolExecutor.submit(new Probe()));
			}
		}
	}

	// Output results (Scannable interface)
	@Override
	public void output() throws InterruptedException, ExecutionException {

		int count = 0;
		ResultValue result = null;
		while (!results.isEmpty()) {
				result = results.remove(0).get();
				String output = String.format("%s\t\t%d\t%s", this.target.getHostAddress(), this.portLow + count++,
						result);
				System.out.println(output);
		}
	}
} // end class AddrScan
