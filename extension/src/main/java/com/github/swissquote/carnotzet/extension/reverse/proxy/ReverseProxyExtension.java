package com.github.swissquote.carnotzet.extension.reverse.proxy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.swissquote.carnotzet.core.Carnotzet;
import com.github.swissquote.carnotzet.core.CarnotzetExtension;
import com.github.swissquote.carnotzet.core.CarnotzetModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReverseProxyExtension implements CarnotzetExtension {

	private final String nginxImage;

	public ReverseProxyExtension() {
		this.nginxImage = "nginx";
	}

	@Override
	public List<CarnotzetModule> apply(Carnotzet carnotzet) {
		Path configDir = carnotzet.getResourcesFolder().resolve("reverse-proxy");
		Boolean success = configDir.toFile().mkdirs();
		if (!success) {
			throw new UncheckedIOException(new IOException("Could not create directory " + configDir));
		}
		Path configFile = configDir.resolve("nginx.conf");
		templateNginxConfig(carnotzet, configFile);

		Set<String> volumes = new HashSet<>();
		volumes.add(configFile.toAbsolutePath().toString() + ":/etc/nginx/nginx.conf:ro");

		CarnotzetModule reverseProxyModule = CarnotzetModule.builder()
				.name("reverse-proxy")
				.imageName(nginxImage)
				.dockerVolumes(volumes)
				.build();

		List<CarnotzetModule> modules = new ArrayList<>();
		modules.addAll(carnotzet.getModules());
		modules.add(reverseProxyModule);

		return modules;
	}

	private void templateNginxConfig(Carnotzet carnotzet, Path configFile) {
		StringBuilder sb = new StringBuilder();
		sb.append("events {\n");
		sb.append("    worker_connections 768;\n");
		sb.append("}\n");
		sb.append("http {\n");
		sb.append("server {\n");
		sb.append("    listen 80 default_server;\n");

		for (CarnotzetModule m : carnotzet.getModules()) {
			String contextPath = contextPathFor(m);
			String port = getPortFor(m);
			sb.append("    location " + contextPath + "/ {\n");
			sb.append("        proxy_pass http://" + m.getName() + ":" + port + contextPath + "/;\n");
			sb.append("        proxy_set_header X-Forwarded-Host $host:$server_port;\n");
			sb.append("        proxy_set_header X-Forwarded-Server $host;\n");
			sb.append("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
			sb.append("    }\n");
		}
		sb.append("}\n");
		sb.append("}\n");

		try {
			Files.write(configFile, sb.toString().getBytes(), StandardOpenOption.CREATE);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String getPortFor(CarnotzetModule m) {
		if (m.getProperties().containsKey("http.port")) {
			return m.getProperties().get("http.port");
		}
		return "80";
	}

	private String contextPathFor(CarnotzetModule m) {
		if (m.getProperties().containsKey("http.context.path")) {
			return m.getProperties().get("http.context.path");
		}
		return "/" + m.getName();
	}
}
