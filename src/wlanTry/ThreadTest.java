package wlanTry;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ThreadTest implements Runnable {
	public static int f2=3;
	private Object key;
	private int f1=3;
	CyclicBarrier barrier;
	public ThreadTest(CyclicBarrier cb, Object o){
		this.barrier=cb;
		this.key=o;
	}
	@Override
	public void run() {
		while (f1>0 || f2>0){
			if (f1>0){
				f1--;
				System.out.println("ID:"+Thread.currentThread().getId()+" f1="+f1);
			}
			synchronized(key){
				if (f2>0){
					setf2(f2-1);
				}
			}
		}
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (f1<3 || f2<3){
			if (f1<3){
				f1++;
				System.out.println("ID:"+Thread.currentThread().getId()+" f1="+f1);
			}
			synchronized(key){
				if (f2<3){
					setf2(f2+1);
				}
			}
		}
	}
	private void setf2(int a){
		f2=a;
		System.out.println("ID:"+Thread.currentThread().getId()+" f2="+a);
	}

}
