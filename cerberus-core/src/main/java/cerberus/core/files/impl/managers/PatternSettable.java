package cerberus.core.files.impl.managers;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.regex.Pattern;

import cerberus.core.files.impl.FilePatternFilter;

public interface PatternSettable {

	public void setFilterRegex(String regex);

	public static final class Util {

		public static DirectoryStream.Filter<Path> makeFilter(String regex) {
			Pattern pattern = Pattern.compile(regex);
			FilePatternFilter fpf = new FilePatternFilter();
			fpf.setFilePattern(pattern);
			return fpf;
		}
	}
}
