package net.mobilim.NaviGateService.Config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan("net.mobilim.NaviGateService")
@EntityScan("net.mobilim.NaviGateData.Entities")
@EnableJpaRepositories("net.mobilim.NaviGateData.Repositories")
public class ApplicationConfig {

}
