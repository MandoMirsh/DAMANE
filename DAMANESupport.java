package agentTest;

import java.util.ArrayList;
import java.util.Scanner;

import jade.core.AID;

public class DAMANESupport {

//public static String()
public static  ArrayList<String> lustrateMas(String[] mas, int noread){
	ArrayList<String> ret2 = lustrateMas(mas),
						ret = new ArrayList<>();
	for (int i = noread;i<ret2.size();i++) {
		ret.add(ret2.get(i));
	}
	return ret;
}
public static ArrayList<String> lustrateMas(String[] mas){
	ArrayList<String> ret = new ArrayList<>();
	for (String s: mas) {
		if ((s!=null) & (s!="")) //seems there will be no nulls, but. . .
			ret.add(s);
	}
	return ret;
}
public void printReport(AID id, String msg) {
	System.out.println(id.getLocalName() + ": "+ msg);
}
private String generateName(int prNum, String specify, int agNum) {
	return ("pr"+prNum+specify+agNum);
}
public String genResName(int projNum,int agNum, String specify) {
	return (generateName(projNum,"Res"+specify,agNum));
}
public String genJobName(int projNum,int agNum) {
	return generateName(projNum, "Job",agNum);
}
public String getLineWithSkip(Scanner from, int skipping) {
	String ret = "";
	for (int i = 0; i <skipping+1;i++)
		{
			if(from.hasNextLine())
			ret = from.nextLine();
		}
	return ret; 
}
public ArrayList<String> getTopFirst(ArrayList<String> lst){
	ArrayList<String> ret = new ArrayList<>();
	if (lst.size() == 0) return ret;
	else
		ret.add(lst.get(0));
	return ret;
}

public ArrayList<String> getAfterTopList(ArrayList<String> lst){
	ArrayList<String> ret = new ArrayList<>();
	if (lst.size()<2)
		return ret;
	else
		for (int i = 2;i<lst.size();i++)
			ret.add(lst.get(i));
	return ret;
}
}