package cerberus.app.servlets;

/**
 * Servlet implementation class ImageServlet
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cerberus.core.persistence.KvpProperties;
import cerberus.core.persistence.util.Keys;

import com.frontier.lib.validation.ObjectValidator;

/**
 * The Image servlet for serving from absolute path. Because things like <img
 * src=image/whatever.jpg/> are just GET requests, a servlet can be created to
 * take advantage of that. The servlet will need knowledge of the absolute path
 * to the images. This just takes the file-based image and returns it as a
 * response stream.
 */
@WebServlet(urlPatterns = { "/image/*" })
public class ImageServlet extends HttpServlet {

	// will be constructed to be:
	// cerberus.app.servlets.ImageServlet.image_directory
	public static final String KEY_IMAGE_DIR = "IMAGE_DIRECTORY";

	private static final Logger LOGGER = Logger.getLogger(ImageServlet.class);

	private static final long serialVersionUID = 87128734871273842L;

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	private static final String ENCODING = "UTF-8";

	private static final String IMG_MIME_TYPE = "image";

	private static final String KEY_CONTENT_LEN = "Content-Length";

	private static final String KEY_CONTENT_DISP = "Content-Disposition";

	private static final String CONTENT_DISP_FMT = "inline; filename=\"%s\"";

	private static final String MSG_OBTAINED_KEY = "Obtained '%s' for %s ";
	
	private static final String LOADING_MSG = "Loading configuration items...";

	// Properties
	// ---------------------------------------------------------------------------------

	private static String imagePath = null;

	// Actions
	// ------------------------------------------------------------------------------------

	static {
		// loadProperties();
		loadPropertiesDB();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Get requested image by path info.
		String requestedImage = request.getPathInfo();

		// Check if file name is actually supplied to the request URI.
		if (requestedImage == null) {
			// Do your thing if the image is not supplied to the request URI.
			// Throw an exception, or send 404, or show default/warning image,
			// or just ignore it.
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		// Decode the file name (might contain spaces and on) and prepare file
		// object.
		File image = new File(imagePath, URLDecoder.decode(requestedImage,
				ENCODING));

		// Check if file actually exists in filesystem.
		if (!image.exists()) {
			// Do your thing if the file appears to be non-existing.
			// Throw an exception, or send 404, or show default/warning image,
			// or just ignore it.
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		// Get content type by filename.
		String contentType = getServletContext().getMimeType(image.getName());

		// Check if file is actually an image (avoid download of other files by
		// hackers!).
		// For all content types, see:
		// http://www.w3schools.com/media/media_mimeref.asp
		if (contentType == null || !contentType.startsWith(IMG_MIME_TYPE)) {
			// Do your thing if the file appears not being a real image.
			// Throw an exception, or send 404, or show default/warning image,
			// or just ignore it.
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		// Init servlet response.
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setContentType(contentType);
		response.setHeader(KEY_CONTENT_LEN, String.valueOf(image.length()));
		response.setHeader(KEY_CONTENT_DISP,
				String.format(CONTENT_DISP_FMT, image.getName()));

		// Prepare streams.
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		final int EOS = 0;
		final int START = 0;

		try {
			// Open streams.
			input = new BufferedInputStream(new FileInputStream(image),
					DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(response.getOutputStream(),
					DEFAULT_BUFFER_SIZE);

			// Write file contents to response.
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length = 0;
			while ((length = input.read(buffer)) > EOS) {
				output.write(buffer, START, length);
			}
		} finally {
			// Gently close streams.
			close(output);
			close(input);
		}
	}

	private static String extract(String key) {
		String value = null;
		key = Keys.make(ImageServlet.class, key);
		try {
			value = KvpProperties.findProperty(key);
			LOGGER.info(String.format(MSG_OBTAINED_KEY, value, key));
		} catch (Exception e) {
			LOGGER.fatal(String.format(
					"Exception caught when extracting %s key '%s'.",
					ImageServlet.class.getName(), key));
			e.printStackTrace();
		}
		if (value == null) {
			LOGGER.fatal(String.format("%s property '%s' is required.",
					ImageServlet.class.getName(), key));
		}
		return value;
	}

	// Helpers (can be refactored to public utility class)
	// ----------------------------------------

	private static void loadPropertiesDB() {
		LOGGER.info(LOADING_MSG);

		String imageDirectory = extract(KEY_IMAGE_DIR.toLowerCase());
		ObjectValidator.raiseIfNull(imageDirectory);
		handleNewImagePath(imageDirectory);
		imagePath = imageDirectory;
	}

	private static void handleNewImagePath(String imagePath) {
		File imagePathFile = new File(imagePath);
		if (imagePathFile.exists()) {
			if (!imagePathFile.isDirectory()) {
				String msg = String
						.format("Directory %s in the ImageServlet properties MUST be a directory",
								imagePath);
				LOGGER.fatal(msg);
				throw new RuntimeException(msg);
			}
		} else {
			if (!imagePathFile.mkdirs()) {
				String msg = String
						.format("An attempt was made to create the non-existant directory '%s'"
								+ " but creation failed. This is a required directory.",
								imagePath);
				LOGGER.fatal(msg);
				throw new RuntimeException(msg);
			}
		}
	}

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				LOGGER.warn(String
						.format("Failed to close the resource [%s]. Will ignore and continue.",
								resource.getClass().getName()));
				e.printStackTrace();
			}
		}
	}

}