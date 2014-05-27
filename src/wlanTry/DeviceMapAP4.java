package wlanTry;
import java.util.Random;



public class DeviceMapAP4 extends DeviceMap {
	public void createMap(int n){
		Random r=new Random();
		double size=distAP/2;
		//for (int i=0;i<4;i++){
			super.addDevice(size, size);
			super.addDevice(-size, size);
			super.addDevice(-size, -size);
			super.addDevice(size, -size);
		//}
		for (int i=0;i<n;i++){
			double x;
			double y;
			do{
				x=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
				y=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
			}while (!this.inAreaAP(x, y));
			this.addDevice(x, y);
		}
	}
	
	public int getAPofIndex(int index){
		double x=this.devices.get(index).x;
		double y=this.devices.get(index).y;
		if (y>0) {
			if (x>0) 
				return 0;
			else return 1;
		}
		else{
			if (x>0)
				return 3;
			else
				return 2;
		}
	}
	
	protected boolean inAreaAP(double x,double y){
		double apX[]=new double[]{0.5,-0.5,-0.5,0.5};
		double apY[]=new double[]{0.5,0.5,-0.5,-0.5};
		for (int i=0;i<4;i++){
			double dx=x-apX[i];
			double dy=y-apY[i];
			if (Math.sqrt(dx*dx+dy*dy)<this.areaAP)
				return true;
		}
		return false;
	}

}
