package wlanTry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import signal.Signal;
class SignalBeginComparator implements Comparator<Signal> {

	@Override
	public int compare(Signal arg0, Signal arg1) {
		double t1=arg0.timeBegin;
		double t2=arg1.timeBegin;
		if (t1<t2)
			return -1;
		else if (t1>t2)
			return 1;
		else
			return 0;
	}
	
}
class NeighborList{
	boolean neighbors[];
	public NeighborList(int deviceNum,ArrayList<Integer> neighbors){
		this.neighbors=new boolean[deviceNum];

		for (Integer p:neighbors){
			this.neighbors[p]=true;
		}
	}
}
public class GodQueue extends God {
	ArrayList<Signal> allReq;
	LinkedList<Signal> channel,queue;
	int time;
	GodResult gr;
	DeviceResult dr[];
	ArrayList<NeighborList> neighbors;
	

	public GodQueue(int n, int type) {
		super(n, type);
		gr=new GodResult();
		dr=new DeviceResult[super.ThreadNum];
		channel=new LinkedList<Signal>();
		queue=new LinkedList<Signal>();
		allReq=new ArrayList<Signal>();
		for (int i=0;i<super.ThreadNum;i++){
			dr[i]=new DeviceResult();
		}
		neighbors=new ArrayList<NeighborList>();
		for (int i=0;i<super.ThreadNum;i++){
			neighbors.add(new NeighborList(super.ThreadNum,dm.getNeighbour(i)));
		}
	}
	
	private void buildQueue(){
		int IDFrom=0;
		for (RequestsQueue qs:dm.requestsList){
			
			for (Request rr:qs.requests){
				Signal signal=rr.toSignal(IDFrom, (int)rr.time);
				allReq.add(signal);
			}
			IDFrom++;
		}
		Collections.sort(allReq, new SignalBeginComparator());
	}
	@Override
	public GodResult call() throws Exception {
		if (dm==null) return null;
		buildQueue();
		
		int ie;
		ie=0;
		
		for (time=0;time<Param.simTimeLength;time++){

			checkEnd();
			while (ie<allReq.size() && allReq.get(ie).timeBegin<=time){
				enqueue(allReq.get(ie));
				
				
				if (Param.isDebug){
					StringBuilder sb=new StringBuilder();
					sb.append("QUE:\n");
					for (Signal s:queue){
						sb.append(s.getShortString());
						sb.append('\n');
					}
					System.out.print(sb.toString());
				}
					ie++;
			}
			checkQueue();
		}
		for (int i=Param.numAP;i<super.ThreadNum;i++){
			if (dm.inCenter(i))
				gr.add(dr[i]);
		}
		if (Param.isDebug){
			for (int i=0;i<ThreadNum;i++){
				System.out.println((dm.inCenter(i)?"C ":"  ")+"MT"+i+": "+dr[i].getThroughputRx()+" "+dr[i].getThroughputTx()+" "+dr[i].getDelayTime());
			}
		}
		return gr;
	}

	private void checkQueue() {
		boolean change=false;
		ArrayList<Signal> delete=new ArrayList<Signal>();
		for (Signal s:queue){
			if (s.timeBegin<=time){
				channel.add(s.getClone());
				change=true;
				if (Param.isDebug){
					System.out.println(time+" ÅFPUT IN "+s.getString());
				}
				delete.add(s);
			}
		}
		queue.removeAll(delete);
		if (Param.isDebug && change){
			StringBuilder sb=new StringBuilder();
			sb.append("CHA:\n");
			for (Signal cha:channel){
				sb.append(cha.getShortString());
				sb.append('\n');
			}
			System.out.print(sb.toString());
		}
	}

	private void enqueue(Signal signal) {
		ListIterator<Signal> li=queue.listIterator();
		Signal toinsert=signal.getClone();

		//For Channel wait until MT near the transmitter ends their receiving
		//and MT near the receiver ends their transmitting
		for (Signal cha:channel){
			int endTime=cha.getEndTime();
			NeighborList receMTs=neighbors.get(cha.IDTo);
			if (receMTs.neighbors[toinsert.IDFrom] && toinsert.timeBegin<=endTime){
				toinsert.timeBegin=cha.getEndTime();
			}
			
			NeighborList tranMTs=neighbors.get(cha.IDFrom);
			if (tranMTs.neighbors[toinsert.IDTo] && toinsert.timeBegin<=endTime){
				toinsert.timeBegin=cha.getEndTime();
			}
		}
		
		//For Queue
		int b,e;
		
		b=e=toinsert.timeBegin;
		if (queue.isEmpty()){
			queue.add(toinsert);
		}else{
			Signal q1,q2 = null;
			while (true){
				NeighborList receMTs,tranMTs;
				do{
					if (li.hasNext())
						q1=li.next();
					else{
						q1=null;
						break;
					}
					
					receMTs=neighbors.get(q1.IDTo);
					tranMTs=neighbors.get(q1.IDFrom);
				}while(!receMTs.neighbors[toinsert.IDFrom] && !tranMTs.neighbors[toinsert.IDTo]);
				
				if (q1!=null) 
					b=q1.timeBegin-1;
				else
					b=Param.simTimeLength*10;
				
				if (q2!=null && q2.getEndTime()+1>e) e=q2.getEndTime()+1;
				
				if (b-e>signal.timeLength){
					if (q1!=null) li.previous();
					
					toinsert.timeBegin=e;
					li.add(toinsert);
					if (q1!=null) li.next();
					if (Param.isDebug)
						System.out.println(time+" ÅFIN QUEUE "+toinsert.getString());
					break;
				}
				q2=q1;
			}
		}
		
		
	}

	private void checkEnd() {
		ArrayList<Signal> delete=new ArrayList<Signal>();
		for (Signal ex:channel){
			if (ex.getEndTime()<=time){
				delete.add(ex);
				dr[ex.IDFrom].receiveACK();
				dr[ex.IDTo].receiveDATA();
				if (Param.isDebug)
				System.out.println(time+": END "+ex.getString());
			}
		}
		channel.removeAll(delete);
	}

	private boolean check(Signal s) {
		NeighborList neighbor=this.neighbors.get(s.IDFrom);

		for (Signal r:queue){
			if (r.IDPacket==s.IDPacket) break;
			if (neighbor.neighbors[r.IDTo]) return false;
		}
		for (Signal ex:channel){
			if (neighbor.neighbors[ex.IDTo]){
				return false;
			}
		}
		return true;
	}

}
