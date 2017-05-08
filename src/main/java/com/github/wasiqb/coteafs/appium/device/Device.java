package com.github.wasiqb.coteafs.appium.device;

import static com.github.wasiqb.coteafs.appium.utils.CapabilityUtils.setCapability;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.github.wasiqb.coteafs.appium.config.ConfigLoader;
import com.github.wasiqb.coteafs.appium.config.DeviceSetting;
import com.github.wasiqb.coteafs.appium.exception.DeviceAppNotClosingException;
import com.github.wasiqb.coteafs.appium.exception.DeviceAppNotFoundException;
import com.github.wasiqb.coteafs.appium.exception.DeviceDriverDefaultWaitException;
import com.github.wasiqb.coteafs.appium.exception.DeviceDriverInitializationFailedException;
import com.github.wasiqb.coteafs.appium.exception.DeviceDriverNotStartingException;
import com.github.wasiqb.coteafs.appium.exception.DeviceDriverNotStoppingException;
import com.github.wasiqb.coteafs.appium.service.AppiumServer;
import com.google.common.reflect.TypeToken;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

/**
 * @author wasiq.bhamla
 * @param <TDriver>
 * @since 12-Apr-2017 9:38:38 PM
 */
public class Device <TDriver extends AppiumDriver <MobileElement>> {
	private static final Logger log;

	static {
		log = LogManager.getLogger (Device.class);
	}

	protected DesiredCapabilities	capabilities;
	protected TDriver				driver;
	protected final AppiumServer	server;
	protected final DeviceSetting	setting;
	private TypeToken <TDriver>		token;

	/**
	 * @author wasiq.bhamla
	 * @param server
	 * @param name
	 * @since 13-Apr-2017 9:10:11 PM
	 */
	public Device (final AppiumServer server, final String name) {
		this.server = server;
		this.setting = ConfigLoader.settings ()
			.getDevice (name);
		buildCapabilities ();
	}

	/**
	 * @author wasiq.bhamla
	 * @since 01-May-2017 7:08:10 PM
	 * @return driver
	 */
	public TDriver getDriver () {
		final String platform = this.setting.getDeviceType ()
			.getName ();
		final String msg = "Getting %s device driver...";
		log.trace (String.format (msg, platform));
		return this.driver;
	}

	/**
	 * @author wasiq.bhamla
	 * @since 17-Apr-2017 4:46:12 PM
	 */
	public void start () {
		final String platform = this.setting.getDeviceType ()
			.getName ();
		final String msg = "Starting %s device driver...";
		log.trace (String.format (msg, platform));
		try {
			this.driver = init (this.server.getServiceUrl (), this.capabilities);
		}
		catch (final Exception e) {
			throw new DeviceDriverNotStartingException ("Error occured starting device driver", e);
		}
		try {
			this.driver.manage ()
				.timeouts ()
				.implicitlyWait (this.setting.getDefaultWait (), TimeUnit.SECONDS);
		}
		catch (final Exception e) {
			throw new DeviceDriverDefaultWaitException ("Error occured while setting device driver default wait.", e);
		}
	}

	/**
	 * @author wasiq.bhamla
	 * @since 17-Apr-2017 4:46:02 PM
	 */
	public void stop () {
		String msg = null;
		final String platform = this.setting.getDeviceType ()
			.getName ();
		if (this.driver != null) {
			msg = "Closign app on %s device...";
			log.trace (String.format (msg, platform));
			try {
				this.driver.closeApp ();
			}
			catch (final Exception e) {
				throw new DeviceAppNotClosingException ("Error occured while closing app.", e);
			}

			msg = "Quitting %s device driver...";
			log.trace (String.format (msg, platform));
			try {
				this.driver.quit ();
			}
			catch (final Exception e) {
				throw new DeviceDriverNotStoppingException ("Error occured while stopping device driver.", e);
			}
			this.driver = null;
		}
		else {
			msg = "%s device driver already stopped...";
			log.trace (String.format (msg, platform));
		}
	}

	/**
	 * @author wasiq.bhamla
	 * @since 13-Apr-2017 3:38:32 PM
	 */
	private void buildCapabilities () {
		log.trace ("Building Device capabilities...");
		Objects.requireNonNull (this.setting.getDeviceName ());
		Objects.requireNonNull (this.setting.getDeviceType ());
		Objects.requireNonNull (this.setting.getAppLocation ());
		this.capabilities = new DesiredCapabilities ();

		setCapability (MobileCapabilityType.DEVICE_NAME, this.setting.getDeviceName (), this.capabilities);
		setCapability (MobileCapabilityType.PLATFORM_NAME, this.setting.getDeviceType ()
			.getName (), this.capabilities);
		setCapability (MobileCapabilityType.PLATFORM_VERSION, this.setting.getDeviceVersion (), this.capabilities);
		setCapability (MobileCapabilityType.BROWSER_NAME, this.setting.getDeviceType ()
			.getName (), this.capabilities);

		String path = System.getProperty ("user.dir") + "/src/test/resources/" + this.setting.getAppLocation ();

		if (this.setting.isExternalApp ()) {
			path = this.setting.getAppLocation ();
		}

		final File file = new File (path);
		if (!file.exists ()) {
			final String msg = "App not found on mentioned location [%s]...";
			log.error (String.format (msg, path));
			throw new DeviceAppNotFoundException (String.format (msg, path));
		}

		setCapability (MobileCapabilityType.NO_RESET, Boolean.toString (this.setting.isNoReset ()), this.capabilities);
		setCapability (MobileCapabilityType.FULL_RESET, Boolean.toString (this.setting.isFullReset ()),
				this.capabilities);
		setCapability (MobileCapabilityType.NEW_COMMAND_TIMEOUT, Integer.toString (this.setting.getSessionTimeout ()),
				this.capabilities);
		setCapability (MobileCapabilityType.APP, path, this.capabilities);
		setCapability (MobileCapabilityType.AUTOMATION_NAME, this.setting.getAutomationName ()
			.getName (), this.capabilities);
		setCapability (AndroidMobileCapabilityType.APP_ACTIVITY, this.setting.getAppActivity (), this.capabilities);
		setCapability (AndroidMobileCapabilityType.APP_PACKAGE, this.setting.getAppPackage (), this.capabilities);
		setCapability (AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, this.setting.getAppWaitActivity (),
				this.capabilities);

		setCapability (IOSMobileCapabilityType.APP_NAME, this.setting.getAppName (), this.capabilities);
		setCapability (IOSMobileCapabilityType.BUNDLE_ID, this.setting.getBundleId (), this.capabilities);
		setCapability ("udid", this.setting.getUdid (), this.capabilities);
		setCapability ("bootstrapPath", this.setting.getBootstrapPath (), this.capabilities);
		setCapability ("agentPath", this.setting.getAgentPath (), this.capabilities);

		log.trace ("Building Device capabilities completed...");
	}

	@SuppressWarnings ("unchecked")
	private TDriver init (final URL url, final Capabilities capability) {
		log.trace ("Initializing driver...");
		this.token = new TypeToken <TDriver> (getClass ()) {
			private static final long serialVersionUID = 1562415938665085306L;
		};
		final Class <TDriver> cls = (Class <TDriver>) this.token.getRawType ();
		final Class <?> [] argTypes = new Class <?> [] { URL.class, Capabilities.class };
		try {
			final Constructor <TDriver> ctor = cls.getDeclaredConstructor (argTypes);
			return ctor.newInstance (url, capability);
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new DeviceDriverInitializationFailedException ("Error occured while initializing device driver.", e);
		}
	}
}