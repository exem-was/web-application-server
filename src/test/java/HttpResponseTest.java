import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class HttpResponseTest extends TestCase {
	private String testDirectory = "./src/main/resources/";

	@Test
	void responseForward() throws Exception {
		// Http_Forward.txt 결과는 응답 body 에 index.html이 포함되어 있어야 한다.
		HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
		response.forward("/index.html");
	}

	@Test
	void responseRedirect() throws Exception {
		// Http_Redirect.txt 결과는 응답 header 에
		// Location 정보가 /index.html 로 포함되어 있어야 한다.
		HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
		response.sendRedirect("/index.html");
	}

	@Test
	void responsCookies() throws Exception {
		// Http_Cookie.txt 결과는 응답 header Set-Cookie 값으로
		// logined=true 값이 포함되어 있어야 한다.
		HttpResponse response = new HttpResponse(createOutputStream("Http_Cookie.txt"));
		response.addHeader("Set-Cookie", "logined=true");
		response.sendRedirect("/index.html");
	}

	private OutputStream createOutputStream(String fileName) throws FileNotFoundException {
		return new FileOutputStream(new File(testDirectory + fileName));
	}

}