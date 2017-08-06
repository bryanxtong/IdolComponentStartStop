package com.bryan.idol.components;

/**
 * create idol task graph and currently two operations supported, start/stop to
 * start/stop idol components via startconnector.sh or stopconnector.sh
 * 
 * @author tongda
 *
 */
public interface IdolTaskGraph {

	public void createIdolTaskGraph(Task.State state);

}
