package agentTest;

import java.util.ArrayList;

public class WeightsList {

	ArrayList<JobWeight> jobs = new ArrayList<>();
	
	public void sort() {
		jobs.sort(new JobWeightComparator());
	}
	public ArrayList<String> toStrings(){
		ArrayList<String> ret = new ArrayList<>();
		for (int i =0;i<jobs.size();i++) {
			ret.add(jobs.get(i).getName());
		}
		return ret;
	}
}
