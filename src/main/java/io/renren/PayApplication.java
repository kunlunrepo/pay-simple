

package io.renren;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class PayApplication {

	public static void main(String[] args) {
		log.info("【pay-simple】---开始启动");
		SpringApplication.run(PayApplication.class, args);
		log.info("【pay-simple】---启动完成");
	}

}