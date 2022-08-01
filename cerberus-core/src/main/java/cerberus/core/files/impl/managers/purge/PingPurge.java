package cerberus.core.files.impl.managers.purge;

import java.io.Serializable;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Hours;

import cerberus.core.files.NonFileHandler;
import cerberus.core.files.impl.FilePatternFilter;
import cerberus.core.files.impl.managers.PatternSettable;
import cerberus.core.files.impl.managers.TimedManager;
import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.PingInfo;
import cerberus.core.tasks.AsyncEligible;

import com.frontier.lib.time.TimeUtil;

// this does not handle files...
// it will simply purge old monitor client pings
// this really doesn't need PatternSettable
@NonFileHandler
@AsyncEligible
public class PingPurge extends TimedManager implements PatternSettable,
		Serializable {

	private static final long serialVersionUID = -6892095963361694981L;

	private static final Logger LOGGER = Logger.getLogger(PingPurge.class);

	// maximum age in hours a ping can be before it's removed from the database
	private static final int MAX_PING_AGE_HOURS = 4;

	private static final String MSG_PURGE = "Will purge %d pings";

	public static final String MSG_PURGE_STARTED_FMT = "PURGE [%s:%s] started...";

	public static final String MSG_PURGE_COMPLETE_FMT = "PURGE [%s:%s] completed. %d pings purged.";

	// this will (hopefully) not match any files
	// it expects a blank string, of which there should no files
	// that match
	private static final String BLANK_REGEX = "^$";
	private static final FilePatternFilter BLANK_FILTER = new FilePatternFilter();
	static {
		BLANK_FILTER.setFilePattern(Pattern.compile(BLANK_REGEX));
	}

	@Override
	public Filter<Path> getFileFilter() {
		return BLANK_FILTER;
	}

	@Override
	protected void doHandleFiles(Collection<Path> files) {
		// no files shall be handled here
		// just querying the persisted pings and
		// determining which need to be purged

		LOGGER.info(String.format(MSG_PURGE_STARTED_FMT, this.getClass()
				.getName(), this.hashCode()));

		List<PingInfo> list = Collections.emptyList();
		int removed = 0;
		try (EntityDao<PingInfo, Long> dao = DaoManager.Factory
				.connect(PingInfo.class)) {

			list = determinePurges(dao.findAll());

			removed = purgeOldPings(list, dao);

		} catch (Exception e) {
			LOGGER.error("Failed to purge old Pings.");
			e.printStackTrace();
		}

		LOGGER.info(String.format(MSG_PURGE_COMPLETE_FMT, this.getClass()
				.getName(), this.hashCode(), removed));
	}

	private static List<PingInfo> determinePurges(List<PingInfo> pings) {
		// isEmpty() is already checked
		List<PingInfo> removals = new LinkedList<>();
		long now = TimeUtil.nowUTC().getTime();
		DateTime nowDt = new DateTime(now);
		for (PingInfo ping : pings) {
			DateTime pingTime = new DateTime(ping.getPingReceivedTime());
			if (nowDt.getDayOfYear() >= pingTime.getDayOfYear()) {
				Hours hours = Hours.hoursBetween(nowDt, pingTime);
				int hoursInt = Math.abs(hours.getHours());
				if (hoursInt >= MAX_PING_AGE_HOURS)
					removals.add(ping);
			}
		}
		return removals;
	}

	private static int purgeOldPings(List<PingInfo> removals,
			EntityDao<PingInfo, Long> dao) {
		int removed = 0;
		if (!removals.isEmpty()) {
			LOGGER.info(String.format(MSG_PURGE, removals.size()));
			PingInfo lastRemoval = null;
			try {
				for (PingInfo remove : removals) {
					lastRemoval = remove;
					dao.remove(remove);
					++removed;
				}
			} catch (Exception e) {
				StringBuilder builder = new StringBuilder();
				builder.append("Failed to remove PingInfo entity: ");
				builder.append(lastRemoval == null ? "NULL" : lastRemoval
						.toString());
				builder.append(". Will not remove the entity but just keep in the list.");
				// so the ping will stay in the list...
				LOGGER.warn(builder);
				e.printStackTrace();
			}
		} else {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("No pings to purge at this time.");
		}
		return removed;
	}

	@Override
	public void setFilterRegex(String regex) {
		// this will actually be ignored...
	}
}
