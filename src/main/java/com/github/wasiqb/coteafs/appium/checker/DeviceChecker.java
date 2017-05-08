package com.github.wasiqb.coteafs.appium.checker;

import com.github.wasiqb.coteafs.appium.exception.AppiumServerNotRunningException;
import com.github.wasiqb.coteafs.appium.exception.DeviceElementDisabledException;
import com.github.wasiqb.coteafs.appium.exception.DeviceElementNotDisplayedException;
import com.github.wasiqb.coteafs.appium.service.AppiumServer;

import io.appium.java_client.MobileElement;

/**
 * @author wasiq.bhamla
 * @since 04-May-2017 4:30:02 PM
 */
public final class DeviceChecker {
	/**
	 * @author wasiq.bhamla
	 * @since 04-May-2017 10:10:28 PM
	 * @param element
	 * @param name
	 */
	public static void checkDeviceElementDisplayed (final MobileElement element, final String name) {
		if (!element.isDisplayed ()) {
			final String msg = "Device element %s is not displayed.";
			throw new DeviceElementNotDisplayedException (String.format (msg, name));
		}
	}

	/**
	 * @author wasiq.bhamla
	 * @since 04-May-2017 11:08:59 PM
	 * @param element
	 * @param name
	 */
	public static void checkDeviceElementEnabled (final MobileElement element, final String name) {
		if (!element.isEnabled ()) {
			final String msg = "Device element %s is disabled.";
			throw new DeviceElementDisabledException (String.format (msg, name));
		}
	}

	/**
	 * @author wasiq.bhamla
	 * @since 04-May-2017 4:36:57 PM
	 * @param server
	 */
	public static void checkServerRunning (final AppiumServer server) {
		if (!server.isRunning ()) {
			throw new AppiumServerNotRunningException ("Server not started yet.");
		}
	}
}