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

import com.nu.art.belog.BeConfig.LoggerConfig;
import com.nu.art.belog.BeConfig.Rule;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.belog.loggers.FileLogger;
import com.nu.art.belog.loggers.FileLogger.Config_FileLogger;
import com.nu.art.belog.loggers.FileLogger.FileLoggerValidator;
import com.nu.art.belog.loggers.JavaLogger.Config_JavaLogger;
import com.nu.art.belog.loggers.JavaLogger.JavaLoggerValidator;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BeLogged {

	private static BeLogged INSTANCE;

	public static synchronized BeLogged getInstance() {
		if (INSTANCE == null)
			INSTANCE = new BeLogged();

		return INSTANCE;
	}

	private LogLevel minLogLevel = LogLevel.Verbose;
	private LogLevel maxLogLevel = LogLevel.Assert;

	private final Map<Class<? extends LoggerConfig>, LoggerValidator<?, ? extends LoggerClient<? extends LoggerConfig>>> validators = new HashMap<>();
	private final Map<String, LoggerClient> logClients = new ConcurrentHashMap<>();
	private Rule[] rules = {};

	private BeLogged() {
		addValidator(Config_FileLogger.class, new FileLoggerValidator());
		addValidator(Config_JavaLogger.class, new JavaLoggerValidator());
	}

	public final <Config extends LoggerConfig> void addValidator(Class<Config> configType,
	                                                             LoggerValidator<Config, ? extends LoggerClient<Config>> validator) {
		validators.put(configType, validator);
	}

	public final void setConfig(BeConfig _config) {
		if (_config.rules == null)
			throw new BadImplementationException("what is the point in having no rules??");

		logClients.clear();
		this.rules = _config.rules;
		for (LoggerConfig config : _config.configs) {
			if (config.key == null)
				throw new BadImplementationException("logger MUST have a key!! ");

			for (LoggerConfig otherConfig : _config.configs) {
				if (otherConfig == config)
					continue;

				if (config.key.equals(otherConfig.key))
					throw new BadImplementationException("Logger key MUST be unique!! Found two entries with key: " + config.key);
			}
		}

		for (LoggerConfig config : _config.configs) {
			LoggerClient loggerClient = createLoggerFromConfig(config);
			loggerClient.init();
			this.logClients.put(config.key, loggerClient);
		}
	}

	@SuppressWarnings("unchecked")
	public <Config extends LoggerConfig> LoggerClient<Config> createLoggerFromConfig(Config config) {
		LoggerValidator<Config, LoggerClient<Config>> validator = (LoggerValidator<Config, LoggerClient<Config>>) validators.get(config.getClass());
		validator.validateConfig(config);

		LoggerClient<Config> logger = ReflectiveTools.newInstance(validator.loggerType);
		logger.setConfig(config);
		logger.init();
		return logger;
	}

	final void log(final LogLevel level, final String tag, final String message, final Object[] params, final Throwable t) {
		if (!(level.ordinal() >= minLogLevel.ordinal() && level.ordinal() <= maxLogLevel.ordinal()))
			return;

		Thread thread = Thread.currentThread();
		String formattedMessage = params == null || params.length == 0 ? message : null;

		for (Rule rule : rules) {
			if (!(level.ordinal() >= rule.minLevel.ordinal() && level.ordinal() <= rule.maxLevel.ordinal()))
				continue;

			if (rule.thread != null && !thread.getName().matches(rule.thread))
				continue;

			if (rule.tag != null && !tag.matches(rule.tag))
				continue;

			if (formattedMessage == null && message != null)
				formattedMessage = String.format(message, params);

			for (String loggerKey : rule.loggerKeys) {
				LoggerClient client = logClients.get(loggerKey);
				client._log(level, thread, tag, formattedMessage, t);
			}
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

	public LoggerClient[] getClients() {
		return ArrayTools.asArray(logClients.values(), LoggerClient.class);
	}
}
