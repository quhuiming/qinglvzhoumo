package com.qinglvzhoumo.api.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {
  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;

  public DatabaseSchemaInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
    this.dataSource = dataSource;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      String productName = metaData.getDatabaseProductName();
      if (productName == null || !productName.toLowerCase().contains("mysql")) {
        return;
      }
    }
    jdbcTemplate.execute("ALTER TABLE user_accounts MODIFY device_id varchar(128) NULL");
    jdbcTemplate.update("UPDATE user_accounts SET account_type = 'ANONYMOUS' WHERE account_type IS NULL OR account_type = ''");
  }
}
