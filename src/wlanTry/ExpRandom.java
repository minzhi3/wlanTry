package wlanTry;

import java.util.Random;

public class ExpRandom{
	double lamda;
	Random r;
	double sum;
	public ExpRandom(double lamda){
		r=new Random();
		this.lamda=lamda;
		clear();
	}
	public void clear(){
		sum=0;
	}
	public double next(){
		return -lamda*Math.log(1-r.nextDouble());
	}
	public double nextSum(){
		double t=this.next();
		sum+=t;
		return sum;
	}
}
