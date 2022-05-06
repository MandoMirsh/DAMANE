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
	//добавить поиск по имени
	int findPos(String name) {
		for (int i=0;i<resourses.size();i++)
			if (resourses.get(i).getName().equals(name))
				return i;
		return -1;
	}
	//добавить обнуление всех флагов
	public void  resetAll() {
		for (int i=0;i<resourses.size();i++)
			{
			    ResourceDescriptor tmp = resourses.get(i);
			    tmp.removeComplete();
				resourses.set(i, tmp);
			}
	}
	//добавить инициализацию по массиву значений
	
	//добавить апдейт количества дней
	public void setDays(int N) {
		for (int i=0;i<resourses.size();i++)
		{
		    ResourceDescriptor tmp = resourses.get(i);
		    tmp.setDate(N);
			resourses.set(i, tmp);
		}
	}
}
