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

import java.text.DecimalFormat;

public class HotSwap<T extends AutoCloseable & Runnable> {

  protected HotswapConfig config;
  protected T hook;

  protected volatile boolean started = false;
  protected volatile HotSwapWatcher hotSwapWatcher;
  protected DecimalFormat decimalFormat = new DecimalFormat("#.#");

  public HotSwap(HotswapConfig undertowConfig) {
    this.config = undertowConfig;
  }

  public HotSwap(Class<T> hook) {
    this(new HotswapConfig(hook));
  }

  public static <T extends AutoCloseable & Runnable> HotSwap<T> create(Class<T> c) {
    return new HotSwap<>(c);
  }

  public synchronized void start() {
    try {

      String msg = "Starting Hotswap... ";
      System.err.println(msg);

      long start = System.currentTimeMillis();
      doStart();
      System.err.println("Starting Hotswap in " + getTimeSpent(start) + " seconds...\n");

      /**
       * 使用 kill pid 命令或者 ctrl + c 关闭 JVM 时，调用 UndertowServer.stop() 方法，
       * 以便触发 JFinalConfig.onStop();
       *
       * 注意：下方代码严格测试过，只支持 kill pid 不支持 kill -9 pid
       */
      Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

    } catch (Exception e) {
      e.printStackTrace();
      stopSilently();

      // 支持在 doStart() 中抛出异常后退出 JVM，例如端口被占用，否则在 linux 控制台 JVM 并不会退出
      System.exit(1);
    }
  }

  protected void doStart() {
    if (started) {
      return;
    }

    init();

    if (isDevMode() && hotSwapWatcher == null) {
      hotSwapWatcher = new HotSwapWatcher(this);
      hotSwapWatcher.start();
    }

    started = true;
  }

  protected void init() {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    Thread currentThread = Thread.currentThread();
    Class<T> hookClass = getHookClass();
    try {
      currentThread.setContextClassLoader(config.getClassLoader());
      T hook = hookClass.newInstance();
      this.hook = hook;
      hook.run();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } finally {
      currentThread.setContextClassLoader(currentClassLoader);
    }
  }

  public final synchronized void stop() {
    if (started) {
      started = false;
    } else {
      return;
    }

    System.out.println("\nShutdown Undertow Server ......");
    long start = System.currentTimeMillis();
    try {
      if (hotSwapWatcher != null) {
        hotSwapWatcher.exit();
      }

      doStop();

    } catch (Exception e) {
      e.printStackTrace();
      stopSilently();
    } finally {
      System.out.println("Shutdown Complete in " + getTimeSpent(start) + " seconds. See you later (^_^)\n");
    }
  }

  protected void doStop() throws Exception {
    // 保留以下三行
    // deploymentManager.undeploy();
    // Servlets.defaultContainer().removeDeployment(deploymentInfo);
    hook.close();

    /**
     * 必须设置 HotSwapWatcher.setDaemon(false)，否则下面两行代码将退出 JVM，无法再次启动 undertow
     * 触发 JFinalConfig.onStop() 方法必须要调用 deploymentManager.stop()
     * 该方法不能在 deploymentManager.undeploy() 这后调用，否则有 NPE
     */
  }

  /**
   * HotSwapWatcher 调用 restart()
   */
  public synchronized void restart() {
    if (started) {
      started = false;
    } else {
      return;
    }

    try {
      System.err.println("\nLoading changes ......");
      long start = System.currentTimeMillis();

      doStop();
      config.replaceClassLoader();
      doStart();

      System.err.println("Loading complete in " + getTimeSpent(start) + " seconds (^_^)\n");

    } catch (Exception e) {
      System.err.println("Error restarting webapp after change in watched files");
      e.printStackTrace();
    }
  }

  protected String getTimeSpent(long startTime) {
    float timeSpent = (System.currentTimeMillis() - startTime) / 1000F;
    return decimalFormat.format(timeSpent);
  }

  protected void stopSilently() {
    try {
      started = false;
      if (hook != null) {
        hook.close();
      }
    } catch (Exception e) {
      HotSwapKit.doNothing(e);
    }
  }


  @SuppressWarnings("unchecked")
  private Class<T> getHookClass() {
    try {
      return (Class<T>) config.getClassLoader().loadClass(config.getHookClass());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }


  public boolean isDevMode() {
    return HotswapConfig.isDevMode();
  }

  public boolean isStarted() {
    return started;
  }

  // ------------------

  /**
   * 仅用于解决项目的 JFinalConfig 继承类打成 jar 包，并且使用 undertow.devMode=true 配置
   * 时报出的异常，以上两个条件没有同时成立时无需理会，也就是说没有报异常就无需理会
   *
   * 假定项目中的 JFinalConfig 的继承类 com.abc.MyConfig 被打进了 jar 包并且
   * undertow.devMode 设置成了 true，这里在启动项目的时候由于 ClassLoader
   * 不同会报出以下异常：
   *   Can not create instance of class: com.abc.MyConfig. Please check the config in web.xml
   *
   * 解决办法是使用 addHotSwapClassPrefix(...) :
   *   UndertowServer.create(MyConfig.class).addHotSwapClassPrefix("com.abc.").start();
   *
   * 只添加 JFinalConfig 的继承类 com.abc.MyConfig 也可以：
   *   UndertowServer.create(MyConfig.class).addHotSwapClassPrefix("com.abc.MyConfig").start();
   *
   * 注意：该配置对生产环境无任何影响，在打包部署前无需删除该配置
   */
  public HotSwap<T> addHotSwapClassPrefix(String prefix) {
    config.addHotSwapClassPrefix(prefix);
    return this;
  }

  public HotSwap<T> addSystemClassPrefix(String prefix) {
    config.addSystemClassPrefix(prefix);
    return this;
  }

}






