package org.webpieces;
import java.io.IOException;

import org.junit.Test;
import org.webpieces.webserver.test.Asserts;

import org.webpieces.ServerConfig;
import org.webpieces.WebpiecesExampleServer;

public class TestLesson5RouteValidation {

	//This test you should always keep to run during the gradle build.  It can only be run after the
	//gradle plugin html template compiler is run as it uses a file that is generated to validate all the
	//routeIds in the html files.  
	@Test
	public void testBasicProdStartup() throws InterruptedException, IOException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		String property = System.getProperty("gradle.running");
		if(property == null || !"true".equals(property))
			return; //don't run test except in gradle build
		
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setHttpPort(0); //bind to any port
		serverConfig.setHttpsPort(0); //bind to any port
		serverConfig.setValidateRouteIdsOnStartup(true);
		//really just making sure we don't throw an exception...which catches quite a few mistakes
		WebpiecesExampleServer server = new WebpiecesExampleServer(null, null, serverConfig);
		
		//Start server to force validation
		server.start();
	}
}
