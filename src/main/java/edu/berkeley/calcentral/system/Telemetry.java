package edu.berkeley.calcentral.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Telemetry {

	private static final Log LOGGER = LogFactory.getLog(Telemetry.class);

	private String caller;

	private long start;

	private long time;

	public Telemetry(Class<?> caller, String method) {
		this.caller = caller.getSimpleName() + "." + method;
		this.start = System.currentTimeMillis();
	}

	public void end() {
		time = System.currentTimeMillis() - start;
		LOGGER.trace(caller + " took " + time + "ms");
	}

	public long getTime() {
		return time;
	}
}
