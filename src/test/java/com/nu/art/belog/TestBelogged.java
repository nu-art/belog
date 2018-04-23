package com.nu.art.belog;

import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.exceptions.runtime.NotImplementedYetException;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by tacb0ss on 23/04/2018.
 */
public class TestBelogged
	extends Logger {

	private static int Count_Repeat = 1000000;
	private static boolean setUpIsDone = false;
	private DefaultLogClient logClient;

	@Before
	public void setUp() {
		if (setUpIsDone) {
			return;
		}

		logClient = new DefaultLogClient();
		BeLogged.getInstance().addClient(logClient);
		setUpIsDone = true;
	}

	@Test
	public void testBelogged() {
		log(LogLevel.Debug, "%s: Testing param", "Test");
		log(LogLevel.Info, "Testing no param");

		logClient.setLogLevel(LogLevel.Warning, LogLevel.Assert);
		log(LogLevel.Info, "Should NOT be shown");
		log(LogLevel.Warning, "Should be shown warning");
		log(LogLevel.Error, "Should be shown error");
		log(LogLevel.Error, "Should be shown With exception", new NotImplementedYetException("Test Exception error"));

		logClient.setLogLevel(LogLevel.Verbose, LogLevel.Warning);
		log(LogLevel.Error, "Should NOT be shown error");
		log(LogLevel.Debug, "Should be shown With exception", new NotImplementedYetException("Test Exception debug"));
		log(LogLevel.Debug, "Should be shown With exception %s", new NotImplementedYetException("Test Exception debug"));
		log(LogLevel.Debug, "Should be shown With param and exception %s and %s", "Donno", new NotImplementedYetException("Test Exception debug"));

		log(LogLevel.Info, new NotImplementedYetException("Exception only"));
		BeLogged.getInstance().setLogLevel(LogLevel.Warning, LogLevel.Assert);
		log(LogLevel.Info, "Should NOT be shown");
	}

	@Test
	public void logBenchmark() {
		benchmarkStringConcat("param1", 2, "param3", 0.84f);
		benchmarkStringFormat("param1", 2, "param3", 0.84f);

		long concatDuration = benchmarkStringConcat("param1", 2, "param3", 0.84f);
		long formatDuration = benchmarkStringFormat("param1", 2, "param3", 0.84f);

		logInfo("format: " + formatDuration + ", concat: " + concatDuration);
	}

	private long benchmarkStringFormat(Object... p) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < Count_Repeat; i++) {
			String str = String.format("Test String format: 1-%s, 2-%d, 3-%s, 4-%f", p);
		}
		return System.currentTimeMillis() - start;
	}

	private long benchmarkStringConcat(Object... p) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < Count_Repeat; i++) {
			String str = "Test String concat: 1-" + p[0] + ", 2-" + p[1] + ", 3-" + p[2] + ", 4-" + p[3];
		}
		return System.currentTimeMillis() - start;
	}
}
