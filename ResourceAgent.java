package agentTest;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

import javax.swing.*;
public class ResourceAgent extends Agent{
	//gets ResName, ResVolume, PlanningHorizon
	private ArrayList<Integer> resavaliability = new ArrayList<Integer>();
	private ArrayList<ResourceReserve> reserves = new ArrayList<ResourceReserve>(); 
	private String resName, reportTo;
	private int firstres = 0, lastres = 0;// for report testing set lastres 10 else set this 0 at startup. lastres is a very first not affected day.
	private int resVolume;
	private int satisfaction;
	
	private void addres(int start, int days, int volume) {
		for (int i = 0; i<days;i++) {
			Integer l = resavaliability.get(i+start);
			l+=volume;
			resavaliability.set(i+start, l);
		}
	}
	private void sendmes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	//TODO: incorporate sendmes instead of all another code about sending messages 
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	private Integer reservePos(String name) {
		boolean found = false;
		int i = 0, len = reserves.size();
		while ((!found) & (i<len)) {
			if (reserves.get(i).name.equals(name)) {
				found = true;
			}else i++;	
		}
		if (found) return i;
		return -1;
		
	}
	
	//search for closest timespan with resources avaliable
	private Integer fetchApprTimespan(Integer start, Integer volume, Integer longevity ) {
		boolean found = false;
		Integer shift = -1;
		while (!found) {
			shift++;
			found = true;
			for (int i = 0; i<longevity;i++) {
				if (resavaliability.get(i+start +shift) < volume) {
					found = false;
					break;
				}
			}
		}
		return (start+shift);	
	}
	public String getLocalName(AgentController ac) {
		String s;
		try {
			s = ac.getName();
			s= s.substring(0,s.indexOf('@'));
		} catch (StaleProxyException e) {
			s = "";
		}
		
		return s;
	}
	
	Behaviour stopReporting = new OneShotBehaviour() {
	 	@Override
		public void action() {
	 		//printReport("invoked stopReporting");
			myAgent.removeBehaviour(IfFinishedRep);
			//send message that report is finished
			ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
			msg1.addReceiver(new AID(reportTo, AID.ISGUID));
			msg1.setContent("rres "+ resName + " " + resVolume);
			myAgent.send(msg1);
		}
	};
	Behaviour IfFinishedRep = new CyclicBehaviour() {
		@Override
		public void action() {
			//printReport("invoked IfFinishedRep");
			//check if report is finished
			if (firstres == lastres) {
				myAgent.removeBehaviour(Reporting);
				myAgent.addBehaviour(stopReporting);
				firstres = 0;
			}
		}
	};
	Behaviour Reporting = new CyclicBehaviour() {
		@Override
		public void action() {
			//printReport("invoked Reporting");
			if (firstres<lastres) {
				//printReport("invoked message assembling");
				//report next piece
				ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
				//printReport("message creation was successful");
				mes.addReceiver(new AID(reportTo, AID.ISGUID));
				//printReport("message start");
				//form message content: resname daynum volume	
				mes.setContent(resName + " " + (firstres) + " " + resavaliability.get(firstres));
				myAgent.send(mes);
				firstres++;
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
				String[] items = msg.getContent().split(" ");
				//case statements after switch (switchdef) are equvalent of switchdef.equal() so this should suffice.
				/*switch (items[0]){
				 * case "suba": {// try to get resource: " subtract avaliability"
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Reserve requested from " + i1 + " till " + i2);
					//lets see if there is needed 
					//int prefplace = i1;
					int i3 = fetchApprTimespan(i1,n,i2-i1);
					i2 += i3 - i1;
					i1 = i3;
					//System.out.println("Trying to reserve from " + (i1+1) + " till " + (i2+1));
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						//printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a - n);
						//if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						//System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}
					//create reserve	
					reserves.add(new ResourceReserve(msg.getSender().getName(),i1, n, i2-i1+1));
					System.out.println(msg.getSender());
					if (lastres<i2) {
						lastres = i2;
					}//
					ACLMessage mes =  new ACLMessage(ACLMessage.INFORM);
					mes.setContent("resq " + i1 + " " + i2);
					mes.addReceiver(new AID(msg.getSender().getName(),AID.ISGUID));
					myAgent.send(mes);
				}; break; 
				 * case "adda":{
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Trying to collect to " + i1 + " till " + i2);
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a + n);
						if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}; break; // give resource back: "add avaliability"
				 * case "acre": {
				//������������� �������	ACcept REserve
					printReport("Recieved acceptance of reserve");
				  //��������� ������� ������� �� ��� ����������� ����������� ���������.
				    String resOwner = msg.getSender().getName();//msg.getSender().getName() - ��� �����������.
				    int i = reservePos(resOwner);
				    if (i ==-1) {
				    	//��� �������, ��������, ��� ������.
				    	printReport("No reserve found!");
				    	sendmes(msg.getSender().getName(),"nres");
				    }else {
				    	//���� ������
				    	printReport("Reserve found, at position " + i);
				    	//��� ��������� � ������������� ���������
				    	
				    	//������� ������
				    	reserves.remove(i);
				    	// ��� �������� �� ������� ����������, ����� ������ ��� int, �� Integer, �.�. Integer ����������� �� object � ��������� remove(object) ������ remove(int) 
				    	
				    }; break; // reservation confirmation : "accept reserve";
				 * case "srep":{//report routine start
					ACLMessage msg1 =  new ACLMessage(ACLMessage.INFORM);
					reportTo = msg.getSender().getName();
					msg1.addReceiver(new AID(reportTo, AID.ISGUID));
					msg1.setContent("rres "+ resName + " " + resVolume);
					myAgent.send(msg1);
					if (lastres == 0) {//nothing to report
						msg1.setContent("rref "+ resName);
						myAgent.send(msg1);
					} else { //we can report something
						myAgent.addBehaviour(Reporting);
						myAgent.addBehaviour(IfFinishedRep);
					}
				}; break; // start routine to send report back to message sender. Only one report at a time is supported!
				case
				 * }
				 * */ 
				if (items[0].equals("suba")) {// try to get resource: " subtract avaliability"
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Reserve requested from " + i1 + " till " + i2);
					//lets see if there is needed 
					//int prefplace = i1;
					int i3 = fetchApprTimespan(i1,n,i2-i1);
					i2 += i3 - i1;
					i1 = i3;
					System.out.println("Trying to reserve from " + (i1+1) + " till " + (i2+1));
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a - n);
						//if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						//System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}
					//create reserve	
					reserves.add(new ResourceReserve(msg.getSender().getName(),i1, n, i2-i1+1));
					System.out.println(msg.getSender());
					if (lastres<i2) {
						lastres = i2;
					}//
					ACLMessage mes =  new ACLMessage(ACLMessage.INFORM);
					mes.setContent("resq " + i1 + " " + i2);
					mes.addReceiver(new AID(msg.getSender().getName(),AID.ISGUID));
					myAgent.send(mes);
				} else if (items[0].equals("adda")){
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Trying to collect to " + i1 + " till " + i2);
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a + n);
						if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}
					
				} 
				else if (items[0].equals("sbta")) {
					int i1 = Integer.parseInt(items[1])-1,
							i2 = Integer.parseInt(items[2])-1,
							n  = Integer.parseInt(items[3]);
						System.out.println("Trying to reserve from " + i1 + " till " + i2);
						//lets see if there is needed 
						if (n > resVolume) {
							ACLMessage mes3 = new ACLMessage(ACLMessage.INFORM);
							mes3.addReceiver(msg.getSender());
							mes3.setContent("cant");
							myAgent.send(mes3);
						}
						int prefplace = i1;
						prefplace = fetchApprTimespan(i1,n,i2-i1); 
						printReport("found start in: "+ prefplace);
				}
				else if (items[0].equals("srep")) {//report routine start
					ACLMessage msg1 =  new ACLMessage(ACLMessage.INFORM);
					reportTo = msg.getSender().getName();
					msg1.addReceiver(new AID(reportTo, AID.ISGUID));
					msg1.setContent("rres "+ resName + " " + resVolume);
					myAgent.send(msg1);
					if (lastres == 0) {//nothing to report
						msg1.setContent("rref "+ resName);
						myAgent.send(msg1);
					} else { //we can report something
						myAgent.addBehaviour(Reporting);
						myAgent.addBehaviour(IfFinishedRep);
					}
				}
				else if (items[0].equals("repr")) {
					for(ResourceReserve i: reserves) {
						printReport(i.volume + " " + i.date + " " + i.days);
					}
				}
				else if (items[0].equals("acre")){
				//������������� �������	ACcept REserve
					printReport("Recieved acceptance of reserve");
				  //��������� ������� ������� �� ��� ����������� ����������� ���������.
				    String resOwner = msg.getSender().getName();//msg.getSender().getName() - ��� �����������.
				    int i = reservePos(resOwner);
				    if (i ==-1) {
				    	//��� �������, ��������, ��� ������.
				    	printReport("No reserve found!");
				    	sendmes(msg.getSender().getName(),"nres");
				    }else {
				    	//���� ������
				    	printReport("Reserve found, at position " + i);
				    	//��� ��������� � ������������� ���������
				    	
				    	//������� ������
				    	reserves.remove(i);
				    	// ��� �������� �� ������� ����������, ����� ������ ��� int, �� Integer, �.�. Integer ����������� �� object � ��������� remove(object) ������ remove(int) 
				    	
				    }
				    
				    
				}else if (items[0].equals("care")){
					//������ ������� CAncel REserve
					printReport("Recieved cancellation of reserve");
					String resOwner = msg.getSender().getName();//msg.getSender().getName() - ��� �����������.
				    int i = reservePos(resOwner);
				    if (i != -1) {
				    	//add resource from reserve
				    	reserves.remove(i);
				    }
				}
			}
		}
	};
	
	
	@Override
	public void setup() {
		
		//JOptionPane.showMessageDialog(null , "Resource Agent name is:" + getAID().getLocalName());
		//System.out.println("Resource Agent name is: "+ getAID().getName());
		Object args[] = getArguments();
		if (args!=null && args.length>0) {
			//printReport("Invoked args getter");
			//reading arguments.
			//ResName,ResVolume,PlanningHorizon;
			resName = args[0].toString();
			resVolume = Integer.parseInt(args[1].toString());
			int planningHorizon = Integer.parseInt(args[2].toString());
			for (int i = 0;i < planningHorizon;i++) {
				resavaliability.add(resVolume); 
			}
			//System.out.println(resavaliability.size() + " " + resavaliability.get(0) + " " + resavaliability.size());
			
		}
		else System.out.println("no arguments!");  
		addBehaviour(NextMSGProcess);
	}
}