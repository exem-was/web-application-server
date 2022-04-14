package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class HttpResponse {

	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

	private Map<String, String> headers = new HashMap<>();
	private DataOutputStream dos;

	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
	}

	public void forward(String path) {
		try {
			byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
			response200Header(dos, body.length, parseContentType(path));
			responseBody(dos, body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void sendRedirect(String path) throws IOException {
		dos.writeBytes("HTTP/1.1 302 Found \r\n");
		processHeaders();
		dos.writeBytes(format("Location: %s\r\n", path));
		dos.writeBytes("\r\n");
	}

	private void processHeaders() {
		headers.forEach((key, value) -> {
			try {
				dos.writeBytes(key + ": " + value + "\r\n");
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		});
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private String parseContentType(String requestPath) {
		if (requestPath.endsWith(".html")) {
			return "text/html";
		}
		if (requestPath.endsWith(".css")) {
			return "text/css";
		}
		if (requestPath.endsWith(".js")) {
			return "text/javascript";
		}
		return "text/plain";
	}
}
