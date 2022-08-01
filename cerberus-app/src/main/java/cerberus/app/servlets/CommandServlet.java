package cerberus.app.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cerberus.core.commands.frontdoor.CommandReceptionHandler;

@WebServlet(urlPatterns = { CommandServlet.SERVLET_CONTEXT })
public class CommandServlet extends HttpServlet {

	/** sid */
	private static final long serialVersionUID = 9112024792341869209L;

	public static final String SERVLET_CONTEXT = "/cmd";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CommandReceptionHandler.Factory.make().handle(req);
	}
}
