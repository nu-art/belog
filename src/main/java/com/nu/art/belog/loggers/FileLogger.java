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

package com.nu.art.belog.loggers;

import com.nu.art.belog.BeConfig;
import com.nu.art.belog.BeConfig.LoggerConfig;
import com.nu.art.belog.BeConfig.Rule;
import com.nu.art.belog.LoggerClient;
import com.nu.art.belog.LoggerValidator;
import com.nu.art.belog.loggers.FileLogger.Config_FileLogger;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.core.tools.FileTools;
import com.nu.art.core.tools.SizeTools;
import com.nu.art.core.utils.InstanceRecycler;
import com.nu.art.core.utils.InstanceRecycler.Instantiator;
import com.nu.art.core.utils.PoolQueue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileLogger
	extends LoggerClient<Config_FileLogger> {

	public static final Rule Rule_AllToFileLogger = new Rule().setLoggerKeys(Config_FileLogger.KEY);
	public static final Config_FileLogger LogConfig_FileLogger = (Config_FileLogger) new Config_FileLogger().setKey(Config_FileLogger.KEY);
	public static final BeConfig Config_FastFileLogger = new BeConfig().setRules(Rule_AllToFileLogger).setLoggersConfig(LogConfig_FileLogger);

	private boolean enable = true;

	private OutputStreamWriter logWriter;

	private long written;

	private PoolQueue<LogEntry> queue = new PoolQueue<LogEntry>() {
		@Override
		protected void onExecutionError(LogEntry item, Throwable e) {
			System.err.println("Error writing log: " + item);
			e.printStackTrace();
		}

		@Override
		protected void executeAction(LogEntry logEntry) {
			try {
				String logMessage = composer.composeEntry(logEntry.level, logEntry.thread, logEntry.tag, logEntry.message, logEntry.t);
				try {
					logWriter.write(logMessage);
					logWriter.flush();
				} catch (IOException e) {
					disable(e);
					return;
				}

				written += logMessage.getBytes().length;
				if (written >= config.size)
					rotate();
			} finally {
				recycler.recycle(logEntry);
			}
		}
	};

	/**
	 * Use {@link #setConfig(LoggerConfig)} instead
	 */
	@Deprecated
	public FileLogger set(File logFolder, String fileNamePrefix, long maxFileSize, int filesCount) {
		Config_FileLogger config = new Config_FileLogger();
		config.count = filesCount;
		config.size = maxFileSize;
		config.fileName = fileNamePrefix;
		config.folder = logFolder;
		this.setConfig(config);
		return this;
	}

	private void disable(Throwable t) {
		System.err.println("DISABLING FILE LOGGER");
		t.printStackTrace();
		enable = false;
	}

	@Override
	protected void init() {
		try {
			FileTools.mkDir(config.folder);
		} catch (IOException e) {
			disable(e);
		}

		File logFile = getLogTextFile(0);
		if (!logFile.exists() || logFile.length() >= config.size)
			rotate();
		else {
			try {
				FileTools.createNewFile(logFile);
			} catch (IOException e) {
				disable(e);
				return;
			}
			written = logFile.length();
			createLogWriter(logFile);
		}

		// Starting the queue after the setup is completed
		queue.createThreads("File logger", 1);
	}

	private void rotate() {
		System.out.println("rotating files");

		try {
			FileTools.delete(getLogZipFile(config.count - 1));
		} catch (IOException e) {
			disable(e);
			return;
		}

		for (int i = config.count - 2; i >= 0; i--) {
			rotateFile(i);
		}

		try {
			dismissLogWriter();
			File file = getLogTextFile(0);
			FileTools.delete(file);
			FileTools.createNewFile(file);
			createLogWriter(file);
		} catch (IOException e) {
			System.err.println("Cannot create new logWriter for file");
			disable(e);
		}
	}

	private void dismissLogWriter() {
		if (logWriter != null) {
			try {
				logWriter.flush();
				logWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createLogWriter(File file) {
		written = file.length();
		try {
			logWriter = new OutputStreamWriter(new FileOutputStream(file, true));
		} catch (FileNotFoundException e) {
			disable(e);
		}
	}

	private void rotateFile(int index) {
		File logTextFile = getLogTextFile(index);
		File logZipFile = getLogZipFile(index);
		File newLogZipFile = getLogZipFile(index + 1);

		try {
			if (!logTextFile.exists() && !logZipFile.exists())
				return;

			if (logTextFile.exists())
				FileTools.archive(logZipFile, logTextFile);

			FileTools.renameFile(logZipFile, newLogZipFile);
		} catch (Exception e) {
			System.err.println("Cannot rotate file from: " + logZipFile.getName() + " ==> " + newLogZipFile.getName() + "\n");
			disable(e);
		}
	}

	private File getLogTextFile(int i) {
		return getFile(i, "txt");
	}

	private File getLogZipFile(int i) {
		return getFile(i, "zip");
	}

	private File getFile(int i, String suffix) {
		return new File(config.folder, config.fileName + "-" + getIndexAsString(i) + "." + suffix);
	}

	private String getIndexAsString(int index) {
		int numDigits = (config.count + "").length();
		int missingZeros = numDigits - (index + "").length();

		String toRet = "";
		for (int i = 0; i < missingZeros; i++) {
			toRet += "0";
		}
		toRet += index;
		return toRet;
	}

	public final File[] getAllLogFiles() {
		List<File> filesToZip = new ArrayList<>();
		for (int i = 0; i < config.count; i++) {
			File file = getLogTextFile(i);
			if (file.exists())
				filesToZip.add(file);

			file = getLogZipFile(i);
			if (file.exists())
				filesToZip.add(file);
		}

		return ArrayTools.asArray(filesToZip, File.class);
	}

	private InstanceRecycler<LogEntry> recycler = new InstanceRecycler<>(new Instantiator<LogEntry>() {
		@Override
		public LogEntry create() {
			return new LogEntry();
		}
	});

	@Override
	protected void log(LogLevel level, Thread thread, String tag, String message, Throwable t) {
		if (!enable)
			return;

		LogEntry instance = recycler.getInstance().set(level, thread, tag, message, t);
		queue.addItem(instance);
	}

	public static class FileLoggerValidator
		extends LoggerValidator<Config_FileLogger, FileLogger> {

		public FileLoggerValidator() {
			super(FileLogger.class);
		}

		@Override
		protected void validateConfig(Config_FileLogger config) {
			if (config.folder == null)
				throw new BadImplementationException("No output folder specified of logger: " + config.key);

			if (config.size < SizeTools.MegaByte)
				throw new BadImplementationException("File size MUST be >= 1 MB");

			if (config.count < 3)
				throw new BadImplementationException("Rotation count MUST be >= 3");

			if (config.fileName == null)
				config.fileName = "logger-" + config.key;
		}
	}

	public static class Config_FileLogger
		extends LoggerConfig {

		public static final String KEY = FileLogger.class.getSimpleName();

		File folder;
		String fileName;
		long size = 5 * SizeTools.MegaByte;
		int count = 10;

		public Config_FileLogger() {
			super(KEY);
		}

		public Config_FileLogger setFolder(File folder) {
			this.folder = folder;
			return this;
		}

		public Config_FileLogger setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public Config_FileLogger setCount(int count) {
			this.count = count;
			return this;
		}

		public Config_FileLogger setSize(long size) {
			this.size = size;
			return this;
		}
	}
}
