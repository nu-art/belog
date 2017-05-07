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
package com.nu.art.belogged;

import com.nu.art.belogged.BeLogged.LogEntry;
import com.nu.art.belogged.consts.LogLevel;
import com.nu.art.software.core.interfaces.ILogger;
import com.nu.art.software.core.tools.ArrayTools;
import com.nu.art.software.core.utils.InstanceRecycler;
import com.nu.art.software.core.utils.InstanceRecycler.Instantiator;
import com.nu.art.software.core.utils.PoolQueue;
import com.nu.art.software.modular.core.Module;

public final class BeLogged
		extends Module
		implements Instantiator<LogEntry> {

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

	private BeLogged() {}

	private BeLoggedClient[] clients = {};

	@Override
	protected void init() {
		runnableQueue.createThreads("BeLogged", 1);
	}

	public final void addClient(BeLoggedClient logClient) {
		clients = ArrayTools.appendElement(clients, logClient);
	}

	public final void removeClient(BeLoggedClient logClient) {
		clients = ArrayTools.removeElement(clients, logClient);
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
		return new Logger().setBeLogged(this).setTag(tag);
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
