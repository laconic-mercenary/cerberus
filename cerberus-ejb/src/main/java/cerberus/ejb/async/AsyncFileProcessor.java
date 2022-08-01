package cerberus.ejb.async;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Future;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.files.FileManager;
import cerberus.core.files.FileManagerService;
import cerberus.core.files.impl.managers.TimedManager;
import cerberus.core.files.impl.managers.filtered.FilteredTimedManager;
import cerberus.core.files.tasks.FileManagerTaskingAdapter;
import cerberus.core.tasks.AsyncEligible;
import cerberus.core.tasks.AsyncTasking;

@Stateless(description = "Async EJB to trigger a round of FileManager handling")
public class AsyncFileProcessor {

	private static final Logger LOGGER = Logger
			.getLogger(AsyncFileProcessor.class);

	private static final String MSG_HANDLING_FILES_FMT = "FileManager -(%s) will handle files in directory %s. "
			+ "Note that the handling may or may not occur right now.";

	private static final String MSG_EXE_ASYNC = "Executing async task...";

	private static final String MSG_DIR_EMPTY = "No target directories were specified. No operations to perform.";

	private static final String MSG_NO_FM_FMT = "No FileManagers found for directory %s";

	/**
	 * This is where the magic happens. A map of super classes implementing
	 * FileManager are stored in the FileManagerService with the directory as
	 * the key. This goes through the list of them and they handle files as
	 * needed. Most of these file managers are stored in the file managers
	 * database table.
	 */
	@Asynchronous
	public Future<Boolean> executeAsync() {
		// there will probably be a minimal amount of logging here due to the
		// frequency that this will be invoked - should expect file managers to
		// handle their own logging appropriately

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(MSG_EXE_ASYNC);

		FileManagerService fms = FileManagerService.Factory.make();

		// this is cached in the FMS
		Collection<Path> directories = fms.getTargetDirectories();

		if (directories.isEmpty()) {
			LOGGER.warn(MSG_DIR_EMPTY);
		} else {

			// loop through all of the cached directories
			for (Path directory : directories) {

				// comes out of a Map<>
				Collection<FileManager> fileManagers = fms
						.getManagersFor(directory);

				if (fileManagers.isEmpty()) {
					LOGGER.warn(String.format(MSG_NO_FM_FMT,
							directory.toString()));
				} else {

					// go through the file managers one by one that are
					// assigned to this directory
					for (FileManager fileManager : fileManagers) {

						// this isEnabled value is probably only loaded
						// on startup and not checked again - for performance
						if (fileManager.isEnabled()) {

							// does this file manager even require files?
							if (fileManager.isRequiringFiles()) {

								// inform the log reader
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug(String.format(
											MSG_HANDLING_FILES_FMT, fileManager
													.getClass().getName(),
											directory.toString()));
								}

								// list only the files that match the
								// pattern of
								// the file manager - which is what the file
								// filter is supposed to do
								try (DirectoryStream<Path> directoryStream = Files
										.newDirectoryStream(directory,
												fileManager.getFileFilter())) {

									// files have been filtered, so the ones
									// in
									// this list are the ones we must
									// operate on
									Collection<Path> files = new LinkedList<>();
									for (Path file : directoryStream)
										files.add(file);

									// is this filemanager meant to be posted as
									// an async task?
									if (fileManager.getClass()
											.isAnnotationPresent(
													AsyncEligible.class)) {

										// it is ready - post to the bus
										handleAsync(fileManager, files);

									} else {
										// handle the filemanager synchronously
										// perform the handling of the files
										fileManager.handleFiles(files);
									}

								} catch (IOException e) {
									LOGGER.error(String
											.format("Error occurred when attempting to handle files with manager [%s]",
													fileManager.getClass()
															.getName()));
									e.printStackTrace();
								}
							} else {

								// is file manager is not meant to handle files
								// but that means it may be a good candidate for
								// asynchronous processing
								if (fileManager.getClass().isAnnotationPresent(
										AsyncEligible.class)) {

									// no need to handle files
									// assume that the lack of files is
									// handled appropriate downstream
									handleAsync(fileManager,
											Collections.emptyList());

								} else {
									try {
										fileManager.handleFiles(Collections
												.emptyList());
									} catch (Exception e) {
										LOGGER.error(String
												.format("Error occurred when a non-file manager [%s] was invokved",
														fileManager.getClass()
																.getName()));
										e.printStackTrace();
									}
								} // is async enabled
							} // does it need files
						} // is the file manager enabled
					} // for each file manager
				}
			}
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Finished async task");

		// unused
		// just be aware that this is available
		// save some memory for now at least...
		return null;/* return new AsyncResult<Boolean>(null); */
	}

	private static void handleAsync(FileManager fileManager,
			Collection<Path> files) {

		// SO...why not TimedManager? It
		// goes FileManager > TimedManager >
		// FilteredTimedManager >
		// (fileManager variable's class
		// type) TimedManager returned false
		// on the instanceof check
		// so.... instanceof must only 'see'
		// the immediate extends and
		// implements on the class
		// the fileManager instance belongs
		// to... odd

		// if it's a timed manager, don't
		// post it automatically - only do
		// the necessary processing iff it
		// is ready

		if (fileManager instanceof TimedManager
				|| fileManager instanceof FilteredTimedManager) {

			TimedManager timedManager = (TimedManager) fileManager;

			if (!timedManager.isReady())
				return;

		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(
					"FileManager %s is annotated %s - posting", fileManager
							.getClass().getName(), AsyncEligible.class
							.getSimpleName()));
		}

		FileManagerTaskingAdapter adapter = FileManagerTaskingAdapter.Factory
				.make();
		AsyncTasking task = null;
		if (files.isEmpty())
			task = adapter.convert(fileManager);
		else
			task = adapter.convert(fileManager, files);

		CerberusEventBus.get().post(task);
	}
}
