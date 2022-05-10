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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
//import java.time.Clock;
public class ProjectController extends Agent {
	
	Integer projectsNum = 0,jobsFinished = 0;
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
	JComboBox<String> projectName = new JComboBox<>();
	
	JFileChooser fileopen = new JFileChooser();
	JButton btnOpenFile = new JButton("Open New File");
	JButton btnRunSimulation = new JButton("Run Simulation");
	JButton btnOpenProjectWindow = new JButton("Open Project");
	ListSelectionModel ls;
	DefaultTableModel model = new DefaultTableModel(new Object[][] {},
				new String[] {
					"AgentName", "FilePath", "Readiness", "Pre-Constraint Finish", "Finish now"
				});
	//SelectionListener tableSelection = new SelectionListener() {
		
//	};
	//GUI part:
	private void prepareGUI() {
		frame.setTitle("DAMANE - Darth Atin's Multi-Agent Network for timEtabling");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setResizable(false); 
        frame.setBounds(100, 100, 650, 220);
        
        scrollTablePane.setPreferredSize(new Dimension(500, 105));
		scrollTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);	
	}
	
	
	String getProjectAgentName(String selection) {
		//TODO: modify this and addition to combobox at checknewproject to use hashmap to get agent name by localname
		return selection;
	}
	private boolean projectStarted(int rownum) {
		String readiness = table.getValueAt(rownum,2).toString();
		switch (readiness) {
		case "NET_WORKING":
		case "NET_STOPPED": return true;
		default: return false;
		}
	}
	private String generateProjectLabel(String projName) {
		//TODO: make sure label does not contain "Controller"
		return projName.split("@")[0];
	}
	private String nameByLabel(String label) {
		return jobsByLabel.get(label);
	}
//Agent Management part	
	String ProjectClass = "agentTest.CtrlAgent", nameAgent = "SuperController";
	ContainerController containerController;
	AgentController taskAgentController, resAgentController;
	private MessagesToSend sendQueue = new MessagesToSend();
	ProjectStorage projects = new ProjectStorage();
	private Map<String,String> jobsByLabel = new HashMap<String,String>();
	
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
					//printReport(resAgentController.getName() + " created.");
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
					//now we add a name to the projectName combobox
					String ProjCommonName = resAgentController.getName(), label = generateProjectLabel(ProjCommonName);
					jobsByLabel.put(label, ProjCommonName);
					projectName.addItem(label);
					
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
								printReport("All we had to do was follow tha damn train CJ!");
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
								printReport("All we had to do was follow tha damn train CJ!");
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
								printReport("All we had to was to follow tha damn train CJ!");
							else {
								ProjectDesc toChange = projects.get(i);
								toChange.setReadiness(ProjectDesc.INITALMOSTREADY);
								projects.changeProjectAt(i,toChange);
								model.setValueAt("INIT_70%", i, 2);
							}
						};break;
						case "3":{
							if (i==-1)
								printReport("All we had to do was follow tha damn train CJ!");
							else {
								ProjectDesc toChange = projects.get(i);
								toChange.setReadiness(ProjectDesc.INITIALIZATIONPASSED);
								projects.changeProjectAt(i,toChange);
								model.setValueAt("NET_WORKING", i, 2);
							}
						};break;
						case "4":{
							if (i==-1)
								printReport("All we had to do was follow tha damn train CJ!");
							else {
								ProjectDesc toChange = projects.get(i);
								toChange.setReadiness(ProjectDesc.READY);
								projects.changeProjectAt(i,toChange);
								model.setValueAt("NET_STOPPED", i, 2);
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
							printReport("All we had to do was follow tha damn train CJ!");
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
							printReport("All we had to do was follow tha damn train CJ!");
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
							printReport("All you had to do was follow tha damn train CJ!");
						else {
							ProjectDesc toChange = projects.get(i);
							toChange.setReadiness(ProjectDesc.READY);
							projects.changeProjectAt(i,toChange);
							model.setValueAt("MAKESPAN_READY", i, 2);
						}
					};break;
					case "jrdy":
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
		projectName.setEditable(false);
		Dimension size =new Dimension(200, 26);
		//projectName.setMaximumSize(size);
		//projectName.setMinimumSize(size);
		projectName.setPreferredSize(size);
		projectName.setToolTipText("There you may choose a project to further look into parameters");
		panel_3.add(projectName);
		panel_3.add(btnOpenProjectWindow);
		btnOpenProjectWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				Integer index = projectName.getSelectedIndex();
				if (projectStarted(index)) {
					//printReport(nameByLabel(projectName.getSelectedItem().toString()));
					Point tmp = frame.getLocation();
					sendMes(nameByLabel(projectName.getSelectedItem().toString()),"shfr "+ tmp.x + " " + tmp.y);
				}
				else JOptionPane.showMessageDialog(frame, "Please retry when project status will be appropriate");
			}
		});
		frame.getContentPane().add(panel, BorderLayout.WEST);
		frame.getContentPane().add(panel_1, BorderLayout.EAST);
		frame.getContentPane().add(panel_2, BorderLayout.NORTH);
		frame.getContentPane().add(panel_3, BorderLayout.SOUTH);
		frame.getContentPane().add(panel_4, BorderLayout.CENTER);
		table.setModel(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollTablePane.setViewportView(table);
		table.setColumnSelectionAllowed(true);
	/*	model.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				// тянем список первых элементов из всех строк таблицы.
				// меняем список элементов комбобокса.
			}
			
		});*/
		
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
