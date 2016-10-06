package javascan;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("unused")
public class AddrScanTest {
	
	InetAddress Target;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Target = InetAddress.getByName("127.0.0.1");
		AddrScan.init();
	}

	@After
	public void tearDown() throws Exception {
		AddrScan.shutdown();
	}

	@Test
	public void testValidatePortValid() {
		// Valid port values
		try {
			AddrScan.validatePorts(1, 65535);
			AddrScan.validatePorts(100, 10000);
			AddrScan.validatePorts(1, 1);
			AddrScan.validatePorts(65535, 65535);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testValidatePortInvalid() {
		// Valid port values
		try {
			AddrScan.validatePorts(0, 65535);
			fail("IllegalArgumentException expected: [ports: 0, 65535]");
		} catch (IllegalArgumentException e) {
		}
		try {
			AddrScan.validatePorts(1, 65536);
			fail("IllegalArgumentException expected: [port: 1, 65536");
		} catch (IllegalArgumentException e) {
		}
		try {
			AddrScan.validatePorts(65535, 1);
			fail("IllegalArgumentException expected: [port: 65535, 1]");
		} catch (IllegalArgumentException e) {
		}
		try {
			AddrScan.validatePorts(1000, 999);
			fail("IllegalArgumentException expected: [port: 1000, 999]");
		} catch (IllegalArgumentException e) {
		}
		try {
			AddrScan.validatePorts(-100, 999);
			fail("IllegalArgumentException expected: [port: -100, 999]");
		} catch (IllegalArgumentException e) {
		}
		try {
			AddrScan.validatePorts(100, 65536);
			fail("IllegalArgumentException expected: [port: 100, 65536]");
		} catch (IllegalArgumentException e) {
		}
		try {
			AddrScan.validatePorts(-100, 65536);
			fail("IllegalArgumentException expected: [port: -100, 65536]");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void testConstructorValid() {
		// Valid constructor
		try {
			AddrScan test = new AddrScan(InetAddress.getByName("127.0.0.1"), 1, 65000);
			if (!test.getTarget().equals(Target)) {
				fail("test.Target not set correctly");
			} else if (test.getPortlow() != 1) {
				fail("test.Portlow incorrect");
			} else if (test.getPorthigh() != 65000 ) {
				fail("test.Porthigh incorrect");
			} else if (test.results == null ) {
				fail("test.results not initialized");
			}
		} catch (UnknownHostException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testConstructorInvalidHostname() {
		// Valid constructor
		try {
			AddrScan test = new AddrScan(InetAddress.getByName("test.local"), 1, 65000);
			fail("UnknownHostException expected");
		} catch (UnknownHostException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	@Test
	public void testConstructorNegativeLowport() {
		// Valid constructor
		try {
			AddrScan test = new AddrScan(InetAddress.getByName("localhost"), -1, 65000);
			fail("IllegalArgumentException expected");
		} catch (UnknownHostException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	@Test
	public void testConstructorPortMismatch() {
		// Valid constructor
		try {
			AddrScan test = new AddrScan(InetAddress.getByName("localhost"), 100, 50);
			fail("IllegalArgumentException expected");
		} catch (UnknownHostException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	@Test
	public void testConstructorExcessiveHighport() {
		// Valid constructor
		try {
			AddrScan test = new AddrScan(InetAddress.getByName("localhost"), 1, 500000);
			fail("IllegalArgumentException expected");
		} catch (UnknownHostException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	// Test permutations of results against well known live hosts
	@Test
	public void testAddrScanGoogle() {
		// Valid constructor
		ArrayList<Future<ResultValue>> testresults;
		try {
			AddrScan test1 = new AddrScan(InetAddress.getByName("www.google.com"), 79, 80);
			test1.scan();
			AddrScan test2 = new AddrScan(InetAddress.getByName("www.google.com"), 25, 25);
			test2.scan();
			testresults = test1.getResults();
			if (testresults.get(0).get() != ResultValue.FILTERED) {
				fail("www.google.com:79 expected FILTERED, got " + testresults.get(0).get());
			} 
			if (testresults.get(1).get() != ResultValue.OPEN) {
				fail("www.google.com:80 expected OPEN, got " + testresults.get(1).get());
			} 
			testresults = test2.getResults();
			if (testresults.get(0).get() != ResultValue.CLOSED) {
				fail("www.google.com:80 expected CLOSED, got " + testresults.get(0).get());
			} 
		} catch (UnknownHostException | IllegalArgumentException | ExecutionException | InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test() {
		// fail("Not yet implemented"); // TODO
	}

}
