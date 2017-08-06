package com.bryan.idol.components;

import java.util.List;

/**
 * This class represents the task to start/stop an idol component
 * 
 * @author tongda
 *
 */
public abstract class Task {

	public enum State {
		START, STOP
	}

	protected State state;

	/**
	 * idol component name in Application.conf
	 */
	protected String componentName;

	/**
	 * linux component location
	 */
	protected String location;

	/**
	 * process name which we need to grep to find whether a process exists in
	 * linux
	 */
	protected String processName;
	/**
	 * linux start/stop script name which we can call to start and stop the idol
	 * components
	 */
	protected String startScript;
	protected String stopScript;

	/**
	 * start/stop task dependencies which we use to create dag dynamically and
	 * It is configured in the configuration files
	 */
	protected List<String> startDependency;
	protected List<String> stopDependency;

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getStartScript() {
		return startScript;
	}

	public void setStartScript(String startScript) {
		this.startScript = startScript;
	}

	public String getStopScript() {
		return stopScript;
	}

	public void setStopScript(String stopScript) {
		this.stopScript = stopScript;
	}

	public List<String> getStartDependency() {
		return startDependency;
	}

	public void setStartDependency(List<String> startDependency) {
		this.startDependency = startDependency;
	}

	public List<String> getStopDependency() {
		return stopDependency;
	}

	public void setStopDependency(List<String> stopDependency) {
		this.stopDependency = stopDependency;
	}

	abstract boolean execute();

}
