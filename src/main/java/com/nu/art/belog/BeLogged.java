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

	private LogLevel minLogLevel;
	private LogLevel maxLogLevel;

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

	final void log(final LogLevel level, final String tag, final String message, final Object[] params, final Throwable t) {
		if (!(level.ordinal() >= minLogLevel.ordinal() && level.ordinal() <= maxLogLevel.ordinal()))
			return;

		final String thread = Thread.currentThread().getName();
		String formattedMessage = params == null || params.length == 0 ? message : null;

		for (BeLoggedClient client : clients) {
			if (!client.loggableCondition.isLoggable(level, thread, tag, formattedMessage, t))
				continue;

			if (formattedMessage == null && message != null)
				formattedMessage = String.format(message, params);

			client._log(level, thread, tag, formattedMessage, t);
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
		this.minLogLevel = minLevel;
		this.maxLogLevel = maxLevel;

		for (BeLoggedClient client : clients) {
			client.setLogLevel(minLevel, maxLevel);
		}
	}
}
