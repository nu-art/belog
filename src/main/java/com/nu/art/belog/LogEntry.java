package com.nu.art.belog;

import com.nu.art.belog.consts.LogLevel;

class LogEntry {

	long timestamp;

	LogLevel level;

	String thread;

	String tag;

	String message;

	Throwable t;

	LogEntry set(LogLevel level, String thread, String tag, String message, Throwable t) {
		this.timestamp = System.currentTimeMillis();
		this.level = level;
		this.thread = thread;
		this.tag = tag;
		this.message = message;
		this.t = t;
		return this;
	}
}