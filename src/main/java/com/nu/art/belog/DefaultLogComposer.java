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
import com.nu.art.core.tools.ExceptionTools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TacB0sS on 27-Feb 2017.
 */
public class DefaultLogComposer
		implements com.nu.art.belog.interfaces.LogComposer {

	public final static SimpleDateFormat DefaultTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	private final StringBuffer buffer = new StringBuffer();

	private final Date date = new Date();

	@Override
	public synchronized String composeEntry(LogEntry logEntry) {
		date.setTime(logEntry.timestamp);
		buffer.append(DefaultTimeFormat.format(date)).append(" ");
		buffer.append(logEntry.level).append("/");
		buffer.append(logEntry.thread).append("/");
		buffer.append(logEntry.tag).append(": ");
		buffer.append(logEntry.message).append("\n");
		if (logEntry.t != null)
			buffer.append(ExceptionTools.getStackTrace(logEntry.t)).append("\n");

		String toRet = buffer.toString();
		buffer.setLength(0);
		return toRet;
	}
}
