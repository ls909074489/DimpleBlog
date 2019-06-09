package com.dimple;

public class TestVmStack {

	private int length;
	
	private void recurison(){
		length++;
		recurison();
	}
	
	public static void main(String[] args) {
		TestVmStack t = new TestVmStack();
		try {
			t.recurison();
		} catch (Throwable e) {//error不能用Exception捕获
			System.out.println("length>>>>>>>"+t.length);
			e.printStackTrace();
		}
				
	}
}
