package org.webpieces.base.examples;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import org.webpieces.base.libs.RemoteService;
import org.webpieces.base.libs.SomeLibrary;
import org.webpieces.base.mgmt.SomeBean;
import org.webpieces.base.mgmt.SomeBeanWebpiecesManaged;

@Singleton
public class ExamplesController {

	@Inject
	private RemoteService service;
	@Inject
	private SomeLibrary someLib;
	
	//This is injected to demonstrate the properties plugin so you can modify properties via a web page and changes are stored in database
	//so changes will survive a restart.
	@Inject
	private SomeBean managed;

	
	public Action index() {
		//this is so the test can throw an exception from some random library that is mocked
		someLib.doSomething(5); 
		
		//renderThis renders index.html in the same package as this controller class
		return Actions.renderThis(); 
	}
	
	public Action exampleList() {
		return Actions.renderThis("user", "Dean Hiller", "count", managed.getCount());
	}

	public Action redirect(String id) {
		return Actions.redirect(ExamplesRouteId.MAIN_ROUTE);
	}
	
	public Action loadUser(String name) {
		return Actions.renderThis("user", name);
	}
	
	public CompletableFuture<Action> myAsyncMethod() {
		CompletableFuture<Integer> remoteValue = service.fetchRemoteValue("dean", 21);
		return remoteValue.thenApply(s -> convertToAction(s));
	}
	//called from method above
	private Action convertToAction(int value) {
		return Actions.renderThis("value", value);
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
	
	public Action internalError() {
		return Actions.renderThis();
	}
}
