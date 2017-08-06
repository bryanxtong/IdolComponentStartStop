package com.bryan.idol.components;

import com.bryan.idol.shell.ShellCommandExecutor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 
 * This is the start/stop task for idol components which we can use
 * startconnector.sh or stopconnector.sh to start or stop idol components
 * 
 * For some script which includes nohup command, It seems that we have to add
 * "</dev/null >/dev/null" in order to make the java process exit successfully
 * and in this way , users may not see the output of the shell scripts
 * 
 * @author tongda
 *
 */
public class IdolComponentTask extends Task {

	private final Config conf = ConfigFactory.load();

	public IdolComponentTask(String componentName, State state) {
		// idol component name in application.conf
		this.componentName = componentName;

		this.state = state;
		// find whether a process exists or not and then call shell scripts to
		// start/stop idol components
		this.processName = conf.getString(this.componentName + ".processname");
		this.location = conf.getString(this.componentName + ".location");
		this.startScript = conf.getString(this.componentName + ".startscript");
		this.stopScript = conf.getString(this.componentName + ".stopscript");

		this.startDependency = conf.getStringList(this.componentName + ".startdependency");
		this.stopDependency = conf.getStringList(this.componentName + ".stopdependency");
	}

	/**
	 * execute a idol start/stop shell script
	 */
	@Override
	boolean execute() {
		System.out
				.println("******executing the shell script on the component " + this.getComponentName() + "**********");
		System.out.println();
		ShellCommandExecutor commandExecutor = new ShellCommandExecutor();

		if (this.state.equals(Task.State.START)) {
			// run the start script in the current shell
			String commandToExecute = "cd " + this.location + ";. ./" + startScript;

			// It seems that nohup in shell script could not return to prompt
			// until we add "</dev/null >/dev/null" for CFS startconnector.sh
			commandExecutor.processIDOLComponentsStartWithNohupInside(this.processName, commandToExecute);

		} else if (this.state.equals(Task.State.STOP)) {
			String commandToExecute = "cd " + this.location + ";. ./" + stopScript;
			commandExecutor.processIDOLComponentsStop(this.processName, commandToExecute);

		} else {
			throw new RuntimeException("unsupported operations.");
		}
		return true;
	}

}
