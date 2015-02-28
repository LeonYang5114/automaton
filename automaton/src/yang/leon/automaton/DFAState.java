package yang.leon.automaton;

import java.text.MessageFormat;

public class DFAState implements Comparable<DFAState> {

    private DFAState[] destinations;
    private boolean isFinalState;
    private int index;

    public DFAState(DFAState[] destinations, boolean isFinalState, int index) {
	this.destinations = destinations;
	this.isFinalState = isFinalState;
	this.index = index;
    }

    public void setDestination(int symbol, DFAState destination) {
	destinations[symbol] = destination;
    }

    public DFAState getDestination(int symbol) {
	return destinations[symbol];
    }

    public DFAState getDestination(int[] string) {
	if (string.length == 0)
	    return this;

	DFAState destination = getDestination(string[0]);
	for (int i = 1; i < string.length; i++) {
	    destination = destination.getDestination(string[i]);
	}
	return destination;
    }

    public boolean isFinalState() {
	return isFinalState;
    }

    public int getIndex() {
	return index;
    }

    public String toString() {
	MessageFormat sf = new MessageFormat("DFAState: {0}\n"
		+ "Is Final State: {1}\n" + "Destinations:\n{2}");
	String dest = "";
	for (int i = 0; i < destinations.length; i++) {
	    dest += (i + ": " + destinations[i].getIndex() + "\n");
	}
	return sf.format(new Object[] { getIndex(), isFinalState(), dest });
    }

    @Override
    public int compareTo(DFAState anotherState) {
	return getIndex() - anotherState.getIndex();
    }

}
