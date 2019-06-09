package com.dimple;

import java.util.ArrayList;
import java.util.List;

public class TestHeap {

	public static void main(String[] args) {
		List<Object> list = new ArrayList<>();
		int i=0;
		while (true) {
			i++;
			if(i==1000){
				System.out.println("i========"+i);
			}
			list.add(String.valueOf(i++).intern());
		}
	}
}
