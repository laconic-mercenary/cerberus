package cerberus.core.files.impl.managers.purge.filtered;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import cerberus.core.files.impl.managers.PatternSettable;
import cerberus.core.files.impl.managers.filtered.Filter;
import cerberus.core.files.impl.managers.filtered.FilteredTimedManager;
import cerberus.core.files.impl.managers.purge.FilesPurge;
import cerberus.core.persistence.KvpProperties;
import cerberus.core.persistence.util.Keys;
import cerberus.core.tasks.AsyncEligible;

@AsyncEligible
public class PreserveYoungerFilesPurge extends FilteredTimedManager implements
		PatternSettable, Serializable {

	private static final long serialVersionUID = -4026449109201083995L;

	private static final Logger LOGGER = Logger
			.getLogger(PreserveYoungerFilesPurge.class);

	private static final class FilterYoungerFiles implements Filter {

		private static final class YoungComparator implements Comparator<Path> {

			@Override
			public int compare(Path o1, Path o2) {
				int result = 0;
				long time1 = 0L;
				long time2 = 0L;
				try {
					FileTime ftime1 = Files.getLastModifiedTime(o1);
					FileTime ftime2 = Files.getLastModifiedTime(o2);

					time1 = ftime1.toMillis();
					time2 = ftime2.toMillis();
				} catch (IOException e) {
					// does not matter i guess
					// just log and will address and when see it in logs
					LOGGER.warn(String
							.format("Exception trapped when attempting to get file times from '%s' and '%s'",
									o1.toString(), o2.toString()));
					e.printStackTrace();
				}
				result = (int) (time2 - time1); // make this a descending list

				// the ones in front should be the younger files
				return result;
			}
		}

		private int amountToPreserve = 1;

		public int getAmountToPreserve() {
			return amountToPreserve;
		}

		public void setAmountToPreserve(int amountToPreserve) {
			this.amountToPreserve = amountToPreserve;
		}

		// this will fire when it comes time to actually process the files
		@Override
		public Collection<Path> doFilter(Collection<Path> files) {
			Collection<Path> resultCollection = Collections.emptyList();

			if (!files.isEmpty()) {
				// are there actually enough files?
				if (getAmountToPreserve() < files.size()) {
					// yes, pass over the original list and filter down from
					// there
					resultCollection = files;
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String
								.format("Will preserve %d files out of %d to be purged.",
										getAmountToPreserve(), files.size()));
					}

					List<Path> modifableList = new ArrayList<>(files.size());
					modifableList.addAll(files);

					// sort descending by timestamp
					// so youngest are in front
					Collections.sort(modifableList, new YoungComparator());

					// youngest are expected to be at the front of the list
					Path removal = null;
					int toPreserve = getAmountToPreserve();

					// protected from infinite loop with the static load
					while (toPreserve-- > 0) {
						// these removals will NOT be purged off
						removal = modifableList.remove(0);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Will not purge: "
									+ removal.getFileName().toString());
						}
					}

					// make modifiable
					resultCollection = modifableList;

				} else {
					// the number of files to purge is less than the
					// number of young files to keep around, so skip this
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String
								.format("Will not perform filter as the amount to preserve (%d) is >= the number of files to handle (%d)",
										getAmountToPreserve(), files.size()));
					}
				}
			}
			return resultCollection;
		}
	}

	private static final String MSG_INVALID_AMOUNT_FMT = "Invalid value for attribute '%s': %s. Will just use the default value of %d.";

	// the default number of files to leave when the purge executes
	private static final Integer DEFAULT_AMOUNT = 8;

	// this must sync up with what's in the database
	private static final String SUB_KEY_AMOUNT = "amount_to_preserve";
	private static final String KEY_AMOUNT = Keys.make(
			PreserveYoungerFilesPurge.class, SUB_KEY_AMOUNT);

	private static final String SUB_KEY_ENABLED = "enabled";
	private static final String KEY_IS_ENABLED = Keys.make(
			PreserveYoungerFilesPurge.class, SUB_KEY_ENABLED);
	private static final String DEFAULT_IS_ENABLED = "true";

	// the one and only filter
	private static final FilterYoungerFiles YOUNGER_FILES_FILTER = new FilterYoungerFiles();

	/**
	 * Statically loaded, when the class is first referenced, not instantiated
	 */
	static {
		int keyValue = DEFAULT_AMOUNT.intValue();
		String keyAmountStr = KvpProperties.findProperty(KEY_AMOUNT,
				DEFAULT_AMOUNT.toString());
		try {
			keyValue = Integer.parseInt(keyAmountStr);

			if (keyValue < 0) {
				LOGGER.warn(String.format(MSG_INVALID_AMOUNT_FMT,
						PreserveYoungerFilesPurge.class.getName(),
						keyAmountStr, DEFAULT_AMOUNT.intValue()));

				keyValue = DEFAULT_AMOUNT.intValue();
			}
		} catch (Exception e) {
			LOGGER.warn(String.format(MSG_INVALID_AMOUNT_FMT,
					PreserveYoungerFilesPurge.class.getName(), keyAmountStr,
					DEFAULT_AMOUNT.intValue()));

			// when the exception is thrown, the keyvalue probably
			// won't change, but pass over the default here anyway just in case
			keyValue = DEFAULT_AMOUNT.intValue();
		}

		// pass over configured amount of files to preserve during the purge
		YOUNGER_FILES_FILTER.setAmountToPreserve(keyValue);
	}

	private java.nio.file.DirectoryStream.Filter<Path> fileFilter = null;

	public PreserveYoungerFilesPurge() {
		addFilter(YOUNGER_FILES_FILTER);
	}

	@Override
	public java.nio.file.DirectoryStream.Filter<Path> getFileFilter() {
		return this.fileFilter;
	}

	@Override
	public void setFilterRegex(String regex) {
		this.fileFilter = PatternSettable.Util.makeFilter(regex);
	}

	@Override
	protected void doHandle(Collection<Path> filteredFiles) {
		if (filteredFiles.size() != 0) {
			// instead of caching this value statically, check everytime it
			// kicks off
			boolean enabled = Boolean.valueOf(KvpProperties.findProperty(
					KEY_IS_ENABLED, DEFAULT_IS_ENABLED));

			if (enabled) {
				LOGGER.info(String.format(FilesPurge.MSG_PURGE_STARTED_FMT,
						this.getClass().getName(), this.hashCode()));

				int filesPurged = FilesPurge.purgeFiles(filteredFiles);

				LOGGER.info(String.format(FilesPurge.MSG_PURGE_COMPLETE_FMT,
						this.getClass().getName(), this.hashCode(), filesPurged));
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("PURGE is currently DISABLED (VIA KVP PROPERTY)");
				}
			}
		}
	}
}
