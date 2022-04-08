import com.google.common.base.Charsets;
import util.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpRequest {

	private String path;
	private String method;
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> parameters = new HashMap<>();

	public HttpRequest(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
		String firstLine = br.readLine();
		path = getRequestPath(firstLine);
		method = parseMethod(firstLine);
		readHeaders(br);
		readParameters(br, firstLine);
	}

	private void readParameters(BufferedReader br, String firstLine) throws IOException {
		if (method.equals("GET")) {
			String keyValue = firstLine.split("\\s")[1].split("\\?")[1];
			parameters.putAll(HttpRequestUtils.parseQueryString(keyValue));
			return;
		}

		String line = br.readLine();
		parameters.putAll(HttpRequestUtils.parseQueryString(line));
	}

	private void readHeaders(BufferedReader br) throws IOException {
		String line = br.readLine();
		while (Objects.nonNull(line) && !"".equals(line)) {
			readHeader(line);
			line = br.readLine();
		}
	}

	private void readHeader(String line) {
		String[] keyValue = line.split(":");
		headers.put(keyValue[0].trim(), keyValue[1].trim());
	}

	private String parseMethod(String line) {
		return line.split("\\s")[0];
	}

	private String getRequestPath(String line) {
		return line.split("\\s")[1].split("\\?")[0];
	}

	public String getPath() {
		return path;
	}

	public String getMethod() {
		return method;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public String getParameter(String key) {
		return parameters.get(key);
	}
}
