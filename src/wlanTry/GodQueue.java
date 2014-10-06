package wlanTry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	int neighbors[];
	public NeighborList(ArrayList<Integer> neighbors){
		this.neighbors=new int[neighbors.size()];
		int i=0;
		for (Integer p:neighbors){
			this.neighbors[i++]=p;
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
			neighbors.add(new NeighborList(dm.getNeighbour(i)));
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
		
		int ib,ie;
		ib=ie=0;
		double timeMT[]=new double[super.ThreadNum];
		
		for (time=0;time<Param.simTimeLength;time++){
			//From Requeue to Queue
			int it=ie;
			/*
			do{
				if (it>=allReq.size()-1) break;
				it++;
				s=allReq.get(it);
				if (s.timeBegin<=time){
					ie=it;
					System.out.println(time+" FIN QUEUE "+s.getString());
				}
			}while(s.timeBegin<time);*/
			while (ie<allReq.size() && allReq.get(ie).timeBegin<time){
				queue.add(allReq.get(ie).getClone());
				//System.out.println(time+" FIN QUEUE "+allReq.get(ie).getString());
				ie++;
			}
			
			/*
			while (ii!=ie){
				s=allReq.get(ii);
				//From Queue to Channel (1)

				if (timeMT[s.IDFrom]>=time) continue;
				
				//From Queue to Channel (2)
				if (check(s)){
					Signal puts=s.getClone();
					puts.timeBegin=time;
					channel.add(puts);
					System.out.println(time+" :PUT IN "+s.getString());
				}
				checkEnd();
				ii++;
			}*/
			ArrayList<Signal> delete=new ArrayList<Signal>();
			for (Signal s:queue){
				if (timeMT[s.IDFrom]>=time) continue;
				
				if (check(s)){
					Signal puts=s.getClone();
					puts.timeBegin=time;
					channel.add(puts);
					//System.out.println(time+" :PUT IN "+puts.getString());
					delete.add(s);
					timeMT[s.IDFrom]=puts.getEndTime();
				}
				checkEnd();
			}
			queue.removeAll(delete);
		}
		for (DeviceResult drs:dr){
			gr.add(drs);
		}
		if (Param.isDebug){
			for (int i=0;i<ThreadNum;i++){
				//System.out.println((dm.inCenter(i)?"C ":"  ")+"MT"+i+": "+dr[i].getThroughputRx()+" "+dr[i].getThroughputTx()+" "+dr[i].getDelayTime());
			}
		}
		return gr;
	}

	private void checkEnd() {
		ArrayList<Signal> delete=new ArrayList<Signal>();
		for (Signal ex:channel){
			if (ex.getEndTime()<=time){
				delete.add(ex);
				dr[ex.IDFrom].receiveACK();
				dr[ex.IDTo].receiveDATA();
				//System.out.println(time+": END "+ex.getString());
			}
		}
		channel.removeAll(delete);
	}

	private boolean check(Signal s) {
		NeighborList neighbor=this.neighbors.get(s.IDFrom);
		boolean interference[]=new boolean[super.ThreadNum];
		for (int i=0;i<dm.getDeviceNum();i++){
			interference[i]=false;
		}
		for (Integer i:neighbor.neighbors){
			interference[i]=true;
		}
		for (Signal ex:channel){
			if (interference[ex.IDTo]){
				return false;
			}
		}
		return true;
	}

}
