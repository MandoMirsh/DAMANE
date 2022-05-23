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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import jade.core.behaviours.*;
import jade.lang.acl.*;
public class CtrlAgent extends Agent{

	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	JPanel panel_1 = new JPanel();
	JPanel panel_2 = new JPanel();
	private Rectangle newpos = new Rectangle(106, 128, 545, 312);
	Component horizontalStrut = Box.createHorizontalStrut(20);
	private Dimension comboSize =new Dimension(150, 26);
	JComboBox <String> comboResourses = new JComboBox<>();
	JComboBox <String> comboJobs = new JComboBox<>();
	JButton btnGetResReport = new JButton("Report!");
	JButton btnGetJobReport = new JButton("Report!");
	private JPanel contentPane = new JPanel();
	JScrollPane scrollPane = new JScrollPane();
	JScrollPane scrollPane_1 = new JScrollPane();
	JTable table = new JTable(){
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	},
	table_1 = new JTable() {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	DefaultTableModel model = new DefaultTableModel(new Object[][] {},
								new String[] {
										"JobName", "Start","Finish","Status", "StartNow","FinishNow"
							}), 
			model_1 = new DefaultTableModel( new Object[][] {},
				new String[] {
					"ResName","ResQ","ResQNow"
			});
	private void setGUI() {
		frame.setTitle(this.getLocalName() + "status");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setBounds(newpos.x, newpos.y, newpos.width, newpos.height);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		frame.setContentPane(contentPane);
		contentPane.add(panel, BorderLayout.NORTH);
		scrollPane.setPreferredSize(new Dimension(500, 105));
		panel.add(scrollPane);
		scrollPane.setViewportView(table);
		contentPane.add(panel_1, BorderLayout.CENTER);
		scrollPane_1.setPreferredSize(new Dimension(500, 105));
		scrollPane_1.setViewportView(table_1);
		panel_1.add(scrollPane_1);
		table.setModel(model);
		table_1.setModel(model_1);
		contentPane.add(panel_2, BorderLayout.SOUTH);
		panel_2.add(comboResourses);
		comboResourses.setPreferredSize(comboSize);
		panel_2.add(btnGetResReport);
		panel_2.add(horizontalStrut);
		panel_2.add(comboJobs);
		comboJobs.setPreferredSize(comboSize);
		panel_2.add(btnGetJobReport);
		btnGetResReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				//send "please, report" to the chosen resource agent. 
				//the chosen resource agent's label is at combobox
				String name = getResByLabel((comboResourses.getSelectedItem()).toString());
				sendMes(name,"srep");
				
			}
		});
		btnGetJobReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				//send "please, report" to the chosen job agent.
				//the chosen resource agent's label is at combobox
			}
		});
	}
	private void startNegotiations() {
		int size = resourseDescs.size();
		for (int i = 0; i <size;i++) {
			sendMes(resourseDescs.get(i).getName(),labelToCommand("START_NEG"));
		}
	}
	private void setSourceReady() {
		int rownum = 0;
		model.setValueAt("0",rownum,1);
		model.setValueAt("0",rownum,2);
		model.setValueAt("SOURCE",rownum,3);
		model.setValueAt("0",rownum,4);
		model.setValueAt("0",rownum,5);
	}
	private void setSinkReady() {
		int rownum = jobNum +1;
		model.setValueAt(projFin.toString(), rownum, 1);
		model.setValueAt(projFin.toString(), rownum, 2);
		model.setValueAt("SINK",rownum,3);
		model.setValueAt(projFin.toString(), rownum, 4);
		model.setValueAt(projFin.toString(), rownum, 5);
		
	}
	private void updateSink(Integer N) {
		int rownum = jobNum+1;
		model.setValueAt(N.toString(), rownum, 4);
		model.setValueAt(N.toString(), rownum, 5);
	}
	private void addNewRowJobs(String name) {
		//add new part to table
		//jobname start finish status startnow finishnow 
		ArrayList<String> toAdd = new ArrayList<>();
		toAdd.add(getJobLabel(name));
		String addStart = "INITIALIZING", addFinish = "INITIALIZING", status = "STARTED", nowStart = "INITIALIZING", nowFinish = "INITIALIZING";
		toAdd.add(addStart); toAdd.add(addFinish); toAdd.add(status); toAdd.add(nowStart); toAdd.add(nowFinish);
		//model.addRow(toAddToTable.toArray());
		model.addRow(toAdd.toArray());
		tablePlaces.put(name, model.getRowCount() - 1);
		//add new part to combobox;
		comboJobs.addItem(getJobLabel(name));
	}
	private void addNewRowResourses(String name) {
		//add new part to table
		//name qstart qnow
		ArrayList<String> toAdd = new ArrayList<>();
		toAdd.add(getResLabel(name));
		String addQ = "INITIALIZING", addQNow = "INITIALIZING";
		toAdd.add(addQ); toAdd.add(addQNow);
		model_1.addRow(toAdd.toArray());
		tablePlaces.put(name, model_1.getRowCount() - 1);
		//add new part to combobox
		comboResourses.addItem(getResLabel(name));
	}
	String getJobLabel(String name) {
		return name.split("@")[0];
	}
	String getResLabel(String name) {
		return name.split("@")[0];
	}
	String getJobByLabel(String label) {
		return label + localPlatform;
		
	}
	String getResByLabel(String label) {
		return (label + localPlatform);
	}
	private Map<String, String> commands = new HashMap<String,String>(), outputVoc = new HashMap<String, String>(), netStatusVoc = new HashMap<String, String>();
	private Map<String, Integer> tablePlaces = new HashMap<String,Integer>();
	Integer tardcost, horizon, resNum;
	Integer projNum, jobNum, relDate, dueDate, tardCost, nPMTime;
	Integer projFin = 0, jobsStarted = 0;
	String resNames = "", resAvals = "", resAgentClass = "agentTest.ResourceAgent",
			transmitterAgent = "agentTest.TransmitterAgent", jobAgentClass = "agentTest.TaskAgent";
	String controller, localPlatform;
	private Map<String, Integer> places = new HashMap<String,Integer>();
	
	boolean showFrame = false;
	Integer initJobs = 0, gotMes = 0, mesToGet1, mesToGet2;//mesToGet1 - how many messages will I get if I go forwards the graph, mesToget2 - backwards.
	Integer JobsNeg = 0;
	
	private ArrayList<String> jobsParams = new ArrayList<>();
	private ResDescStore resourseDescs = new ResDescStore();
	//private TaskDescStore TaskDescs = new TaskDescStore();

	private void outputVocabularyInit() {
		outputVoc.put("REPORT_REQUEST", "srep");
		outputVoc.put("ERROR_READING_FILE","errf");
		//outputVoc.put(controller, resAvals);
		outputVoc.put("PROJECT_START", "meaf");
		outputVoc.put("PROJECT_FINISH", "meat");
		outputVoc.put("INIT_PROGRESS","strt");
		outputVoc.put("STARTUP_NET", "stup");
	}
	private void netStatusVocInit() {
		netStatusVoc.put("NET_STARTED", "0");
		netStatusVoc.put("ENVIRONMENT_FIXED", "1");
		netStatusVoc.put("LATE_STARTS_FIXED", "2");
		netStatusVoc.put("EARLYFINISHES_FIXED", "3");
		netStatusVoc.put("NET_STOPPED", "4");
	}
	private void initiateVocabulary() {
		//"rrep", "RESOURSE_REPORTING", "meaf","MY_LATE_FINISH","stup", "STARTED_UP","shfr", "SHOW_FRAME" 
		//"meat", "MY_EARLY_START", "mini", "MY_INITIALIZATION_INT", "tire","I_AM_READY", "tnre", "I_AM_NOT_READY"
		commands.put("rrep", "RESOURSE_REPORTING");
		commands.put("stup", "STARTED_UP");
		commands.put("shfr", "SHOW_FRAME");
		commands.put("meaf", "MY_LATE_FINISH");
		commands.put("meat", "MY_EARLY_START");
		commands.put("mini", "MY_INITIALIZATION_INT");
		commands.put("tire","I_AM_READY");
		commands.put("tnre", "I_AM_NOT_READY");
		commands.put("rare", "RESOURSE_REPORT");
		commands.put("jore", "JOB_REPORT");
		commands.put("mnef", "MY_NEW_LATE_FINISH");
	}
	String netStatus(String statusName) {
		if (netStatusVoc.get(statusName) == null)
			return "NO_EXPLANATION";
		return netStatusVoc.get(statusName);
	}
	String commandExplain(String command) {
		if (commands.get(command) == null)
			return "NO_EXPLANATION";
		return commands.get(command);
	}
	String labelToCommand(String label) {
		return outputVoc.get(label);
	}
	private void PSPLibParse(File f, ArrayList<String> jobsParams) throws FileNotFoundException {
		try {
			String[] items;
			Scanner scanner = new Scanner(f);
			
			String line = scanner.nextLine();
			// ��� �������� �����.
			line = scanner.nextLine();
				//System.out.println(line);
			// ��� �������
			line = scanner.nextLine();
				//System.out.println(line);
			line = scanner.nextLine();
			//����� ��������
			line = scanner.nextLine();
			//����� �����
			line = scanner.nextLine();
			//�������� ������������
			line = scanner.nextLine();
			int projectHorizon = readNumFromLine(line,25);
			//System.out.println(projectHorizon); // 158
			line = scanner.nextLine();
			//���������� �������������� ��������
			line = scanner.nextLine();
			int renewNum = readNumFromLine(line,22);
			//System.out.println(renewNum); // 4
			//���������� ���������������� ��������
			line = scanner.nextLine();	
			int nonRenewNum = readNumFromLine(line,19);
			//System.out.println(nonRenewNum); // 0
			
			// ���������� �������� � �������� ������������
			line = scanner.nextLine();
			int doubleConsRes = readNumFromLine(line,14);
			//System.out.println(doubleConsRes); // 0
			
			//������� ��������� �� ������� ���������� ��� ������ ��������.
			line = scanner.nextLine();
			line = scanner.nextLine();
			line = scanner.nextLine();
			//project properties: projectnum, 
			//���� �������: ����� ������� 4, ���������� ����� 9, ���� 15, ���� �� ������� ������� ���������� 22, �������� 30 , ����� ���(?) 37
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
			
			//��������� ��������������: ����� ������, �����, ���������� ����������, ���������. 
			//TODO: ������ � 0 �� N+1,��� N - ���������� ��������� � �������.
			//printReport("Starting to read precedence");
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
			//����� ������, �����, ������������, ������_������, ������_������, ������_������, ��������_������
			//TODO:������ � 0 �� N+1, ��� N - ���������� ��������� � �������.
			for (int i = 0; i < jobNum+2;i++)
			{
				line = scanner.nextLine();
				//printReport(arrToString(lustrateMas(line.split(" "), 1).toArray()));
				String st = jobsParams.get(i),
						nline = arrToString(lustrateMas(line.split(" "), 1).toArray());
				//processing 
				jobsParams.set(i, uniteStrings(st,nline));
				//printReport(jobsParams.get(i));
			}
			line = scanner.nextLine();

			line = scanner.nextLine();
			
			//resnames
			line = scanner.nextLine();
			String resNames = arrToString(lustrateMas(line.split(" ")).toArray());
			//printReport(resNames);
			//resavaliability
			line = scanner.nextLine();
			String resAvals = arrToString(lustrateMas(line.split(" ")).toArray());
			//printReport(resAvals);
			//last line of file.
			line = scanner.nextLine();
			scanner.close();
			//return 0;
		} catch (FileNotFoundException e) {
			//unbelievable scenario, except for someone managed to delete file beforehand.
			System.out.println("�� ���������� ������� ��������� ����. ��������, ���-�� ��������� ��� �������. ��������� ��� ������� � ����������� ��������.");
			e.printStackTrace() ;
			//return -1;
		}
	}

	
	private void generateResourses(String resNames, String resAvals, String at, ResDescStore desc, ContainerController controller, ArrayList<String> rnames) {
	//INPUT: resNames, resAvals,localPlatfrom,resourseDescs
			String[] resNamesArr = resNames.split(" ");
			String[] resVolArr = resAvals.split(" ");
			int nowLen = resNamesArr.length / 2;
			for(int i = 0;i<nowLen;i++){
				try {
					rnames.add(genResName(i+1,resNamesArr[i*2]));
					AgentController resAgentController = controller.createNewAgent(rnames.get(i), resAgentClass,new String[]{genResName(i+1,resNamesArr[i*2]), resVolArr[i], horizon.toString()});
					resAgentController.start();
					String name = genResName(i+1,resNamesArr[i*2])+at;
					desc.add(new ResourceDescriptor(name));
					printReport(name+ ": generated");
					addNewRowResourses(name);
					//printReport(resAgentController.getName() + " created.");
				}
				catch(StaleProxyException e) {
					e.printStackTrace();
				}
			}
			printReport("Resource agents created");
			
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
	
	//TODO: change 1 to 0
	private void sendtoNetStart(String where, String message) {
		sendMes(genJobName(1)+ "@" + where,message);
	}
	//TODO: change +2 to +3
	private void sendToNetFinish(String where, String message) {
		sendMes(genJobName(jobNum+2)+ "@" + where,message);
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
	Behaviour ifShowFrame = new CyclicBehaviour() {//showFrame is set when we get "shfr"
		@Override
		public void action() {
			if (showFrame) {
				showFrame= false;
				if (!frame.isVisible())
					frame.setBounds(newpos.x, newpos.y, newpos.width, newpos.height);
				frame.setVisible(true);
			}
		}
	};
	
	Behaviour StopInit4 = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(init4);
			printReport("init4 finished!");
			myAgent.addBehaviour(nextMsg);
			sendMes(controller,labelToCommand("STARTUP_NET") + " " + netStatus("EARLYFINISHES_FIXED"));
			//sendMes(genJobName(1)+"@" + myAgent.getAID().getName().split("@")[1].toString(),"stup");
			//��������� "INIT_PROGRESS" �� ������� ��������
		}
		
	};
	//stup	
	Behaviour  init4 = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg !=null) {
				//printReport(msg.getSender().getName() + " " + msg.getContent());
				String[] items = msg.getContent().split(" ");
				switch (commandExplain(items[0])) {
				case "STARTED_UP": {
							gotMes++;
							if (gotMes == mesToGet2) {
								myAgent.addBehaviour(StopInit4);
							}	
					};
				break;
				case "MY_LATE_FINISH":{//very unlikely but there can be changes in finishes before getting first stup. . .
					int newFin = Integer.parseInt(items[1].toString());
					if (newFin>projFin) {
						projFin = newFin;
						String local = myAgent.getAID().getName().split("@")[1].toString();
						String message = "meat "+ projFin;
						sendToNetFinish(local, message);
					}
				} break;
				case "JOB_REPORT"://Job reports are being taken as fast as 
					{
						int rowNum = tablePlaces.get(getJobLabel(items[3]));
					
						if (model.getValueAt(rowNum, 1) == "INITIALIZING")
						{
							model.setValueAt(items[1].toString(),rowNum,1);
							model.setValueAt(items[2].toString(),rowNum,2);
						}
						model.setValueAt(items[1].toString(),rowNum,4);
						model.setValueAt(items[2].toString(),rowNum,5);
						JobsNeg++;
						printReport("got JObReport in 4: " + rowNum + " " +items[1] + " "+ items[2]);
						if (JobsNeg == 30)
							{
								setSinkReady();
								startNegotiations();
							}
					}
					break;
				case "MY_NEW_LATE_FINISH":{
					int newFin = Integer.parseInt(items[1].toString());
					if (newFin>projFin) {
						projFin = newFin;
						String local = myAgent.getAID().getName().split("@")[1].toString();
						String message = "meat "+ projFin;
						sendToNetFinish(local, message);
					}
				}
					break;
				} 
			}
		} 
	};
	Behaviour StopInit3 = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(init3);
			printReport("init3 finished!");
			gotMes=0;
			sendMes(controller,labelToCommand("STARTUP_NET") + " " + netStatus("LATE_STARTS_FIXED"));
			setSourceReady();
			myAgent.addBehaviour(init4);
			String message = "stup";
			String where = myAgent.getAID().getName().split("@")[1].toString();
			sendtoNetStart(where, message);
		}
		
	};
	//meat
	Behaviour  init3 = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg !=null) {
				//printReport(msg.getSender().getName() + " " + msg.getContent());
				String[] items = msg.getContent().split(" ");
				switch (commandExplain(items[0])) {
				case "MY_EARLY_START": {
							gotMes++;
							//printReport("Got Meat! Now "+ gotMes +" times!");
							if (gotMes == mesToGet2) {
								myAgent.addBehaviour(StopInit3);
							}	
					};
				break;
				}
			}
		} 
	};
	
	
	Behaviour StopInit2 = new OneShotBehaviour() {
		@Override
		public void action() {
			printReport("finished init2!");
			myAgent.removeBehaviour(init2);
			gotMes = 0;
			//myAgent.addBehaviour(nextMsg);
			//sendMes()
			myAgent.addBehaviour(init3);
			sendMes(controller, "ffin " + projFin);
			String local = myAgent.getAID().getName().split("@")[1].toString();
			String message = "meat "+ projFin;
			sendToNetFinish(local, message);
		} 
	};
	
	//meaf
	Behaviour  init2 = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg !=null) {
				//printReport(msg.getSender().getName() + " " + msg.getContent());
				String[] items = msg.getContent().split(" ");
				switch (commandExplain(items[0])) {
				case "MY_LATE_FINISH": {
							//printReport("Got Meaf!");
							gotMes++;
							int newFin = Integer.parseInt(items[1].toString());
							if (newFin>projFin) {
								projFin = newFin;
							}
							if (gotMes == mesToGet1)
							{//�������� ����� ����� ������� ������������ ������.
								sendMes(controller,"nfin "+ newFin);
								//printReport("Finished init2! with fin: "+ newFin);
								myAgent.addBehaviour(StopInit2);
								
							}
					};
				break;
				
				}
			}
		} 
	};
	Behaviour  StopInit1 = new OneShotBehaviour() {
		@Override
		public void action() {
			printReport("finished init1!");
			myAgent.removeBehaviour(init1);
			gotMes = 0;
			//sendMes()
			myAgent.addBehaviour(init2);
			sendMes(controller, "stup "+ netStatus("ENVIRONMENT_FIXED"));
			String message = "meaf 0";
			String where = myAgent.getAID().getName().split("@")[1].toString();
			sendtoNetStart(where, message);
		} 
	};
	//mini
	Behaviour  init1 = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg !=null) {
				//printReport(msg.getSender().getName() + " " + msg.getContent());
				String[] items = msg.getContent().split(" ");
				switch (commandExplain(items[0])) {
				case "MY_INITIALIZATION_INT": {
					//printReport("got mini!");
						int inits = Integer.parseInt(items[1].toString());
						if (inits !=0) {
							initJobs += inits;
							//printReport("jobnum is: "+ jobNum+ ". And jobs initiated: "+ initJobs);
							if (initJobs == jobNum) {
							printReport("Initiation complete!"); myAgent.addBehaviour(StopInit1);}
						}
					};
				break;
				}
			}
			
		} 
	};
	/*Behaviour setStartsAndFinishes = new OneShotBehaviour() {
		@Override
		public void action() {
			sendMes(genJobName(1)+"@" + myAgent.getAID().getName().split("@")[1].toString(),"stag");
			sendMes(genJobName(jobNum+2)+"@" + myAgent.getAID().getName().split("@")[1].toString(),"fiag");
			myAgent.addBehaviour(init1);
		}
	};*/
	Behaviour  StopInit0 = new OneShotBehaviour() {
		@Override
		public void action() {
			//printReport("finished init!");
			myAgent.removeBehaviour(init0);
			initJobs = 0;
			//setStartsAndFinishes OR init1  
			//myAgent.addBehaviour(setStartsAndFinishes);
			myAgent.addBehaviour(init1);
			sendMes(controller, "stup "+ netStatus("NET_STARTED"));
			
			String message = "mini 0";
			String where = myAgent.getAID().getName().split("@")[1].toString();
			sendtoNetStart(where, message);
		} 
	};
	//stup
	Behaviour init0 = new CyclicBehaviour() {
		@Override
		public void action() {
			//getmessages
			ACLMessage msg = receive();
			if (msg !=null) {
				//printReport(msg.getSender().getName() + " " + msg.getContent());
				//String[] items = msg.getContent().split(" ");
				switch (commandExplain(msg.getContent())) {
				case "STARTED_UP": {
						initJobs++;
						//printReport("" + initJobs);
						if (initJobs == jobNum) {
							myAgent.addBehaviour(StopInit0);
						}
					};
				break;
				}
			}
		} 
	};
	
	Behaviour nextMsg = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg!=null) {
				String sender = (msg.getSender().getName());
				String[] items = msg.getContent().split(" ");
				
				//System.out.println(getAID().getName() + ": " + msg.getContent());
				switch (commandExplain(items[0].toString())){
				case "MY_LATE_FINISH": {
					int newFin = Integer.parseInt(items[1].toString());
					if (newFin>projFin) {
						//�������� ����� ����� ������� ������������ ������.
						sendMes(controller,"nfin "+ newFin);
						projFin = newFin;
						//update sink at table
						updateSink(newFin);
					}
				}; break;
				case "MY_NEW_LATE_FINISH":{
					int newFin = Integer.parseInt(items[1].toString());
					if (newFin>projFin) {
						projFin = newFin;
						sendMes(genJobName(jobNum+2)+"@" + myAgent.getAID().getName().split("@")[1].toString(),"meat "+ projFin);
					}
				}
					break;
				case "I_AM_READY":{
					int rownum = tablePlaces.get(getJobLabel(items[2]));
					if (model.getValueAt(rownum, 3)!="PLANNED")
						{
							jobsStarted++;
							model.setValueAt("PLANNED",rownum,3);
							printReport("Job Finished: "+(rownum+1));
						}
						
					if (jobsStarted == jobNum) {
						sendMes(controller,labelToCommand("STARTUP_NET") + " " + netStatus("NET_STOPPED"));
						//��������� �� ������� �������� ��������� � ����������� �����.
					}
				};break;
				case "I_AM_NOT_READY":{
					printReport("Job not ready: "+ items[2]);
					int rownum = tablePlaces.get(getJobLabel(items[2]));
					if (model.getValueAt(rownum, 3)=="PLANNED")
					{
						jobsStarted--;
						model.setValueAt("REPLANNING",rownum,3);
						printReport("Job not Finished anymore: "+rownum);
					}
					
					if (jobsStarted == jobNum) {
						sendMes(controller,"stup 3");
					}
					jobsStarted--;
				};break;
				case "SHOW_FRAME":newpos.x =Integer.parseInt(items[1]) + 6;newpos.y =Integer.parseInt(items[2]) + 28;showFrame = true; 
				break;
				case "RESOURSE_REPORT":
					
					//	������� ����������� - sender.
					int rownum = tablePlaces.get(sender);
					if (model_1.getValueAt(rownum, 1) == "INITIALIZING")
						model_1.setValueAt(items[1].toString(), rownum, 1);
					model_1.setValueAt(items[1].toString(), rownum, 2);
					// ���� � ������� table_1  �� ��������������� ������� � ����� resQ ��������� INITIALIZING
					//
					//
					break; 
				case "JOB_REPORT":
					int rowNum = tablePlaces.get(getJobLabel(items[3]));
					if (model.getValueAt(rowNum, 1) == "INITIALIZING")
					{
						model.setValueAt(items[1].toString(),rowNum,1);
						model.setValueAt(items[2].toString(),rowNum,2);
					}
					model.setValueAt(items[1].toString(),rowNum,4);
					model.setValueAt(items[2].toString(),rowNum,5);
					JobsNeg++;
					//printReport("got JObReport: " + rowNum + " " +items[1] + " "+ items[2]);
					if (JobsNeg == 30)
					{
						setSinkReady();
						startNegotiations();
					}
				}
				
			}
		}
	};
	//I.1 - ��������� ������ ��������
	//gets ResName, ResVolume, PlanningHorizon
	//�������� ����� ������� ������
	//fistnum secondnum FIRSTS SECONDS
	//II.1 - �������� ������ �����
	//TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES 
	//�������� ����� �������� ������
	//I,II.2 - �������� ������� ��������� �����
	
	@Override
	public void takeDown() {
		frame.setVisible(false);
	}
	@Override
	public void setup() {
		setGUI();
		initiateVocabulary();
		outputVocabularyInit();
		netStatusVocInit();
		
		Object[] args = getArguments();//controllerAgentName  projecFilePath projectNumber
		
		controller = args[0].toString();
		//INPUT: args[1].toString() - filepath
		//OUTPUT: horizon, resNum, projNum, jobNum, dueDate
		//resNames, resAvals,
		String filepath = args[0].toString();
	    try {
			String[] items;
			Scanner scanner = new Scanner(new File(args[1].toString()));//we get FilePath from arguments
			String line = getLineWithSkip(scanner,6);//*,basedata (4),random seed (4),*,projects (2), jobnumWithMock (3), horizon (2)
			horizon = Integer.parseInt(lustrateMas(line.split(" ")).get(2));//158
			line = getLineWithSkip(scanner,1);//RESOURSE, renewNum (3)
			resNum = Integer.parseInt(lustrateMas(line.split(" ")).get(3));
			line = getLineWithSkip(scanner,5);//nonRenewNum (3),doublyConstr (3),*,PROJECTINFO,proinfo, projinfo: jobNum (1) reldate (2) dueDate (3) tardCost (4) MPM-Time (Multi-process mode possibly, 5)
			ArrayList<String> larr = lustrateMas(line.split(" "));
			projNum = Integer.parseInt(args[2].toString());
			jobNum = Integer.parseInt(larr.get(1)); relDate = Integer.parseInt(larr.get(2)); 
			
			dueDate = Integer.parseInt(larr.get(3)); tardCost = Integer.parseInt(larr.get(4)); nPMTime = Integer.parseInt(larr.get(5));	
			//Proj: 1 30 0 38 26 38	
			//System.out.println("Proj: " + projNum + " " + jobNum + " " + relDate + " " + dueDate + " " + tardCost + " " + nPMTime);
			line = getLineWithSkip(scanner,2);// *,PRECEDENCE RELATIONS, precedence_headlines
			//printReport("Starting to read precedence");
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
				//��������� 
				jobsParams.set(i, uniteStrings(st,nline));
				//printReport(jobsParams.get(i));
			}
			line = getLineWithSkip(scanner,2);//*,RESOURCEAVALIABILITIES,ResNames
			resNames = arrToString(lustrateMas(line.split(" ")).toArray());
			//printReport(resNames);
			line = getLineWithSkip(scanner,0);//ResAvails
			resAvals = arrToString(lustrateMas(line.split(" ")).toArray());
			//printReport(resAvals);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// ������-�� �� �����,�.�. ���� APPROVE_OPTION, �� ��� ��� ����� �� � ������� � ���� ������ � �� ����������. . .
			// ����. . . ���� ����� �����-�� ������ ������ ������� �� ��� ������� ����� ������ ����, �� ����� ���� � ����� ����������. �� ��� ������ ������������ ��������.
			printReport("Can't open file at: "+args[1].toString() +  " It might be deleted or moved. Please check it's avaliability to the process.");
			//TODO: send back error message:
			sendMes(controller,"errf");
			//on the outside we need to get the message to sieze all operations and 
			e.printStackTrace() ; 
		}
		//�������-�� ������ ������, �� ��������� �� ������������ �� ����, ��� ����� �������.
		/*Runtime runtime = Runtime.instance();
	    Profile profile = new ProfileImpl();
	    profile.setParameter(Profile.MAIN_HOST, "lochalhost");
	    profile.setParameter(Profile.GUI, "true");
		ContainerController containerController = runtime.createMainContainer(profile);//������ ����� ��� �������� ������� ����������, � ������� ������ ����� � ������ ���������.
		//������ ��� � ����. ���� ���� ����� �� ��������� ������ ��������� � � �� ��� ���������.
		*/
		ContainerController containerController = this.getContainerController();
		AgentController taskAgentController, resAgentController;

		localPlatform = "@" + getAID().getName().split("@")[1].toString();
		//printReport("Local Platform name is :" + localPlatform);

		//Generating Resource Agents
		//gets ResName, ResVolume, PlanningHorizon	
		//INPUT: resNames, resAvals,localPlatfrom,resourseDescs
		ArrayList <String> resAgents = new ArrayList<>();//is used to store resAgentNames
		generateResourses(resNames,resAvals, localPlatform, resourseDescs, containerController,resAgents);
		//Hooray! It Works!
		//���� ��� ������ ������ � ����� ��� ������� �������, ������ �������� ������-���������.
		//������� ��������� �� 30, � 32 �������� �������������, �� ���� �� 2 �������� ������������� ������ ������ � ����� � STUP � MINI, 
		//�� � ����� � ���������� ������ � ������� ������� ����� ����������� ����� - ����� �������� ����� �� ���� ���������.
		
		//��������� ������ � ��������� ������ �����. ��� �������� � ����. �� ������������� ����� TransmitterAgent

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
			//printReport("Source params 1:");
			//for (String s:params) {
			//	printReport(s);
			//}
			String name = genJobName(1);
			taskAgentController = containerController.createNewAgent(name, transmitterAgent,params.toArray(jobParams));
			taskAgentController.start();
			addNewRowJobs(name);
			//printReport(taskAgentController.getName() + " created.");
		}
		catch(StaleProxyException e) {
			e.printStackTrace();
		}
		ArrayList<String> connectedToSink = new ArrayList<>();
		
		//printReport("started");
		mesToGet2 =   params.size()/2;
		for (int i = 2;i<=jobNum+1;i++) {
		//{int i = 2; //test line to replace prev one in testing env
			params.clear();
			// TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES
			// in jobsParams:jobnbr, nummodes, numsuc, SUCCESSORS, mode, RESVOLUMES
			try {
				jobParams = jobsParams.get(i-1).split(" ");
				//jobsParams[2] - ���������� ����������� �����.
				int sucN = Integer.parseInt(jobParams[2]);
				if (jobParams[sucN + 2].equals(((Integer)(jobNum+2)).toString()))
					connectedToSink.add(genJobName(Integer.parseInt(jobParams[0])) + localPlatform);
				params.add(this.getAID().getName());
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
				String name = genJobName(i);
				taskAgentController = containerController.createNewAgent(name, jobAgentClass,params.toArray(jobParams));
				taskAgentController.start();
				//printReport(taskAgentController.getName() + " created.");
				addNewRowJobs(name);
			}
				catch (StaleProxyException e) {
					e.printStackTrace();
				}
		}
		
		mesToGet1 = connectedToSink.size();
		//sink
		//��� ������ ��� ��������� ��������, ��� ��� ��������� ������ ����� ����������� ���� �� ��������� ����� � ���������� ������. 
		//������. ���������� ������.
		try { 
			
			params.clear();
			int i2 =connectedToSink.size();
			params.add("1");
			params.add("" + i2);
			params.add(this.getAID().getName());
			
			 for (int i = 0; i<i2;i++) {
				 params.add(connectedToSink.get(i));
			 }
			/*printReport("Source params 2:");
			for (String s:params) {
				printReport(s);
			} */
			String name = genJobName(jobNum+2);
			taskAgentController = containerController.createNewAgent(name, transmitterAgent,params.toArray(jobParams));
			taskAgentController.start();
			addNewRowJobs(name);
			//printReport(taskAgentController.getName() + " created.");
		}
		catch(StaleProxyException e) {
			e.printStackTrace();
		}
		//now we need to initialize our network and build up connections
		addBehaviour(init0);
		addBehaviour(ifShowFrame);
		//TODO: ������� ������� ���������. ���� � ���, ��� �� ������.
		//addBehaviour(nextMsg);
	}
}