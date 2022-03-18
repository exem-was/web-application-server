package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Charsets;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            String line = br.readLine();
            String requestPath = getRequestPath(line);
            log.debug("line : {}, request page : {}", line, requestPath);
            String requestResource = getRequestResource(requestPath);
            Map<String, String> requestParameters = getRequestParameters(requestPath);

            while (!line.equals("")) {
                line = br.readLine();
                log.debug("line: {}", line);
            }
            if (requestResource.equals("/user/create")) {
                User createdUser = User.from(requestParameters);
                log.info("create user: {}", createdUser);
            }
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, String> getRequestParameters(String requestPath) {
        if (requestPath.indexOf("?") == -1) {
            return Collections.emptyMap();
        }
        String parameters = requestPath.split("\\?")[1];
        return HttpRequestUtils.parseQueryString(parameters);
    }

    private String getRequestResource(String requestPath) {
        return requestPath.split("\\?")[0];
    }

    private String getRequestPath(String line) {
        return line.split("\\s")[1];
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
}
