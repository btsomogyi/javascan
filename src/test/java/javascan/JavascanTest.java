package javascan;

import static org.junit.Assert.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class JavascanTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTargetSpecValid() {
		// Valid TargetSpec Constructor
		try {
			TargetSpec target = new TargetSpec("10.10.10.10@100-200");
			if (!target.getInetTarget().equals(InetAddress.getByName("10.10.10.10"))) {
				fail("Target address not expected value: 10.10.10.10 actually: " + target.getInetTarget().getHostAddress());
			} 
			if ( target.portLow != 100 || target.portHigh != 200) {
				fail("Target ports not expected values: [portLow: 100] [portHigh: 200] actually: [portLow: " + target.portLow +
						"] [portHigh: " + target.portHigh + "]");
			}
			
			target = new TargetSpec("10.10.20.10@1000");
			if (!target.getInetTarget().equals(InetAddress.getByName("10.10.20.10"))) {
				fail("Target address not expected value: 10.10.20.10 actually: " + target.getInetTarget().getHostAddress());
			} 
			if ( target.portLow != 1000 || target.portHigh != 1000) {
				fail("Target ports not expected values: [portLow: 1000] [portHigh: 1000] actually: [portLow: " + target.portLow +
						"] [portHigh: " + target.portHigh + "]");
			}
			
			target = new TargetSpec("@2000");
			if (!target.getInetTarget().equals(InetAddress.getByName("127.0.0.1"))) {
				fail("Target address not expected value: 127.0.0.1 actually: " + target.getInetTarget().getHostAddress());
			} 
			if ( target.portLow != 2000 || target.portHigh != 2000) {
				fail("Target ports not expected values: [portLow: 2000] [portHigh: 2000] actually: [portLow: " + target.portLow +
						"] [portHigh: " + target.portHigh + "]");
			}
			
			target = new TargetSpec("10.10.30.0/30@1-2000");
			if (!target.getSubnetTarget().getInfo().getCidrSignature().equals(new SubnetUtils("10.10.30.0/30").getInfo().getCidrSignature())) {
				fail("Target network not expected value: 10.10.30.0/30 actually: " + target.getSubnetTarget().getInfo().getCidrSignature());
			} 
			if ( target.portLow != 1 || target.portHigh != 2000) {
				fail("Target ports not expected values: [portLow: 1] [portHigh: 2000] actually: [portLow: " + target.portLow +
						"] [portHigh: " + target.portHigh + "]");
			}
			
		} catch (IllegalArgumentException | UnknownHostException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testTargetSpecInvalid() {
		// Invalid TargetSpec Constructor
		TargetSpec target;
		try {
			target = new TargetSpec("10.10.40.10@100:200");
			fail("IllegalArgumentException expected: [ports: 100:200]");
		} catch (IllegalArgumentException | UnknownHostException e) {
		}
		
		try {
			target = new TargetSpec("10.10.abc.10@10000");
			fail("UnknownHostException expected: [host: 10.10.abc.10]");
		} catch (IllegalArgumentException | UnknownHostException e) {
		}
		
	}

}
