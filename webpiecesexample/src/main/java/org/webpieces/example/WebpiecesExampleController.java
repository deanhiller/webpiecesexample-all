package org.webpieces.example;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import org.webpieces.example.example.SomeLibrary;

@Singleton
public class WebpiecesExampleController {
	
	@Inject
	private SomeLibrary someLibrary;
	
	public Action myMethod() {
		someLibrary.doSomething();
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		return Actions.renderThis(
				"user", "Dean Hiller",
				"id", 500,
				"otherKey", "key");
	}
	
	public Action anotherMethod() {
		return Actions.redirect(WebpiecesExampleRouteId.SOME_ROUTE);
	}
	
}
