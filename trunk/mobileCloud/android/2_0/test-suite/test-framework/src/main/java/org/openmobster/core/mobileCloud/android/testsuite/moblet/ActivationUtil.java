/**
 * Copyright (c) {2003,2009} {openmobster@gmail.com} {individual contributors as indicated by the @authors tag}.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openmobster.core.mobileCloud.android.testsuite.moblet;

import android.content.Context;

import org.openmobster.core.mobileCloud.api.service.Response;
import org.openmobster.core.mobileCloud.api.service.MobileService;
import org.openmobster.core.mobileCloud.api.service.Request;
import org.openmobster.core.mobileCloud.api.service.ServiceInvocationException;
import org.openmobster.core.mobileCloud.android.service.Registry;
import org.openmobster.core.mobileCloud.android.configuration.Configuration;
import org.openmobster.core.mobileCloud.android.module.bus.Bus;
import org.openmobster.core.mobileCloud.android.module.bus.Invocation;


/**
 * @author openmobster@gmail
 *
 */
public final class ActivationUtil
{
	public static String cloudServerIp = "192.168.1.107"; //Modify Allowed: This should be the IP address of your server used for running the testsuite
	
	
	public static String deviceIdentifier = "IMEI:8675309"; //Do Not Modify
	public static String email = "blah2@gmail.com"; //Do Not Modify
	public static String password = "blahblah2"; //Do Not Modify
	
	public static void activateDevice() throws Exception
	{
		bootup(deviceIdentifier, cloudServerIp, null);
		
		
		Request request = new Request("provisioning");
		request.setAttribute("email", email);
		request.setAttribute("password", password);			
		request.setAttribute("identifier", deviceIdentifier);
		
		Response response = MobileService.invoke(request);
					
		if(response.getAttribute("idm-error") == null)
		{
			//Success Scenario
			processProvisioningSuccess(email, response);
						
			Invocation invocation = new Invocation("org.openmobster.core.mobileCloud.invocation.StartCometDaemon");
			Bus.getInstance().invokeService(invocation);
		}
		else
		{
			//Error Scenario
			String errorKey = response.getAttribute("idm-error");									
			throw new RuntimeException(errorKey);
		}
	}
	
	private static void processProvisioningSuccess(String email,Response response)
	{	
		Context context = Registry.getActiveInstance().getContext();
		Configuration configuration = Configuration.getInstance(context);
		
		String authenticationHash = response.getAttribute("authenticationHash");
		configuration.setEmail(email);
		configuration.setAuthenticationHash(authenticationHash);
		configuration.setActive(true);
		
		configuration.save(context);
	}	
	
	private static synchronized void bootup(String deviceIdentifier,String serverIp, String port) 
	throws ServiceInvocationException
	{
		Context context = Registry.getActiveInstance().getContext();
		Configuration conf = Configuration.getInstance(context);
		
		conf.deActivateSSL();
		
		conf.setDeviceId(deviceIdentifier);
		conf.setServerIp(serverIp);
		if(port != null && port.trim().length()>0)
		{
			conf.setPlainServerPort(port);
		}
		conf.setActive(false);
		
		conf.save(context);
		
		Request request = new Request("provisioning");
		request.setAttribute("action", "metadata");
		Response response = MobileService.invoke(request);
		
		//Read the Server Response
		String serverId = response.getAttribute("serverId");
		String plainServerPort = response.getAttribute("plainServerPort");
		String secureServerPort = response.getAttribute("secureServerPort");
		String isSSlActive = response.getAttribute("isSSLActive");
		String maxPacketSize = response.getAttribute("maxPacketSize");
		String httpPort = response.getAttribute("httpPort");
		
		//Setup the configuration
		conf.setServerId(serverId);
		conf.setPlainServerPort(plainServerPort);
		if(secureServerPort != null && secureServerPort.trim().length()>0)
		{
			conf.setSecureServerPort(secureServerPort);
		}
		
		if(isSSlActive.equalsIgnoreCase("true"))
		{
			conf.activateSSL();
		}
		else
		{
			conf.deActivateSSL();
		}
		
		conf.setMaxPacketSize(Integer.parseInt(maxPacketSize));
		conf.setHttpPort(httpPort);
				
		conf.save(context);
	}
}