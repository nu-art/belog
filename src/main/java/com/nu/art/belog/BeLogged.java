/*
 * belog is an extendable infrastructure to manage and customize
 * your application output.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nu.art.belog;

import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.tools.ArrayTools;

public final class BeLogged {

	private static BeLogged INSTANCE;

	public static synchronized BeLogged getInstance() {
		if (INSTANCE == null)
			INSTANCE = new BeLogged();

		return INSTANCE;
	}

	private BeLogged() {}

	private BeLoggedClient[] clients = {};

	public final void addClient(BeLoggedClient logClient) {
		clients = ArrayTools.appendElement(clients, logClient);
		logClient.init();
	}

	public final void removeClient(BeLoggedClient logClient) {
		clients = ArrayTools.removeElement(clients, logClient);
		logClient.dispose();
	}

	final void log(final LogLevel level, final String tag, final String message, final Throwable t) {
		final String thread = Thread.currentThread().getName();
		for (BeLoggedClient client : clients) {
			client._log(level, thread, tag, message, t);
		}
	}

	public final Logger getLogger(Object objectForTag) {
		String tag;
		if (objectForTag instanceof String)
			tag = (String) objectForTag;
		else
			tag = objectForTag.getClass().getSimpleName();
		return new Logger().setTag(tag);
	}

	public BeLoggedClient[] getClients() {
		return clients;
	}

	public void setLogLevel(final LogLevel minLevel, final LogLevel maxLevel) {
		for (BeLoggedClient client : clients) {
			client.setLogLevel(minLevel, maxLevel);
		}
	}
}
