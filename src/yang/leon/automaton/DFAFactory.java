package yang.leon.automaton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
	Arrays.sort(dfa);
    }

    public void makeFromNFA(NFAFactory nfaFactory) {
	int numSymbols = nfaFactory.getNumSymbols();
	Map<Integer, DFAState> nfa2dfaMap = new HashMap<Integer, DFAState>();
	buildDFA(nfaFactory.getNFA()[0].getEpsilonClosure(null), nfa2dfaMap,
		numSymbols);
	dfa = new DFAState[nfa2dfaMap.size()];
	nfa2dfaMap.values().toArray(dfa);
	Arrays.sort(dfa);
    }

    private static DFAState buildDFA(Set<NFAState> nfaStateSet,
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

    private static int DISTINCT = -1;
    private static int NONDISTINCT = 1;
    private static int INITIAL = 0;
    private static int PENDING = 2;

    public void simplifyDFA() {
	if (dfa.length <= 1)
	    return;
	int[] mergeTable = new int[dfa.length * (dfa.length - 1) / 2];
	Stack<Integer> merge = new Stack<Integer>();
	for (int j = 1; j < dfa.length; j++) {
	    for (int i = 0; i < j; i++)
		if (mergeTable[getMergeTableIndex(i, j)] == INITIAL) {
		    int distinctCode = buildMergedStack(dfa[i], dfa[j],
			    mergeTable, merge, dfa.length);
		    while (!merge.isEmpty())
			mergeTable[merge.pop()] = (distinctCode == DISTINCT) ? DISTINCT
				: NONDISTINCT;
		}
	}

	DFAState[] mergedStates = new DFAState[dfa.length];
	int firstDistinct = Integer.MAX_VALUE;
	int i = 0;
	int mergedCount = 0;
	while (i < dfa.length) {
	    firstDistinct = i + 1;
	    boolean isFinalState = dfa[i].isFinalState();
	    DFAState merged = new DFAState(
		    new DFAState[dfa[0].getNumSymbols()], false, mergedCount);
	    mergedStates[i] = merged;
	    boolean hasMetFirstDistinct = false;
	    for (int j = i + 1; j < dfa.length; j++) {
		int index = getMergeTableIndex(i, j);
		if (mergeTable[index] == NONDISTINCT) {
		    isFinalState |= dfa[j].isFinalState();
		    mergedStates[j] = merged;
		} else if (!hasMetFirstDistinct) {
		    firstDistinct = j;
		    hasMetFirstDistinct = true;
		}
	    }
	    merged.setIsFinalState(isFinalState);
	    mergedCount++;
	    i = firstDistinct;
	}

	DFAState[] visitedMerged = new DFAState[mergedCount];
	for (int j = 0; j < dfa.length; j++) {
	    if (visitedMerged[mergedStates[j].getIndex()] != null)
		continue;
	    visitedMerged[mergedStates[j].getIndex()] = mergedStates[j];
	    for (int symbol = 0; symbol < dfa[0].getNumSymbols(); symbol++) {
		mergedStates[j].setDestination(
			symbol,
			getMergedDestination(mergedStates,
				mergedStates[j].getIndex(), symbol, j,
				new boolean[dfa.length]));
	    }
	}
	dfa = visitedMerged;
    }

    private static int buildMergedStack(DFAState low, DFAState high,
	    int[] mergeTable, Stack<Integer> merge, int numStates) {
	int index = getMergeTableIndex(low.getIndex(), high.getIndex());

	if (mergeTable[index] != INITIAL)
	    return mergeTable[index];

	if (low.isFinalState() != high.isFinalState())
	    return mergeTable[index] = DISTINCT;

	int distinctCode = PENDING;
	merge.push(new Integer(index));
	boolean isNonDistinct = true;
	for (int i = 0; i < low.getNumSymbols() && distinctCode != DISTINCT; i++) {
	    DFAState lowDest = low.getDestination(i);
	    DFAState highDest = high.getDestination(i);
	    int code = 0;
	    if (lowDest.compareTo(highDest) == 0) {
		continue;
	    } else if (lowDest.compareTo(highDest) < 0) {
		code = buildMergedStack(lowDest, highDest, mergeTable, merge,
			numStates);
	    } else {
		code = buildMergedStack(highDest, lowDest, mergeTable, merge,
			numStates);
	    }
	    isNonDistinct &= (code == NONDISTINCT);
	    if (code == DISTINCT)
		distinctCode = DISTINCT;
	}
	if (isNonDistinct) {
	    mergeTable[merge.pop()] = distinctCode = NONDISTINCT;
	}
	return distinctCode;
    }

    private static int getMergeTableIndex(int low, int high) {
	return (high - 1) * high / 2 + low;
    }

    private DFAState getMergedDestination(DFAState[] mergedStates,
	    int mergedIndex, int symbol, int visiting, boolean[] visited) {
	if (visited[visiting])
	    return mergedStates[visiting];
	if (mergedStates[visiting].getIndex() == mergedIndex) {
	    visited[visiting] = true;
	    int next = dfa[visiting].getDestination(symbol).getIndex();
	    return getMergedDestination(mergedStates, mergedIndex, symbol,
		    next, visited);
	}
	return mergedStates[visiting];

    }

    public DFAState[] getDFA() {
	return dfa;
    }
}
