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
import com.nu.art.belog.interfaces.LogComposer;

public abstract class BeLoggedClient {

	private LogComposer composer = new DefaultLogComposer();

	private LogLevel minLogLevel = LogLevel.Verbose;

	private LogLevel maxLogLevel = LogLevel.Assert;

	public final void setComposer(LogComposer composer) {
		this.composer = composer;
	}

	protected void init() { }

	public final void setLogLevel(LogLevel minLogLevel, LogLevel maxLogLevel) {
		this.minLogLevel = minLogLevel;
		this.maxLogLevel = maxLogLevel;
	}

	final void _log(LogEntry logEntry) {
		if (!isLoggable(logEntry.level))
			return;

		log(logEntry, composer.composeEntry(logEntry));
	}

	protected abstract void log(LogEntry entry, String logEntry);

	protected boolean isLoggable(LogLevel level) {
		return level.ordinal() >= minLogLevel.ordinal() && level.ordinal() <= maxLogLevel.ordinal();
	}

	protected void dispose() {}
}
