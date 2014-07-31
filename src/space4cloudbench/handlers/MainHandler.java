/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * 
 */
package space4cloudbench.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;

import space4cloudbench.main.Space4CloudBench;

// TODO: Auto-generated Javadoc
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MainHandler extends AbstractHandler {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MainHandler.class);
	/**
	 * The constructor.
	 */
	public MainHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 * 
	 * @param event
	 *            the event
	 * @return the object
	 * @throws ExecutionException
	 *             the execution exception
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		logger.info("Running space4cloud bench");
		Space4CloudBench bench = new Space4CloudBench();
		bench.execute();	
		return null;
	}
}
