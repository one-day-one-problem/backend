package site.haruhana.www.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "site.haruhana.www.feign")
public class OpenFeignConfig {

}
