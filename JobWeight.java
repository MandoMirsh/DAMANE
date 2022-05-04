package agentTest;

import java.util.ArrayList;

public class JobWeight {
	private String jobName;
	private Integer N1, N2;
	public void setWeights(int a, int b) {
		if (a>b) {
			N1 = a;
			N2 = b;
		}
		else
		{
			N1 = b;
			N2 = a;
		}
	}
	JobWeight(String name, Integer a, Integer b){
		setWeights(a,b);
	}
	JobWeight(Integer a, Integer b){
		setWeights(a,b);
	}
	
	public String getName() {
		return jobName;
	}
	
	public ArrayList<Integer> getWeights(){
		ArrayList<Integer> ret = new ArrayList<>();
		ret.add(N1);
		ret.add(N2);
		return ret;
	}

}
