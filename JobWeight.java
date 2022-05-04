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
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass()!= obj.getClass())
			return false;
		JobWeight other = (JobWeight) obj;
		ArrayList<Integer> i = other.getWeights();
		if (i.get(0)!=N1)
			return false;
		if (i.get(1)!=N2)
			return false;
		return true;
	}
	
	
	public ArrayList<Integer> getWeights(){
		ArrayList<Integer> ret = new ArrayList<>();
		ret.add(N1);
		ret.add(N2);
		return ret;
	}

}
