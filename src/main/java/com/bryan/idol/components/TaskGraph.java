package com.bryan.idol.components;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.bryan.idol.components.Task.State;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public class TaskGraph implements IdolTaskGraph {

	/**
	 * load all the task keys from the configuration file and remove the
	 * duplicates
	 */
	private Set<String> loadAllTaskKeys() {
		final Config conf = ConfigFactory.defaultApplication();
		Set<String> sets = new HashSet<>();
		for (Entry<String, ConfigValue> entry : conf.entrySet()) {
			sets.add(entry.getKey().substring(0, entry.getKey().indexOf('.')));
		}

		return sets;
	}

	@Override
	public void createIdolTaskGraph(State state) {

		DefaultDirectedGraph<IdolComponentTask, DefaultEdge> g = new DefaultDirectedGraph<IdolComponentTask, DefaultEdge>(
				DefaultEdge.class);

		LinkedHashMap<String, IdolComponentTask> taskMapping = new LinkedHashMap<>();

		Set<String> tasks = this.loadAllTaskKeys();
		for (String taskName : tasks) {
			IdolComponentTask task = new IdolComponentTask(taskName, state);
			// add vertex
			g.addVertex(task);
			taskMapping.put(taskName, task);
		}

		Set<IdolComponentTask> vertexSet = g.vertexSet();
		if (State.START.equals(state)) {

			for (IdolComponentTask task : vertexSet) {
				List<String> dependencyComponentTasks = task.getStartDependency();
				if (dependencyComponentTasks.size() > 0) {
					for (String dependencyTask : dependencyComponentTasks) {
						g.addEdge(taskMapping.get(dependencyTask), task);
					}
				}
			}

		} else if (State.STOP.equals(state)) {
			for (IdolComponentTask task : vertexSet) {
				List<String> dependencyComponentTasks = task.getStopDependency();
				if (dependencyComponentTasks.size() > 0) {
					for (String dependencyTask : dependencyComponentTasks) {
						g.addEdge(taskMapping.get(dependencyTask), task);
					}
				}
			}

		} else {
			// nothing to do
		}

		IdolComponentTask v;
		TopologicalOrderIterator<IdolComponentTask, DefaultEdge> orderIterator;

		orderIterator = new TopologicalOrderIterator<IdolComponentTask, DefaultEdge>(g);
		System.out.println("\nOrdering:");
		while (orderIterator.hasNext()) {
			v = orderIterator.next();
			v.execute();
			// System.out.println(v.getComponentName());
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Please provide start/stop param, using java -jar <jarname> start/stop instead");
			System.exit(1);
		}
		String param = args[0];

		if (param.equals("start")) {
			new TaskGraph().createIdolTaskGraph(State.START);
		} else if (param.equals("stop")) {
			new TaskGraph().createIdolTaskGraph(State.STOP);

		}
	}

}
