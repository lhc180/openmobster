/**
 * Copyright (c) {2003,2011} {openmobster@gmail.com} {individual contributors as indicated by the @authors tag}.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openmobster.core.synchronizer.server.workflow;

import org.apache.log4j.Logger;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;


import org.openmobster.core.synchronizer.model.SyncCommand;
import org.openmobster.core.synchronizer.model.SyncMessage;
import org.openmobster.core.synchronizer.server.Session;
import org.openmobster.core.synchronizer.server.SyncXMLGenerator;
import org.openmobster.core.synchronizer.server.SyncServer;

/**
 * @author openmobster@gmail.com
 */
public class EnterNormalSync implements ActionHandler 
{
	/**
	 * 
	 */
	private static Logger log = Logger.getLogger(EnterNormalSync.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5637962645508771892L;

	/**
	 * 
	 */
	public void execute(ExecutionContext context) throws Exception 
	{
		Session session = Utilities.getSession(context);
		SyncXMLGenerator syncXMLGenerator = Utilities.getSyncXMLGenerator(context);
		
		int cmdId = 1; //id for first command in this message...keep progressing
		//this as new commands are added to this message
		SyncMessage reply = Utilities.setUpReply(context);
				
		//Send status on successfull client modifications synced up with the server
		/**
		 * Consume the data changes by passing to the synchronization
		 * engine
		 */
		Utilities.processSyncCommands(context, cmdId, reply);	
		
		SyncCommand syncCommand = null;
		if(session.getSyncType().equals(SyncServer.TWO_WAY) || 
		   session.getSyncType().equals(SyncServer.SLOW_SYNC) ||
		   session.getSyncType().equals(SyncServer.ONE_WAY_SERVER)
		)
		{
			//Send back sync commands
			/**
			 * get this information by performing synchronization with engine
			 */
			syncCommand = Utilities.generateSyncCommand(context,cmdId,reply);
		}
				
				
		//Check to see if this is the last message of this phase from server end
		/**
		 * handle MoreData/non-final usecase based on the message size that 
		 * can be accepted by the client or some other criteria
		 */
		Utilities.setUpSyncFinal(context, reply, syncCommand);
		
		session.getServerSyncPackage().addMessage(reply);
		Utilities.preparePayload(context,
		syncXMLGenerator.generateSyncMessage(session, reply));
	}
}
