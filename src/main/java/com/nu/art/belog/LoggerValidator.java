package com.nu.art.belog;

import com.nu.art.belog.BeConfig.LoggerConfig;

public class LoggerValidator<Config extends LoggerConfig, Logger extends LoggerClient> {

	final Class<Logger> loggerType;

	public LoggerValidator(Class<Logger> loggerType) {
		this.loggerType = loggerType;
	}

	protected void validateConfig(Config config)
		throws RuntimeException {
	}
}
