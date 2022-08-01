
INSERT INTO KVP_PROPERTIES (KEY_ENTRY, VALUE_ENTRY, DESCRIPTION)
VALUES 
	('cerberus.core.files.impl.managers.CameraImageManager.move_dir', '/opt/apps/cerberus/intake/images/', 'Where the image servlet will have access'),
	('cerberus.core.files.impl.managers.CameraImageManager.new_filename_fmt', 'img-%d-%d.%s', 'Java String format which must have the d for timestamp and s for extension'),
	('cerberus.app.servlets.ImageServlet.image_directory', '/opt/apps/cerberus/intake/images', 'Where the servlet will soak up the images for display'),
	('cerberus.core.files.impl.managers.purge.filtered.PreserveYoungerFilesPurge.enabled', 'true', ''),
	('cerberus.core.files.impl.managers.purge.filtered.PreserveYoungerFilesPurge.amount_to_preserve', '10', 'Amount of files to NOT purge'),
	('cerberus.app.motion.MotionSnapshotBean.attempt_hardlink', 'true', 'Hardlink motion images to the images directory rather than move them'),
	('cerberus.core.files.impl.managers.MotionImageMovementManager.target_image_dir', '/opt/apps/cerberus/intake/images/motion', 'Where the motion snapshots go when received by monitor client'),
	('cerberus.app.monitor_client_listing_port', '8888', 'Port that the monitor client listens on for requests from the cerberus server'),
	('cerberus.app.monitor_client_address_format', 'http://%s:%d/', 'String format of the monitor client address');
COMMIT;

INSERT INTO FILE_MANAGER_ENTRIES (ID, CLASSNAME)
VALUES
	(1, 'cerberus.core.files.impl.managers.CameraImageManager'),
	(2, 'cerberus.core.files.impl.managers.purge.FilesPurge'),
	(3, 'cerberus.core.files.impl.managers.purge.filtered.PreserveYoungerFilesPurge'),
	(4, 'cerberus.core.files.impl.managers.purge.PingPurge'),
	(5, 'cerberus.core.files.impl.managers.MotionImageMovementManager');
COMMIT;

INSERT INTO TARGET_DIRECTORIES (ID, ABSOLUTE_PATH)
VALUES
	(1, '/opt/apps/cerberus/lz'),
	(2, '/opt/apps/cerberus/intake/images'),
	(3, '/opt/apps/cerberus/intake/images/motion'),
	(4, '/opt/apps/cerberus/intake');
COMMIT;

INSERT INTO FILE_MANAGERS (ID, TARGET_DIRECTORY, FILE_MANAGER_KEY, INTERVAL, PATTERN, ENABLED, REQUIRES_FILES, DESCRIPTION, PARAMETERS)
VALUES
	(1, 1, 1, '00,00,00,00:00:02', '.*\.jpg', 1, 1, 'Receives image files', ''),
	(2, 1, 3, '00,00,05,00:00:00', '(.*)', 1, 1, 'Leave behind a handful of files', ''),
	(3, 2, 3, '00,00,00,05:00:00', '(.*)', 1, 1, 'Leave behind a handful of images', ''),
	(4, 1, 5, '00,00,00,00:01:00', '.*\.MOTION\.jpeg', 1, 1, 'Transports motion snapshots to a target directory This ext must sync with monitor clients', ''),
	(5, 3, 3, '00,00,07,00:00:00', '(.*)', 1, 1, 'Purge off motion images every week', ''),
	(6, 4, 4, '00,00,00,02:00:00', ' ', 1, 0, 'Purge off old Pings in database No files are handled here The pattern and dir are ignored', '');
COMMIT;

SELECT * FROM KVP_PROPERTIES;
