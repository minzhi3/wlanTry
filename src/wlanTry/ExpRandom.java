package wlanTry;

import java.util.Random;

public class ExpRandom{
	double lamda;
	Random r;
	public ExpRandom(double lamda){
		r=new Random();
		this.lamda=lamda;
	}
	public double next(){
		return -lamda*Math.log(1-r.nextDouble());
	}
}
