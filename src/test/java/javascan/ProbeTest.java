package javascan;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class ProbeTest {
	InetAddress Target;
	
	@Before
	public void setUp() throws Exception {
		Target = InetAddress.getLocalHost();
	}
	
	@Test
	public void testConstructorValid() {
		// Valid constructor
		try {
			Probe test = new Probe(Target, 1);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testConstructorZeroPort() {
		// Valid constructor
		try {
			Probe test = new Probe(Target, 0);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	@Test
	public void testConstructorExcessiveHighport() {
		// Valid constructor
		try {
			Probe test = new Probe(Target, 65999);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	@Test
	public void test() {
		// fail("Not yet implemented"); // TODO
	}

}
