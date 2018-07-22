package org.webpieces.base;

import java.util.List;
import java.util.Map;

import org.webpieces.plugins.backend.BackendConfig;
import org.webpieces.plugins.backend.BackendPlugin;
import org.webpieces.plugins.hibernate.HibernateConfig;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.plugins.json.JacksonConfig;
import org.webpieces.plugins.json.JacksonPlugin;
import org.webpieces.plugins.sslcert.InstallSslCertConfig;
import org.webpieces.plugins.sslcert.InstallSslCertPlugin;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import org.webpieces.base.crud.CrudRoutes;
import org.webpieces.base.crud.ajax.AjaxCrudRoutes;
import org.webpieces.base.crud.login.LoginRoutes;
import org.webpieces.base.json.JsonCatchAllFilter;
import org.webpieces.base.json.JsonRoutes;

//This is where the list of Guice Modules go as well as the list of RouterModules which is the
//core of anything you want to plugin to your web app.  To make re-usable components, you create
//GuiceModule paired with a RouterModule and app developers can plug both in here.  In some cases,
//only a RouterModule is needed and in others only a GuiceModule is needed.
//BIG NOTE: The webserver loads this class from the appmeta.txt file which is passed in the
//start method below.  This is a hook for the Development server to work that is a necessary evil
public class WebpiecesExampleMeta implements WebAppMeta {

	private static final Logger log = LoggerFactory.getLogger(WebpiecesExampleMeta.class);
	public static final String BACKEND_PATH = "/backend1";
	private String persistenceUnit;

	@Override
	public void initialize(Map<String, String> props) {
		persistenceUnit = props.get(HibernatePlugin.PERSISTENCE_UNIT_KEY);
	}

	//When using the Development Server, changes to this inner class will be recompiled automatically
	//when needed..  Changes to the outer class will not take effect until a restart
	//In production, we don't have a compiler on the classpath nor any funny classloaders so that
	//production is very very clean and the code for this non-dev server is very easy to step through
	//if you have a production issue
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new GuiceModule());
	}

	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(
				new AppRoutes(),
				new LoginRoutes("/org/webpieces/base/crud/login/AppLoginController", "/secure/.*"),
				new CrudRoutes(),
				new AjaxCrudRoutes(),
				new JsonRoutes()
				);
	}

	//NOTE: EACH Plugin takes a Config object such that we can add properties later without breaking your compile
	//This allows us to be more backwards compatible
	
	@Override
	public List<Plugin> getPlugins() {
		log.info("classloader for meta="+this.getClass().getClassLoader());
		return Lists.newArrayList(
				//if you want to remove hibernate, just remove it first from the build file and then remove
				//all the compile error code(it will remove more than half of the jar size of the web app actually due
				//to transitive dependencies)
				new HibernatePlugin(new HibernateConfig(persistenceUnit)),
				new JacksonPlugin(new JacksonConfig("/json/.*", JsonCatchAllFilter.class)),
				new BackendPlugin(new BackendConfig()),
				new InstallSslCertPlugin(new InstallSslCertConfig("acme://letsencrypt.org/staging"))
				);
	}

}