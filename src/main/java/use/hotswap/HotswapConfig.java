/**
 Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package use.hotswap;


/**
 * UndertowConfig
 */
public class HotswapConfig {

  protected String hookClass;

  // 开发模式才支持热加载，此配置与 jfinal 中的是不同的用途
  protected volatile static boolean devMode = true;

  protected String hotSwapClassPrefix = null;

  protected String[] classPathDirs;        // 存放 .class 文件的目录
  protected HotSwapResolver hotSwapResolver;
  protected ClassLoaderKit classLoaderKit;

  public HotswapConfig(Class<?> jfinalConfigClass) {
    this(jfinalConfigClass.getName());
  }

  public HotswapConfig(String jfinalConfigClass) {
    this.hookClass = jfinalConfigClass;
  }

  public static boolean isBlank(String str) {
    return str == null || "".equals(str.trim());
  }

  public static boolean notBlank(String str) {
    return !isBlank(str);
  }

  public String getHookClass() {
    return hookClass;
  }

  protected ClassLoaderKit getClassLoaderKit() {
    if (classLoaderKit == null) {
      //classLoaderKit = new ClassLoaderKit(Undertow.class.getClassLoader(), getHotSwapResolver());
      classLoaderKit = new ClassLoaderKit(Thread.currentThread().getContextClassLoader(), getHotSwapResolver());
    }
    return classLoaderKit;
  }

  public ClassLoader getClassLoader() {
    // return isDevMode() ? getClassLoaderKit().getClassLoader() : Undertow.class.getClassLoader();

    /**
     * 不论是否为 devMode 都使用 HotSwapClassLoader
     * HotSwapClassLoader 添加了 isDevMode() 判断
     * 一直使用 HotSwapClassLoader 是因为为其添加了
     * 配置文件 config 目录到 class path，以便可以加载
     * 外部配置文件
     */
    return getClassLoaderKit().getClassLoader();
  }

  public void replaceClassLoader() {
    if (isDevMode()) {
      getClassLoaderKit().replaceClassLoader();
    }
  }

  public HotSwapResolver getHotSwapResolver() {
    if (hotSwapResolver == null) {
      hotSwapResolver = new HotSwapResolver(getClassPathDirs());
      // 后续将此代码转移至 HotSwapResolver 中去，保持 UndertowConfig 的简洁
      if (hotSwapClassPrefix != null) {
        for (String prefix : hotSwapClassPrefix.split(",")) {
          if (notBlank(prefix)) {
            hotSwapResolver.addHotSwapClassPrefix(prefix);
          }
        }
      }
    }
    return hotSwapResolver;
  }

  public void setHotSwapResolver(HotSwapResolver hotSwapResolver) {
    this.hotSwapResolver = hotSwapResolver;
  }

  public void addSystemClassPrefix(String prefix) {
    getHotSwapResolver().addSystemClassPrefix(prefix);
  }

  public void addHotSwapClassPrefix(String prefix) {
    getHotSwapResolver().addHotSwapClassPrefix(prefix);
  }

  /**
   * 获取存放 .class 文件的所有 classPath 目录，绝大部分场景下只有一个目录
   */
  public String[] getClassPathDirs() {
    if (classPathDirs == null) {
      classPathDirs = HotSwapKit.getClassPathDirs();
    }
    return classPathDirs;
  }
	
	/*
	public void setClassPathDirs(String[] classPathDirs) {
		this.classPathDirs = classPathDirs;
	}*/


  public static boolean isDevMode() {
    return devMode;
  }


}