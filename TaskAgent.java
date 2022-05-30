package agentTest;


import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class TaskAgent extends Agent {
	/**
	 * @param reciever - global JADE agent name
	 * @param msg - message content to send
	 */
	private void sendmes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	
	private String constructName(String name, String projname, String agentName) {
		return name + agentName + projname;
	}
	
	private String constructTaskName(String name, String projname) {
		return constructName(name, projname, "task");
	}   
	
	private String constructResName(String name, String projname) {
		return constructName(name, projname, "res");
	}
	private int findPredName(String name) {
		int i = 0, i2 = prev.size();
		while (i <i2){
			prev.get(i).equals(name);
			if (prev.get(i).equals(name)) return i;
			i++;
		}
		
		return -1;
	}
	
	ArrayList<String> getTopFirst(ArrayList<String> lst){
		ArrayList<String> ret = new ArrayList<>();
		if (lst.size() == 0) return ret;
		else
			ret.add(lst.get(0));
		return ret;
	}
	
	ArrayList<String> getAfterTopList(ArrayList<String> lst){
		ArrayList<String> ret = new ArrayList<>();
		if (lst.size()<2)
			return ret;
		else
			ret = new ArrayList<>(lst);
			ret.remove(0);
		return ret;
	}

	private String reportTo, projName = "";
	private Map<String, String> commands = new HashMap<String,String>(), outputVoc = new HashMap<String, String>();
	private ArrayList<String> prev = new ArrayList<>(),next = new ArrayList<>();//previous and next ones
	private Map<String,Integer> mPrev = new HashMap<String,Integer>(),mNext = new HashMap<String,Integer>();
	private ArrayList<ResourceReserve> reserved = new ArrayList<>(); //resource reserves
	private int earlyStart = -1, earlyFinish, lateStart, lateFinish = 0, timeReq = 0; 
	private int newstart, newfinish;
	private int satisfaction = 100;//satisfaction percentage
	private boolean initialFinished = false; //indicator that we have forgone initial boundaries establishment
	private Integer gotMes1 = 0, gotMes2 = 0, mesToGet1 = 0, mesToGet2;//mesToGet1 - how many messages will I get if I go forwards the graph, mesToget2 - backwards.
	private MessagesToSend sendQueue = new MessagesToSend();
	private boolean myStartIsMoved = false;//this one is flag for changes in initialization 
	private Integer resAnswCount = 0;//how many Resourse agents replied so far during negotiations
	private JobWeight myWeight;
	private Integer toRecieveFinishes = 0, toRecieveStarts = 0; 
	private boolean startAgent = false, finishAgent = false;
	private static final Integer STARTED = 0, NEGOTIATIONS = 1, MOVED = 2, WAIT_RECONF = 3, PLANNED = 4;
	//LISTENNING_TO_INIT = , LISTENING_TO_LATES = , LISTENING_TO_EARLIES = , INITIATED = ,
	private Integer agentStatus = STARTED;
	private void updateWeight() {
		myWeight = new JobWeight(earlyFinish - lateStart,lateFinish - newstart);
	}
	private void updateEarly(int newStart) {
		earlyStart = newStart;
		earlyFinish = earlyStart + timeReq;
	}
	private void updateLate(int newFinish) {
		lateFinish = newFinish;
		lateStart = lateFinish - timeReq;
	}
	private boolean needShift() {
		return (earlyFinish>lateFinish);
	}
	private void initiateVocabulary() {	
		commands.put("acre","RESERVE_ACCEPTED");
		commands.put("dere","RESERVE_DECLINED");
		commands.put("meaf", "MY_LATE_FINISH");
		commands.put("meat", "MY_EARLY_START");
		commands.put("mini", "MY_INITIALIZATION_INT");
		commands.put("stag", "NEAR_NET_START");
		commands.put("fiag", "AT_NET_FINISH");
		commands.put("jore", "REPORT_STATUS");
		commands.put("stup", "STARTUP_TIME");
		commands.put("remp", "REMOVE_FROM_PREV");
		commands.put("tire", "TASK_READY");
		commands.put("tnre", "TASK_NOT_READY");
		commands.put("mnef", "PREDECESSOR_MOVED");
		commands.put("mnes", "SUCCESSOR MOVED");
	}
	private void outputVocabularyInit() {
		outputVoc.put("TRY_TO_RESERVE", "rreq");
		outputVoc.put("STARTED_UP", "stup");
		outputVoc.put("REPORT_STATUS", "jore");
		outputVoc.put("BREAK_PRECEDENCE", "remp");
		outputVoc.put("I_AM_READY", "tire");
		outputVoc.put("I_AM_NOT_READY", "tnre");
		outputVoc.put("MY_NEW_FINISH", "mnef");
		outputVoc.put("MY_NEW_LATE_START", "mnes");
		outputVoc.put("MY_NEW_LATE_START", "mnes");
		outputVoc.put("RELEASING_RESOURSE", "rref");
		outputVoc.put("MY_NEW_START","meat");
		outputVoc.put("GET_RESERVED", "rget");
		outputVoc.put("MY_NEW_FINISH", "meaf");
		outputVoc.put("ABORT_NEGOTIATIONS","abre");
	}
	String commandExplain(String command) {
		if (commands.get(command) == null)
			return "NO_EXPLANATION";
		return commands.get(command);
	}
	String labelToCommand(String label) {
		return outputVoc.get(label);
	}
	private void sendReport(String agentName) {
		sendQueue.add(new SendingTask(getTopFirst(next),labelToCommand("REPORT_STATUS") + " " + earlyStart + " " + earlyFinish+ " " + agentName));
	}
	private void updateLates(String successor, int newstart) {
		mNext.put(successor,newstart);
		ArrayList<Integer> starts = new ArrayList<Integer>(mNext.values());
		starts.sort(Comparator.naturalOrder());
		int l = starts.get(0);
		starts.clear();
		if (l!=lateFinish)
			updateLate(l);
	}
	Behaviour SendingBehaviour = new CyclicBehaviour() {
		@Override
		public void action() {
			ArrayList<String> next = sendQueue.getNextToSend();
				
				if (next.size()!=0) {
					//printReport("Trying to send message: "+ next.get(0) + " to: "+ next.get(1));
					String sendTo = next.get(1),
							message = next.get(0);	
					//printReport("output "+ sendTo + " "+ message);
					sendmes(sendTo,message);
				}
			}
	};
	
	
	//NEGOTIATIONS PART
	private ResDescStore resourseDescs = new ResDescStore(); 
	private static final Integer NEG_NONE = 0, NEG_START = 1, NEG_RUN = 2, NEG_RESET = 3, NEG_WAIT = 4, NEG_NEXT = 5, NEG_STOP = 6;
	Integer resourcePointer = 0, negotiationStatus = NEG_NONE, closeMore = -1;
	
	private void releaseResourse() {
		//достаём 
		for (int i = 0;i<resourseDescs.size();i++) {
			ResourceDescriptor tmp = resourseDescs.get(i);
			String sendNext = tmp.getName();
			ArrayList<String> recievers = new ArrayList<>();
			recievers.add(sendNext);
			//rref DATE, T, N
			sendQueue.add(new SendingTask(recievers,labelToCommand("RELEASING_RESOURSE") + " " +earlyStart + " " + timeReq + " " + tmp.volume() ));
		}	
	}
	
	private void releaseResource(ResourceDescriptor tmp) {
			String sendNext = tmp.getName();
			ArrayList<String> recievers = new ArrayList<>();
			recievers.add(sendNext);
			//rref DATE, T, N
			sendQueue.add(new SendingTask(recievers,labelToCommand("RELEASING_RESOURSE") + " " +earlyStart + " " + timeReq + " " + tmp.volume() ));	
	}
	Behaviour StartNegotiation = new OneShotBehaviour() {
		@Override
		public void action() {
			//we send our current position at negotiations
			sendReport(myAgent.getAID().getName());
			
			ResourceDescriptor tmp = resourseDescs.get(resourcePointer);
			String message = labelToCommand("TRY_TO_RESERVE") + " " + earlyStart + " " + timeReq + " " +tmp.volume()+ " "+myWeight.getWeights().get(0) + " "+myWeight.getWeights().get(1);
			ArrayList <String> recievers = new ArrayList<>();
			recievers.add(tmp.getName());
			sendQueue.add(new SendingTask(recievers,message));
		}
	};
	Behaviour ResetNegotiation = new OneShotBehaviour() {
		@Override
		public void action() {
			for (int i = resourcePointer - 1;i>=0;i--) {
				//ВОЗВРАТ РЕСУРСОВ.
				printReport("Trying to give back resources at the " + i + "'th position");
				 releaseResource(resourseDescs.get(i));
			}
			updateEarly(newstart);
			//SEND BACK info on new start Это следует делать при помощи:
			ArrayList<String> recievers1 = new ArrayList<>(mNext.keySet());
			//SEND FORWARD  new finish info.
			SendNewFinish();
			resourcePointer = 0;
			if (earlyFinish<=lateFinish)
				negotiationStatus = NEG_START;
			else
				negotiationStatus = NEG_WAIT;//the job is moving forward, so it need some time to wait for others to move
			updateWeight();
		}
	};
	Behaviour Negotiation = new CyclicBehaviour() {
		@Override
		public void action() { 
			if (negotiationStatus == NEG_START) {
				myAgent.addBehaviour(StartNegotiation);
				negotiationStatus =  NEG_RUN;
			}
			if (negotiationStatus == NEG_NEXT) {
				resourcePointer++;
				if (resourcePointer<resourseDescs.size())
					negotiationStatus = NEG_START;//there are resources left to negotiate on
				else
					negotiationStatus = NEG_STOP;//we fulfilled our negotiation purposes!
			}
			if (negotiationStatus == NEG_WAIT) {
				if (earlyFinish<=lateFinish)
					negotiationStatus = NEG_RESET;
			}
			if (negotiationStatus == NEG_RESET) {
				myAgent.addBehaviour(ResetNegotiation);
				negotiationStatus = NEG_RUN;
			}
			
		}
	};
	Behaviour ClosedDoors = new CyclicBehaviour() {
		@Override
		public void action() {
			if (closeMore>0) {
				closeMore--;
			}	
			if (closeMore==0) {
				closeMore--;
				myAgent.addBehaviour(NxtMSGProc);
			}
		}
	};
	void SendNewFinish() {
			ArrayList <String> recievers = new ArrayList<String>(next);
			String message = "meaf "+ (earlyFinish);
			sendQueue.add(new SendingTask(recievers,message));
	};
	Behaviour AgentStatusProcessing = new CyclicBehaviour() {
		@Override
		public void action() {
			if (agentStatus == MOVED) {
				myAgent.removeBehaviour(NxtMSGProc);
				//printReport("I moved, so I inform everyone!");
				//inform about our current status
				ArrayList<String> recievers = new ArrayList<>(getTopFirst(next));
				sendQueue.add(new SendingTask(recievers,labelToCommand("I_AM_NOT_READY") + " 0 " + myAgent.getAID().getName()));
				//send to the next ones, that we are moved.
				ArrayList<String> recievers2 = new ArrayList<>(next);
				sendQueue.add(new SendingTask(recievers2,labelToCommand("MY_NEW_FINISH") +" "+  earlyFinish));
				//send to the previous ones
				ArrayList<String> recievers3 = new ArrayList<>(prev);
				sendQueue.add(new SendingTask(recievers3,labelToCommand("MY_NEW_START") +" " + newstart));
				//We need to make sure we are satisfied with our position, so:
				ArrayList<String> recievers4 = new ArrayList<>();
				recievers4.add(resourseDescs.get(resourcePointer).getName());
				sendQueue.add(new SendingTask(recievers3,labelToCommand("ABORT_NEGOTIATIONS")));
				
				closeMore = 600;
				
				negotiationStatus = WAIT_RECONF;
				
				
			}
			if (agentStatus == NEGOTIATIONS) {
				if (negotiationStatus == NEG_STOP) {
					//send readiness
					ArrayList<String> recievers = new ArrayList<>(getTopFirst(next));
					sendQueue.add(new SendingTask(recievers,labelToCommand("I_AM_READY") +" 0 "+  myAgent.getAID().getName()));
					agentStatus = PLANNED;
				}
			}
			if (agentStatus == WAIT_RECONF) {
				if (myWeight.getWeights().get(1) >= 0){
					printReport("Reinitiating negotiations with "+ myWeight.getWeights().get(1));
					//make sure we reset negotiations
					negotiationStatus = NEG_RESET;
					agentStatus = NEGOTIATIONS;
				}
				/*else
					{
						try {
							Thread.sleep(1000);
							}
						catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}*/
			}
			//PLANNED && RESET -> NEGOTIATIONS + ResetNegotiation
		}
	};
	
	Behaviour NxtMSGProc = new CyclicBehaviour() {
		@Override
		public void action() {
			//checking new messages
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			if (msg!=null) {
				String[] items = msg.getContent().split(" ");
				String name = msg.getSender().getName();
				int l = new ArrayList<>(mNext.values()).get(0);
				if (items.length>1) {
					l = Integer.parseInt(items[1]);
				}
				switch (commandExplain(items[0].toString())){
				//we cannot get extra
					case "MY_EARLY_START":{ 
							mNext.put(name,l);
							ArrayList<Integer> starts = new ArrayList<Integer>(mNext.values());
							starts.sort(Comparator.naturalOrder());//from lesser to great
					 		l = starts.get(0);
							//l now is the earliest start of all the successors
				 			if (l>lateFinish) {
				 				//if we have   stopped yet, we just changed our things and that's it
				 				updateLate(l);
				 				updateWeight();
				 				//if we are in the middle of negotiations, we need to reset them, our parameters were changed otherwise we have no need in resetting
				 				printReport("moved by next one: " + msg.getSender().getName()); 
				 				if (agentStatus != PLANNED) agentStatus = MOVED;
				 			}
				 	}; break;
				 	
					case "MY_LATE_FINISH":{
						mPrev.put(name,l);
							if (l > newstart) {
								newstart = l;
								printReport("moved ny previous one: " + msg.getSender().getName());
								agentStatus = MOVED;
								updateWeight(); 
							}		
					}; break;
				//negotiations income handler (both acre and dere)
					case "RESERVE_ACCEPTED":{
						printReport("acre!");
						ArrayList<String> replyTo = new ArrayList<>();
						replyTo.add(msg.getSender().getName());
						sendQueue.add(new SendingTask(replyTo, labelToCommand("GET_RESERVED")));
						negotiationStatus = NEG_NEXT;
					};break;
					case "RESERVE_DECLINED":{
						printReport(msg.getContent());
						newstart = Integer.parseInt(items[1]);
						printReport("need to move to: "+ newstart);
						updateEarly(newstart);
						agentStatus = MOVED;
						updateWeight();
					};break;
					case "REPORT_STATUS":{
						sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent()));
					} break;
					case "TASK_READY":{
						sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent()));
					} break;
					case "TASK_NOT_READY":{
						sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent()));
					}break;
				}
				
			}
		}
	};
	//time to start negotiations
	Behaviour ChangeSendingBeh = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(NextMSGProcess);
			myAgent.addBehaviour(NxtMSGProc);
			myAgent.addBehaviour(Negotiation);
			myAgent.addBehaviour(AgentStatusProcessing);
			myAgent.addBehaviour(ClosedDoors);
			for (String s:next) {
			mNext.put(s, lateFinish);	
			}
			for (String s:prev) {
				mPrev.put(s, earlyStart);
			}
			agentStatus = NEGOTIATIONS;
			negotiationStatus = NEG_START;
			newstart = earlyStart;
		}
	};
	//MSG Process atStartUp
	Behaviour NextMSGProcess = new CyclicBehaviour() {
		@Override
		public void action() {
			//checking new messages
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			//if there's any, process it
			if (msg!=null) {
				String sender = (msg.getSender().getName());
				//printReport("Message not null! " + msg.getContent());
				String[] items = msg.getContent().split(" ");
				switch (commandExplain(items[0].toString())){
				/*case "srep":printReport("srep");{String message = "";// rept START FINISH
				ArrayList<String> send = new ArrayList<>();
				send.add(sender);
				sendQueue.add(new SendingTask(send,"rept "+earlyFinish));
				} break;*/
				case "MY_LATE_FINISH": 
					{Integer l = Integer.parseInt(items[1]);
						//count initial run messages
						gotMes1++;
						
						if (l > earlyStart) {
							//printReport("Early Start moved, sending messagelabelToCommands. . .");
							updateEarly(l);
							//myAgent.addBehaviour(SendNewFinish); 
							//sendmes(sendTo,"meaf "+earlyFinish);
							if (gotMes1>mesToGet1)
								sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
							
						}
						if (gotMes1==mesToGet1) {
							sendQueue.add(new SendingTask(next,"meaf " + earlyFinish));
						}
						myWeight = new JobWeight(earlyFinish - lateStart,lateFinish - earlyStart);
					}; break;
				case "MY_EARLY_START": //MylatEstArT
					//printReport("gotmeat");
				 	{int l = Integer.parseInt(items[1]);
				 		gotMes2++;
				 		if (l>lateFinish) {
				 			updateLate(l);
				 			if (gotMes2>mesToGet2)
				 			{
				 				sendQueue.add(new SendingTask(prev, "meat " + lateStart));
				 			}
				 		}
				 		if (gotMes2==mesToGet2) {
				 			//printReport("sentmeat");
				 			sendQueue.add(new SendingTask(prev, "meat " + lateStart));
				 			myWeight = new JobWeight(earlyFinish - lateStart,lateFinish - earlyStart);
				 			//sendQueue.add(new SendingTask(prev, "tiko "+ (earlyFinish - lateStart) + " " + (lateFinish - earlyStart)));
				 			
				 		}
			 				
				 	};break;
				 	
				case "MY_INITIALIZATION_INT":{// we need to start two behaviours 1) send strt to all others 2) //CORRECT
						//Проблема - сообщения с нулями после начальной инициализации избыточны.
						Integer sendTo1 = Integer.parseInt(items[1]);
						//add predecessor
						if (findPredName(sender) == -1) {
							//printReport("new predecessor! Name's: "+ sender);
							prev.add(sender);
							mesToGet1++;
						}
						
						//printReport("input "+ msg.getSender().getName() + " "+ msg.getContent());
						
						//getTopFirst, getAfterTopList
						ArrayList<String> send1 = getTopFirst(next), send2 = getAfterTopList(next);
						if (!initialFinished) {
							//printReport("initiated!");
							if (send2.size()!=0)//"mini 0" after initial transmission is unnecessary
								sendQueue.add(new SendingTask(send2,"mini 0"));
							initialFinished = true;//initial transmission has been made
							sendTo1++;
						}
						sendQueue.add(new SendingTask(send1, "mini " + sendTo1));
						//sendQueue.add(new SendingTask(send1, msg.getContent()));
				}
					break;
				case "REMOVE_FROM_PREV": {

					int i = findPredName(sender); 
					if (i>-1) {
						prev.remove(i);
						mesToGet1--;
					}
					};break;
				case "STARTUP_TIME":{
					printReport("stup");
					//если не стартовый, то пересылаем дальше полученное сообщение
					sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent()));
					printReport(getTopFirst(next).get(0) + "!!");
					sendReport(myAgent.getAID().getName());//свой 
					if (next.size()>1)
						sendQueue.add(new SendingTask(getAfterTopList(next), "stup"));
					myAgent.addBehaviour(ChangeSendingBeh);
				};break;
				case "NEAR_NET_START": startAgent = true; break;
				case "AT_NET_FINISH": finishAgent = true; break;
				case "TASK_NOT_READY": sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent())); break;
				case "TASK_READY": sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent())); break;
				case "REPORT_STATUS":{
					sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent()));
				} break;
				//case "tiko":{};break; 
				case "NO_EXPLANATION":{
					printReport(items[0].toString() + " !!!");
				} break;
				default: printReport("else" + msg.getContent()); break;
				}
			
			}
		}
	};
	void skipUpForTesting() {
		addBehaviour(NxtMSGProc);
		this.addBehaviour(SendingBehaviour);
		next.add("pr1task3@127.0.0.1:80/JADE");
		prev.add("pr1task3@127.0.0.1:80/JADE");
		initialFinished = true;
	}
	void startUp(String controller) {
		sendmes(controller,"stup");
		this.addBehaviour(NextMSGProcess);
		this.addBehaviour(SendingBehaviour);
	}
	@Override
	public void setup() {
		initiateVocabulary();
		outputVocabularyInit();
		//printReport("Task agent booting. . .");
		//First, we get arguments Those are for initial setup
		//TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES 
		Object args[] = getArguments();
		int sucNum = Integer.parseInt(args[2].toString()),
			resNum = Integer.parseInt(args[3].toString()),
			sucStart = 5, resNmStart = sucStart + sucNum,  
			resVolStart = resNmStart + resNum;
			timeReq = Integer.parseInt(args[4].toString()); 
				
				//add successors
				for (int i = 0; i<sucNum;i++) {
					next.add(args[sucStart+i].toString());
					//printReport(args[sucStart+i].toString() + " is added to successors");
				}
				
				//add resource descriptors
				ResourceDescriptor tmp;
				for (int i = 0;i<resNum;i++) {
					tmp = new ResourceDescriptor(args[resNmStart+i].toString(), timeReq, Integer.parseInt(args[resVolStart+i].toString()));
					//printReport("res: " + tmp.getName() + " " + tmp.volume());
					resourseDescs.add(tmp);
				}

			mesToGet2 = next.size();
			
			startUp(args[0].toString());
			//printReport("stup");
			/*printReport("now I'll print which arguments were taken");	
			//let's see which ones are there
			System.out.println();
			for(int i=0;i<args.length;i++) {
				System.out.println(args[i].toString());
			}*/
		}
}

