package com.github.swissquote.carnotzet.extension.reverse.proxy;

import java.util.Properties;

import com.github.swissquote.carnotzet.core.CarnotzetExtension;
import com.github.swissquote.carnotzet.maven.plugin.spi.CarnotzetExtensionsFactory;

public class ReverseProxyExtensionFactory implements CarnotzetExtensionsFactory {

	@Override
	public CarnotzetExtension create(Properties configuration) {
		if (configuration.containsKey("nginx.image")){
			return new ReverseProxyExtension(configuration.getProperty("nginx.image"));
		}
		return new ReverseProxyExtension();
	}

}
