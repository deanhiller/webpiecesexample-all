package org.webpieces;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.router.api.extensions.NeedsSimpleStorage;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.api.HttpSvrInstanceConfig;

public class ServerConfig {

	/**
	 * The bootstrap file that separates code that can't be recompiled from all the code
	 * that can in your DevelopmentServer.  This file references the Meta file needed to
	 * load your web application.  The default is the 'production' appmeta.txt meta file
	 * while the DevelopmentServer uses appmetadev.txt such that it can load a few more
	 * plugins.
	 */
	private VirtualFile metaFile= new VirtualFileClasspath("appmeta.txt", Server.class.getClassLoader());
	
	/**
	 * This is not used in production so we default it to false, but a special test that we
	 * already wrote for you sets this to true such that we validate all route ids in your
	 * html files on startup.  Go us!!!
	 */
	private boolean validateRouteIdsOnStartup = false;
	
	/**
	 * In issuing a redirect, it is important to send back to clients the correct port so
	 * instead of redirecting to /asdfsdf:12343 which the server is bound to, we redirect back
	 * to asdfsdf:80.  
	 * 
	 * This is due to if you send no port information in the redirect, some browsers would redirect
	 * back to port 80 which would be wrong when testing on localhost:8080.  TODO: test again in 
	 * modern day browsers to see if we can eliminate this code completely and just send a redirect 
	 * to the url and hope the browser knows which port it originally requested.  test in opera, firefox
	 * IE, safari and chrome
	 */
	private boolean isUseFirewall = false;
	
	/**
	 * This sets the cache-control: max-age={time} for production server.  The DevelopmentServer
	 * completely avoids caching.  Also, as long as you use the %%{ }%% tag, webpieces will change
	 * the url of the updated file for you.  ie. update, the file and the hash changes on all pages
	 * referencing that file.   This means you should ALWAYS cache at the max time which is 1 year
	 * since as you update your app, customers avoid the old cached version anyways.
	 */
	private Long staticFileCacheTimeSeconds = TimeUnit.SECONDS.convert(255, TimeUnit.DAYS);
	
	/**
	 * On startup, webpieces compresses all text/css/js resources such that when it sends it to
	 * a browser, it does not have to do compression but can just send the compressed file to the
	 * browser for better speed.
	 */
	private File compressionCacheDir;
	
	/**
	 * This is how properties can be passed to plugins if needed.  See the plugins documentation
	 * to know if you need to use this.
	 */
	private Map<String, String> webAppMetaProperties = new HashMap<>();
	
	/**
	 * For testing, it's easier to turn this off.  When rendering the webpieces #{form}# tag,
	 * webpieces sticks a special security token as one of the fields that it will verify on the
	 * POST request for higher security.  This is very annoying for testing and is turned off
	 * here by flipping this flag to false
	 */
	private boolean tokenCheckOn = true;
	
	/**
	 * Configuration for the Http Server.  See the HttpSvrInstanceConfig class documentation for more info.
	 */
	private HttpSvrInstanceConfig httpConfig = new HttpSvrInstanceConfig(new InetSocketAddress(8080), null);
	
	/**
	 * Configuration for the Https Server.  See the HttpSvrInstanceConfig class documentation for more info.
	 */
	private HttpSvrInstanceConfig httpsConfig = new HttpSvrInstanceConfig(new InetSocketAddress(8443), new WebSSLFactory());
	
	/**
	 * Configuration for the Backend Http or Https Server.  See the HttpSvrInstanceConfig class documentation for more info.
	 */
	private HttpSvrInstanceConfig backendSvrConfig = new HttpSvrInstanceConfig();	
	
	/**
	 * Because Guice creation happens 'after' you create some classes that need access to the SimpleStorage
	 * mechanism, you can provide those classes here and we will inject the Storage after it has been created.
	 * 
	 * Specifically, SimpleStorage is created when you start the server which is after you created a few objects
	 * like WebSSLFactory that may want access to that storage.
	 */
	private List<NeedsSimpleStorage> needsStorage = new ArrayList<NeedsSimpleStorage>();

	public ServerConfig(int httpPort, int httpsPort, SSLEngineFactory sslFactory, String persistenceUnit, File compressionCache) {
		webAppMetaProperties.put(HibernatePlugin.PERSISTENCE_UNIT_KEY, persistenceUnit);
		httpConfig = new HttpSvrInstanceConfig(new InetSocketAddress(httpPort), null);
		httpsConfig = new HttpSvrInstanceConfig(new InetSocketAddress(httpsPort), sslFactory);
		this.compressionCacheDir = compressionCache;
	}

	public ServerConfig(String persistenceUnit, File compressionCache) {
		//For tests, we need to bind to port 0, then lookup the port after that...
		this(0, 0, new WebSSLFactory(), persistenceUnit, compressionCache);
		tokenCheckOn = false;
	}

	public ServerConfig(SSLEngineFactory factory, String persistenceUnit) {
		this(8080, 8443, factory, persistenceUnit, FileFactory.newBaseFile("webpiecesCache/precompressedFiles"));
	}
	
	public VirtualFile getMetaFile() {
		return metaFile;
	}
	public void setMetaFile(VirtualFile metaFile) {
		this.metaFile = metaFile;
	}
	public boolean isValidateRouteIdsOnStartup() {
		return validateRouteIdsOnStartup;
	}
	public void setValidateRouteIdsOnStartup(boolean validateRouteIdsOnStartup) {
		this.validateRouteIdsOnStartup = validateRouteIdsOnStartup;
	}

	public Long getStaticFileCacheTimeSeconds() {
		return staticFileCacheTimeSeconds ;
	}

	public void setStaticFileCacheTimeSeconds(Long staticFileCacheTimeSeconds) {
		this.staticFileCacheTimeSeconds = staticFileCacheTimeSeconds;
	}

	public Map<String, String> getWebAppMetaProperties() {
		return webAppMetaProperties;
	}

	public File getCompressionCacheDir() {
		return compressionCacheDir;
	}

	public void setCompressionCacheDir(File compressionCacheDir) {
		this.compressionCacheDir = compressionCacheDir;
	}

	public boolean isTokenCheckOn() {
		return tokenCheckOn;
	}
	public ServerConfig setTokenCheckOn(boolean tokenCheckOff) {
		this.tokenCheckOn = tokenCheckOff;
		return this;
	}

	public boolean isUseFirewall() {
		return isUseFirewall;
	}

	public void setUseFirewall(boolean isUseFirewall) {
		this.isUseFirewall = isUseFirewall;
	}

	public HttpSvrInstanceConfig getHttpConfig() {
		return httpConfig;
	}

	public ServerConfig setHttpConfig(HttpSvrInstanceConfig httpConfig) {
		this.httpConfig = httpConfig;
		return this;
	}

	public HttpSvrInstanceConfig getHttpsConfig() {
		return httpsConfig;
	}

	public ServerConfig setHttpsConfig(HttpSvrInstanceConfig httpsConfig) {
		this.httpsConfig = httpsConfig;
		return this;
	}

	public HttpSvrInstanceConfig getBackendSvrConfig() {
		return backendSvrConfig;
	}

	public ServerConfig setBackendSvrConfig(HttpSvrInstanceConfig backendSvrConfig) {
		this.backendSvrConfig = backendSvrConfig;
		return this;
	}

	public ServerConfig addNeedsStorage(NeedsSimpleStorage needsStorage) {
		this.needsStorage.add(needsStorage);
		return this;
	}

	public List<NeedsSimpleStorage> getNeedsStorage() {
		return needsStorage;
	}
}
