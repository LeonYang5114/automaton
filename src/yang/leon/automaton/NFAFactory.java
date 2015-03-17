package yang.leon.automaton;

import java.text.MessageFormat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class NFAFactory {

    private int numSymbols;
    private int numStates;
    private NFAState[] nfa;

    @SuppressWarnings("unchecked")
    public void make(boolean[][][] relations, boolean[] finalStates) {
	this.numSymbols = relations[0].length - 1;
	this.numStates = relations.length;
	if (finalStates.length != numStates)
	    throw new IllegalArgumentException(
		    "Number of states in relations and finalStates do not match.");

	nfa = new NFAState[numStates];

	/**
	 * Create an array of sets that stores the one-step epsilon extension
	 * which can be reused.
	 */
	Set<Integer>[] epsRelations = (HashSet<Integer>[]) new Set[numStates];
	for (int from = 0; from < numStates; from++) {
	    epsRelations[from] = new HashSet<Integer>();
	    for (int to = 0; to < numStates; to++) {
		if (relations[from][numSymbols + 1][to])
		    epsRelations[from].add(to);
	    }
	}

	EpsilonClosureBuilder builder = new EpsilonClosureBuilder(epsRelations);
	builder.buildEpsilonClosure();
	Set<Integer>[] epsExts = builder.getBuiltEpsClosure();

	/**
	 * Convert the epsilon extension from a set of indices to BitSet
	 */
	BitSet[] epsExtsInBits = new BitSet[numStates];
	for (int from = 0; from < numStates; from++) {
	    epsExtsInBits[from] = new BitSet(numStates);
	    for (int to : epsExts[from])
		epsExtsInBits[from].set(to);
	}

	for (int from = 0; from < numStates; from++) {
	    BitSet[] destinations = new BitSet[numSymbols];
	    for (int symb = 0; symb < numSymbols; symb++) {
		destinations[symb] = new BitSet(numStates);
		for (int to = 0; to < numStates; to++) {
		    if (relations[from][symb][to])
			destinations[symb].set(to);
		}
	    }

	    nfa[from] = new NFAState(destinations, epsExtsInBits[from],
		    finalStates[from], from);
	}
    }

    public int getNumSymbols() {
	return numSymbols;
    }

    public NFAState[] getNFA() {
	return nfa;
    }

    /**
     * post-epsilon-extension
     * 
     * @param origin
     * @param symbol
     * @return
     */
    public BitSet getDestination(int origin, int symbol) {
	BitSet extendedDest = new BitSet(numStates);
	BitSet nonExtDest = nfa[origin].getDestinations()[symbol];
	for (int from = 0; from < numStates; from++) {
	    if (nonExtDest.get(from))
		extendedDest.or(nfa[from].getEpsilonExt());
	}
	return extendedDest;
    }

    public BitSet getDestination(BitSet epsExtendedOrigin, int symbol) {
	BitSet extendedDest = new BitSet(numStates);
	for (int from = 0; from < numStates; from++) {
	    if (epsExtendedOrigin.get(from))
		extendedDest.or(nfa[from].getDestinations()[symbol]);
	}
	return extendedDest;
    }

    public BitSet getDestination(int origin, int[] string) {
	if (string.length == 0)
	    return nfa[origin].getEpsilonExt();
	BitSet dest = getDestination(origin, string[0]);
	for (int i = 1; i < string.length; i++) {
	    dest = getDestination(dest, string[i]);
	}
	return dest;
    }

    public boolean isFinalState(BitSet setState) {
	boolean isFinalState = false;
	for (int i = setState.nextSetBit(0); i >= 0; i = setState
		.nextSetBit(i + 1)) {
	    isFinalState |= nfa[i].isFinalState();
	}
	return isFinalState;
    }

    public String toString(BitSet setState) {
	String s = "";
	for (int i = setState.nextSetBit(0); i >= 0; i = setState
		.nextSetBit(i + 1)) {
	    s += ", " + nfa[i].getIndex();
	}
	if (s.length() > 1)
	    s = s.substring(2);
	return "{" + s + "}";
    }

    public String toString(int state) {
	MessageFormat sf = new MessageFormat("NFAState: {0}\n"
		+ "Is Final State: {1}\n" + "Destinations:\n{2}"
		+ "Epsilon Extention:\n{3}\n");
	String dest = "";
	for (int i = 0; i < numStates; i++) {
	    dest += (i + ": " + toString(nfa[state].getDestinations()[i]) + "\n");
	}
	String epsil = "";
	BitSet epsExt = nfa[state].getEpsilonExt();
	for (int i = epsExt.nextSetBit(0); i >= 0; i = epsExt.nextSetBit(i + 1)) {
	    epsil += ", " + nfa[i].getIndex();
	}
	epsil = "{" + epsil.substring(2) + "}";
	return sf.format(new Object[] { nfa[state].getIndex(),
		nfa[state].isFinalState(), dest, epsil });
    }

    @SuppressWarnings("unchecked")
    static void testBuildEpsClosure() {
	final int NUM_STATES = 30;

	Set<Integer>[] epsRelations = (HashSet<Integer>[]) new HashSet[NUM_STATES];

	System.out.println("Relations of epsilon extension (one step): ");
	for (int i = 0; i < NUM_STATES; i++) {
	    epsRelations[i] = new HashSet<Integer>();
	    epsRelations[i].add(i);
	    System.out.print("Node " + i + "=> " + i + ", ");
	    int numEps = 1;
	    for (int j = 0; j < numEps; j++) {
		int add = (int) (Math.random() * NUM_STATES);
		if (epsRelations[i].add(add)) {
		    System.out.print(add + ", ");
		}
	    }
	    System.out.println();
	}
	System.out.println();

	EpsilonClosureBuilder builder = new EpsilonClosureBuilder(epsRelations);
	long duration = System.currentTimeMillis();
	builder.buildEpsilonClosure();
	Set<Integer>[] epsExts = builder.getBuiltEpsClosure();
	duration = System.currentTimeMillis() - duration;
	System.out.println("It takes the computer " + duration
		+ " milliseconds to compute " + NUM_STATES
		+ " nodes's eps-closure\n");

	System.out.println("linear epsilon extension case:");
	for (int i = 0; i < NUM_STATES; i++) {
	    System.out.print("the eps-closure of " + i + " is: ");
	    for (int j : epsExts[i]) {
		System.out.print(j + ", ");
	    }
	    System.out.println();
	}
	System.out.println("*********************************");
    }
}
