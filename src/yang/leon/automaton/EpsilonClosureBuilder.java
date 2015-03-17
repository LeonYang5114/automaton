package yang.leon.automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class EpsilonClosureBuilder {

    private Map<Integer, Integer> enclosed;
    private Stack<Integer> toBuild;
    private Set<Integer>[] epsRelations;
    private Set<Integer>[] builtEpsClosure;

    public EpsilonClosureBuilder(Set<Integer>[] epsRelations) {
	setParameters(epsRelations);
    }

    @SuppressWarnings("unchecked")
    public void setParameters(Set<Integer>[] epsRelations) {
	this.enclosed = new HashMap<Integer, Integer>();
	this.toBuild = new Stack<Integer>();
	this.epsRelations = epsRelations;
	this.builtEpsClosure = (HashSet<Integer>[]) new HashSet[epsRelations.length];
    }

    public Set<Integer>[] getBuiltEpsClosure() {
	return this.builtEpsClosure;
    }

    public void buildEpsilonClosure() {
	for (int from = 0; from < epsRelations.length; from++) {
	    if (builtEpsClosure[from] != null)
		continue;
	    buildEpsilonClosure(from, 0);
	}
    }

    /**
     * Gets the depth of the top dependence of the origin
     * 
     * @param origin
     * @param visited
     * @param epsRelations
     * @param builtEpsExt
     * @return
     */
    private int buildEpsilonClosure(int origin, int depth) {
	if (enclosed.containsKey(origin)) {
	    if (builtEpsClosure[origin] != null) {
		System.out.println("Node " + origin
			+ " has been visited and included in the chart! "
			+ "It will be put on top of the stack.");
		toBuild.remove(Integer.valueOf(origin));
		toBuild.push(origin);
	    }
	    System.out
		    .println("Node " + origin
			    + " has been visited! Its depth is "
			    + enclosed.get(origin));
	    return enclosed.get(origin);
	}
	if (builtEpsClosure[origin] != null) {
	    System.out.print("Node " + origin
		    + " is already in the chart! It reaches to: ");
	    for (int i : builtEpsClosure[origin]) {
		enclosed.put(i, Integer.MAX_VALUE);
		System.out.print(i + ", ");
	    }
	    System.out.println();
	    toBuild.push(origin);
	    System.out.println("--continue-- Node " + origin
		    + " is pushed into the stack!");
	    return Integer.MAX_VALUE;
	}
	if (epsRelations[origin].size() == 1) {
	    System.out.println("Node " + origin
		    + " has no outgoing esp-closure! "
		    + "It is included in the chart and pushed to the stack!");
	    builtEpsClosure[origin] = epsRelations[origin];
	    enclosed.put(origin, Integer.MAX_VALUE);
	    toBuild.push(origin);
	    return Integer.MAX_VALUE;
	}
	System.out.println("The eps-closure of node " + origin
		+ " can not be determined right now. "
		+ "All of its children will be examined.");
	enclosed.put(origin, depth);
	toBuild.push(origin);
	int topDependence = depth;
	for (int i : epsRelations[origin]) {
	    if (origin == i)
		continue;
	    topDependence = Math.min(topDependence,
		    buildEpsilonClosure(i, depth + 1));
	}
	if (topDependence >= depth) {
	    System.out
		    .println("No dependence of children has higher level than node "
			    + origin
			    + " (depth: "
			    + depth
			    + ")! The eps-closure of it is being constructed!");
	    enclosed.put(origin, Integer.MAX_VALUE);
	    Set<Integer> epsClosure = new HashSet<Integer>();
	    epsClosure.add(origin);
	    while (toBuild.peek() != origin) {
		int next = toBuild.pop();
		if (builtEpsClosure[next] != null) {
		    System.out.println("The eps-closure of node " + origin
			    + " is unioned with that of the node " + next);
		    epsClosure.addAll(builtEpsClosure[next]);
		} else {
		    System.out.println("Node " + next
			    + " is part of the cycle that contains node "
			    + origin);
		    epsClosure.add(next);
		    builtEpsClosure[next] = epsClosure;
		}
	    }
	    builtEpsClosure[origin] = epsClosure;
	    return Integer.MAX_VALUE;
	}
	System.out.println("Node " + origin + " dependes on ancestor on level "
		+ topDependence);
	enclosed.put(origin, topDependence);
	return topDependence;
    }

}
