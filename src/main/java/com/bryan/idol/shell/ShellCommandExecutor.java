package com.bryan.idol.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.LogOutputStream;

/*
 * Execute shell commands with java library zeroturnaround which you can find the information in github.
 * 
 */
public class ShellCommandExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(ShellCommandExecutor.class);
	private final ProcessExecutor processExecutor = new ProcessExecutor();

	public int processCommandWithLog(String... command) {
		try {
			int exitValue = processExecutor.command(command).redirectOutput(new LogOutputStream() {
				@Override
				protected void processLine(String line) {
					System.out.println(line);
				}
			}).execute().getExitValue();
			return exitValue;
		} catch (InvalidExitValueException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} catch (InterruptedException e) {
			LOG.error(e.getMessage());
		} catch (TimeoutException e) {
			LOG.error(e.getMessage());
		}
		// -1 indicates not success
		return -1;
	}

	/**
	 * process a shell command and return the results, if you want to invoke a
	 * shell command including pipe you should execute it in a shell
	 * 
	 * @param command
	 * @return
	 */
	public ProcessResult processShellCommand(String... command) {
		try {
			ProcessResult processResult = processExecutor.command(command).readOutput(true).execute();
			return processResult;
		} catch (InvalidExitValueException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} catch (InterruptedException e) {
			LOG.error(e.getMessage());
		} catch (TimeoutException e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

	/**
	 * execute linux top command and get the results
	 * 
	 * %CPU -- CPU usage The task's share of the elapsed CPU time since the last
	 * screen update, expressed as a percentage of total CPU time. In a true SMP
	 * environment, if 'Irix mode' is Off, top will operate in 'Solaris mode'
	 * where a task's cpu usage will be divided by the total number of CPUs. You
	 * toggle 'Irix/Solaris' modes with the 'I' interactive command
	 * 
	 * @return
	 */
	public List<String> getTopInfomation() {
		// ProcessResult processResult = this.processShellCommand("/bin/sh",
		// "-c", "/usr/bin/top -b -n 1");
		// top -bn 1 | grep "^[0-9 ]" | awk '{ printf("%-8s\t%-8s\t%-8s \n",
		// $2,$9,$10); }' | head -8
		ProcessResult processResult = this.processShellCommand("/bin/sh", "-c",
				"/usr/bin/top -bn 1 | grep \"^[0-9 ]\" | awk '{ printf(\"%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s \\n\", $1,$2,$5,$6,$9,$10,$11,$12); }' | head -10");
		List<String> result = processResult.getOutput().getLinesAsUTF8();
		return result;
	}

	/**
	 * Process the output of a command which contains a header and re-organize
	 * the data as a list of LinkedHashMap
	 * 
	 * @param commandResults
	 * @param delimiter
	 * @return
	 */
	public List<LinkedHashMap<String, String>> reOrganizeCommandOutput(List<String> commandResults, String delimiter) {

		List<LinkedHashMap<String, String>> commandOutput = new ArrayList<LinkedHashMap<String, String>>();
		// suppose the first line is the header
		String[] headerArray = commandResults.get(0).split(delimiter);
		for (int index = 1; index < commandResults.size(); index++) {
			LinkedHashMap<String, String> lineOfData = new LinkedHashMap<String, String>();
			String[] dataArray = commandResults.get(index).split(delimiter);
			for (int ele = 0; ele < dataArray.length; ele++) {
				lineOfData.put(headerArray[ele], dataArray[ele]);
			}
			commandOutput.add(lineOfData);
		}
		return commandOutput;
	}

	/**
	 * get process information and separate it with a tab, also donot include
	 * the header line
	 * 
	 * List processes by mem usage command : ps -e -orss=,args= | sort -b -k1,1n
	 * | pr -TW$COLUMNS\ List the process : ps -e
	 * -opid=,user=,pcpu=,pmem=,etime=,rss=,vsize=,args=
	 * 
	 * %cpu %CPU cpu utilization of the process in "##.#" format. Currently, it
	 * is the CPU time used divided by the time the process has been running
	 * (cputime/realtime ratio), expressed as a percentage. It will not add up
	 * to 100% unless you are lucky. (alias pcpu)
	 */
	public List<String> getProcessInformation() {
		/*
		 * String output = processShellCommand("/bin/sh", "-c",
		 * "ps -eo pcpu,pid -o comm= | sort -k1 -n -r | head -10 | awk '{print $1\"\t\"$2\"\t\"$3}'"
		 * ).getOutput() .getUTF8();
		 */
		List<String> outputLines = processShellCommand("/bin/sh", "-c",
				"ps -eopcpu=,pid=,user=,pmem=,etime=,rss=,vsize=,args= | sort -k1 -n -r | head -10 | awk '{print $1\"\t\"$2\"\t\"$3\"\t\"$4\"\t\"$5\"\t\"$6\"\t\"$7\"\t\"$8}'")
						.getOutput().getLinesAsUTF8();

		return outputLines;
	}

	/**
	 * check whether a process exists or not
	 * 
	 * @param processName
	 * @param startCommand
	 * @return
	 */
	public int processIDOLComponentsStart1(String processName, String startCommand) {
		// $ ps -eaf | grep -q <[p]rocess name> || <process name>
		// The bit with the [p] makes it so that the grep won't find itself as a
		// result.
		processName = "[" + processName.charAt(0) + "]" + processName.substring(1);
		String shellScripts = "res=$(ps -ef | grep " + processName
				+ " | wc -l); if [ \"$res\" -eq \"1\" ] ; then echo \"this process started already\"; else "
				+ startCommand + "; fi";
		return this.processCommandWithLog("/bin/sh", "-c", shellScripts);
	}

	/**
	 * 
	 * Process the stop command for the idol components
	 * 
	 * @param prcessName
	 * @param command
	 * @return
	 */
	public int processIDOLComponentsStop1(String processName, String stopCommand) {
		processName = "[" + processName.charAt(0) + "]" + processName.substring(1);
		String shellScripts = "res=$(ps -ef | grep " + processName + " | wc -l); if [ \"$res\" -eq \"1\" ] ; then "
				+ stopCommand + "; else echo \"this process stopped already\"; fi";
		return this.processCommandWithLog("/bin/sh", "-c", shellScripts);

	}

	/**
	 * process shell scripts and we can use it to start idol components with
	 * java
	 * 
	 * @return
	 */
	public int processIDOLComponentsStart(String processName, String startCommand) {
		String findProcessCommand = "`ps -ef | grep " + processName + " | grep -v grep | wc -l`";
		String shellScripts = "if [ " + findProcessCommand
				+ " -eq \"1\" ] ; then echo \"this process started already\"; else " + startCommand + "; fi;";
		return this.processCommandWithLog("/bin/sh", "-c", shellScripts);
	}

	/**
	 * As the start shell scripts startconnector.sh/start-idolserver.sh contain
	 * nohup command which we could not get the real status of components, so we
	 * keep checking the status of a process to know it.
	 * 
	 * I have also tried with built-in func wait, but fail to make it work
	 * 
	 * @param processName
	 * @param startCommand
	 * @return
	 */
	public int processIDOLComponentsStartWithNohupInside(String processName, String startCommand) {
		// $ ps -eaf | grep process name> | grep -v grep , find a process name
		// which excludes the grep commands
		String findProcessCommand = "`ps -ef | grep " + processName + " | grep -v grep | wc -l`";
		String shellScripts = "if [ " + findProcessCommand
				+ " -eq \"1\" ] ; then echo \"this process started already\"; else " + startCommand + "; while [ "
				+ findProcessCommand + " -eq \"0\" ]; do sleep 2; echo \"wait to finish...\"; done;fi;";
		return this.processCommandWithLog("/bin/sh", "-c", shellScripts);
	}

	/**
	 * 
	 * Process the stop command for the idol components. currently for IDOL stop
	 * command, most of them doesnot include nohup command
	 * 
	 * @param prcessName
	 * @param command
	 * @return
	 */
	public int processIDOLComponentsStop(String processName, String stopCommand) {
		String findProcessCommand = "`ps -ef | grep " + processName + " | grep -v grep | wc -l`";
		String shellScripts = "if [ " + findProcessCommand + " -eq \"1\" ] ; then " + stopCommand
				+ "; else echo \"this process stopped already\"; fi";
		return this.processCommandWithLog("/bin/sh", "-c", shellScripts);

	}
}