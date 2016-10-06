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
	public void testValidatePortValid() {
		// Valid port values
		try {
			Probe.validatePort(1);
			Probe.validatePort(100);
			Probe.validatePort(65535);
		} catch (IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testValidatePortInvalid() {
		// Invalid port values
		try {
			Probe.validatePort(0);
			fail("IllegalArgumentException expected: [port: 0]");
		} catch (IllegalArgumentException e) {
		}
		try {
			Probe.validatePort(65536);
			fail("IllegalArgumentException expected: [port: 65536]");
		} catch (IllegalArgumentException e) {
		}
		try {
			Probe.validatePort(-100);
			fail("IllegalArgumentException expected: [port: -100]");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void testConstructorValid() {
		// Valid constructor
		try {
			Probe test = new Probe(Target, 1);
		} catch (IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testConstructorZeroPort() {
		// Invalid constructor 
		try {
			Probe test = new Probe(Target, 0);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
		}
	}
	
	@Test
	public void testConstructorExcessiveHighport() {
		// Invalid constructor
		try {
			Probe test = new Probe(Target, 65999);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
		}
	}
	

}
