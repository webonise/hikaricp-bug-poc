package poc;

import com.zaxxer.hikari.HikariDataSource;

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
  }

  private static void checkFreshHikariCPConnectionHasDefaultSchema() throws Exception {
    HikariDataSource hikari = new HikariDataSource();
    hikari.setJdbcUrl(JDBC_URL);
    hikari.setUsername(USERNAME);
    hikari.setPassword(PASSWORD);
    try(Connection conn = hikari.getConnection()) {
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
  }

}
