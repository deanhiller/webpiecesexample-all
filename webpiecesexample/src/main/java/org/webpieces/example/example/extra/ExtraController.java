package org.webpieces.example.example.extra;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import org.webpieces.example.WebpiecesExampleRouteId;

@Singleton
public class ExtraController {

	public Action relativeController() {
		return Actions.redirect(WebpiecesExampleRouteId.ANOTHER);
	}
}
