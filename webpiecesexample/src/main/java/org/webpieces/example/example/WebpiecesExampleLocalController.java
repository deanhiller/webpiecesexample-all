package org.webpieces.example.example;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import org.webpieces.example.WebpiecesExampleRouteId;

@Singleton
public class WebpiecesExampleLocalController {

	@Inject
	private RemoteService service;

	public Action index() {
		return Actions.renderThis();
	}
	
	public Action exampleList() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public CompletableFuture<Action> myAsyncMethod() {
		CompletableFuture<Integer> remoteValue = service.fetchRemoteValue();
		return remoteValue.thenApply(s -> convertToAction(s));
	}
	
	private Action convertToAction(int value) {
		return Actions.renderThis("value", value);
	}
	
	public Action redirect(String id) {
		return Actions.redirect(WebpiecesExampleRouteId.RENDER_PAGE);
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
	
	public Action internalError() {
		return Actions.renderThis();
	}
}
