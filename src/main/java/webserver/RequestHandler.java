package webserver;

import com.google.common.base.Charsets;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private int contentLength = 0;

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
            boolean logined = false;

            while (!line.equals("")) {
                line = br.readLine();
                parseContentLength(line);
                if (line.contains("Cookie")) {
                    logined = isLogin(line);
                }
                log.debug("line: {}", line);
            }

            DataOutputStream dos = new DataOutputStream(out);

            if (requestResource.equals("/user/create")) {
                Map<String, String> body = getRequestBody(br, contentLength);
                User createdUser = User.from(body);
                DataBase.addUser(createdUser);
                log.info("create user: {}", createdUser);
                response302Header(dos, "/index.html");
                dos.flush();
                return;
            }
            if (requestResource.equals("/user/login")) {
                Map<String, String> body = getRequestBody(br, contentLength);
                User user = DataBase.findUserById(body.get("userId"));
                if (Objects.isNull(user) || !user.getPassword().equals(body.get("password"))) {
                    requestPath = "/user/login_failed.html";
                } else {
                    response302WithLoginSuccess(dos, "/index.html");
                }
            }
            if (requestResource.equals("/user/list")) {
                if (!logined) {
                    requestPath = "/user/login.html";
                }
                if (logined) {
                    responseUserListPage(dos);
                }
            }

            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            response200Header(dos, body.length, parseContentType(requestPath));
            responseBody(dos, body);
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

    private void responseUserListPage(DataOutputStream dos) {
        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for (User user : users) {
            sb.append("<br>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</br>");
        }
        sb.append("</table>");
        byte[] bytes = sb.toString().getBytes();
        response200Header(dos, bytes.length, "text/html");
        responseBody(dos, bytes);
    }

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String logined = cookies.get("logined");
        if (logined == null) {
            return false;
        }
        return Boolean.parseBoolean(logined);
    }

    private void response302WithLoginSuccess(DataOutputStream dos, String redirectPath) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes(format("Location: %s\r\n", redirectPath));
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, String> getRequestBody(BufferedReader br, int contentLength) throws IOException {
        return HttpRequestUtils.parseQueryString(IOUtils.readData(br, contentLength));
    }

    private void parseContentLength(String line) {
        if (line.contains("Content-Length")) {
            contentLength = Integer.parseInt(line.split(":")[1].trim());
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

    private void response302Header(DataOutputStream dos, String redirectPath) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes(format("Location: %s\r\n", redirectPath));
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes(format("Content-Type: %s;charset=utf-8\r\n", contentType));
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
