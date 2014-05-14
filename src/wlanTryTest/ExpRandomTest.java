package wlanTryTest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wlanTry.ExpRandom;

public class ExpRandomTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNext() {

		ExpRandom r=new ExpRandom(100);
		double k=0;
		int N=100000;
		for (int i=0;i<N;i++){
			k+=r.next();
		}
		System.out.println(k/N);
	}

}
