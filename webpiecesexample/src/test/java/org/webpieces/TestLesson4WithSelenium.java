package org.webpieces;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.SeleniumOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.webpieces.ServerConfig;
import org.webpieces.WebpiecesExampleServer;

public class TestLesson4WithSelenium {
	
	private static WebDriver driver;
	
	//see below comments in AppOverrideModule
	//private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	
	private int port;

	@BeforeClass
	public static void staticSetup() {
		driver = new FirefoxDriver();
	}
	@AfterClass
	public static void tearDown() {
		driver.close();
		driver.quit();
	}
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WebpiecesExampleServer webserver = new WebpiecesExampleServer(new SeleniumOverridesForTest(), new AppOverridesModule(), new ServerConfig(0, 0));
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
	}
	

	
	//You must have firefox installed to run this test...
	@Ignore
	@Test
	public void testSomething() throws ClassNotFoundException {

		driver.get("http://localhost:"+port+"/examples");
		
		String pageSource = driver.getPageSource();
		
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("TagArgs from variable otherArgument=Dean Hiller"));
		
	}
	
	//You must have firefox installed to run this test...
	@Ignore
	@Test
	public void testChunking() throws ClassNotFoundException {

		driver.get("http://localhost:"+port+"");
		
		String pageSource = driver.getPageSource();
		
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("Webpieces Webserver is the most"));
		
	}	
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			//ie.
			//binder.bind(SomeRemoteSystem.class).toInstance(mockRemote); //see above comment on the field mockRemote
		}
	}
	
}
