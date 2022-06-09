package use.aop;

import cn.hutool.core.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import use.aop.proxy.DefaultProxyClassFactory;
import use.aop.proxy.ProxyClassFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static use.kit.Helper.as;

public class Aop {
  private Aop() {
  }

  private static void enhanceSpring(ConfigurableApplicationContext spring, ProxyClassFactory proxyFactory) {
    DefaultListableBeanFactory factory = as(spring.getBeanFactory());
    AbstractAutowireCapableBeanFactory factory_ = as(factory);
    factory_.setInstantiationStrategy(new SimpleInstantiationStrategy() {
      @Override
      public Object instantiate(@NotNull RootBeanDefinition bd, String beanName, org.springframework.beans.factory.@NotNull BeanFactory owner) {
        bd.setBeanClass(proxyFactory.get(bd.getBeanClass()));
        return super.instantiate(bd, beanName, owner);
      }

      @Override
      public Object instantiate(@NotNull RootBeanDefinition bd, String beanName, org.springframework.beans.factory.@NotNull BeanFactory owner, @NotNull Constructor<?> ctor, Object @NotNull ... args) {
        bd.setBeanClass(proxyFactory.get(bd.getBeanClass()));
        Class<?> beanClass = bd.getBeanClass();

        Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);

        ctor = ReflectUtil.getConstructor(beanClass, types);
        Object o = BeanUtils.instantiateClass(ctor, args);
        return o;
      }

      @Override
      public Object instantiate(@NotNull RootBeanDefinition bd, String beanName, org.springframework.beans.factory.@NotNull BeanFactory owner, Object factoryBean, @NotNull Method factoryMethod, Object @NotNull ... args) {
        return super.instantiate(bd, beanName, owner, factoryBean, factoryMethod, args);
      }
    });
  }

  public static ConfigurableApplicationContext bySpring(String... packageNames) {
    AnnotationConfigApplicationContext spring = new AnnotationConfigApplicationContext();
    enhanceSpring(spring, new DefaultProxyClassFactory(spring::getBean));
    spring.scan(packageNames);
    spring.refresh();
    return spring;
  }

  public static ConfigurableApplicationContext bySpringBoot(Class<?> config) {
    SpringApplication springboot = new SpringApplication(config) {
      @Override
      protected ConfigurableApplicationContext createApplicationContext() {
        ConfigurableApplicationContext spring = super.createApplicationContext();
        enhanceSpring(spring, new DefaultProxyClassFactory(spring::getBean));
        return spring;
      }
    };
    springboot.setLogStartupInfo(false);
    springboot.setBannerMode(Banner.Mode.OFF);
    return as(springboot.run());
  }
}




