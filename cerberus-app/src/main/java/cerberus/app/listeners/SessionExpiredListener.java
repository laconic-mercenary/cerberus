package cerberus.app.listeners;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.log4j.Logger;

public class SessionExpiredListener implements PhaseListener {

	private static final long serialVersionUID = -2424733823305770269L;

	private static final Logger LOGGER = Logger
			.getLogger(SessionExpiredListener.class);

	private static final String CONTEXT_ROOT = "/cerberus";

	/**
	 * no bean actions will trigger on the ajax request - just this logic.
	 */
	@Override
	public void afterPhase(PhaseEvent event) {
		if (isSessionExpired()) {
			LOGGER.warn(String
					.format("A user session has expired. Refreshing that session and redirecting to %s.",
							CONTEXT_ROOT));
			redirect(event);
		}
	}

	@Override
	public void beforePhase(PhaseEvent arg0) {
	}

	private static boolean isSessionExpired() {
		// was noted that pretty much everything in the session map is blown
		// away when it expires
		return FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().isEmpty();
	}

	private static void redirect(PhaseEvent event) {
		try {
			event.getFacesContext().getExternalContext().redirect(CONTEXT_ROOT);
		} catch (IOException e) {
			LOGGER.error(String
					.format("Failed to redirect to %s", CONTEXT_ROOT));
			e.printStackTrace();
		}
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

	/*
	 * private static void printOutSession() { Map<String, Object> map =
	 * FacesContext.getCurrentInstance() .getExternalContext().getSessionMap();
	 * 
	 * for (String key : map.keySet()) {
	 * System.out.println(String.format("%s : %s", key, map.get(key))); } }
	 */

}
