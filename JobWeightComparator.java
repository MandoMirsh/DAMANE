package agentTest;

import java.util.ArrayList;
import java.util.Comparator;

public class JobWeightComparator implements Comparator<JobWeight> {
	@Override
	public int compare(JobWeight Ob1, JobWeight Ob2) {
		ArrayList<Integer> A1,A2;
		A1 = Ob1.getWeights();
		A2 = Ob2.getWeights();
		return (A2.get(0)*A1.get(1) - A1.get(0)*A2.get(1)); // we escape division. 
	}
}
