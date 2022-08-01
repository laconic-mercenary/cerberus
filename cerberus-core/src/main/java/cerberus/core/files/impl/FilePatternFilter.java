package cerberus.core.files.impl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class FilePatternFilter implements DirectoryStream.Filter<Path> {

	private Pattern filePattern = null;

	public Pattern getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(Pattern filePattern) {
		this.filePattern = filePattern;
	}

	@Override
	public boolean accept(Path entry) throws IOException {
		return (getFilePattern().matcher(entry.getFileName().toString())
				.matches());
	}
}
