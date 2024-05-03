package com.nse.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    GreetingClient greetingClient = context.getBean(GreetingClient.class);
    //We need to block for the content here or the JVM might exit before the message is logged
    //System.out.println(">> message = " + greetingClient.getMessage().block());
    System.out.println(">> message = " + greetingClient.getMessage());
  }
}