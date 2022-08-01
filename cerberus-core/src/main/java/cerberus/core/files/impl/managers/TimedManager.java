package cerberus.core.files.impl.managers;

import java.nio.file.Path;
import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.Period;

import cerberus.core.files.FileManager;

import com.frontier.lib.time.TimeUtil;

public abstract class TimedManager implements FileManager {

	private long nextTargetTime = -1;

	private boolean enabled = true;

	private boolean requiringFiles = true;

	private Period timeInterval = null;

	public void setTimeInterval(int days, int hours, int minutes, int seconds) {
		setTimeInterval(0, 0, days, hours, minutes, seconds);
	}

	public void setTimeInterval(int years, int months, int days, int hours,
			int minutes, int seconds) {
		timeInterval = new Period();
		timeInterval = timeInterval.withYears(makePos(years))
				.withMonths(makePos(months)).withDays(makePos(days))
				.withHours(makePos(hours)).withMinutes(makePos(minutes))
				.withSeconds(makePos(seconds));
	}

	private int makePos(int arg) {
		if (arg <= 0) {
			arg = 0;
		}
		return arg;
	}

	// very important that is called again after isReady() returns true
	public void calculateNextTargetTime() {
		final int years = timeInterval.getYears();
		final int months = timeInterval.getMonths();
		final int day = timeInterval.getDays();
		final int hour = timeInterval.getHours();
		final int minute = timeInterval.getMinutes();
		final int second = timeInterval.getSeconds();
		nextTargetTime = DateTime.now().plusYears(years).plusMonths(months)
				.plusDays(day).plusHours(hour).plusMinutes(minute)
				.plusSeconds(second).getMillis();
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}

	@Override
	public boolean isRequiringFiles() {
		return this.requiringFiles;
	}

	@Override
	public void setRequiringFiles(boolean isRequiringFiles) {
		this.requiringFiles = isRequiringFiles;
	}

	public boolean isReady() {
		return (TimeUtil.nowUTC().getTime() >= nextTargetTime);
	}

	@Override
	public void handleFiles(Collection<Path> files) {
		if (isReady()) {
			doHandleFiles(files); // implementors will handle this
			calculateNextTargetTime(); // very important call
		}
	}

	protected abstract void doHandleFiles(Collection<Path> files);
}
