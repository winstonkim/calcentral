package edu.berkeley.calcentral.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Telemetry {

	private static final Log LOGGER = LogFactory.getLog(Telemetry.class);

	private String caller;

	private long start;

	public Telemetry(Class<?> caller, String method) {
		this.caller = caller.getSimpleName() + "." + method;
		this.start = System.currentTimeMillis();
	}

	public void end() {
		long time = System.currentTimeMillis() - start;
		LOGGER.info(caller + " took " + time + "ms");
	}

}
