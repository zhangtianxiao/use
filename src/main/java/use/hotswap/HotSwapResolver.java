/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package use.hotswap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * HotSwapResolver
 */
public class HotSwapResolver {
	
	protected String[] classPathDirs;
	
	protected String[] systemClassPrefix = {
			"java.",
			"javax.",
			"sun.",								// 支持 IDEA
			"com.sun.",
			// "jdk.",
			// "org.xml.",
			// "org.w3c.",
			
			// "io.undertow.",
			// "org.xnio.",
			
			"com.jfinal.server.undertow."		// undertow server 项目自身
			
			// "org.apache.jasper.",				// 支持 jsp，不影响 org.apache.shiro
			// "org.apache.taglibs.",			// 支持 jsp，不影响 org.apache.shiro
			// "org.glassfish.jsp.",				// 支持 jsp
			// "org.slf4j."						// 支持slf4j
	};
	
	protected String[] hotSwapClassPrefix = {
			"com.jfinal.",
			
			"net.sf.ehcache.",					// 支持 ehcache，否则从 ehcache 中读取到的数据将出现类型转换异常
			"redis.clients.", "org.nustaq.",		// 支持 RedisPlugin
			"org.quartz.",						// 支持 quartz
			
			"net.dreamlu."						// 支持 JFinal-event 等出自 net.dreamlu 的插件
	};
	
	public HotSwapResolver(String[] classPathDirs) {
		// 不必判断 length == 0，因为在打包后的生产环境获取到的 length 可以为 0
		// if (classPathDirs == null /* || classPathDirs.length == 0*/) {
			// throw new IllegalArgumentException("classPathDirs can not be null");
		// }
		
		if (classPathDirs != null) {
			this.classPathDirs = classPathDirs;
		} else {
			this.classPathDirs = new String[0];
		}
	}
	
	/**
	 * 判断是否为系统类文件，系统类文件无条件使用 parent 类加载器加载
	 */
	public boolean isSystemClass(String className) {
		for (String s : systemClassPrefix) {
			if (className.startsWith(s)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 判断是否为热加载类文件，热加载类文件无条件使用 HotSwapClassLoader 加载
	 * 
	 * 热加载类文件满足两个条件：
	 * 1：通过 hotSwapClassPrefix 指定的类文件
	 * 2：在 class path 目录下能找到的 .class 文件
	 */
	public boolean isHotSwapClass(String className) {
		for (String s : hotSwapClassPrefix) {
			if (className.startsWith(s)) {
				return true;
			}
		}
		
		/**
		 * 所有 classPath 目录下的所有 .class 文件需要热加载
		 */
		if (findClassInClassPathDirs(className)) {
			return true;
		}
		
		return false;
	}
	
	protected boolean findClassInClassPathDirs(String className) {
		String fileName = className.replace('.', '/').concat(".class");
		
		if (classPathDirs.length == 1) {
			if (findFile(classPathDirs[0], fileName)) {
				return true;
			}
		} else {
			for (String dir : classPathDirs) {
				if (findFile(dir, fileName)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected boolean findFile(String filePath, String fileName) {
		File file = new File(filePath + fileName);
		return file.isFile();
	}
	
	/**
	 * 添加系统类前缀，系统类由系统类加载器进行加载
	 */
	public synchronized void addSystemClassPrefix(String prefix) {
		List<String> list = new ArrayList<>();
		for (String s : systemClassPrefix) {
			list.add(s);
		}
		list.add(prefix.trim());
		systemClassPrefix = list.toArray(new String[list.size()]);
	}
	
	/**
	 * 添加需要热加载的类前缀，由 HotSwapClassLoader 加载
	 * 
	 * 重要：在热加载过后，如果出现类型转换异常，找到无法转换的类
	 *      调用本方法添加相关前缀即可解决
	 */
	public synchronized  void addHotSwapClassPrefix(String prefix) {
		List<String> list = new ArrayList<>();
		for (String s : hotSwapClassPrefix) {
			list.add(s);
		}
		list.add(prefix.trim());
		hotSwapClassPrefix = list.toArray(new String[list.size()]);
	}
}







