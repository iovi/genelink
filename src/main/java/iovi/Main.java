package iovi;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

/**Запускаемый класс приложения */
@ComponentScan
@EnableAutoConfiguration
public class Main {
    /**Запускаемый метод приложения*/
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}