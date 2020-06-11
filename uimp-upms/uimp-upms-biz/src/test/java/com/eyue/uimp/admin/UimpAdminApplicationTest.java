/*
 *    Copyright (c) 2018-2025, uimp All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: uimp
 */

package com.eyue.uimp.admin;

import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import java.util.ArrayList;

/**
 * @author uimp
 * @date 2018/10/7
 * <p>
 */
public class UimpAdminApplicationTest {

	@Test
	public void test(){
		ArrayList<String> stringList = new ArrayList<>();
		stringList.size();
		System.setProperty("jasypt.encryptor.password","pigx");

		StringEncryptor string1=new DefaultLazyEncryptor(new StandardEnvironment());
	System.out.println(string1.decrypt("ltJPpR50wT0oIY9kfOe1Iw=="));


	}
}
