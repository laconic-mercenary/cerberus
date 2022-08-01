package cerberus.core.files.impl.managers;

import org.junit.Assert;
import org.junit.Test;

public class CameraImageManagerTest {

	@Test
	public void testIsTimedManager() {
		Assert.assertTrue(TimedManager.class.isAssignableFrom(CameraImageManager.class));
	}

}
