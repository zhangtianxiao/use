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
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class HotSwapKit {
	
	private static String[] classPathDirs = null;
	
	/**
	 * 该方法起始用于 HotSwapResolver.isHotSwapClass() 下面的 findClassInClassPathDirs()
	 * 用于判断当前被加载的类是不是处在 class path 之下，从而判断该类是不是需要被 HotSwapClassLoader
	 * 进行热加载
	 * 
	 * 注意：返回值中的 path 全都以 '/' 或 '\\' 字符结尾，因为该方法一开始的用途需要这个结尾字符
	 * 
	 * 
	 * 后来也被辅助用于 buildDeployMode()、buildWebRootPath()、buildRootClass() 这三个方法之中
	 * 1：buildDeployMode() 通过判断是否存在以 "classes" 结尾的 class path 来确定是否处于部署模式
	 * 2：buildWebRootPath() 判断同上，当处于部署模式时，指向 web 资源文件目录：APP_BASE/webapp
	 * 3：buildRootClass() 判断同上，当处于部署模式时，指向用于存放配置文件的目录：APP_BASE/config
	 */
	public static String[] getClassPathDirs() {
		if (classPathDirs == null) {
			classPathDirs = buildClassPathDirs();
		}
		return classPathDirs;
	}
	
	private static String[] buildClassPathDirs() {
		List<String> list = new ArrayList<>();
		
		String[] classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
		for(String classPath : classPathArray) {
			classPath = classPath.trim();
			
			if (classPath.startsWith("./")) {
				classPath = classPath.substring(2);
			}
			
			File file = new File(classPath);
			if (file.exists() && file.isDirectory()) {
				// if (!classPath.endsWith("/") && !classPath.endsWith("\\")) {
				if (!classPath.endsWith(File.separator)) {
					classPath = classPath + File.separator;		// append postfix char "/"
				}
				
				list.add(classPath);
			}
		}
		
		return list.toArray(new String[0]);
	}
	
	public static boolean notAvailablePort(int port) {
		return ! isAvailablePort(port);
	}
	
	public static boolean isAvailablePort(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
		
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			doNothing(e);
		} finally {
			closeQuietly(ds);
			closeQuietly(ss);
		}
		return false;
	}
	
	public static void closeQuietly(java.io.Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			// should not be thrown, just detect port available.
			doNothing(e);
		}
	}
	
	public static void doNothing(Throwable e) {
		
	}
	
	// ----------------------------------------------------------------
	
	private static Boolean deployMode = null;
	
	public static boolean isDeployMode() {
		if (deployMode == null) {
			deployMode = buildDeployMode();
		}
		return deployMode;
	}
	
	public static boolean notDeployMode() {
		return ! isDeployMode();
	}
	
	/**
	 * 非部署模式下，可以获取到 target/classes
	 * 
	 * 有了这个方法, PathKitExt 中的有关 UndertowKit.getClassPathDirs()
	 * 判断可以使用这个方法了，不用再重复写代码了
	 */
	private static boolean buildDeployMode() {
		String[] classPathDirs = HotSwapKit.getClassPathDirs();
		if (classPathDirs != null && classPathDirs.length > 0) {
			for (String path : classPathDirs) {
				if (path != null) {
					path = PathKitExt.removeSlashEnd(path); 
					if (path.endsWith("classes")) {
						return false;
					}
				}
			}
		}
		
		return true;
	}

}



