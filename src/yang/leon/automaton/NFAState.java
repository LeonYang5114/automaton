package yang.leon.automaton;

import java.text.MessageFormat;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is a 
 * @author Leon Yang
 *
 */
public class NFAState implements Comparable<NFAState> {

    private boolean isFinalState;
    private BitSet[] destinations;
    private BitSet epsilonExt;
    private int index;

    public NFAState(BitSet[] destinations, BitSet epsilonExt,
	    boolean isFinalState, int index) {
	this.destinations = destinations;
	this.epsilonExt = epsilonExt;
	this.isFinalState = isFinalState;
	this.index = index;
    }

    public BitSet[] getDestinations() {
	return destinations;
    }

    public BitSet getEpsilonExt() {
	return epsilonExt;
    }

    public int getIndex() {
	return index;
    }

    public int getNumSymbols() {
	return destinations.length;
    }

    public boolean isFinalState() {
	return isFinalState;
    }

    @Override
    public int compareTo(NFAState o) {
	return getIndex() - o.getIndex();
    }
}
