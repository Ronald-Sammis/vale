package pe.com.sammis.vale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ValeApplication {

    public static void main(String[] args) {


        try {
            SpringApplication.run(ValeApplication.class, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
