package wlanTry;

import java.util.ArrayList;

abstract class DeviceMap {
	final double distAP=Param.distAP;
	final double areaAP=Param.areaAP;
	final int numMT;
	final int numDevice;
	final double error;
	public final int carrierSenseRange=Param.carrierSenseRange;
	protected ArrayList<Location> devices;
	protected ArrayList<RequestsQueue> requestsList;
	//private ArrayList<Location> accessPoints; 

	/**
	 * The distance of carrier sense.
	 */
	public DeviceMap (int n, double error){
		devices=new ArrayList<Location>();
		requestsList=new ArrayList<RequestsQueue>();
		this.numMT=n;
		this.error=error;
		this.numDevice=(numMT+1)*Param.numAP;
		//accessPoints=new ArrayList<Location>();
	}

	protected void addDevice(double x, double y){
		//DebugOutput.output(this.devices.size()+": "+x+" "+y);
		devices.add(new Location(x,y));
	}
	/*
	public void addAccessPoints(double x, double y){
		accessPoints.add(new Location(x,y));
	}*/
	public ArrayList<Integer> getNeighbour(int id){
		ArrayList<Integer> ret=new ArrayList<Integer>();
		double x0=devices.get(id).x;
		double y0=devices.get(id).y;
		for (int i=0;i<devices.size();i++){
			//if (i==id) continue; 
			double dx=devices.get(i).x-x0;
			double dy=devices.get(i).y-y0;
			if (Math.sqrt(dx*dx+dy*dy)<carrierSenseRange)
				ret.add(i);
		}
		DebugOutput.outputNeighbor(id, ret);
		return ret;
	}
	public RequestsQueue getRequests(int id){
		return requestsList.get(id);
	}
	public void setRequest(ArrayList<RequestsQueue> reqs){
		this.requestsList=reqs;
	}
	public void createRequest(double pps) {
		
		//DownLink
		int IDPacket=1;
		
		for (int ap=0;ap<Param.numAP;ap++){//for AP in each CELL
			if (Param.withDownlink){
				RequestsQueue rqs=new RequestsQueue();
				//each receiver
				for (int i=Param.numAP;i<(numMT+1)*Param.numAP;i++){
					if (this.getAPofIndex(i)==ap){
						double time=0;
						ExpRandom r=new ExpRandom(pps);
						while (time<Param.simTimeLength*2){
							time=r.nextSum();
							if (Param.deviceType==DeviceType.CSMA){
								rqs.addRequest(new Request(i, time, IDPacket++, PacketType.DATA, 1, Param.timeData));
							}else{
								rqs.addRequest(new Request(i, time, IDPacket++, PacketType.DATA, Param.numSubpacket, Param.timeSubpacket));
							}
							
						};
					}
				}
				rqs.sort();
				requestsList.add(rqs);
			}else{
				requestsList.add(new RequestsQueue());
			}
		}
		//Uplink
		for (int i=Param.numAP;i<(numMT+1)*Param.numAP;i++){// for MT
			if (Param.withUplink){
				RequestsQueue rqs=new RequestsQueue();
				double time=0;
				ExpRandom r=new ExpRandom(pps);
				while (time<Param.simTimeLength*2){
					time=r.nextSum();
					if (Param.deviceType==DeviceType.CSMA){
						rqs.addRequest(new Request(this.getAPofIndex(i),time,IDPacket++,PacketType.DATA, 1,Param.timeData));
					}else{
						rqs.addRequest(new Request(this.getAPofIndex(i),time,IDPacket++,PacketType.DATA, Param.numSubpacket,Param.timeSubpacket));
					}
				}
				requestsList.add(rqs);
			}else{
				requestsList.add(new RequestsQueue());
			}
		}
	}
	/**
	 * Create a map with square;
	 * @param n
	 */
	public void createMap(ArrayList<Location> pos){
		this.devices=pos;
	}
	public boolean inCenter(int id){
		if (!Param.noiseMT)
			return Param.allMT||(Math.abs(devices.get(id).x)<Param.distAP/2) && (Math.abs(devices.get(id).y)<Param.distAP/2);
		else
			return id>0 && id<Param.noiseNum;
	}
	public abstract void createMap();
	public abstract int getAPofIndex(int index);
	protected abstract boolean inAreaAP(double x,double y);
	public int getDeviceNum(){
		return devices.size();
	}

	public boolean isNoiseArea(int id) {
		if (Param.noiseMT)
			return id<Param.noiseNum && id>0;
		else
			return Param.vsBER;
	}

}
