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

import com.nu.art.belog.consts.LogLevel;
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
	public synchronized String composeEntry(LogLevel level, String thread, String tag, String message, Throwable t) {
		date.setTime(System.currentTimeMillis());
		buffer.append(DefaultTimeFormat.format(date)).append(" ");
		buffer.append(level).append("/");
		buffer.append(thread).append("/");
		buffer.append(tag).append(": ");
		buffer.append(message).append("\n");
		if (t != null)
			buffer.append(ExceptionTools.getStackTrace(t)).append("\n");

		String toRet = buffer.toString();
		buffer.setLength(0);
		return toRet;
	}
}
