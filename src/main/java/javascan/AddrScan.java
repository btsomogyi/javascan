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

public class AddrScan implements Scannable {

	// Constants
	//private static final int MAXTHREADS = Integer.MAX_VALUE;
	private static final int MAXTHREADS = 1024;
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

	// Scan target ports
	public void scan() throws UnknownHostException {
		// TODO Auto-generated method stub

		for (int x = this.portLow; x <= this.portHigh; x++) {
			try {
				results.add(threadPoolExecutor.submit(new Probe(this.target, x)));
			} catch (NullPointerException e) {
				NullPointerException e2 = new NullPointerException(
						"AddrScan results ArrayList not initialized: invoke AddrScan.init() prior to scan()");
				throw e2;
			} catch (RejectedExecutionException e) {
				System.out.println(threadPoolExecutor.getPoolSize());
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				//System.out.println(threadPoolExecutor.getPoolSize());
				//e.printStackTrace();
				threadPoolExecutor.setCorePoolSize(threadPoolExecutor.getPoolSize() - 1);
				results.add(threadPoolExecutor.submit(new Probe()));
			}
		}
	}

	@Override
	public void output() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub

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
