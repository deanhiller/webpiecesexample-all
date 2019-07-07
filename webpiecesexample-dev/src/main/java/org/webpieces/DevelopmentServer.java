package org.webpieces;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class DevelopmentServer {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	//NOTE: This whole project brings in jars that the main project does not have and should never
	//have like the eclipse compiler(a classloading compiler jar), webpieces runtimecompile.jar
	//and finally the http-router-dev.jar which has the guice module that overrides certain core
	//webserver classes to put in a place a runtime compiler so we can compile your code as you
	//develop
	public static void main(String[] args) throws InterruptedException {
		try {
			String version = System.getProperty("java.version");
			log.info("Starting Development Server under java version="+version);

			new DevelopmentServer(false).start();
			
			//Since we typically use only 3rd party libraries with daemon threads, that means this
			//main thread is the ONLY non-daemon thread letting the server keep running so we need
			//to block it and hold it up from exiting.  Modify this to release if you want an ability
			//to remotely shutdown....
			synchronized(DevelopmentServer.class) {
				DevelopmentServer.class.wait();
			}
		} catch(Throwable e) {
			log.error("Failed to startup.  exiting jvm", e);
			System.exit(1); // should not be needed BUT some 3rd party libraries start non-daemon threads :(
		}
	}
	
	private Server server;

	public DevelopmentServer(boolean usePortZero) {
		
        VirtualFileImpl directory = ProdServerForIDE.modifyForIDE();
        
		//list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
		//These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
		List<VirtualFile> srcPaths = new ArrayList<>();
		srcPaths.add(directory.child("webpiecesexample/src/main/java"));
		srcPaths.add(directory.child("webpiecesexample-dev/src/main/java"));
		
		VirtualFile metaFile = directory.child("webpiecesexample/src/main/resources/appmetadev.txt");
		log.info("LOADING from meta file="+metaFile.getCanonicalPath());
		
		//html and json template file encoding...
		TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths)
														.setFileEncoding(Server.ALL_FILE_ENCODINGS);
		
		//java source files encoding...
		CompileConfig devConfig = new CompileConfig(srcPaths, CompileConfig.getTmpDir())
										.setFileEncoding(Server.ALL_FILE_ENCODINGS);
		Module platformOverrides = Modules.combine(
										new DevRouterModule(devConfig),
										new DevTemplateModule(templateConfig));
		
		WebSSLFactory sslFactory = new WebSSLFactory();
		
		ServerConfig config = new ServerConfig(sslFactory, HibernatePlugin.PERSISTENCE_TEST_UNIT);
		if(usePortZero) {
			config.getHttpConfig().setListenAddress(() -> new InetSocketAddress(0));
			config.getHttpsConfig().setListenAddress(() -> new InetSocketAddress(0));
		}

		//READ the documentation in HttpSvrInstanceConfig for more about these settings
//		HttpSvrInstanceConfig backendSvrConfig = new HttpSvrInstanceConfig(new InetSocketAddress(8444), sslFactory);
//		backendSvrConfig.setFunctionToConfigureServerSocket((s) -> Server.configure(s));
//		config.setBackendSvrConfig(backendSvrConfig );
		
		//It is very important to turn off BROWSER caching or developers will get very confused when they
		//change stuff and they don't see changes in the website
		config.setStaticFileCacheTimeSeconds(null);
		config.setMetaFile(metaFile);
		
		server = new Server(platformOverrides, null, config);
	}
	
	public void start() throws InterruptedException {
		server.start();		
	}

	public void stop() {
		server.stop();
	}
}
