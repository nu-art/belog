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

import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.core.tools.FileTools;
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

public class FileLoggerClient
	extends BeLoggedClient {

	private File logFolder;

	private long maxFileSize;

	private String fileNamePrefix;

	private int filesCount;

	private boolean enable = true;

	private OutputStreamWriter logWriter;

	private long written;

	private PoolQueue<LogEntry> queue = new PoolQueue<LogEntry>() {
		@Override
		protected void onExecutionError(LogEntry item, Throwable e) {

		}

		@Override
		protected void executeAction(LogEntry logEntry)
			throws Exception {

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
				if (written >= maxFileSize)
					rotate();
			} finally {
				recycler.recycle(logEntry);
			}
		}
	};

	public void set(File logFolder, String fileNamePrefix, long maxFileSize, int filesCount) {
		this.logFolder = logFolder;
		this.maxFileSize = maxFileSize;
		this.fileNamePrefix = fileNamePrefix;
		this.filesCount = filesCount;
	}

	private void disable(Throwable t) {
		System.err.println("DISABLING FILE LOGGER");
		t.printStackTrace();
		enable = false;
	}

	@Override
	protected void init() {
		try {
			FileTools.mkDir(logFolder);
		} catch (IOException e) {
			disable(e);
		}

		File logFile = getLogTextFile(0);
		if (!logFile.exists() || logFile.length() >= maxFileSize)
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

		for (int i = filesCount - 2; i >= 0; i--) {
			rotateFile(i);
		}

		try {
			dismissLogWriter();
			File file = getLogTextFile(0);
			if (file.exists())
				FileTools.delete(file);

			if ((file.exists() && !file.delete()) || !file.createNewFile()) {
				//				disable();
				return;
			}

			createLogWriter(file);
		} catch (IOException e) {
			//			disable();
			e.printStackTrace();
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
			System.err.println("Cannot rotate file from: " + logZipFile.getName() + " ==> " + newLogZipFile.getName());
			e.printStackTrace();
			disable(e);
		}
	}

	public File getLogTextFile(int i) {
		return getFile(i, "txt");
	}

	public File getLogZipFile(int i) {
		return getFile(i, "zip");
	}

	private File getFile(int i, String suffix) {
		return new File(logFolder, fileNamePrefix + "-" + getIndexAsString(i) + "." + suffix);
	}

	public int getFilesCount() {
		return filesCount;
	}

	private String getIndexAsString(int index) {
		int numDigits = (filesCount + "").length();
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
		for (int i = 0; i < filesCount; i++) {
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
}
