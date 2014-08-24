package wlanTry;

import java.util.Random;

public class DeviceMapAP1 extends DeviceMap {

	public DeviceMapAP1(int n) {
		super(n);
	}
	@Override
	public void createMap(){
		Random r=new Random();
		this.addDevice(0, 0);
		for (int i=0;i<numMT;i++){
			double x;
			double y;
			do{
				x=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
				y=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
			}while (!this.inAreaAP(x, y));
			this.addDevice(x, y);
		}
	}
	protected boolean inAreaAP(double x,double y){
		return Math.sqrt(x*x+y*y)<this.areaAP;
	}
	public int getAPofIndex(int index) {
		return 0;
	}

}
