package org.webpieces.base.crud.login;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.webserver.api.login.AbstractLoginRoutes;
import static org.webpieces.router.api.routing.Port.HTTPS;

/** 
 * Move this to the client applications instead since it is specific to one of the app's login methods
 * @author dhiller
 *
 */
public class LoginRoutes extends AbstractLoginRoutes {

	/**
	 * @param controller
	 * @param basePath The 'unsecure' path that has a login page so you can get to the secure path
	 * @param securePath The path for the secure filter that ensures everyone under that path is secure
	 * @param sessionToken
	 */
	public LoginRoutes(String controller, String securePath, String ... secureFields) {
		super(controller, null, securePath, secureFields);
	}
	
	@Override
	protected RouteId getPostLoginRoute() {
		return LoginRouteId.POST_LOGIN;
	}

	@Override
	protected RouteId getRenderLoginRoute() {
		return LoginRouteId.LOGIN;
	}

	@Override
	protected RouteId getRenderLogoutRoute() {
		return LoginRouteId.LOGOUT;
	}

	@Override
	protected String getSessionToken() {
		return AppLoginController.TOKEN;
	}

	@Override
	protected void addLoggedInHome(Router baseRouter, Router scopedRouter1) {
		Router scopedRouter = scopedRouter1.getScopedRouter("/secure");
		scopedRouter.addRoute(HTTPS, HttpMethod.GET ,   "/loggedinhome",        "AppLoginController.home", LoginRouteId.LOGGED_IN_HOME);
	}

}
