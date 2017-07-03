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

import com.nu.art.core.interfaces.ILogger;

import static com.nu.art.belog.consts.LogLevel.Debug;
import static com.nu.art.belog.consts.LogLevel.Error;
import static com.nu.art.belog.consts.LogLevel.Info;
import static com.nu.art.belog.consts.LogLevel.Verbose;
import static com.nu.art.belog.consts.LogLevel.Warning;

/**
 * Created by TacB0sS on 27-Feb 2017.
 */

public class Logger
		implements ILogger {

	private BeLogged beLogged;

	private String tag = getClass().getSimpleName();

	protected Logger() {
		beLogged = BeLogged.getInstance();
	}

	public final Logger setTag(String tag) {
		this.tag = tag;
		return this;
	}

	/*
	 * VERBOSE
	 */
	@Override
	public void logVerbose(String verbose) {
		beLogged.log(Verbose, tag, verbose, null);
	}

	@Override
	public void logVerbose(String verbose, Object... params) {
		beLogged.log(Verbose, tag, String.format(verbose, params), null);
	}

	@Override
	public void logVerbose(Throwable e) {
		beLogged.log(Verbose, tag, "", e);
	}

	@Override
	public void logVerbose(String verbose, Throwable e) {
		beLogged.log(Verbose, tag, verbose, e);
	}

	/*
	 * DEBUG
	 */
	@Override
	public void logDebug(String debug) {
		beLogged.log(Debug, tag, debug, null);
	}

	@Override
	public void logDebug(String debug, Object... params) {
		beLogged.log(Debug, tag, String.format(debug, params), null);
	}

	@Override
	public void logDebug(Throwable e) {
		beLogged.log(Debug, tag, "", e);
	}

	@Override
	public void logDebug(String debug, Throwable e) {
		beLogged.log(Debug, tag, debug, e);
	}

	/*
	 * INFO
	 */
	@Override
	public void logInfo(String info) {
		beLogged.log(Info, tag, info, null);
	}

	@Override
	public void logInfo(String info, Object... params) {
		beLogged.log(Info, tag, String.format(info, params), null);
	}

	@Override
	public void logInfo(Throwable e) {
		beLogged.log(Info, tag, "", e);
	}

	@Override
	public void logInfo(String info, Throwable e) {
		beLogged.log(Info, tag, info, e);
	}

	/*
	 * WARNING
	 */
	@Override
	public void logWarning(String warning) {
		beLogged.log(Warning, tag, warning, null);
	}

	@Override
	public void logWarning(String warning, Object... params) {
		beLogged.log(Warning, tag, String.format(warning, params), null);
	}

	@Override
	public void logWarning(Throwable e) {
		beLogged.log(Warning, tag, "", e);
	}

	@Override
	public void logWarning(String warning, Throwable e) {
		beLogged.log(Warning, tag, warning, e);
	}

	/*
	 * ERROR
	 */
	@Override
	public void logError(String error) {
		beLogged.log(Error, tag, error, null);
	}

	@Override
	public void logError(String error, Object... params) {
		beLogged.log(Error, tag, String.format(error, params), null);
	}

	@Override
	public void logError(String error, Throwable e) {
		beLogged.log(Error, tag, error, e);
	}

	@Override
	public void logError(Throwable e) {
		beLogged.log(Error, tag, "", e);
	}
}
