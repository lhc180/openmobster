/**
 * Copyright (c) {2003,2011} {openmobster@gmail.com} {individual contributors as indicated by the @authors tag}.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openmobster.core.console.server;

import junit.framework.TestCase;

import org.openmobster.core.common.ServiceManager;

import org.openmobster.cloudConnector.api.Configuration;
import org.openmobster.cloudConnector.api.SecurityConfig;
import org.openmobster.cloudConnector.api.service.MobileService;
import org.openmobster.cloudConnector.api.service.Request;
import org.openmobster.cloudConnector.api.service.Response;

import org.openmobster.core.security.Provisioner;

/**
 * @author openmobster@gmail.com
 */
public class TestErrorLogCommand extends TestCase
{
	public void setUp() throws Exception
	{
		ServiceManager.bootstrap();
		
		Provisioner provisioner = Provisioner.getInstance();
		provisioner.registerIdentity("blah@gmail.com", "blahblah");
		provisioner.registerIdentity("blah2@gmail.com", "blahblah2");
		provisioner.registerDevice("blah@gmail.com", "blahblah", "console:localhost");
		
		Configuration configuration = Configuration.getInstance();
		configuration.setSecurityConfig((SecurityConfig)ServiceManager.locate("/cloudConnector/securityConfig"));
		configuration.setDeviceId("console:localhost");
		configuration.setAuthenticationHash(""); //empty
		configuration.setServerIp("localhost");
		configuration.setServerId("localhost");
		configuration.setSecureServerPort("1500");
		configuration.setPlainServerPort("1502");
		configuration.bootup();
		
		
		//activate the blah@gmail.com account
		Request request = new Request("/console/activateCommand");
		request.setAttribute("username", "blah@gmail.com");
		new MobileService().invoke(request);
	}
	
	public void tearDown() throws Exception
	{
		ServiceManager.shutdown();
	}
	
	public void testErrorLogDump() throws Exception
	{
		Request request = new Request("/console/errorlog");
		request.setAttribute("user.id", "blah@gmail.com");
		request.setAttribute("error.log", "BlahBlah!!NullPointerException...");
		request.setAttribute("device.platform", "android");
		
		MobileService service = new MobileService();
		Response response = service.invoke(request);
		
		this.assertNotNull(response);
		this.assertEquals(response.getStatusCode(), "204");
	}
}
