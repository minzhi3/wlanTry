package wlanTry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class TransmissionRequest{
	public ArrayList<RequestElement> requestList;
	private int next;
	public TransmissionRequest(){
		requestList=new ArrayList<RequestElement>();
		next=0;
	}
	/**
	 * build random transmission request in a fixed data rates;
	 * @param dataRates :data rates.
	 * @param a :ID of receiver begin.
	 * @param b :ID of receiver end.
	 */
	public void buildRequestList(double pps, int a, int b, int len){
		ExpRandom r=new ExpRandom(pps);
		for (int i=a;i<=b;i++){
			r.clear();
			for (int j=0;j<len;j++)
				requestList.add(new RequestElement(r.nextSum(),i));
		}
		Collections.sort(requestList, new Comparator<RequestElement>() {
					@Override
					public int compare(RequestElement o1, RequestElement o2) {
						return o1.time<o2.time?-1:1;
					}
		});
		next=0;
	}
	public RequestElement getTime(){
		if (next<requestList.size()){
			return requestList.get(next);
		}else {
			return null;
		}
	}
	public void popFront(){
		if (next<requestList.size()){
			next++;
		}
	}
}
