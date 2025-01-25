package Services.PaymentService.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String host = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
        dataSource.setUrl(String.format("jdbc:mysql://%s:3306/payment_db", host));
        //dataSource.setUrl("jdbc:mysql://${MYSQL_HOST:localhost}:3306/payment_db");
        dataSource.setUsername("root");
        dataSource.setPassword("password");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }
}
