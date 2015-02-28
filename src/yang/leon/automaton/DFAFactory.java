package yang.leon.automaton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DFAFactory {

    private DFAState[] dfa;

    public void make(int[][] relations, boolean[] finalStates) {
	int numStates = relations.length;
	int numSymbols = relations[0].length;
	if (relations.length != finalStates.length)
	    throw new IllegalArgumentException(
		    "Number of states in relations and finalStates do not match.");

	dfa = new DFAState[numStates];
	for (int i = 0; i < numStates; i++) {
	    dfa[i] = new DFAState(new DFAState[numStates], finalStates[i], i);
	}

	for (int from = 0; from < numStates; from++) {
	    for (int symbol = 0; symbol < numSymbols; symbol++) {
		dfa[from].setDestination(symbol, dfa[relations[from][symbol]]);
	    }
	}
    }

    public void makeFromNFA(NFAFactory nfaFactory) {
	int numSymbols = nfaFactory.getNumSymbols();
	Map<Integer, DFAState> nfa2dfaMap = new HashMap<Integer, DFAState>();
	buildDFA(nfaFactory.getNFA()[0].getEpsilonClosure(null), nfa2dfaMap,
		numSymbols);
	dfa = new DFAState[nfa2dfaMap.size()];
	nfa2dfaMap.values().toArray(dfa);
    }

    private DFAState buildDFA(Set<NFAState> nfaStateSet,
	    Map<Integer, DFAState> nfa2dfaMap, int numSymbols) {
	int nfaIndex = NFAState.getIndex(nfaStateSet, numSymbols);
	DFAState state = nfa2dfaMap.get(nfaIndex);

	if (state != null)
	    return state;

	state = new DFAState(new DFAState[numSymbols],
		NFAState.isFinalState(nfaStateSet), nfa2dfaMap.size());
	nfa2dfaMap.put(nfaIndex, state);

	for (int symbol = 0; symbol < numSymbols; symbol++) {
	    state.setDestination(
		    symbol,
		    buildDFA(NFAState.getDestination(nfaStateSet, symbol),
			    nfa2dfaMap, numSymbols));
	}
	return state;
    }

    public DFAState[] getDFA() {
	return dfa;
    }
}
