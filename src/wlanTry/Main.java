package wlanTry;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Main {

	public static void main(String[] args) throws InterruptedException {
		//DebugOutput.fileDebugInit();
		for (int i=0;i<25;i+=1){

			long begintime = System.nanoTime();
			DebugOutput.time=0;
			God g=new God(i);
			g.run();

			long endtime = System.nanoTime();
			double costTime = (endtime - begintime)/1e9;
			//DebugOutput.outputAlways("Num="+i+" Time:"+costTime);
		}
		/*
		CyclicBarrier cb=new CyclicBarrier(2,new Runnable(){
			public void run(){
				System.out.println("Barrier:"+ThreadTest.f2);
			}
		});
		Object o=new Object();
		ThreadTest r1=new ThreadTest(cb,o);
		ThreadTest r2=new ThreadTest(cb,o);
		Thread t1=new Thread(r1);
		Thread t2=new Thread(r2);
		t1.start();
		t2.start();
		*/
		
	}

}
