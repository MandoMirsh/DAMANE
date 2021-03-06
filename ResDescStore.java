package agentTest;

import java.util.ArrayList;

public class ResDescStore {
	private ArrayList<ResourceDescriptor> resourses = new ArrayList<>();
	
	public boolean allEqual() {
		int i2 = resourses.size(), toCompare = resourses.get(0).getDate();
		
		if (i2<2)
			return true;
		for (int i=1;i<resourses.size();i++) {
			if (toCompare != resourses.get(i).getDate())
				return false;
		}
		return true;
	}
	public boolean allSet() {
		for (int i = 0;i<resourses.size();i++)
			if (!resourses.get(i).complete())
				return false;
		return true;
	}
	public void add(ResourceDescriptor a) {
		resourses.add(a);
	}
	public int size() {
		return resourses.size();
	}
	public ResourceDescriptor get(int i) {
		return resourses.get(i);
	}
	public void set(int i, ResourceDescriptor e) {
		resourses.set(i, e);
	}
	//???????? ????? ?? ?????
	int findPos(String name) {
		for (int i=0;i<resourses.size();i++)
			if (resourses.get(i).getName().equals(name))
				return i;
		return -1;
	}
	/**reset all readiness flags
	 */
	public void  resetAll() {
		for (int i=0;i<resourses.size();i++)
			{
			    ResourceDescriptor tmp = resourses.get(i);
			    tmp.removeComplete();
				resourses.set(i, tmp);
			}
	}
	//reinitialization
	//TODO: add array initialization
	
	//days update
	public void setDays(int N) {
		for (int i=0;i<resourses.size();i++)
		{
		    ResourceDescriptor tmp = resourses.get(i);
		    tmp.setDate(N);
			resourses.set(i, tmp);
		}
	}
	public void setUp(String name) {
		int i = findPos(name);
		if (i>=0)
		{
			ResourceDescriptor tmp = resourses.get(i);
			tmp.setComplete();
			resourses.set(i, tmp);
		}
	}
	public int maxDate() {
		switch (resourses.size()) {
		case 0: return 0;
		case 1: return resourses.get(0).getDate();
		default: 
			int max = resourses.get(0).getDate(); 
			for (int i = 1; i < resourses.size();i++) {
				int newmax = resourses.get(i).getDate();
				if (newmax > max)
					max = newmax;
			}
			return max;
		}
	}
	public void setDate(String name, Integer date) {
		int i = findPos(name);
		if (i>=0) {
			ResourceDescriptor tmp = resourses.get(i);
			tmp.setDate(date);
			resourses.set(i, tmp);
		}
	}
}
