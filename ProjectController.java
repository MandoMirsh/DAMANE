package agentTest;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
//import java.time.Clock;
public class ProjectController extends Agent {
	
	Integer projectsNum = 0;
	boolean newProject = false, newFileOpened = false;
	JFrame frame = new JFrame();
	
	JPanel panel = new JPanel();
	JPanel panel_1 = new JPanel();
	JPanel panel_2 = new JPanel();
	JPanel panel_3 = new JPanel();
	JPanel panel_4 = new JPanel();
	JScrollPane scrollTablePane = new JScrollPane();
	JTable table = new JTable(){
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	JTextField txtFileName = new JTextField("No File Selected!",30);
	
	JFileChooser fileopen = new JFileChooser();
	JButton btnOpenFile = new JButton("Open New File");
	JButton btnRunSimulation = new JButton("Run Simulation");
	DefaultTableModel model = new DefaultTableModel(new Object[][] {},
				new String[] {
					"AgentName", "FilePath", "Readiness", "Pre-Constraint Finish", "Finish now"
				});
	//GUI part:
	private void prepareGUI() {
		frame.setTitle("DAMANE - Darth Atin's Multi-Agent Network for timEtabling");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //frame.setSize(600, 400);
        frame.setBounds(100, 100, 650, 400);
        
        scrollTablePane.setPreferredSize(new Dimension(500, 105));
		scrollTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);	
	}
	
	
//Agent Management part	
	String ProjectClass = "agentTest.CtrlAgent", nameAgent = "SuperController";
	ContainerController containerController;
	AgentController taskAgentController, resAgentController;
	private MessagesToSend sendQueue = new MessagesToSend();
	ProjectStorage projects = new ProjectStorage();
	
	private void sendMes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	private String genProjName(int agNum) {
		return ("Project"+agNum+"Controller");
	} 
	
//Behaviours
	Behaviour CheckNewProject = new CyclicBehaviour() {
		@Override
		public void action() {
			if (newProject) {
				newProject = false;
				//processing
				//System.out.println("Button has been hit!");
				
				try {
					String proj = genProjName(++projectsNum);
					resAgentController = containerController.createNewAgent(proj, ProjectClass ,new String[]{myAgent.getAID().getName(),txtFileName.getText() ,(projectsNum).toString()});
					resAgentController.start();
					printReport(resAgentController.getName() + " created.");
					//if successfully created agent then add its descriptor.
					projects.add(new ProjectDesc(proj,txtFileName.getText()));
					//add new row to the table
					ArrayList<String> toAddToTable = new ArrayList<String>();
					toAddToTable.add(proj);
					toAddToTable.add(txtFileName.getText());
					toAddToTable.add("STARTING_UP");
					toAddToTable.add("0");
					toAddToTable.add("0");
					model.addRow(toAddToTable.toArray());
					
					txtFileName.setText("No File Selected!");
				}
				catch(StaleProxyException e) {
					e.printStackTrace();
				}

				//System.out.println("I tried to change something inside form. Did that work?");
			}
		}
	};
	Behaviour NextMSGProcess = new CyclicBehaviour() {
		@Override
		public void action() {
			//checking new messages
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			//if there's any, process it
			if (msg!=null) {
				String sender = (msg.getSender().getName());
				String senderShort = (msg.getSender().getLocalName());
				String[] items = msg.getContent().split(" ");
				switch (items[0]) {
					case "stup":{
						//find sender
						Integer i = projects.searchByAgent(senderShort);
						switch (items[1]) {
						case "0":{
							if (i==-1)
								printReport("All you had to do is to follow tha damn train CJ!");
							else {
								//printReport("got stup 0 and at position" + i+ " from "+ sender);
								ProjectDesc toChange = projects.get(i);
								//change readiness to the INITINPROGRESS
								toChange.setReadiness(ProjectDesc.INITINPROGRESS);
								//save it
								projects.changeProjectAt(i, toChange);
								//change table
								model.setValueAt("INIT_IN_PROGRESS", i, 2);
							}
						};break;
						case "1":{
							if (i==-1)
								printReport("All you had to do is to follow tha damn train CJ!");
							else {
								//printReport("got stup 1 and at position" + i+ " from "+ sender);
								ProjectDesc toChange = projects.get(i);
								//change readiness to the INITINPROGRESS
								toChange.setReadiness(ProjectDesc.INITHALFREADY);
								//save it
								projects.changeProjectAt(i, toChange);
								//change table
								model.setValueAt("INIT_40%", i, 2);
							}
						};break;
						case "2":{
							if (i==-1)
								printReport("All you had to do is to follow tha damn train CJ!");
							else {
								ProjectDesc toChange = projects.get(i);
								toChange.setReadiness(ProjectDesc.INITALMOSTREADY);
								projects.changeProjectAt(i,toChange);
								model.setValueAt("INIT_70%", i, 2);
							}
						};break;
						case "3":{
							if (i==-1)
								printReport("All you had to do is to follow tha damn train CJ!");
							else {
								ProjectDesc toChange = projects.get(i);
								toChange.setReadiness(ProjectDesc.INITIALIZATIONPASSED);
								projects.changeProjectAt(i,toChange);
								model.setValueAt("NET_WORKNIG", i, 2);
							}
						};break;
						}
						//find 
					};break;
					
					case "ffin":{
						Integer newFin = Integer.parseInt(items[1]);
						//find sender
						Integer i = projects.searchByAgent(senderShort);
						if (i==-1)
							printReport("All you had to do is to follow tha damn train CJ!");
						else {
								ProjectDesc toChange = projects.get(i);
							//setNewFin
								toChange.setInitFinish(newFin);
								toChange.setFinish(newFin);
								projects.changeProjectAt(i,toChange);
								model.setValueAt(newFin.toString(), i, 3);
								model.setValueAt(newFin.toString(), i, 4);
						}
					}; break; 
					case "nfin":{
						Integer newFin = Integer.parseInt(items[1]);
						//find sender
						Integer i = projects.searchByAgent(senderShort);
						if (i==-1)
							printReport("All you had to do is to follow tha damn train CJ!");
						else {
								ProjectDesc toChange = projects.get(i);
							//setNewFin
								toChange.setInitFinish(newFin);
								toChange.setFinish(newFin);
								projects.changeProjectAt(i,toChange);
								model.setValueAt(newFin.toString(), i, 4);
						}
					}; break; 
					case "pfin":{
						Integer i = projects.searchByAgent(senderShort);
						if (i==-1)
							printReport("All you had to do is to follow tha damn train CJ!");
						else {
							ProjectDesc toChange = projects.get(i);
							toChange.setReadiness(ProjectDesc.READY);
							projects.changeProjectAt(i,toChange);
							model.setValueAt("MAKESPAN_READY", i, 2);
						}
					}
				}
			}
		}
	};
	@Override
	public void setup() {
		containerController = this.getContainerController();
		//
		prepareGUI();
		panel_2.add(btnOpenFile);
		txtFileName.setEditable(false);
		panel_2.add(txtFileName);
		txtFileName.setToolTipText("A file path is shown there before running a project");
		panel_2.add(btnRunSimulation);
		btnOpenFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				int ret = fileopen.showOpenDialog(null);
				switch (ret) {
					case (JFileChooser.APPROVE_OPTION):{ txtFileName.setText(fileopen.getSelectedFile().getPath()); newFileOpened = true;};break;
					case (JFileChooser.CANCEL_OPTION):{};break;
					case(JFileChooser.ERROR_OPTION):{};break;
				}
			}
		});
		frame.getContentPane().add(panel, BorderLayout.WEST);
		frame.getContentPane().add(panel_1, BorderLayout.EAST);
		frame.getContentPane().add(panel_2, BorderLayout.NORTH);
		frame.getContentPane().add(panel_3, BorderLayout.SOUTH);
		frame.getContentPane().add(panel_4, BorderLayout.CENTER);
		table.setModel(model);
		scrollTablePane.setViewportView(table);
		table.setColumnSelectionAllowed(true);
		
		panel_4.add(scrollTablePane);
		
		btnRunSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newProject = newFileOpened;
				newFileOpened = false;
			}
		});
		//Object[] args = getArguments();
		
		this.addBehaviour(CheckNewProject);
		this.addBehaviour(NextMSGProcess);
		
		frame.setVisible(true);
		//System.out.println(ZonedDateTime.now().toString());
		//System.out.println(LocalDateTime.now().toString());
	}
}
