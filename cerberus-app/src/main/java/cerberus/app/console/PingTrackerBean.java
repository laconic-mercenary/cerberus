package cerberus.app.console;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.PingInfo;

@Named
@SessionScoped
public class PingTrackerBean implements Serializable {

	private static final long serialVersionUID = -6657239577616342653L;

	private static final Logger LOGGER = Logger
			.getLogger(PingTrackerBean.class);

	private static final String DATE_FMT = "yyyy-MMM-dd (HH:mm:ss-SSS) [z]";

	// NOTE: this was originally a local variable in the toDateFormat()
	// method, if encounter problems, consider moving this back
	private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormat
			.forPattern(DATE_FMT).withZoneUTC();

	public String toDateFormat(long timestamp) {
		return UTC_FORMATTER.print(timestamp);
	}

	public List<PingInfo> refreshPingList() {
		List<PingInfo> list = Collections.emptyList();
		try (EntityDao<PingInfo, Long> dao = DaoManager.Factory
				.connect(PingInfo.class)) {
			list = dao.findAll();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug(String.format("Found %d Pings", list.size()));

		} catch (Exception e) {
			LOGGER.error("Failed to query for a list of Pings.");
			e.printStackTrace();
		}
		return list;
	}

	public List<PingInfo> getPingList() {
		return refreshPingList();
	}
}
