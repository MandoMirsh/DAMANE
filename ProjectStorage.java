package agentTest;

import java.util.ArrayList;

public class ProjectStorage {
	private ArrayList<ProjectDesc> projects = new ArrayList<>();
	
	public Integer getSize() {
		return projects.size();
	}
	public ProjectDesc get(Integer i) {
		return projects.get(i);
	}
	public Integer searchByAgent(String pattern) {
		int i2 = projects.size();
		for (int i = 0; i <i2;i++) {
			if (projects.get(i).getName().equals(pattern))
				return i;
		}
		return -1;
	}
	public void add(ProjectDesc newOne) {
		projects.add(newOne);
	}
	public void changeProjectAt(Integer position,ProjectDesc replacement) {
		projects.set(position, replacement);
	}
}