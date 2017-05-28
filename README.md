[![Build Status](https://travis-ci.org/swissquote/carnotzet-extension-reverse-proxy.svg?branch=master)](https://travis-ci.org/swissquote/carnotzet-extension-reverse-proxy)

#Automatically add and configure a reverse proxy in your carnotzet environments

Adds an nginx reverse proxy container in your environment, with automatically generated configuration 
to forward http requests to all other webapps in the environment

This allows you to avoid cross-domain limitations in browsers.

##Usage
```
<dependency>
	<groupId>com.github.swissquote</groupId>
	<artifactId>carnotzet-extension-reverse-proxy</artifactId>
	<version>${carnotzet-extension-reverse-proxy.version}</version>
</dependency>
```

###Java
register a ReverseProxyExtension object in your CarnotzetConfig ([Carnotzet documentation](https://swissquote.github.io/carnotzet/user-guide/java-api) for details)

###Maven
```
<plugin>
	<groupId>com.github.swissquote</groupId>
	<artifactId>zet-maven-plugin</artifactId>
	<version>${carnotzet.version}</version>
	<configuration>
		<extensions>
			<extension>
				<factoryClass>com.github.swissquote.carnotzet.extension.reverse.proxy.ReverseProxyExtensionFactory</factoryClass>
			</extension>
		</extensions>
	</configuration>
	<dependencies>
		<dependency>
			<groupId>com.github.swissquote</groupId>
			<artifactId>carnotzet-extension-reverse-proxy</artifactId>
			<version>${carnotzet-extension-reverse-proxy.version}</version>
		</dependency>
	</dependencies>
</plugin>
```

##Configuration

###Docker image
By default, the latest nginx official docker image will be used as reverse proxy.
You can override this by using the appropriate constructor when building your ReverseProxyExtension.
When configuring through the maven plugin, you may use the nginx.image property.

###Context path
By default, the module name is used as contextPath.
you can override the contextPath of each webapp in their respective carnotzet.properties file.
```
http.context.path=/custom-context
```
###Port
By default, the reverse proxy forwards requests to port 80 of each container.
 you can override the contextPath of each webapp in their respective carnotzet.properties file.
 ```
 http.port=8080
 ```