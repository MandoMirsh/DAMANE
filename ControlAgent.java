package agentTest;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.awt.Container;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import jade.core.behaviours.*;
import jade.lang.acl.*;
public class ControlAgent extends Agent{

	Integer tardcost, horizon, resNum;
	Integer projNum, jobNum, relDate, dueDate, tardCost, nPMTime;
	String resNames = "", resAvals = "", resAgentClass = "agentTest.ResourceAgent",
			transmitterAgent = "agentTest.TransmitterAgent", jobAgentClass = "agentTest.TaskAgentRewrite";
	
	private ArrayList<String> jobsParams = new ArrayList<>();
	private void PSPLibParse(File f, ArrayList<String> jobsParams) throws FileNotFoundException {
		try {
			String[] items;
			Scanner scanner = new Scanner(f);
			
			String line = scanner.nextLine();
			// имя базового файла.
			line = scanner.nextLine();
				//System.out.println(line);
			// сид рандома
			line = scanner.nextLine();
				//System.out.println(line);
			line = scanner.nextLine();
			//число проектов
			line = scanner.nextLine();
			//число работ
			line = scanner.nextLine();
			//горизонт планирования
			line = scanner.nextLine();
			int projectHorizon = readNumFromLine(line,25);
			//System.out.println(projectHorizon); // 158
			line = scanner.nextLine();
			//количество возобновляемых ресурсов
			line = scanner.nextLine();
			int renewNum = readNumFromLine(line,22);
			//System.out.println(renewNum); // 4
			//количество невозобновляемых ресурсов
			line = scanner.nextLine();	
			int nonRenewNum = readNumFromLine(line,19);
			//System.out.println(nonRenewNum); // 0
			
			// количество ресурсов с двойными огранчениями
			line = scanner.nextLine();
			int doubleConsRes = readNumFromLine(line,14);
			//System.out.println(doubleConsRes); // 0
			
			//скипаем следующие не несущие информации для машины элементы.
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			//project properties: projectnum, 
			//инфа проекта: номер проекта 4, количество работ 9, дата 15, дата до которой следует отработать 22, тардкост 30 , время мпм(?) 37
			line = scanner.nextLine();
			//ArrayList<String> larr = lustrateMas(line.split(" "));
			//projNum = Integer.parseInt(larr.get(0)); jobNum = Integer.parseInt(larr.get(1)); relDate = Integer.parseInt(larr.get(2)); 
			//dueDate = Integer.parseInt(larr.get(3)); tardCost = Integer.parseInt(larr.get(4)); nPMTime = Integer.parseInt(larr.get(5));	
			// System.out.println("Proj: " + projNum + " " + jobNum + " " + relDate + " " + dueDate + " " + tardCost + " " + nPMTime); // 1 30 0 38 26 38
			/*int il = 0;
			for (String l: items) {
				System.out.println(il++);
				System.out.println(l);
			}*/
			 
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			
			//отношения прецедентности: номер работы, режим, количество преемников, преемники. 
			//TODO: циклом с 0 по N+1,где N - количество элементов в проекте.
			printReport("Starting to read precedence");
			for (int i = 0; i < jobNum+2;i++)
			{
				line = scanner.nextLine();
				//printReport("Read line "+ i +". Processing. . .");
				//processing:	
				jobsParams.add(arrToString(lustrateMas(line.split(" ")).toArray()));
				//printReport(jobsParams.get(i));
			}
			
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			//jobnum,mode,time,res1,res2,res3,res4  
			//номер работы, режим, длительность, первый_ресурс, второй_ресурс, третий_ресурс, четвёртый_ресурс
			//TODO:циклом с 0 по N+1, где N - количество элементов в проекте.
			for (int i = 0; i < jobNum+2;i++)
			{
				line = scanner.nextLine();
				//printReport(arrToString(lustrateMas(line.split(" "), 1).toArray()));
				String st = jobsParams.get(i),
						nline = arrToString(lustrateMas(line.split(" "), 1).toArray());
				//processing 
				jobsParams.set(i, uniteStrings(st,nline));
				printReport(jobsParams.get(i));
			}
			line = scanner.nextLine();

			line = scanner.nextLine();
			
			//resnames
			line = scanner.nextLine();
			String resNames = arrToString(lustrateMas(line.split(" ")).toArray());
			printReport(resNames);
			//resavaliability
			line = scanner.nextLine();
			String resAvals = arrToString(lustrateMas(line.split(" ")).toArray());
			printReport(resAvals);
			//last line of file.
			line = scanner.nextLine();
			scanner.close();
			//return 0;
		} catch (FileNotFoundException e) {
			//unbelievable scenario, except for someone managed to delete file beforehand.
			System.out.println("Не получилось открыть выбранный файл. Вероятно, кто-то умудрился его удалить. Проверьте его наличие и доступность процессу.");
			e.printStackTrace() ;
			//return -1;
		}
	}
	private ArrayList<String> lustrateMas(String[] mas, int noread){
		ArrayList<String> ret2 = lustrateMas(mas),
							ret = new ArrayList<>();
		for (int i = noread;i<ret2.size();i++) {
			ret.add(ret2.get(i));
		}
		return ret;
	}
	
	private ArrayList<String> lustrateMas(String[] mas){
		ArrayList<String> ret = new ArrayList<>();
		for (String s: mas) {
			if ((s!=null) & (s!="")) //seems there will be no nulls, but. . .
				ret.add(s);
		}
		return ret;
	}
	private String uniteStrings(String s1, String s2) {
		return (s1 + " " + s2);
	}
	private String arrToString(Object[] sArr) {
		String s = sArr[0].toString();;
		for (int i = 1;i < sArr.length; i++) {
			s = uniteStrings(s,sArr[i].toString());
		}
		return s;
	}
	
	private void sendMes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	
	private int readNumFromLine(String s, int p, int failnum) {
		String[] items = s.split(" ");
		int ret;
		try{
			ret = Integer.parseInt(items[p]);
		}
		catch (NumberFormatException e) {
			ret = failnum;
		}
		return ret;
	}
	private int readNumFromLine(String s, int p) {
		return readNumFromLine(s,p,-1);
	}
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	private String generateName(int prNum, String specify, int agNum) {
		return ("pr"+prNum+specify+agNum);
	}
	private String genResName(int agNum, String specify) {
		return (generateName(projNum,"Res"+specify,agNum));
	}
	private String genJobName(int agNum) {
		return generateName(projNum, "Job",agNum);
	}
	private String getLineWithSkip(Scanner from, int skipping) {
		String ret = "";
		for (int i = 0; i <skipping+1;i++)
			{
				if(from.hasNextLine())
				ret = from.nextLine();
			}
		return ret; 
	}
	//раздвоиться
	//I.1 - прородить агенты ресурсов
	//gets ResName, ResVolume, PlanningHorizon
	//породить агент нулевой работы
	//fistnum secondnum FIRSTS SECONDS
	//II.1 - породить агенты работ
	//TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES 
	//породить агент конечной работы
	//I,II.2 - передать агентам параметры работ
	
	
	@Override
	public void setup() {
		JFileChooser fileopen = new JFileChooser();
		int ret = fileopen.showDialog(null, "Открыть файл");
		switch (ret) {
		case (JFileChooser.APPROVE_OPTION): {
		    //File file = fileopen.getSelectedFile();
			System.out.println("Файл открыт");
			/*try {
				readPSPLibProj(fileopen.getSelectedFile(),jobsParams);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			printReport("Starting to read files");
			for (String s:jobsParams) {
				printReport(s);
			}*/
			/*fileopen.getSelectedFile() - выбранный файл*/
			/*FileReader c = new FileReader(fileopen.getSelectedFile());
			char [] s = new char[20];
			System.out.println(c.read(s));
		/* Попытка грохнуть всю жаду (чёт не работает, впрочем):
		 * try {
			this.getContainerController().getPlatformController().kill(); 
		}
		catch (final ControllerException e) {
			System.out.println("Failed to end simulation.");
		}*/  try {
			String[] items;
			Scanner scanner = new Scanner(fileopen.getSelectedFile());
			String line = getLineWithSkip(scanner,6);//*,basedata (4),random seed (4),*,projects (2), jobnumWithMock (3), horizon (2)
			horizon = Integer.parseInt(lustrateMas(line.split(" ")).get(2));//158
			line = getLineWithSkip(scanner,1);//RESOURSE, renewNum (3)
			resNum = Integer.parseInt(lustrateMas(line.split(" ")).get(3));
			line = getLineWithSkip(scanner,5);//nonRenewNum (3),doublyConstr (3),*,PROJECTINFO,proinfo, projinfo: jobNum (1) reldate (2) dueDate (3) tardCost (4) MPM-Time (Multi-process mode possibly, 5)
			ArrayList<String> larr = lustrateMas(line.split(" "));
			projNum = Integer.parseInt(larr.get(0)); //this one is unnecessary and must be gotten from arguments as, in fact, name of file.
			jobNum = Integer.parseInt(larr.get(1)); relDate = Integer.parseInt(larr.get(2)); 
			
			dueDate = Integer.parseInt(larr.get(3)); tardCost = Integer.parseInt(larr.get(4)); nPMTime = Integer.parseInt(larr.get(5));	
			//Proj: 1 30 0 38 26 38	
			System.out.println("Proj: " + projNum + " " + jobNum + " " + relDate + " " + dueDate + " " + tardCost + " " + nPMTime);
			line = getLineWithSkip(scanner,2);// *,PRECEDENCE RELATIONS, precedence_headlines
			printReport("Starting to read precedence");
			for (int i = 0; i < jobNum+2;i++)
			{
				line = scanner.nextLine();
				//printReport("Read line "+ i +". Processing. . .");
				//processing:	
				jobsParams.add(arrToString(lustrateMas(line.split(" ")).toArray()));
				//printReport(jobsParams.get(i));
			}
			line = getLineWithSkip(scanner, 3);//*, REQUESTS/DURATIONS, request_headlines, _
			for (int i = 0; i < jobNum+2;i++)
			{
				line = scanner.nextLine();
				//printReport(arrToString(lustrateMas(line.split(" "), 1).toArray()));
				String st = jobsParams.get(i),
						nline = arrToString(lustrateMas(line.split(" "), 1).toArray());
				//обработка 
				jobsParams.set(i, uniteStrings(st,nline));
				printReport(jobsParams.get(i));
			}
			line = getLineWithSkip(scanner,2);//*,RESOURCEAVALIABILITIES,ResNames
			resNames = arrToString(lustrateMas(line.split(" ")).toArray());
			printReport(resNames);
			line = getLineWithSkip(scanner,0);//ResAvails
			resAvals = arrToString(lustrateMas(line.split(" ")).toArray());
			printReport(resAvals);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// вообще-то не нужно,т.к. если APPROVE_OPTION, то там уже точно всё в порядке и файл выбран и он существует. . .
			// ХОТЯ. . . Если вдруг какой-то шутник успеет удалить за три секунды после выбора файл, то может быть и будет исключение. Но это крайне маловерятный сценарий.
			System.out.println("Не получилось открыть выбранный файл. Вероятно, кто-то умудрился его удалить. Проверьте его наличие и доступность процессу.");
			e.printStackTrace() ; 
		}
		
		} break; 
		case (JFileChooser.ERROR_OPTION): System.out.println("Ошибка чтения файла");try {
			this.getContainerController().getPlatformController().kill(); 
		}
		catch (final ControllerException e) { 
			System.out.println("Failed to end simulation.");
		} break;
		case(JFileChooser.CANCEL_OPTION): System.out.println("Отказ выбора файла"); break;
		default:System.out.println("Свяжитесь с автором программы и опишите ваши действия, из-за которых активировался этот кусок кода. Пожалуйста. ^_^");try {
			this.getContainerController().getPlatformController().kill(); 
		}
		catch (final ControllerException e) {
			System.out.println("Failed to end simulation.");
		} break;
		}
		//создать-то создаёт агента, но проверить на корректность не могу, что очень странно.
		/*Runtime runtime = Runtime.instance();
	    Profile profile = new ProfileImpl();
	    profile.setParameter(Profile.MAIN_HOST, "lochalhost");
	    profile.setParameter(Profile.GUI, "true");
		ContainerController containerController = runtime.createMainContainer(profile);//скорее всего тут дублится главный контроллер, и поэтому агенты сидят в разных инстансах.
		//именно так и было. надо было взять из контекста агента контейнер и в нём уже запустить.
		*/
		ContainerController containerController = this.getContainerController();
		AgentController taskAgentController, resAgentController;
		/*for (int i = 0;i<5;i++)*/
		// ResName, ResVolume, Restype, PlanHorizon 
		// TaskName, numSuc, numRes, Time Need SUCCESSORS, RESNAMES, RESVOLUMES
		/*int i = 0;
		{try {
			taskAgentController = containerController.createNewAgent("Task" + (i+1), "AgentTest.TaskAgent",new String[]{"Task1", "0", "0","0"});
			taskAgentController.start();
			System.out.println("task "+ (i+1) + " created.");
			System.out.println(taskAgentController.getName() + " created.");
		}
		catch(StaleProxyException e) {
			e.printStackTrace();
		}*/
		String localPlatform = "@" + getAID().getName().split("@")[1].toString();
		//printReport("Local Platform name is :" + localPlatform);
			
		/*try{
			taskAgentController.activate();	}
		catch (StaleProxyException e) {
			e.printStackTrace();
		}
		}*/
		//Generating Resource Agents
		//gets ResName, ResVolume, PlanningHorizon
		String[] resNamesArr = resNames.split(" ");
		String[] resVolArr = resAvals.split(" ");
		ArrayList <String> resAgents = new ArrayList<>();//is used to store resAgentNames
		int nowLen = resNamesArr.length / 2;
		for(int i = 0;i<nowLen;i++){
			try {
				resAgents.add(genResName(i+1,resNamesArr[i*2]));
				resAgentController = containerController.createNewAgent(resAgents.get(i), resAgentClass,new String[]{genResName(i+1,resNamesArr[i*2]), resVolArr[i], horizon.toString()});
				resAgentController.start();
				printReport(resAgentController.getName() + " created.");
			}
			catch(StaleProxyException e) {
				e.printStackTrace();
			}
		}
		//Hooray! It Works!
		//запускаем первый и последний агенты работ. Это источник и сток. Им соответствует класс TransmitterAgent

		ArrayList<String> jobAgents = new ArrayList<>();
		for (int i = 0;i<jobNum+2;i++) {
			jobAgents.add(genJobName(i+1) + localPlatform);
			//printReport(" " + i + " " + jobAgents.get(i));
		}
		ArrayList<String> params = new ArrayList<>();
		String[] jobParams = {};
		//fistnum secondnum FIRSTS SECONDS
		//source
		try {
			params.add("1");
			jobParams = jobsParams.get(0).split(" ");
			params.add(jobParams[2]);
			params.add(this.getAID().getName());
			int i2 = Integer.parseInt(params.get(1));
			for (int i = 0;i<i2;i++){
				params.add(jobAgents.get(Integer.parseInt(jobParams[i2+i])-1));
			}
			printReport("Source params 1:");
			for (String s:params) {
				printReport(s);
			}
			taskAgentController = containerController.createNewAgent(genJobName(1), transmitterAgent,params.toArray(jobParams));
			taskAgentController.start();
			printReport(taskAgentController.getName() + " created.");
		}
		catch(StaleProxyException e) {
			e.printStackTrace();
		}
		ArrayList<String> connectedToSink = new ArrayList<>();
		for (int i = 2;i<=jobNum+1;i++) {
		//{int i = 2; //test line to replace prev one in testing env
			params.clear();
			//TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES
			// in jobsParams:jobnbr, nummodes, numsuc, SUCCESSORS, mode, RESVOLUMES
			try {
				jobParams = jobsParams.get(i-1).split(" ");
				//jobsParams[2] - количество последующих работ.
				int sucN = Integer.parseInt(jobParams[2]);
				if (jobParams[sucN + 2].equals(((Integer)(jobNum+2)).toString()))
					connectedToSink.add(genJobName(Integer.parseInt(jobParams[0])) + localPlatform);
				params.add(genJobName(i)+localPlatform);//TaskName
				params.add(""+sucN);//numSuc
				params.add(""+resNum);//numRes
				//add timeNeed
				int j2 = jobParams.length - 1;
				params.add(jobParams[j2 - resNum]);
				//add successors
				for (int j = 0 ; j < sucN;j++) {
					params.add(jobAgents.get(Integer.parseInt(jobParams[3+j]) - 1));
				}
				
				//add resnames
				for (int j = resNum;j>0;j--) {
					params.add(resAgents.get(j-1) + localPlatform);
				}
				

				//add resvolumes
				for (int j =0;j<resNum;j++) {
					params.add(jobParams[j2 -j]);
				}
				//params.add(jobParams[3+sucN]); 
				taskAgentController = containerController.createNewAgent(genJobName(i), jobAgentClass,params.toArray(jobParams));
				taskAgentController.start();
				//printReport(taskAgentController.getName() + " created.");
			}
				catch (StaleProxyException e) {
					e.printStackTrace();
				}
		}
		
		
		//sink
		//або пихать все возможные названия, або при генерации прочих работ фиксировать есть ли последний номер в разделённой строке. 
		//Второе. Однозначно второе.
		try { 
			params.clear();
			int i2 =connectedToSink.size();
			params.add("1");
			params.add("" + i2);
			params.add(this.getAID().getName());
			
			 for (int i = 0; i<i2;i++) {
				 params.add(connectedToSink.get(i));
			 }
			printReport("Source params 2:");
			for (String s:params) {
				printReport(s);
			} 
			taskAgentController = containerController.createNewAgent(genJobName(jobNum+2), transmitterAgent,params.toArray(jobParams));
			taskAgentController.start();
			printReport(taskAgentController.getName() + " created.");
		}
		catch(StaleProxyException e) {
			e.printStackTrace();
		}
		//now we need to initialize our network and build up connections
		
		//sendMes(jobAgents.get(0),"mini 0");
		//TODO: Добавим приёмник сообщений. Пока о том, что всё встало.
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg!=null) {
					System.out.println(getAID().getName() + ": " + msg.getContent());
				}else block();
			}
		});
	}
}