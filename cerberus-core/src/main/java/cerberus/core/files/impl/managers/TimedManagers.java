package cerberus.core.files.impl.managers;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class TimedManagers {

	private static final String SEP_COMMA = ",";

	private static final String SEP_COLON = ":";

	private static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
			.appendYears().appendSeparator(SEP_COMMA).appendMonths()
			.appendSeparator(SEP_COMMA).appendDays().appendSeparator(SEP_COMMA)
			.appendHours().appendSeparator(SEP_COLON).appendMinutes()
			.appendSeparator(SEP_COLON).appendSeconds().toFormatter();

	// yy,MM,dd,HH:mm:ss
	// this is pretty much the only thing used in this class
	public static Period parsePeriod(String date) {
		return PERIOD_FORMATTER.parsePeriod(date);
	}
}
