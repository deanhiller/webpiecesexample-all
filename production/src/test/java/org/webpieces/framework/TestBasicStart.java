package org.webpieces.framework;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.util.futures.Logging;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.Asserts;

import org.webpieces.webserver.test.EnvSimModule;
import org.webpieces.Server;
import org.webpieces.mock.JavaCache;

public class TestBasicStart {

	static {
		Logging.setupMDCForLogging();
	}

	private JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
	private String[] args = {
			"-http.port=:0",
			"-https.port=:0",
			"-hibernate.persistenceunit=org.webpieces.db.DbSettingsInMemory",
			"-hibernate.loadclassmeta=true"
	};

	private Map<String, String> simulatedEnv = Map.of(
			//use a different in-memory db each test class so we can be multi-threaded
			"DB_URL","jdbc:log4jdbc:h2:mem:"+getClass().getSimpleName(),
			"DB_USER", "sa",
			"DB_PASSWORD", ""
	);

	//This exercises full startup with no mocking in place whatsoever BUT as you add remote systems to 
	//talk to, you will need to change this test and pass in appOverridesModule to override those 
	//pieces.  In this test, we literally bind a port.  We only do this in one or two tests just to
	//ensure full server basic functionality is working.  All other tests create a server and pass
	//in http requests directly.  This test can use http client to send requests in which exercises
	//our http parser and other pieces (which sometimes can catch bugs when you upgrade webpieces
	// so in some cases, this can be valuable)
	@Test
	public void testBasicProdStartup() throws InterruptedException, IOException, ClassNotFoundException, ExecutionException, TimeoutException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		//clear in-memory database
		jdbc.dropAllTablesFromDatabase();
		
		//SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		
		//really just making sure we don't throw an exception...which catches quite a few mistakes
		Server server = new Server(new EnvSimModule(simulatedEnv), null, new ServerConfig(JavaCache.getCacheLocation()), args);
		//In this case, we bind a port
		server.start();

		System.out.println("bound port="+server.getUnderlyingHttpChannel().getLocalAddress());
		//we should depend on http client and send a request in to ensure operation here...
		
		server.stop();
		
		//ALSO, it is completely reasonable to create a brand new instance(ie. avoid statics and avoid
		// non-guice singletons).  A guice singleton is only a singleton within the scope of a server
		//while a java singleton....well, pretty much sucks.  Google "Singletons are evil".
		Server server2 = new Server(new EnvSimModule(simulatedEnv), null, new ServerConfig(JavaCache.getCacheLocation()), args);
		//In this case, we bind a port
		server2.start();
		System.out.println("bound port="+server.getUnderlyingHttpChannel().getLocalAddress());
		
		//we should depend on http client and send a request in to ensure operation here...
		
		server2.stop();
	}
}
