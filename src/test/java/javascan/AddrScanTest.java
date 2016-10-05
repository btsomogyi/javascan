package javascan;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructorValid() {
		// Valid constructor
		try {
			AddrScan test = new AddrScan(InetAddress.getByName("127.0.0.1"), 1, 65000);
			if (!test.GetTarget().equals(Target)) {
				fail("test.Target not set correctly");
			} else if (test.GetPortlow() != 1) {
				fail("test.Portlow incorrect");
			} else if (test.GetPorthigh() != 65000 ) {
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
	
	@Test
	public void test() {
		// fail("Not yet implemented"); // TODO
	}

}
