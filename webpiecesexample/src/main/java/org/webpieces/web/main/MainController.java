package org.webpieces.web.main;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

import org.webpieces.GlobalAppContext;
import org.webpieces.mgmt.SomeBean;
import org.webpieces.service.RemoteService;
import org.webpieces.service.SomeLibrary;

@Singleton
public class MainController {

	private final RemoteService service;
	private final SomeLibrary someLib;
	
	//This is injected to demonstrate the properties plugin so you can modify properties via a web page and changes are stored in database
	//so changes will survive a restart.
	private final SomeBean managed;
	private GlobalAppContext injectedCtx;

	@Inject
	public MainController(RemoteService service, SomeLibrary someLib, SomeBean managed, GlobalAppContext injectedCtx) {
		super();
		this.service = service;
		this.someLib = someLib;
		this.managed = managed;
		this.injectedCtx = injectedCtx;
	}

	public Action index() {
		//this is so the test can throw an exception from some random library that is mocked
		someLib.doSomething(5); 
		
		//renderThis renders index.html in the same package as this controller class
		return Actions.renderThis(); 
	}

	public Action mySyncMethod() {
		
		GlobalAppContext ctx = (GlobalAppContext) Current.applicationContext();
		
		if(ctx != injectedCtx)
			throw new RuntimeException("We should fail here");
		
		return Actions.renderThis("value", 21);
	}
	
	public CompletableFuture<Action> myAsyncMethod() {
		CompletableFuture<Integer> remoteValue = service.fetchRemoteValue("dean", 21);
		return remoteValue.thenApply(s -> convertToAction(s));
	}
	
	//called from method above
	private Action convertToAction(int value) {
		return Actions.renderThis("value", value);
	}
	
	public Render notFound() {
		return Actions.renderThis();
	}
	
	public Render internalError() {
		return Actions.renderThis();
	}
	
	public CompletableFuture<StreamWriter> myStream(RequestContext requestCtx, RouterStreamHandle handle) {
		Http2Request req = requestCtx.getRequest().originalRequest;
		Http2Response resp = handle.createBaseResponse(req, "application/x-ourexample", 200, "OK");
		
		return handle.process(resp).thenApply(s -> new RequestStreamEchoWriter(s));
	}
	
	private static class RequestStreamEchoWriter implements StreamWriter {

		private StreamWriter responseWriter;

		public RequestStreamEchoWriter(StreamWriter responseWriter) {
			this.responseWriter = responseWriter;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			return responseWriter.processPiece(data);
		}
	}
}
