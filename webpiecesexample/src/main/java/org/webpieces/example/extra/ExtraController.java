package org.webpieces.example.extra;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import org.webpieces.WebpiecesExampleRouteId;

@Singleton
public class ExtraController {

	public Action relativeController() {
		return Actions.redirect(WebpiecesExampleRouteId.ANOTHER);
	}
}
