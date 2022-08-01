package cerberus.core.commands.gatekeeper.impl;

import static com.frontier.lib.validation.TextValidator.isEmptyStr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cerberus.core.commands.SenderInformation;
import cerberus.core.commands.gatekeeper.GateKeeper;
import cerberus.core.commands.impl.BasicSenderInformation;

public class RequestGateKeeper implements
		GateKeeper<HttpServletRequest, SenderInformation> {

	private static final Logger LOGGER = Logger
			.getLogger(RequestGateKeeper.class);

	private static final String KEY_MACHINE_NAME = "MACHINE";

	private static final String CA_XFWDFOR = "X-Forwarded-For";

	private static final String CA_PROXYCLIENTIP = "Proxy-Client-IP";

	private static final String CA_WLPROXYCLIENTIP = "WL-Proxy-Client-IP";

	private static final String CA_HTTPCLIENTIP = "HTTP_CLIENT_IP";

	private static final String CA_HTTPXFWDFOR = "HTTP_X_FORWARDED_FOR";

	private static final String UNK_RET = "unknown";

	private static final String MSG_MISSING_REQUIRED_FMT = "Request is missing required entry '%s'";

	private final static String KEY_PREAMBLE = "cerberus";

	private HttpServletRequest request = null;

	private boolean isProcessed = false;

	private boolean accepted = false;

	public RequestGateKeeper(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public void processRx() {
		this.accepted = (getRx() != null)
				&& acceptable(extractClientName(getRx()),
						extractClientAddr(getRx()), getRx().getParameterMap());
		this.isProcessed = true;
	}

	@Override
	public boolean isRxProcessed() {
		return isProcessed;
	}

	@Override
	public boolean wasRxAccepted() {
		return accepted;
	}

	@Override
	public HttpServletRequest getRx() {
		return this.request;
	}

	@Override
	public SenderInformation getResults() {
		BasicSenderInformation bsi = new BasicSenderInformation(
				extractClientName(getRx()), extractClientAddr(getRx()));
		return bsi;
	}

	private static String extractClientName(HttpServletRequest request) {
		return request.getParameter(KEY_MACHINE_NAME);
	}

	private static String extractClientAddr(HttpServletRequest request) {
		String ip = request.getHeader(CA_XFWDFOR);
		if (isEmptyStr(ip) || UNK_RET.equalsIgnoreCase(ip)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using Proxy-Client-IP");
			ip = request.getHeader(CA_PROXYCLIENTIP);
		}
		if (isEmptyStr(ip) || UNK_RET.equalsIgnoreCase(ip)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using WL-Proxy-Client-IP");
			ip = request.getHeader(CA_WLPROXYCLIENTIP);
		}
		if (isEmptyStr(ip) || UNK_RET.equalsIgnoreCase(ip)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using HTTP_CLIENT_IP");
			ip = request.getHeader(CA_HTTPCLIENTIP);
		}
		if (isEmptyStr(ip) || UNK_RET.equalsIgnoreCase(ip)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using HTTP_X_FORWARDED_FOR");
			ip = request.getHeader(CA_HTTPXFWDFOR);
		}
		if (isEmptyStr(ip) || UNK_RET.equalsIgnoreCase(ip)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using getRemoteAddr()");
			ip = request.getRemoteAddr();
		}

		// check if the sender is localhost
		ip = checkLoopback(ip);
		return ip;
	}

	private static String checkLoopback(String ipAddress) {
		String finalAddress = ipAddress;
		String loopback = null;
		try {
			loopback = InetAddress.getLoopbackAddress().getHostName();
		} catch (Exception e) {
			LOGGER.error("Failed to determine the loopback address");
			e.printStackTrace();
			return finalAddress;
		}

		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			LOGGER.error("Failed to invoke getByName() for IP: " + ipAddress);
			e.printStackTrace();
			return finalAddress;
		}

		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
			finalAddress = loopback;
		}
		return finalAddress;
	}

	private static boolean acceptable(String name, String address,
			Map<String, String[]> map) {
		boolean accepted = true;
		if (isEmptyStr(name)) {
			accepted = false;
			LOGGER.warn(String.format(MSG_MISSING_REQUIRED_FMT, "name"));
		}
		if (isEmptyStr(address)) {
			accepted = false;
			LOGGER.warn(String.format(MSG_MISSING_REQUIRED_FMT, "address"));
		}
		if (map == null) {
			accepted = false;
			LOGGER.warn("No request map to evaluate");
		}
		if (map != null && !map.containsKey(KEY_PREAMBLE)) {
			accepted = false;
			LOGGER.warn(String.format(MSG_MISSING_REQUIRED_FMT, "preamble key"));
		}
		return accepted;
	}
}
