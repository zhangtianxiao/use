package use.test.mvc.servlet;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import use.aop.Aop;
import use.kit.BeanHolder;
import use.kit.VertxKit;
import use.mvc.router.Router;
import use.mvc.router.Router_Servlet;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

@Configuration
public class WithUndertow {
  /**
   创建vertx实例, 即便当前项目不一定用到
   */
  @Bean(destroyMethod = "destroy")
  public BeanHolder<Vertx> vertxBeanHolder() {
    return new BeanHolder<>() {
      final VertxOptions vertxOptions = new VertxOptions().setBlockedThreadCheckInterval(100 * 1000);
      final Vertx vertx = Vertx.vertx(vertxOptions);

      @Override
      public Vertx get() {
        return vertx;
      }

      @Override
      public void destroy() {
        VertxKit.await(vertx.close());
        System.err.println("vertx closed..." + vertx);
      }
    };
  }

  @Bean
  public Vertx vertx(BeanHolder<Vertx> vertxBeanHolder) {
    return vertxBeanHolder.get();
  }

  /**
   自定义路由实例
   */
  @Bean
  public Router router(ConfigurableApplicationContext $) {
    Router_Servlet router_servlet = new Router_Servlet();
    router_servlet.scan($::getBean, "use.test.web.test");
    return router_servlet;
  }

  public static void main(String[] args) throws ServletException {
    ConfigurableApplicationContext $ = Aop.bySpring("use.test.web.test", "use.test.mvc.servlet");
    Router_Servlet router = $.getBean(Router_Servlet.class);

    /*
     * 创建undertow server, 绑定router.servlet
     * */
    DeploymentInfo servletBuilder = Servlets.deployment()
      .setClassLoader(WithUndertow.class.getClassLoader())
      .setContextPath("/")
      .setDeploymentName("undertow.war")
      .addServlets(Servlets.servlet("MessageServlet", GenericServlet.class, () -> new InstanceHandle<>() {
          @Override
          public Servlet getInstance() {
            return router.Servlet;
          }

          @Override
          public void release() {

          }
        })
        .addMapping("/*"));

    DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
    manager.deploy();


    Undertow server = Undertow.builder()
      .addHttpListener(8081, "localhost")
      .setHandler(manager.start())
      .build();
    server.start();
  }
}
