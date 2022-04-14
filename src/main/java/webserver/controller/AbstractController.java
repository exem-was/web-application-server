package webserver.controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) {
		if ("POST".equals(request.getMethod())) {
			doPost(request, response);
			return;
		}
		if ("GET".equals(request.getMethod())) {
			doGet(request, response);
			return;
		}
		throw new RuntimeException("not implemented");
	}

	public abstract void doPost(HttpRequest request, HttpResponse response);

	public abstract void doGet(HttpRequest request, HttpResponse response);
}
