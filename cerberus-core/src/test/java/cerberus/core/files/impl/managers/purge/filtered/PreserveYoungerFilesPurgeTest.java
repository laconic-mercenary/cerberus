package cerberus.core.files.impl.managers.purge.filtered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PreserveYoungerFilesPurgeTest {

	private static final class TestComparator implements Comparator<Long> {

		@Override
		public int compare(Long o1, Long o2) {
			// notice how the 2nd argument must be bigger
			// in order to make this a DESCENDING list
			int result = (int) (o2.longValue() - o1.longValue());
			return result;
		}

	}

	@Test
	public void test_sort_does_as_expected() {
		List<Long> longList = Arrays.asList(90L, 83L, 95L, 40L, 122L);
		List<Long> modLongList = new ArrayList<>(longList.size());
		modLongList.addAll(longList);

		Collections.sort(modLongList, new TestComparator());
		Assert.assertEquals(122L, modLongList.get(0).longValue());
		Assert.assertEquals(95L, modLongList.get(1).longValue());
		Assert.assertEquals(90L, modLongList.get(2).longValue());
		Assert.assertEquals(83L, modLongList.get(3).longValue());
	}

}
