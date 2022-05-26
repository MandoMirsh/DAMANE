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
	private int satisfaction = 100;//satisfaction percentage
	private boolean initialFinished = false; //indicator that we have forgone initial boundaries establishment
	private Integer gotMes1 = 0, gotMes2 = 0, mesToGet1 = 0, mesToGet2;//mesToGet1 - how many messages will I get if I go forwards the graph, mesToget2 - backwards.
	private MessagesToSend sendQueue = new MessagesToSend();
	private boolean myStartIsMoved = false;//this one is flag for changes in initialization 
	private Integer resAnswCount = 0;//how many Resourse agents replied so far during negotiations
	private JobWeight myWeight;
	private ResDescStore resourseDescs = new ResDescStore(); 
	private Integer toRecieveFinishes = 0, toRecieveStarts = 0; 
	private boolean startAgent = false, finishAgent = false;
	private static final Integer STARTED = 0, NEGOTIATIONS = 1, MOVED = 2, PLANNED = 3;
	//LISTENNING_TO_INIT = , LISTENING_TO_LATES = , LISTENING_TO_EARLIES = , INITIATED = ,
	private Integer agentStatus = STARTED;
	private void updateWeight() {
		myWeight = new JobWeight(earlyFinish - lateStart,lateFinish - earlyStart);
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
		outputVoc.put("MY_NEW_EARLY_FINISH", "mnef");
		outputVoc.put("MY_NEW_LATE_START", "mnes");
		outputVoc.put("RELEASING_RESOURSE", "rref");
	}
	String commandExplain(String command) {
		if (commands.get(command) == null)
			return "NO_EXPLANATION";
		return commands.get(command);
	}
	String labelToCommand(String label) {
		return outputVoc.get(label);
	}
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
	
/*	Behaviour Reporting = new OneShotBehaviour(){
		@Override
		public void action() {
			ArrayList <String> recievers = new ArrayList<>();
			recievers.add(myAgent.getArguments()[0].toString());
			String message = "rrep " + ((agentStatus==PLANNED)?1:0) + " " + earlyStart + " " + earlyFinish + " "  
							+ myWeight.getWeights().get(0) + " " + myWeight.getWeights().get(1); 
				printReport(message);		
				//sendQueue.add( new SendingTask(recievers, message));
		}
	};*/
	Behaviour NegotiationStart = new OneShotBehaviour() {
		@Override
		public void action() {
			String mesPrefics = labelToCommand("TRY_TO_RESERVE") + " " + earlyStart + " " + timeReq + " ", mesPostfix = " "+myWeight.getWeights().get(0) + " "+myWeight.getWeights().get(1);
			resAnswCount = 0;
			for (int i = 0;i<resourseDescs.size();i++) {
				ArrayList <String> recievers = new ArrayList<>();
				ResourceDescriptor tmp = resourseDescs.get(i);
				String message = mesPrefics +tmp.volume()+ mesPostfix;	//int start, int span, int volume, JobWeight mark
				recievers.add(tmp.getName());
				//printReport("RESERVE "+tmp.volume()+" FROM: "+tmp.getName());
				//printReport(tmp.getName()+ " sending request. . .");
				sendQueue.add(new SendingTask(recievers,message));
				
				tmp.removeComplete();
				resourseDescs.set(i, tmp);
			}
			agentStatus = NEGOTIATIONS;
			myAgent.addBehaviour(IfNegoiationEnded);
		}
	};
	Behaviour IfNegoiationEnded = new CyclicBehaviour() {
		@Override
		public void action() {
			if (resAnswCount == resourseDescs.size()) {
				myAgent.addBehaviour(NegotiationStop);
			}
		} 
	};
	Behaviour NegotiationStop = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(IfNegoiationEnded);
			if  (resourseDescs.allSet())
			{ //really ended:
				if (agentStatus == MOVED)
				{
					printReport("I am moved, so I need to reinitiate negotiations");
					myAgent.addBehaviour(NegotiationStart);
				}
				else 
				{
					printReport("Negotiations finished!");
					//sending confirmation to resource agents
					ArrayList <String> recievers = new ArrayList<>();
					for (int i = 0;i<resourseDescs.size();i++) {
						recievers.add(resourseDescs.get(i).getName());
					}
					if (recievers.size()>0)
						sendQueue.add(new SendingTask(recievers,"rget"));
					//recievers.clear();
					sendQueue.add(new SendingTask(getTopFirst(next),labelToCommand("I_AM_READY") +" 0 "+  myAgent.getAID().getName()));//tire - Task Is REady, tnre - Task Not REady
					agentStatus = PLANNED;// setting flag that the negotiations ended and task Agent is now just listening
					//recievers.clear();
				}
			}
			else
			{
				//сменить свой ранний старт и ранний финиш
				updateEarly(resourseDescs.maxDate());
				if (needShift())//если ранний финиш >позднего старта,  
				{
					//извещаем последователей о своём раннем финише
					toRecieveStarts = next.size();
					SendNewFinish();
					// активируем поведение-заглушку, которое ждёт ответа от последователей о своих ранних стартах и только потом рестартит процесс договора.
					printReport("startCollection");
					myAgent.addBehaviour(startCollection);
				}
				
					 
					
			}
		}
	};
	//смотрим, получили ли ответы о старте от последователей, если да - начинаем переговоры с ресурсами заново.
	Behaviour startCollection = new CyclicBehaviour() {
		@Override
		public void action() {
			if (toRecieveStarts == 0) {
				myAgent.addBehaviour(NegotiationStart);
			}
				
		}
		
	};
	//заготовка поведения для ужимки с конца.( вроде бы нет вероятности того, что это понадобится, но на всякий)
	Behaviour finishCollection = new CyclicBehaviour() {
		@Override
		public void action() {
			if (toRecieveFinishes == 0) {
				myAgent.addBehaviour(NegotiationStart);
			}
				
		}
		
	};
	void SendNewFinish() {
			ArrayList <String> recievers = new ArrayList<String>(next);
			String message = "meaf "+ (earlyFinish);
			sendQueue.add(new SendingTask(recievers,message));
	};
	/*Behaviour SendNewStart = new OneShotBehaviour() {
		@Override
		public void action() {
			//if (sendStart) {
				ArrayList <String> recievers = new ArrayList<String>(prev);
				String message = "meat "+ (lateStart);
				sendQueue.add(new SendingTask(recievers,message));
				toRecieveFinishes += prev.size();
			//}
			
		}
	};*/
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
				case "MY_EARLY_START":
					mNext.put(name,l);
						{ 
							ArrayList<Integer> starts = new ArrayList<Integer>(mNext.values());
							starts.sort(Comparator.naturalOrder());
					 		l = starts.get(0);
							starts.clear();
							//l now is the earliest start of all the successors
				 			if (l>lateFinish) {
				 				//if we have   stopped yet, we just changed our things and that's it
				 				updateLate(l);
				 				
				 				sendQueue.add(new SendingTask(prev, "meat " + lateStart));
				 				toRecieveFinishes+=prev.size();
				 				
				 				updateWeight();
				 			}
				 			else
				 			{
				 				ArrayList<String> reciever = new ArrayList<>();
				 				reciever.add(name);
				 				sendQueue.add(new SendingTask(reciever, "reaf " + earlyFinish));
				 			}
				 				
				 		}; break;
				 	
				case "MY_LATE_FINISH":{
					mPrev.put(name,l);
						if (l > earlyStart) {
							updateEarly(l);
							//set flag that start is gonna be changed
							//myAgent.addBehaviour(SendNewFinish); 
							//sendmes(sendTo,"meaf "+earlyFinish);
							if (gotMes1>mesToGet1)
								sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
					
						}
						if (gotMes1==mesToGet1) {
							sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
						}
						updateWeight();
					}; break;
				case "reaf": 
					mPrev.put(name,l);
					toRecieveFinishes--;
					 
					break;
				//negotiations income handler (both acre and dere)
				case "RESERVE_ACCEPTED":{
				printReport("acre!");
					String sender = msg.getSender().getName();
					resourseDescs.setUp(sender);
					resAnswCount++;
				};break;
				case "RESERVE_DECLINED":{
					//как вариант - сместить свои сроки выполнения и известить соседей, извините, последователей о смещении сроков.
					/*resAnswCount++;
					printReport("resAnswCount: "+resAnswCount);
					String sender = msg.getSender().getName();
					resourseDescs.setDate(sender, l);
					*/
					printReport(msg.getContent());
					Integer newStart = Integer.parseInt(items[1]);
					updateEarly(newStart);
					if (needShift()) {
						updateLate(earlyFinish);
						sendQueue.add(new SendingTask(next,labelToCommand("MY_NEW_EARLY_FINISH") + " " + lateFinish));
					}
					updateWeight();
					myAgent.removeBehaviour(IfNegoiationEnded);
					myAgent.addBehaviour(NegotiationStart);
					sendReport(myAgent.getAID().getName());
				};break;
				case "REPORT_STATUS":{
					sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent()));
				} break;
				case "PREDECESSOR_MOVED":{
					printReport("agent " + msg.getSender().getLocalName() + " says that it moved.");
					if (agentStatus == PLANNED) {
						printReport("I was planned, but I no more am! Due to message from: " + msg.getSender().getLocalName());
						releaseResourse();
						ArrayList<String> recievers = new ArrayList<>(getTopFirst(next));
						sendQueue.add(new SendingTask(recievers,labelToCommand("I_AM_NOT_READY") + " 0 " + myAgent.getAID().getName()));
						agentStatus = MOVED;
						myAgent.removeBehaviour(IfNegoiationEnded);
					}
					if (l > earlyStart) {
						printReport("So I need to move!");
						updateEarly(l);
						//now we need to send our successors message that we moved:
						sendQueue.add(new SendingTask(next,labelToCommand("MY_NEW_EARLY_FINISH") + " " + lateFinish));
					}
					/*printReport("agent " + msg.getSender().getLocalName() + " says that it moved.");
					// если это число больше нашего раннего старта - нужно а) если статус агента -запланировано, то отдать ресурсы всем агентам ресурса и отослать сообщение о том, что агент снова активен
					if (l > earlyStart) {//we need move
						printReport("So I need to move too!");
						if (agentStatus == PLANNED) {
							releaseResourse();
							ArrayList<String> recievers = new ArrayList<>(getTopFirst(next));
							sendQueue.add(new SendingTask(recievers,labelToCommand("I_AM_NOT_READY") + " 0 " + myAgent.getAID().getName()));
							agentStatus = MOVED;
							myAgent.removeBehaviour(IfNegoiationEnded);
						}
						// б) проапдейтить ранние старт и финиш, проверить, пересекли ли границу позднего финиша, если да - присваиваем ранние поздним, шлём нашим последователям сообщение о новом своём финише
						
						updateEarly(l);
						if (needShift()) {
							updateLate(earlyFinish);
							sendQueue.add(new SendingTask(next,labelToCommand("MY_NEW_EARLY_FINISH") + " " + lateFinish));
							agentStatus = MOVED;
							myAgent.removeBehaviour(IfNegoiationEnded);
						}
						updateWeight();
						if (agentStatus != PLANNED) {
							myAgent.addBehaviour(NegotiationStart);
							sendReport(myAgent.getAID().getName());
						}
					}
					else
						printReport("But I have no need to move!");
					*/
				} break;
				case "SUCCESSOR MOVED":
					// now we need to update all possible late finishes and starts.
					if (toRecieveStarts >0) toRecieveStarts--;
					updateLates(msg.getSender().getName(),l);
					updateWeight();
					break;
				case "TASK_NOT_READY": sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent())); break;
				case "TASK_READY": sendQueue.add(new SendingTask(getTopFirst(next), msg.getContent())); break;
				case "NO_EXPLANATION":{
					printReport(items[0].toString() + " !!! " + msg.getSender().getName() +" !! " + msg.getContent());
				}
				/*case "srep":{
					//старт поведения-отчётности
				}*/
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
			myAgent.addBehaviour(NegotiationStart);
			for (String s:next) {
			mNext.put(s, lateFinish);	
			}
			for (String s:prev) {
				mPrev.put(s, earlyStart);
			}
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
				case "REMOVE_FROM_PREV": {//TODO:

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
				case "PREDECESSOR_MOVED":{
					Integer l = Integer.parseInt(items[1]);
					printReport("agent " + msg.getSender().getLocalName() + " says that it moved.");
					if (agentStatus == PLANNED) {
						printReport("I was planned, but I no more am! Due to message from: " + msg.getSender().getLocalName());
						releaseResourse();
						ArrayList<String> recievers = new ArrayList<>(getTopFirst(next));
						sendQueue.add(new SendingTask(recievers,labelToCommand("I_AM_NOT_READY") + " 0 " + myAgent.getAID().getName()));
						agentStatus = MOVED;
						myAgent.removeBehaviour(IfNegoiationEnded);
					}
					if (l > earlyStart) {
						printReport("So I need to move!");
						updateEarly(l);
						//now we need to send our successors message that we moved:
						sendQueue.add(new SendingTask(next,labelToCommand("MY_NEW_EARLY_FINISH") + " " + lateFinish));
					}/*
					// если это число больше нашего раннего старта - нужно а) если статус агента -запланировано, то отдать ресурсы всем агентам ресурса и отослать сообщение о том, что агент снова активен  
					Integer nlf = Integer.parseInt(items[1]);
					if (nlf > earlyStart) {
						if (agentStatus == PLANNED) {
							releaseResourse();
							ArrayList<String> recievers = new ArrayList<>(getTopFirst(next));
							sendQueue.add(new SendingTask(recievers,labelToCommand("I_AM_NOT_READY")));
							agentStatus = NEGOTIATIONS;
						}
						else
							myAgent.removeBehaviour(IfNegoiationEnded);
						// б) проапдейтить ранние старт и финиш, проверить, пересекли ли границу позднего финиша, если да - присваиваем ранние поздним, шлём нашим последователям сообщение о новом своём финише
						updateEarly(nlf);
						if (needShift()) {
							updateLate(earlyFinish);
							sendQueue.add(new SendingTask(next,labelToCommand("MY_NEW_EARLY_FINISH") + " " + lateFinish));
						}
					}
					  */
					
					
				} break;
				default: printReport("else" + msg.getContent()); break;
				}
			
			}
		}
	};
	void skipUpForTesting() {
		addBehaviour(NxtMSGProc);
		addBehaviour(NegotiationStart);
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

