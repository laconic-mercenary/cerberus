package cerberus.core.persistence.tasks;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.frontier.lib.time.TimeUtil;

import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.tasks.impl.EntityTaskingManager;

public final class PersistableTasks {

	private static final Logger LOGGER = Logger
			.getLogger(PersistableTasks.class);

	private static final int HOURS_IN_DAY = TimeUtil.HOURS_PER_FULL_DAY;

	private static EntityTaskingManager MANAGER = null;
	static {
		try {
			MANAGER = new EntityTaskingManager(
					DaoManager.Factory.EMF.createEntityManager());
		} catch (Exception e) {
			LOGGER.fatal(
					"Failed to create EntityManager - this is BAD BAD BAD", e);
			throw new RuntimeException(e);
		}
	}

	public static void schedule(long nextExecutionTimeUTC, PersistableTask task) {
		MANAGER.schedule(nextExecutionTimeUTC, task);
	}

	public static void scheduleMinutesAfter(int minutes, PersistableTask task) {
		if (minutes > 0) {
			long now = TimeUtil.nowUTC().getTime();
			DateTime time = new DateTime(now);
			DateTime next = time.plusMinutes(minutes);
			schedule(next.getMillis(), task);
		} else {
			LOGGER.warn(String.format(
					"Not scheduling %s - invalid minutes specified: %d", task
							.getClass().getName(), minutes));
		}
	}

	public static void scheduleHoursAfter(int hours, PersistableTask task) {
		if (hours > 0) {
			long now = TimeUtil.nowUTC().getTime();
			DateTime time = new DateTime(now);
			DateTime next = time.plusHours(hours);
			schedule(next.getMillis(), task);
		} else {
			LOGGER.warn(String.format(
					"Not scheduling %s - invalid hours specified: %d", task
							.getClass().getName(), hours));
		}
	}

	public static void scheduleNextDay(PersistableTask task) {
		scheduleHoursAfter(HOURS_IN_DAY, task);
	}
	
	public static Collection<PersistableTask> fetchAll() {
		return MANAGER.fetchReadyTasks();
	}
}
