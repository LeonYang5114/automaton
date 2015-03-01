package yang.leon.automaton;

import java.util.Arrays;

public class Demo {

    public static void main(String[] args) {
	boolean[][][] test = new boolean[9][3][9];
	test[0][0][0] = true;
	test[0][1][0] = true;
	test[0][1][1] = true;
	test[1][0][6] = true;
	test[1][1][2] = true;
	test[1][1][7] = true;
	test[2][0][3] = true;
	test[2][1][4] = true;
	test[3][1][4] = true;
	test[4][1][5] = true;
	test[5][0][5] = true;
	test[5][1][5] = true;
	test[6][1][3] = true;
	test[7][1][8] = true;
	test[8][0][4] = true;
	boolean[] finalStates = new boolean[9];
	finalStates[5] = true;
	NFAFactory nfaFactory = new NFAFactory();
	DFAFactory dfaFactory = new DFAFactory();
	nfaFactory.make(test, finalStates);
	NFAState[] nfa = nfaFactory.getNFA();
	dfaFactory.makeFromNFA(nfaFactory);
	dfaFactory.simplifyDFA();
	DFAState[] dfa = dfaFactory.getDFA();
	int[] s = new int[] { 0, 1, 1, 0, 1, 0 };
	System.out.println(NFAState.isFinalState(nfa[0].getDestination(s)));
	System.out.println();
	System.out.println(dfa[0].getDestination(s).isFinalState());
	System.out.println();
	for (DFAState state : dfa)
	    System.out.println(state);
    }
}
