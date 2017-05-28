package com.github.swissquote.carnotzet.extension.reverse.proxy;

import static com.github.swissquote.carnotzet.core.maven.CarnotzetModuleCoordinates.fromPom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.swissquote.carnotzet.core.Carnotzet;
import com.github.swissquote.carnotzet.core.CarnotzetConfig;
import com.github.swissquote.carnotzet.core.runtime.log.LogEvents;
import com.github.swissquote.carnotzet.core.runtime.log.StdOutLogPrinter;
import com.github.swissquote.carnotzet.runtime.docker.compose.DockerComposeRuntime;

public class ReverseProxyExtensionTest {

	private static DockerComposeRuntime runtime;
	private static LogEvents logEvents = new LogEvents();

	@BeforeClass
	public static void setup() throws Throwable {
		CarnotzetConfig config = CarnotzetConfig.builder()
				.topLevelModuleId(fromPom(Paths.get("../examples/webapp-2/pom.xml")))
				.extensions(Collections.singletonList(new ReverseProxyExtension()))
				.build();
		Carnotzet carnotzet = new Carnotzet(config);
		runtime = new DockerComposeRuntime(carnotzet);
		runtime.registerLogListener(logEvents);
		runtime.registerLogListener(new StdOutLogPrinter());
		runtime.start();
		logEvents.waitForEntry("webapp-1", "Running", 5000, 50);
		logEvents.waitForEntry("webapp-2", "Running", 5000, 50);
	}

	@Test
	public void reverseProxyTest() throws IOException {
		String ip = runtime.getContainer("reverse-proxy").getIp();
		try {
			new URL("http://" + ip + "/webapp-1/fakepath").openConnection().getInputStream();
		}
		catch (FileNotFoundException e) {
			// expected
		}
		logEvents.waitForEntry("webapp-1", "GET /webapp-1/fakepath", 1000, 50);

		try {
			new URL("http://" + ip + "/webapp-2/fakepath").openConnection().getInputStream();
		}
		catch (FileNotFoundException e) {
			// expected
		}
		logEvents.waitForEntry("webapp-2", "GET /webapp-2/fakepath", 1000, 50);

	}

	@AfterClass
	public static void tearDown() {
		runtime.stop();
		runtime.clean();
	}

}
