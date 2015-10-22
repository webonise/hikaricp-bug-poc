package poc;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariJNDIFactory;

import javax.naming.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * As root, run the following commands
 *
 */
public class Main {

  public static final String SCHEMA = "hikaribug";
  public static final String JDBC_URL="jdbc:mysql://localhost:3306/" + SCHEMA;
  public static final String USERNAME="hikaripoc";
  public static final String PASSWORD="hikari good or bad";
  public static final String QUERY="SELECT SCHEMA()";

  public static void main(String[] args) throws Exception {
    checkDirectJdbcConnectionHasDefaultSchema();
    checkFreshHikariCPConnectionHasDefaultSchema();
    checkReturnedAndCheckedOutHikariCPConnectionHasDefaultSchema();
    checkHikariCPJNDIFactoryHasDefaultSchema();
  }

  private static void checkHikariCPJNDIFactoryHasDefaultSchema() throws Exception {
    DataSource dataSource = fetchDataSourceFromHikariCPJNDIFactory(generateHikariCPJNDIFactory());
    try(Connection conn = dataSource.getConnection()) {
      checkConnection("fresh connection from JNDI Factory", conn);
    }
    for(int i = 0; i < 10; i++) {
      try (Connection conn = dataSource.getConnection()) {
        checkConnection("reacquired connection #" + (i+1) + " from JNDI Factory", conn);
      }
    }
  }

  private static final DataSource fetchDataSourceFromHikariCPJNDIFactory(HikariJNDIFactory factory) throws Exception {
    Reference ref = new Reference("javax.sql.DataSource");
    ref.add(new StringRefAddr("jdbcUrl", JDBC_URL));
    ref.add(new StringRefAddr("username", USERNAME));
    ref.add(new StringRefAddr("password", PASSWORD));
    DataSource dataSource = DataSource.class.cast(factory.getObjectInstance(ref, null, null, null));
    Objects.requireNonNull(dataSource, "returned datasource from JDNI Factory");
    return dataSource;
  }

  private static final HikariJNDIFactory generateHikariCPJNDIFactory() {
    HikariJNDIFactory factory = new HikariJNDIFactory();
    return factory;
  }

  private static final HikariDataSource generateHikariCPDataSource() {
    HikariDataSource hikari = new HikariDataSource();
    hikari.setJdbcUrl(JDBC_URL);
    hikari.setUsername(USERNAME);
    hikari.setPassword(PASSWORD);
    return hikari;
  }

  private static void checkReturnedAndCheckedOutHikariCPConnectionHasDefaultSchema() throws Exception {
    HikariDataSource hikari = generateHikariCPDataSource();
    try(Connection conn = hikari.getConnection()) {
      checkConnection("initially acquired Hikari connection", conn);
    }
    for(int i = 0; i < 10; i++) {
      try (Connection conn = hikari.getConnection()) {
        checkConnection("reacquired Hikari connection #" + (i+1), conn);
      }
    }
  }

  private static void checkFreshHikariCPConnectionHasDefaultSchema() throws Exception {
    try(Connection conn = generateHikariCPDataSource().getConnection()) {
      checkConnection("fresh Hikari connection", conn);
    }
  }

  private static void checkDirectJdbcConnectionHasDefaultSchema() throws Exception {
    try(Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
      checkConnection("direct JDBC connection", conn);
    }
  }

  private static void checkConnection(String connectionName, Connection conn) throws Exception {
    Objects.requireNonNull(conn);
    try(PreparedStatement stmt = conn.prepareStatement(QUERY)) {
      try(ResultSet rs = stmt.executeQuery()) {
        if(!rs.next()) {
          throw new IllegalStateException("No results from " + QUERY + " from " + connectionName);
        }
        String schema = rs.getString(1);
        if(schema == null || "".equals(schema)) {
          throw new IllegalStateException("Found a null or empty schema when querying using " + connectionName);
        }
        if(!SCHEMA.equalsIgnoreCase(schema)) {
          throw new IllegalStateException("Found the wrong schema when using " + connectionName + " => " + schema);
        }
      }
    }
    System.out.println("Successfully checked " + connectionName);
    System.out.flush();
  }

}
