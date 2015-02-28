package yang.leon.automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFAFactory {

    private int numSymbols;
    private NFAState[] nfa;

    @SuppressWarnings("unchecked")
    public void make(boolean[][][] relations, boolean[] finalStates) {
	this.numSymbols = relations[0].length - 1;
	int numStates = relations.length;
	if (finalStates.length != numStates)
	    throw new IllegalArgumentException(
		    "Number of states in relations and finalStates do not match.");

	nfa = new NFAState[numStates];

	for (int from = 0; from < numStates; from++) {
	    nfa[from] = new NFAState((Set<NFAState>[]) new Set[numSymbols],
		    null, finalStates[from], from);
	}

	Map<Integer, Set<NFAState>> setStateTable = new HashMap<Integer, Set<NFAState>>();
	for (int from = 0; from < numStates; from++) {
	    for (int symbol = 0; symbol < numSymbols; symbol++) {
		int index = NFAState.getIndex(relations[from][symbol]);
		Set<NFAState> content = setStateTable.get(index);
		if (content == null) {
		    content = new HashSet<NFAState>();
		    for (int to = 0; to < numStates; to++) {
			if (relations[from][symbol][to])
			    content.add(nfa[to]);
		    }
		    setStateTable.put(index, content);
		}
		nfa[from].setDestination(symbol, content);
	    }

	    /**
	     * Epsilon extension construction.
	     */
	    relations[from][numSymbols][from] = true;
	    int index = NFAState.getIndex(relations[from][numSymbols]);
	    Set<NFAState> epsilonExt = setStateTable.get(index);
	    if (epsilonExt == null) {
		epsilonExt = new HashSet<NFAState>();
		for (int to = 0; to < numStates; to++) {
		    if (relations[from][numSymbols][to])
			epsilonExt.add(nfa[to]);
		}
		setStateTable.put(index, epsilonExt);
	    }
	    nfa[from].setEpsilonExt(epsilonExt);
	}
    }

    public int getNumSymbols() {
	return numSymbols;
    }

    public NFAState[] getNFA() {
	return nfa;
    }
}
