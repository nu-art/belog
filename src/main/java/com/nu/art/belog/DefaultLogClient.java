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

/**
 * Created by TacB0sS on 28-Feb 2017.
 */

public class DefaultLogClient
		extends BeLoggedClient {

	@Override
	protected void log(LogLevel level, String thread, String tag, String message, Throwable t) {
		String s = composer.composeEntry(level, thread, tag, message, t);
		switch (level) {

			case Verbose:
			case Debug:
			case Info:
				System.out.println(s);
				break;
			case Warning:
			case Error:
			case Assert:
				System.err.println(s);
		}
	}
}
