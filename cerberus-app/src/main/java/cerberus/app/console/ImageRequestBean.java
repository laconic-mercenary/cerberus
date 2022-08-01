package cerberus.app.console;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.log4j.Logger;

import cerberus.core.eventbus.CerberusEventBus;

// could also have used the annotation ManagedBean
// using the Named annotation is saying that this is a CDI bean
@Named
@SessionScoped
public class ImageRequestBean implements Serializable {

	private static final long serialVersionUID = 3366731617040020442L;

	private static final Logger LOGGER = Logger
			.getLogger(ImageRequestBean.class);

	// this won't actually be closed until the bean is destroyed
	private CloseableHttpAsyncClient client = null;

	private static final String MSG_SEND_REQ = "Sending Image Request to: %s";

	private static final String MSG_RESPONSE_OK = "Response received, Rx = OK.";

	//
	// Methods
	//

	@PostConstruct
	public void initialize() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Initializing http async client");
		}
		// register to receive image available events
		CerberusEventBus.get().register(this);

		// client that will send image requests
		if (client == null) {
			client = HttpAsyncClientBuilder.create().build();
			client.start();
		}
	}

	@PreDestroy
	public void shutdown() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Shutting down http async client");
		}

		// unregister
		CerberusEventBus.get().unregister(this);

		if (client != null) {
			try {
				client.close();
				client = null;
			} catch (IOException e) {
				LOGGER.error("Failed to close async client.", e);
				e.printStackTrace();
			}
		}
	}

	public void sendImageRequestAsync(String urlTarget) {
		LOGGER.info(String.format(MSG_SEND_REQ, urlTarget));
		try {
			HttpPost request = new HttpPost(urlTarget);
			HttpClientContext context = HttpClientContext.create();
			// Future<HttpResponse> future = client
			// .execute(request, context, null);

			// execute asynchronously
			Future<HttpResponse> future = client.execute(request, context,
					new FutureCallback<HttpResponse>() {

						@Override
						public void failed(Exception e) {
							LOGGER.error("Request failed, see server.log for details.");
							e.printStackTrace();
						}

						@Override
						public void completed(HttpResponse response) {
							final int rxCode = response.getStatusLine()
									.getStatusCode();
							if (rxCode != HttpServletResponse.SC_OK) {
								LOGGER.warn(String
										.format("Request completed: received response code %d",
												rxCode));
							} else {
								LOGGER.info(MSG_RESPONSE_OK);
							}
						}

						@Override
						public void cancelled() {
							LOGGER.warn("Request was cancelled.");
						}
					});
			// calling the following:
			// future.get()
			// will actually result in a BLOCKING action - just for future
			// reference.
			// HttpResponse response = future.get();
		} catch (Exception e) {
			LOGGER.error("Failed to send request to target: " + urlTarget);
			e.printStackTrace();
		}
	}
}
