package cerberus.core.files.impl;

import static cerberus.core.files.FileManager.Factory.make;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.Period;

import cerberus.core.files.FileManagerService;
import cerberus.core.files.impl.managers.PatternSettable;
import cerberus.core.files.impl.managers.TimedManager;
import cerberus.core.files.impl.managers.TimedManagers;
import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.FileManager;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;
import cerberus.core.persistence.entities.validation.impl.FileManagerVisitor;

import com.frontier.lib.validation.TextValidator;

/**
 * This represents the core file management service in cerberus. Much of this
 * will be statically loaded for performance reasons
 */
public class PersistenceFileManagerService implements FileManagerService {

	private static final String MSG_TIMED_MANAGER_INTERVAL_FMT = "Created TimedManager [%s] with interval %s";

	private static final String MSG_MANAGER_CREATED_FMT = "Successfully setup File Manager [%s]";

	private static final String MSG_DIR_NO_EXIST_FMT = "The directory %s does not exist.";

	private static final String MSG_FOUND_FILE_MANAGERS = "Found %d file managers for directory %s";

	private static final String MSG_PATTERN_SETTABLE = "[%s] will have a pattern set to '%s'";

	private static final String MSG_LOAD_START = "Fetching current list of file manager entries...";

	private static final String MSG_FOUND_MAP_ENTRIES_FMT = "Found %d entries, building File Manager map...";

	private static final String MSG_FM_BUILDING_COMPLETE = "File Manager map building is complete. Building cached target list...";

	private static final String MSG_CACHED_DIR_COMPLETE = "Cached target list building is complete";

	private static final String MSG_NO_FM = "No file managers specified, the software will run but no files can be received.";

	private static final String MSG_LOADED_FM_START = "Loading FileManager configuration...";

	private static final String MSG_LOADED_FM_COMPLETE = "Loading FileManager configuration complete";

	private static final Logger LOGGER = Logger
			.getLogger(PersistenceFileManagerService.class);

	private static final Map<String, List<cerberus.core.files.FileManager>> MANAGERS = new HashMap<>();

	private static final List<Path> DIRECTORIES = new ArrayList<>();

	/**
	 * fires when the class is loaded, not instantiated, (generally the first
	 * time the class itself is referenced)
	 */
	static {
		load();
	}

	private static void load() {
		LOGGER.info(MSG_LOAD_START);
		List<FileManager> managerEntries = queryManagerList();
		if (managerEntries.isEmpty()) {
			LOGGER.warn(MSG_NO_FM);
		} else {
			LOGGER.info(String.format(MSG_FOUND_MAP_ENTRIES_FMT,
					managerEntries.size()));

			buildMap(MANAGERS, managerEntries);
			LOGGER.info(MSG_FM_BUILDING_COMPLETE);

			buildDirectoryList(DIRECTORIES, MANAGERS);
			LOGGER.info(MSG_CACHED_DIR_COMPLETE);
		}
	}

	private static void buildDirectoryList(List<Path> directories,
			Map<String, List<cerberus.core.files.FileManager>> map) {
		// the directory is the key in the map
		for (String dirKey : map.keySet()) {
			// go through and ensure that the directory doesn't already exist
			// in our cached directory list
			boolean cached = false;
			for (Path dir : directories) {
				if (dirKey.equals(dir.toAbsolutePath().toString())) {
					// yep, already exists
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String
								.format("Directory '%s' is already cached, will not add to cache.",
										dirKey));
						cached = true;
						break;
					}
				}
			}
			if (!cached) {
				Path dir = Paths.get(dirKey);
				directories.add(dir);
			}
		}
	}

	private static void buildMap(
			Map<String, List<cerberus.core.files.FileManager>> map,
			List<FileManager> entries) {

		for (FileManager entry : entries) {

			// instantiate it
			cerberus.core.files.FileManager newManager = null;

			try {
				newManager = make(entry.getFileManagerEntry().getClassName(),
						entry.getEnabled(), entry.getRequiresFiles());
			} catch (Exception e) {
				LOGGER.error(String
						.format("Failed to instantiate configured File Manager: '%s'. Moving to next in the list.",
								entry.getFileManagerEntry().getClassName()));
				continue;
			}

			// check if it's a timed manager and build it out appropriately
			if (newManager instanceof TimedManager)
				if (!buildTimedManager((TimedManager) newManager, entry))
					continue;

			// if the filter pattern is settable, then pass it over
			// don't much like this approach but it was the easiest to
			// integrate into an already established system
			if (PatternSettable.class.isAssignableFrom(newManager.getClass())) {

				// only set it if it exists, otherwise
				// we can assume that it was hopefully set elsewhere
				if (!TextValidator.isEmptyStr(entry.getPattern())) {

					LOGGER.info(String.format(MSG_PATTERN_SETTABLE, newManager
							.getClass().getName(), entry.getPattern()));

					PatternSettable ps = (PatternSettable) newManager;
					ps.setFilterRegex(entry.getPattern());

				} else {
					// just let the log reader know what's going on
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String
								.format("Will not set regex for [%s], even though it is settable.",
										newManager.getClass().getName()));
					}
				}
			}

			// notify log reader
			LOGGER.info(String.format(MSG_MANAGER_CREATED_FMT, newManager
					.getClass().getName()));

			final String key = entry.getTargetDirectory().getAbsolutePath();
			List<cerberus.core.files.FileManager> currentList = null;

			if (!map.containsKey(key)) {
				currentList = new ArrayList<>();
				map.put(key, currentList);
			}

			currentList = map.get(key);
			currentList.add(newManager);

			if (LOGGER.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("Adding entry [%s] for Key '%s'.",
						newManager.getClass().getName(), entry
								.getTargetDirectory().getAbsolutePath()));
				sb.append(String.format(
						" Currently there are %d entries for this key.",
						currentList.size()));
				sb.append(String
						.format(" For a total of %d entries in the map (may be larger).",
								map.size()));

				LOGGER.debug(sb.toString());
			}
		}
	}

	private static boolean buildTimedManager(TimedManager timedManager,
			FileManager entry) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Building TimedManager: "
					+ timedManager.getClass().getName());
		}

		String time = entry.getInterval();
		Period period = null;
		try {
			period = TimedManagers.parsePeriod(time);
		} catch (Exception e) {
			LOGGER.error("Failed to parse time: " + time);
			e.printStackTrace();
			return false;
		}

		LOGGER.info(String.format(MSG_TIMED_MANAGER_INTERVAL_FMT, timedManager
				.getClass().getName(), time));

		timedManager.setTimeInterval(period.getDays(), period.getHours(),
				period.getMinutes(), period.getSeconds());
		timedManager.calculateNextTargetTime();

		return true;
	}

	private static boolean validateEntry(FileManager entry) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(String.format("Validating entry w ID [%s]",
					entry.getId()));

		boolean success = true;
		final String msg = "File Manager Entry [%s] is not valid. Reason: %s";

		Validator<FileManager> validator = FileManagerVisitor.Factory.make();
		ValidationResult result = validator.validate(entry);

		if (!result.isSuccessful()) {
			LOGGER.error(String.format(msg, entry.getId(), result.getMessage()
					.getText()));
			success = false;
		}

		return success;
	}

	private static List<FileManager> queryManagerList() {
		LOGGER.info(MSG_LOADED_FM_START);

		List<FileManager> resultList = Collections.emptyList();
		List<FileManager> queryResult = Collections.emptyList();

		try (EntityDao<FileManager, Long> dao = DaoManager.Factory
				.connect(FileManager.class)) {
			queryResult = dao.findAll();
		} catch (Exception e) {
			LOGGER.error("Exception trapped attempting to query for all File Manager (entity) entries");
			e.printStackTrace();
			queryResult = Collections.emptyList();
		}

		if (!queryResult.isEmpty()) {

			resultList = new LinkedList<>();

			for (FileManager fme : queryResult) {

				if (validateEntry(fme)) {

					// append to list
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String.format(
								"FM [%s] passed validation.", fme
										.getFileManagerEntry().getClassName()));
					}

					if (!fme.getEnabled()) {
						LOGGER.warn(String
								.format("FM [%s:%s] is DISABLED in the database and will not be added to the cached list.",
										fme.getFileManagerEntry()
												.getClassName(), fme
												.getTargetDirectory()
												.getAbsolutePath()));
					} else {
						resultList.add(fme);
					}
				} else {
					LOGGER.warn(String
							.format("File Manager [%s] was not valid, will discard this FM.",
									fme.getFileManagerEntry().getClassName()));
				}
			}
		}

		LOGGER.info(MSG_LOADED_FM_COMPLETE);
		return resultList;
	}

	// these need to be performant operations
	// cache what can be cached

	@Override
	public List<Path> getTargetDirectories() {
		return DIRECTORIES;
	}

	@Override
	public List<cerberus.core.files.FileManager> getManagersFor(Path directory) {
		List<cerberus.core.files.FileManager> fileManagers = Collections
				.emptyList();
		if (!Files.isDirectory(directory)) {
			LOGGER.warn(String.format(MSG_DIR_NO_EXIST_FMT,
					directory.toString()));
		} else {
			if (MANAGERS.containsKey(directory.toString())) {
				fileManagers = MANAGERS.get(directory.toString());
			} else {
				// this will continuously print out
				// which is ok, it indicates something
				// is pretty seriously wrong
				LOGGER.error(String
						.format("The cached directory list contained a directory '%s' that was not in the map!",
								directory.toString()));
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (!fileManagers.isEmpty()) {
				LOGGER.debug(String.format(MSG_FOUND_FILE_MANAGERS,
						fileManagers.size(), directory.toString()));
			}
		}

		return fileManagers;
	}

}
