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

public class AddrScan implements Scannable {

	// Constants
	private static final int MAXTHREADS = Integer.MAX_VALUE;
	private static final long THREAD_IDLE_SEC = 5;
	static final int MAXPORT = 65535;

	// Statics
	private static ThreadPoolExecutor threadPoolExecutor;

	// Class Fields
	private InetAddress Target;
	private int portlow;
	private int porthigh;
	ArrayList<Future<ResultValue>> results;

	// Static Initialization
	public static void init(int throttle) {
		int max;

		if (throttle >= MAXTHREADS || throttle <= 0) {
			max = MAXTHREADS;
		} else {
			max = throttle;
		}

		// System.out.println("throttle: " + throttle + " max: " + max);
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

	// Constructor
	public AddrScan() {
		results = new ArrayList<Future<ResultValue>>();
	}

	public AddrScan(InetAddress Target, int low, int high) throws IllegalArgumentException {
		results = new ArrayList<Future<ResultValue>>();
		this.Target = Target;
		if (low < 1 || low > MAXPORT || high < low || high > MAXPORT) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Invalid port arguments: [low: " + low + "] [high: " + high + "]");
			throw e;
		} else {
			this.portlow = low;
			this.porthigh = high;
		}
	}

	// Getters
	public InetAddress GetTarget() {
		return this.Target;
	}

	public int GetPortlow() {
		return this.portlow;
	}

	public int GetPorthigh() {
		return this.porthigh;
	}

	// Scan target ports
	public void scan() throws UnknownHostException {
		// TODO Auto-generated method stub

		for (int x = this.portlow; x <= this.porthigh; x++) {
			try {
				results.add(threadPoolExecutor.submit(new Probe(this.Target, x)));
			} catch ( NullPointerException e) {
				NullPointerException e2 = new NullPointerException("AddrScan results ArrayList not initialized: invoke AddrScan.init() prior to scan()");
				throw e2;
			} catch (RejectedExecutionException e) {
				System.out.println(threadPoolExecutor.getPoolSize());
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				System.out.println(threadPoolExecutor.getPoolSize());
				e.printStackTrace();
				threadPoolExecutor.setCorePoolSize(threadPoolExecutor.getPoolSize());
				results.add(threadPoolExecutor.submit(new Probe()));
				// System.exit(1);
			}
		}
	}

	@Override
	public void output() {
		// TODO Auto-generated method stub

		int count = 0;
		ResultValue result = null;
		while (!results.isEmpty()) {

			try {
				result = results.remove(0).get();
				String output = String.format("%s\t\t%d\t%s", this.Target.getHostAddress(), this.portlow + count++,
						result);
				System.out.println(output);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) {
		AddrScan.init();

		AddrScan first;
		try {
			first = new AddrScan(InetAddress.getByName("www.google.com"), 1, 1000);
			first.scan();
			first.output();
		} catch (UnknownHostException | IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		AddrScan.shutdown();
	}

}
