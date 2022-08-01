package cerberus.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class Playground {

	@Test
	public void doTest() throws UnknownHostException {
		System.out.println(InetAddress.getLocalHost());
	}
}
