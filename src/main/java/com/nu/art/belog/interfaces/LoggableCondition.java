package com.nu.art.belog.interfaces;

import com.nu.art.belog.consts.LogLevel;

public interface LoggableCondition {

	boolean isLoggable(LogLevel level, Thread thread, String tag, String formattedMessage, Throwable t);
}
