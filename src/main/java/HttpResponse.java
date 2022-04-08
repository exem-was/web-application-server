import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private Map<String, String> headers = new HashMap<>();
	private DataOutputStream dos;

	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
	}

	public void forward(String path) throws IOException {
		dos.writeBytes("HTTP/1.1 301 Found \r\n");
		dos.writeBytes("Set-Cookie: logined=true\r\n");
		dos.writeBytes(format("Location: %s\r\n", redirectPath));
		dos.writeBytes("\r\n");
	}

	public void sendRedirect(String path) throws IOException {
		dos.writeBytes("HTTP/1.1 302 Found \r\n");
		dos.writeBytes("Set-Cookie: logined=true\r\n");
		dos.writeBytes(format("Location: %s\r\n", redirectPath));
		dos.writeBytes("\r\n");
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
}
