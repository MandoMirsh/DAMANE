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
import java.awt.event.*;
//import java.time.Clock;
public class ProjectController extends Agent {
	
	Integer projectsNum = 0;
	boolean newProject = false, newFileOpened = false;
	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	JTextField fileName = new JTextField("No File Selected!",30);
	
	JFileChooser fileopen = new JFileChooser();
	JButton openFile = new JButton("Open New File");
	JButton runSimulation = new JButton("Run Simulation");
	//GUI part:
	private void prepareGUI() {
		frame.setTitle("DAMANE - Darth Atin's Multi-Agent Network for timEtabling");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(400, 400);
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
					resAgentController = containerController.createNewAgent(proj, ProjectClass ,new String[]{myAgent.getAID().getName(),fileName.getText() ,(projectsNum).toString()});
					resAgentController.start();
					printReport(resAgentController.getName() + " created.");
					//if successfully created agent then add its descriptor.
					projects.add(new ProjectDesc(proj,fileName.getText()));
					fileName.setText("No File Selected!");
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
				String[] items = msg.getContent().split(" ");
				switch (items[0]) {
					case "stup":{
						//find 
					};break;
					case "ffin":{
						//find sender
						Integer newFin = Integer.parseInt(items[1]);
						//setNewFin
					}; break; 
				}
			}
		}
	};
	@Override
	public void setup() {
		containerController = this.getContainerController();
		//
		prepareGUI();
		panel.add(openFile);
		fileName.setEditable(false);
		panel.add(fileName);
		panel.add(runSimulation);
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				int ret = fileopen.showOpenDialog(null);
				switch (ret) {
					case (JFileChooser.APPROVE_OPTION):{ fileName.setText(fileopen.getSelectedFile().getPath()); newFileOpened = true;};break;
					case (JFileChooser.CANCEL_OPTION):{};break;
					case(JFileChooser.ERROR_OPTION):{};break;
				}
			}
		});
		runSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newProject = newFileOpened;
				newFileOpened = false;
			}
		});
		//Object[] args = getArguments();
		
		this.addBehaviour(CheckNewProject);
		
		frame.add(panel);
		frame.setVisible(true);
		//System.out.println(ZonedDateTime.now().toString());
		//System.out.println(LocalDateTime.now().toString());
	}
}
