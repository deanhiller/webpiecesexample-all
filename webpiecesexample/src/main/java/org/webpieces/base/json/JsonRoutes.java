package org.webpieces.base.json;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Router;
import org.webpieces.router.api.routing.Routes;

public class JsonRoutes implements Routes {

	@Override
	public void configure(Router router) {
		
		router.addContentRoute(BOTH, GET , "/json/read",         "JsonController.readOnly");

		router.addContentRoute(BOTH, GET , "/json/{id}",         "JsonController.jsonRequest");
		router.addContentRoute(BOTH, POST , "/json/{id}",        "JsonController.postJson");

		router.addContentRoute(BOTH, GET , "/json/async/{id}",   "JsonController.asyncJsonRequest");
		router.addContentRoute(BOTH, POST, "/json/async/{id}",   "JsonController.postAsyncJson");

		router.addContentRoute(BOTH, GET , "/json/throw/{id}",        "JsonController.throwNotFound");

	}

}
