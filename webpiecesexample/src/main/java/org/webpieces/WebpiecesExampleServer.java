package org.webpieces;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.security.SecretKeyInfo;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import org.webpieces.example.WebpiecesExampleRouteModule;
import org.webpieces.example.tags.TagLookupOverride;

/**
 * Except for changing WebpiecesExampleMeta inner class, changes to this one bootstrap class
 * and any classes you refer to here WILL require a restart when you are running the 
 * DevelopmentServer.  This class should try to remain pretty thin.
 * 
 * @author dhiller
 *
 */
public class WebpiecesExampleServer {
	
	//This is where the list of Guice Modules go as well as the list of RouterModules which is the
	//core of anything you want to plugin to your web app.  To make re-usable components, you create
	//GuiceModule paired with a RouterModule and app developers can plug both in here.  In some cases,
	//only a RouterModule is needed and in others only a GuiceModule is needed.
	//BIG NOTE: The webserver loads this class from the appmeta.txt file which is passed in the
	//start method below.  This is a hook for the Development server to work that is a necessary evil
	public static class WebpiecesExampleMeta implements WebAppMeta {

		//When using the Development Server, changes to this inner class will be recompiled automatically
		//when needed..  Changes to the outer class will not take effect until a restart
		//In production, we don't have a compiler on the classpath nor any funny classloaders so that
		//production is very very clean and the code for this non-dev server is very easy to step through
		//if you have a production issue
		@Override
        public List<Module> getGuiceModules() {
			return Lists.newArrayList(new WebpiecesExampleGuiceModule());
		}
		
		@Override
        public List<RouteModule> getRouteModules() {
			return Lists.newArrayList(new WebpiecesExampleRouteModule());
		}

		//ALL APPLICATION plugins are added to classpath as a jar and then depending on the plugin has a RouteModule
		//and/or a GuiceModule depending on the plugin that you configure and add to your list of modules
		//in this inner class.  Then there are platform plugins which are plugged in using the main methods below
	}
	
	
	/*******************************************************************************
	 * When running the dev server, changes below this line require a server restart(you can try not to but it won't work)
	 * Changes above this line in the WebpiecesExampleMeta inner class WILL work....those changes will be recompiled and
	 * loaded
	 *******************************************************************************/
	
	private static final Logger log = LoggerFactory.getLogger(WebpiecesExampleServer.class);
	
	public static final Charset ALL_FILE_ENCODINGS = StandardCharsets.UTF_8;
	
	//Welcome to YOUR main method as webpieces webserver is just a library you use that you can
	//swap literally any piece of
	public static void main(String[] args) throws InterruptedException {
		WebpiecesExampleServer server = new WebpiecesExampleServer(null, null, new ServerConfig());
		
		server.start();
		
		synchronized (WebpiecesExampleServer.class) {
			//wait forever so server doesn't shut down..
			WebpiecesExampleServer.class.wait();
		}	
	}

	private WebServer webServer;

	public WebpiecesExampleServer(Module platformOverrides, Module appOverrides, ServerConfig svrConfig) {
		String filePath = System.getProperty("user.dir");
		log.info("original user.dir before modification="+filePath);

		modifyUserDirForManyEnvironments(filePath);
				
		VirtualFile metaFile = svrConfig.getMetaFile();
		//Dev server has to override this
		if(metaFile == null)
			metaFile = new VirtualFileClasspath("appmeta.txt", WebpiecesExampleServer.class.getClassLoader());

		//This override is only needed if you want to add your own Html Tags to re-use
		//you can delete this code if you are not adding your own html tags
		Module allOverrides = new TagLookupOverride();
		if(platformOverrides != null) {
			allOverrides = Modules.combine(platformOverrides, allOverrides);
		}
		
		SecretKeyInfo signingKey = new SecretKeyInfo(fetchKey(), "HmacSHA1");
		
		//Different pieces of the server have different configuration objects where settings are set
		//You could move these to property files but definitely put some thought if you want people 
		//randomly changing those properties and restarting the server without going through some testing
		//by a QA team.  We leave most of these properties right here.
		RouterConfig routerConfig = new RouterConfig()
											.setMetaFile(metaFile)
											.setWebappOverrides(appOverrides)
											.setSecretKey(signingKey);
		WebServerConfig config = new WebServerConfig()
										.setPlatformOverrides(allOverrides)
										.setHttpListenAddress(new InetSocketAddress(svrConfig.getHttpPort()))
										.setHttpsListenAddress(new InetSocketAddress(svrConfig.getHttpsPort()))
										.setSslEngineFactory(new WebpiecesExampleSSLFactory())
										.setFunctionToConfigureServerSocket(s -> configure(s))
										.setValidateRouteIdsOnStartup(svrConfig.isValidateRouteIdsOnStartup())
										.setStaticFileCacheTimeSeconds(svrConfig.getStaticFileCacheTimeSeconds());
		TemplateConfig templateConfig = new TemplateConfig();
		
		webServer = WebServerFactory.create(config, routerConfig, templateConfig);
	}

	private byte[] fetchKey() {
		//This is purely so it works before template creation
		//NOTE: our build runs all template tests that are generated to make sure we don't break template 
		//generation but for that to work pre-generation, we need this code but you are free to delete it...
		String base64Key = "95ml/qxHIcZsH0qEDiA5YeG8OFr/AJL/8uwJv0sfQiY1KNygsqYZIX1EOy7yBchZjfdU/NuB1NXLxFFqCcKmLQ==";  //This gets replaced with a unique key each generated project which you need to keep or replace with your own!!!
		if(base64Key.startsWith("__SECRETKEY"))  //This does not get replaced (user can remove it from template)
			return base64Key.getBytes();
		return Base64.getDecoder().decode(base64Key);
	}

	private void modifyUserDirForManyEnvironments(String filePath) {
		String finalUserDir = modifyUserDirForManyEnvironmentsImpl(filePath);
		System.setProperty("user.dir", finalUserDir);
		log.info("RECONFIGURED user.dir="+finalUserDir);
	}

	/**
	 * I like things to work seamlessly but user.dir is a huge issue in multiple environments I am working in.
	 * main server has to work in N configurations that should be tested and intellij is a PITA since
	 * it is inconsistent.  PP means runs in the project the file is in and eclipse is consistent with
	 * gradle while intellij is only half the time....
	 * 
	 * app dev - The environment when you generate a project and import it into an IDE
	 * webpieces - The environment where you test the template directly when bringing in webpieces into an IDE
	 * 
	 * 
	 * * app dev / eclipse -
	 *    * PP - running myapp/src/tests - user.dir=myapp-all/myapp
	 *    * PP - DevServer - user.dir=myapp-all/myapp-dev
	 *    * PP - SemiProductionServer - user.dir=myapp-all/myapp-dev
	 *    * PP - ProdServer - user.dir=myapp-all/myapp
	 * * app dev / intellij (it's different paths than eclipse :( ).  user.dir starts as myapp directory
	 *    * PP - running myapp/src/tests - myapp-all/myapp
	 *    * NO - DevServer - user.dir=myapp-all :( what the hell!  different from running tests
	 *    * NO - SemiProductionServer - user.dir=myapp-all
	 *    * NO - ProdServer - user.dir=myapp-all
	 * * webpieces / eclipse - same as app dev because eclipse is nice in this aspect
	 * * webpieces / intellij - ANNOYING and completely different.  Runs out of webpieces a few levels down from actual subproject
	 * * PP - tests in webpieces gradle - myapp-all/myapp
	 * * PP - tests in myapp's gradle run - myapp-all/myapp
	 * * NO - production - user.dir=from distribution myapp directory which has subdirs bin, lib, config, public
	 * * Future? - run DevSvr,SemiProdSvr,ProdSvr from gradle?....screw that for now..it's easy to run from IDE so why bother(it may just work though too)
	 * 
	 * - so in production, the relative paths work from myapp so 'public/' is a valid location for html files resolving to myapp/public
	 * - in testing, IF we want myapp-all/myapp/src/dist/public involved, it would be best to run from myapp-all/myapp/src/dist so 'public/' is still a valid location
	 * - in devserver, semiprodserver, and prod server, the same idea follows where myapp-all/myapp/src/dist should be the user.dir!!!
	 * 
	 * - sooooo, algorithm is this
	 * - if user.dir=myapp-all, modify user.dir to myapp-all/myapp/src/dist (you are in intellij)
	 * - else if user.dir=myapp-dev, modify to ../myapp/src/dist
	 * - else if myapp has directories bin, lib, config, public then do nothing
	 * - else modify user.dir=myapp to myapp/src/dist
	 */
	private String modifyUserDirForManyEnvironmentsImpl(String filePath) {
		File f = new File(filePath);
		String name = f.getName();
		if("webpiecesexample-all".equals(name)) {
			return new File(filePath, "webpiecesexample/src/dist").getAbsolutePath();
		} else if("webpiecesexample-dev".equals(name)) {
			File parent = f.getParentFile();
			return new File(parent, "webpiecesexample/src/dist").getAbsolutePath();
		} else if(!"webpiecesexample".equals(name)) {
			if(filePath.endsWith("webpiecesexample/src/dist"))
				return filePath; //This occurs when a previous test ran already and set user.dir
			else if(filePath.endsWith("webpieces")) //
				return filePath+"/webserver/webpiecesServerBuilder/templateProject/webpiecesexample/src/dist";
			throw new IllegalStateException("bug, we must have missed an environment="+name);
		}
		
		File bin = new File(f, "bin");
		File lib = new File(f, "lib");
		File config = new File(f, "config");
		File publicFile = new File(f, "public");
		if(bin.exists() && lib.exists() && config.exists() && publicFile.exists()) {
			return filePath;
		}
		
		return new File(f, "src/dist").getAbsolutePath();
	}

	/**
	 * This is a bit clunky BUT if jdk authors add methods that you can configure, we do not have
	 * to change our platform every time so you can easily set the new properties rather than waiting for
	 * us to release a new version 
	 */
	public void configure(ServerSocketChannel channel) throws SocketException {
		channel.socket().setReuseAddress(true);
		//channel.socket().setSoTimeout(timeout);
		//channel.socket().setReceiveBufferSize(size);
	}
	
	public RequestListener start() {
		return webServer.start();	
	}

	public void stop() {
		webServer.stop();
	}

	public TCPServerChannel getUnderlyingHttpChannel() {
		return webServer.getUnderlyingHttpChannel();
	}

	public TCPServerChannel getUnderlyingHttpsChannel() {
		return webServer.getUnderlyingHttpsChannel();
	}
}
