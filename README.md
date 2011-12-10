# CWTRPC - Controlled Web Toolkit RPC #

## Introduction ##
CWTRPC provides a controller component for GWT based remote services (GWT 
RPC), that can be configured in a Spring XML application context configuration 
file. It also provides an optional integration of GWT based remote services 
(GWT RPC) into Spring Security.

This version of CWTRPC provides the following features:

*	Controller component for GWT based remote services
*	Configuration of controller component with custom XML structure
*	Support of request and session scoped remote services
*	Flexible RPC token integration
*	Support to invoke login and logout on Spring Security with GWT based 
	remote services (integration into Spring Security)
*	Configurable HTTP cache control

The following example demonstrates how easy it is to provide services, that 
are configured as beans in a Spring XML application context, as a GWT based 
remote services. 

	@RemoteServiceRelativePath("hello.service")
	public interface HelloService1 extends RemoteService {
		public String hello(String name);
	}

	public class HelloService1Impl implements TestService1 {
		...
	}

	<beans xmlns="http://www.springframework.org/schema/beans"
		xmlns:controller="http://schema.itsvs.de/cwtrpc/controller"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="
			http://www.springframework.org/schema/beans 
			http://www.springframework.org/schema/beans/spring-beans-3.0.xsd 
			http://schema.itsvs.de/cwtrpc/controller 
			http://schema.itsvs.de/cwtrpc/cwtrpc-controller-0.9.xsd">
		...
		<bean id="helloService" class="demo.HelloService1Impl" />
		...
		<controller:controller>
			<controller:module module-name="sample">
				<controller:autowired-service-group base-packages="demo">
					<controller:description>
						Registers all remote services that are included in
						package demo and are defined as bean in the 
						application context. 
					</controller:description>
				</controller:autowired-service-group>
			</controller:module>
		</controller:controller>
		...
	</beans>
	...
	<web-app>
	...
		<servlet>
			<servlet-name>Remote Service Controller Servlet</servlet-name>
			<servlet-class>
				de.itsvs.cwtrpc.controller.RemoteServiceControllerServlet
			</servlet-class>
			<load-on-startup>1</load-on-startup>
		</servlet>
		<servlet-mapping>
			<servlet-name>Remote Service Controller Servlet</servlet-name>
			<url-pattern>*.service</url-pattern>
		</servlet-mapping>
	...
	</web-app>

Note: If you would like to run your application with the GWT web application 
server, which is also used by the GWT plugin for Eclipse, and you do not want 
to put the libraries into WEB-INF/lib folder (for example, Maven dependencies 
that are included in classpath), you must follow the class loading 
instructions in the 
[Wiki](https://github.com/itsvs/cwtrpc/wiki/Resolve:-Unable-to-locate-Spring-NamespaceHandler)
of this project. Otherwise you may get "Unable to locate Spring 
NamespaceHandler" error messages when starting your application. If you use
other web application servers like Apache Tomcat, you will not have this 
issue.

The project includes a sample that demonstrates the controller component and
the integration into Spring Security. It runs inside Eclipse IDE as GWT Web 
Application. A detailed description how to setup Eclipse (required plugins)
and how to checkout this sample can be found in the 
[Wiki](https://github.com/itsvs/cwtrpc/wiki/Setup-of-Eclipse-to-checkout-and-run-Samples)
of this project.

## Download ##
The latest stable version is distributed as atrifacts stored in a Maven 
repository. In order to use the controller component and the integration into
Spring Security you must add the following configuration to the POM of your
project.

    <repositories>
    	...
        <repository>
            <releases>
                <enabled>true</enabled>
                <updatePolicy>never</updatePolicy>
                <checksumPolicy>fail</checksumPolicy>
            </releases>
            <id>maven-repository.itsvs.de</id>
            <name>IT Services VS GmbH - Public Repository</name>
            <url>http://maven-repository.itsvs.de/</url>
            <layout>default</layout>
        </repository>
        ...
    </repositories>
    ...
    <dependencies>
    	...
        <dependency>
            <groupId>de.itsvs</groupId>
            <artifactId>cwtrpc-controller</artifactId>
            <version>0.9.0</version>
        </dependency>
        <dependency>
            <groupId>de.itsvs</groupId>
            <artifactId>cwtrpc-security</artifactId>
            <version>0.9.0</version>
        </dependency>
        ...
    </dependencies>

## License ##
Copyright 2011 [IT Services VS GmbH][]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[IT Services VS GmbH]: http://www.itsvs.de/
