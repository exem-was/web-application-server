package webserver.controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Objects;

public class LoginController extends AbstractController {
	@Override
	public void doPost(HttpRequest request, HttpResponse response) {

		User user = DataBase.findUserById(request.getParameter("userId"));
		if (Objects.isNull(user) || !user.getPassword().equals(request.getParameter("password"))) {
//			requestPath = "/user/login_failed.html";
		} else {
			response.forward("/index.html");
//			response302WithLoginSuccess(dos, "/index.html");
		}
	}

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {

	}
}
