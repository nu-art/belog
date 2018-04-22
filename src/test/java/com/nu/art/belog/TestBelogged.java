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
	public void test() {
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
}
