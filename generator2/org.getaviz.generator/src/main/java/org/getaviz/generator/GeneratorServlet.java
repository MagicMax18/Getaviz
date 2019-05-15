package org.getaviz.generator;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration.OutputFormat;

public class GeneratorServlet extends HttpServlet {
	private static final long serialVersionUID = -5343549433924172589L;
	private static final Log log = LogFactory.getLog(GeneratorServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("GET request generator");
		SettingsConfiguration config = SettingsConfiguration.getInstance("/opt/config/settings.properties");
		Generator generator = new Generator(config);
		generator.run();
		writeResponse(response, config);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("POST request generator");
		SettingsConfiguration config = SettingsConfiguration.getInstance(request);
		Generator generator = new Generator(config);
		generator.run();
		writeResponse(response, config);
	}
	
	private void writeResponse(HttpServletResponse response, SettingsConfiguration config) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println("<h1>Getaviz</h1>");
		out.println("<h3>Visualization is generated, please wait. You will be redirected automatically.</h3>");
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		InetAddress addr = InetAddress.getByName("frontend");
		String url = "http://" + addr.getHostAddress() + "/ui/index.php";
		String name = config.getName();
		boolean aframe = config.getOutputFormat() == OutputFormat.AFrame;
		response.setHeader("Location", url + "?aframe=" + aframe + "&model=" + name
				+ "&setup=web_a-frame/default" + "&srcDir=data-gen");
	}
}
