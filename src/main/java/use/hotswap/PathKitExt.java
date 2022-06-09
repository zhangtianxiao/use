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

import java.io.File;
import java.nio.charset.StandardCharsets;

class PathKitExt {

  private static String locationPath = null;  // 定位路径


  /**
   * 1：获取 PathKitExt 类文件所处 jar 包文件所在的目录，注意在 "非部署" 环境中获取到的
   *    通常是 maven 本地库中的某个目录，因为在开发时项目所依赖的 jar 包在 maven 本地库中
   *    这种情况不能使用
   *
   * 2：PathKitExt 自身在开发时，也就是未打成 jar 包时，获取到的是 APP_BASE/target/classes
   *    这种情况多数不必关心，因为 PathKitExt 在使用时必定处于 jar 包之中
   *
   * 3：获取到的 locationPath 目录用于生成部署时的 config 目录，该值只会在 "部署" 环境下被获取
   *    也用于生成 webRootPath、rootClassPath，这两个值也只会在 "部署" 时被获取
   *    这样就兼容了部署与非部署两种场景
   *
   * 注意：该路径尾部的 "/" 或 "\\" 已被去除
   */
  public static String getLocationPath() {
    if (locationPath != null) {
      return locationPath;
    }

    try {
      // Class<?> clazz = io.undertow.Undertow.class;		// 仅测试用
      Class<?> clazz = PathKitExt.class;
      String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
      path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
      path = path.trim();
      File file = new File(path);
      if (file.isFile()) {
        path = file.getParent();
      }

      path = removeSlashEnd(path);    // 去除尾部 '/' 或 '\' 字符
      locationPath = path;

      return locationPath;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public static String removeSlashEnd(String path) {
    if (path != null && path.endsWith(File.separator)) {
      return path.substring(0, path.length() - 1);
    } else {
      return path;
    }
  }


}








