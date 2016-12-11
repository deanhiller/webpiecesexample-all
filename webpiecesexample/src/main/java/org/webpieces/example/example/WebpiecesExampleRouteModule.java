package org.webpieces.example.example;

import static org.webpieces.ctx.api.HttpMethod.*;

import org.webpieces.router.api.routing.AbstractRouteModule;

import org.webpieces.example.ExampleRouteId;
import org.webpieces.example.WebpiecesExampleRouteId;

public class WebpiecesExampleRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {

		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way.  The controller string is the 3rd parameter in the addRoute calls
		
		//That path is a semi-regular expression that we match on
		//The controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		addRoute(GET, "/",              "WebpiecesExampleLocalController.index", WebpiecesExampleRouteId.MAIN_ROUTE);
		
		addRoute(GET, "/examples",      "WebpiecesExampleLocalController.exampleList", WebpiecesExampleRouteId.SOME_ROUTE);      //local controller(same package as your RouteModule!!!!)
		addRoute(GET, "/nextexample",   "extra/ExtraController.relativeController", WebpiecesExampleRouteId.EXTRA_ROUTE); //shows a controller relative to THIS module
		addRoute(GET, "/another",       "../WebpiecesExampleController.anotherMethod", WebpiecesExampleRouteId.ANOTHER);  //relative path from THIS module to Controller going down
		addRoute(GET, "/absolute",      "/org/webpieces/example/WebpiecesExampleController.myMethod", WebpiecesExampleRouteId.ABSOLUTE);    //absolute path from root of classpath...
		addRoute(GET, "/absolute2",     "org.webpieces.example.WebpiecesExampleController.myMethod", WebpiecesExampleRouteId.ABSOLUTE2);    //we don't advise this as it is not consistent with relative paths but it can be done if you like
		addRoute(GET, "/redirect/{id}", "WebpiecesExampleLocalController.redirect", WebpiecesExampleRouteId.REDIRECT_PAGE);    //shows a redirect example in the controller method
		addRoute(GET, "/async",         "WebpiecesExampleLocalController.myAsyncMethod", WebpiecesExampleRouteId.ASYNC_ROUTE); //for advanced users who want to release threads to do more work
		
		//basic crud example
		addRoute(GET,  "/userform",     "crud/ExampleController.userForm", ExampleRouteId.GET_USER_FORM);
		addRoute(POST, "/postuser",     "crud/ExampleController.postUser", ExampleRouteId.POST_USER_FORM);
		addRoute(GET,  "/listusers",    "crud/ExampleController.listUsers", ExampleRouteId.GET_LIST_USERS);
		
		addStaticFile("/favicon.ico", "public/favicon.ico", false);
		String workingDir = System.getProperty("user.dir");
		addStaticDir("/public/", workingDir+"/public/", false);
		
		setPageNotFoundRoute("WebpiecesExampleLocalController.notFound");
		setInternalErrorRoute("WebpiecesExampleLocalController.internalError");
	}

}
