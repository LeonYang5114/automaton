package yang.leon.automaton;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NFAState implements Comparable<NFAState> {

    private boolean isFinalState;
    private Set<NFAState>[] destinations;
    private Set<NFAState> epsilonExt;
    private int index;

    public NFAState(Set<NFAState>[] destinations, Set<NFAState> epsilonExt,
	    boolean isFinalState, int index) {
	this.destinations = destinations;
	setEpsilonExt(epsilonExt);
	this.isFinalState = isFinalState;
	this.index = index;
    }

    public void setDestination(int symbol, Set<NFAState> dest) {
	destinations[symbol] = dest;
    }

    public static Set<NFAState> getDestination(Set<NFAState> setState,
	    int symbol) {
	Iterator<NFAState> itr = setState.iterator();
	HashSet<NFAState> origins = new HashSet<NFAState>();
	while (itr.hasNext()) {
	    NFAState next = itr.next();
	    next.getEpsilonClosure(origins);
	}
	HashSet<NFAState> contentUnion = new HashSet<NFAState>();
	itr = origins.iterator();
	while (itr.hasNext()) {
	    NFAState next = itr.next();
	    contentUnion.addAll(next.getDestination(symbol));
	}
	return NFAState.getEpsilonClosure(contentUnion, null);
    }

    public Set<NFAState> getDestination(int symbol) {
	return destinations[symbol];
    }

    public Set<NFAState> getDestination(int[] string) {
	if (string.length == 0)
	    return getEpsilonClosure(null);

	Set<NFAState> dest = getDestination(string[0]);
	for (int i = 1; i < string.length; i++) {
	    dest = getDestination(dest, string[i]);
	}
	return dest;
    }

    public void setEpsilonExt(Set<NFAState> epsilonExt) {
	this.epsilonExt = epsilonExt;
    }

    public static Set<NFAState> getEpsilonClosure(Set<NFAState> setState,
	    Set<NFAState> visited) {
	Iterator<NFAState> itr = setState.iterator();
	if (visited == null)
	    visited = new HashSet<NFAState>();
	while (itr.hasNext()) {
	    itr.next().getEpsilonClosure(visited);
	}
	return visited;
    }

    public Set<NFAState> getEpsilonClosure(Set<NFAState> visited) {
	if (visited == null) {
	    (visited = new HashSet<NFAState>()).addAll(epsilonExt);
	    return visited;
	}

	if (!visited.add(this) || epsilonExt.size() == 1)
	    return visited;

	Iterator<NFAState> itr = epsilonExt.iterator();
	while (itr.hasNext()) {
	    NFAState next = itr.next();
	    if (!next.equals(this))
		next.getEpsilonClosure(visited);
	}
	return visited;
    }

    public static int getIndex(Set<NFAState> setState, int numStates) {
	int index = 0;
	for (NFAState us : setState) {
	    index += Math.pow(2, us.getIndex());
	}
	return index;
    }

    public static int getIndex(boolean[] setState) {
	int index = 0;
	int radix = setState.length;
	for (int i = 0; i < radix; i++) {
	    if (setState[i])
		index += Math.pow(2, i);
	}
	return index;
    }

    public int getIndex() {
	return index;
    }

    public int getNumSymbols() {
	return destinations.length;
    }

    public static boolean isFinalState(Set<NFAState> setState) {
	boolean isFinalState = false;
	Iterator<NFAState> itr = setState.iterator();
	while (itr.hasNext()) {
	    isFinalState |= itr.next().isFinalState();
	}
	return isFinalState;
    }

    public boolean isFinalState() {
	return isFinalState;
    }

    public static String toString(Set<NFAState> setState) {
	String s = "";
	for (NFAState state : setState) {
	    s += ", " + state.getIndex();
	}
	if (s.length() > 1)
	    s = s.substring(2);
	return "{" + s + "}";
    }

    public String toString() {
	MessageFormat sf = new MessageFormat("NFAState: {0}\n"
		+ "Is Final State: {1}\n" + "Destinations:\n{2}"
		+ "Epsilon Extention:\n{3}\n");
	String dest = "";
	for (int i = 0; i < destinations.length; i++) {
	    dest += (i + ": " + NFAState.toString(destinations[i]) + "\n");
	}
	String epsil = "";
	for (NFAState us : epsilonExt) {
	    epsil += ", " + us.index;
	}
	epsil = "{" + epsil.substring(2) + "}";
	return sf
		.format(new Object[] { getIndex(), isFinalState(), dest, epsil });
    }

    @Override
    public int compareTo(NFAState o) {
	return getIndex() - o.getIndex();
    }
}
