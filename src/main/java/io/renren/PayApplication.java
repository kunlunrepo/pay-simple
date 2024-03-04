

package io.renren;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;


@SpringBootApplication
@Slf4j
@ComponentScan({"com.github.wxpay.sdk", "io.renren"})
public class PayApplication {

	public static void main(String[] args) {
		log.info("【pay-simple】---开始启动");
		SpringApplication.run(PayApplication.class, args);
		log.info("【pay-simple】---启动完成");
	}

}