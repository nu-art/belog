/*
 * belog is an extendable infrastructure to manage and customize
 * your application output.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

import com.nu.art.belog.BeLogged.LogEntry;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.core.utils.InstanceRecycler;
import com.nu.art.core.utils.InstanceRecycler.Instantiator;
import com.nu.art.core.utils.PoolQueue;

public final class BeLogged
		implements Instantiator<LogEntry> {

	private static BeLogged INSTANCE;

	public static synchronized BeLogged getInstance() {
		if (INSTANCE == null)
			INSTANCE = new BeLogged();

		return INSTANCE;
	}

	private InstanceRecycler<LogEntry> recycler = new InstanceRecycler<>(this);

	private PoolQueue<LogEntry> runnableQueue = new PoolQueue<LogEntry>() {
		@Override
		protected void onExecutionError(LogEntry item, Throwable e) {
			System.err.print(item);
			e.printStackTrace();
		}

		@Override
		protected void executeAction(LogEntry logEntry)
				throws Exception {
			for (BeLoggedClient client : clients) {
				client._log(logEntry);
			}
			recycler.recycle(logEntry);
		}
	};

	private BeLogged() {
		runnableQueue.createThreads("BeLogged", 1);
	}

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
		runnableQueue.addItem(recycler.getInstance().set(level, thread, tag, message, t));
	}

	@Override
	public final LogEntry create() {
		return new LogEntry();
	}

	public final ILogger getLogger(Object objectForTag) {
		String tag;
		if (objectForTag instanceof String)
			tag = (String) objectForTag;
		else
			tag = objectForTag.getClass().getSimpleName();
		return new Logger().setTag(tag);
	}

	public class LogEntry {

		public long timestamp;

		public LogLevel level;

		public String thread;

		public String tag;

		public String message;

		public Throwable t;

		private LogEntry set(LogLevel level, String thread, String tag, String message, Throwable t) {
			this.timestamp = System.currentTimeMillis();
			this.level = level;
			this.thread = thread;
			this.tag = tag;
			this.message = message;
			this.t = t;
			return this;
		}
	}
}
