import java.sql.*;

public class H2QueryExecutor {
    Connection connection;
    public H2QueryExecutor(String dbName) throws Exception{
        Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:h2:./"+dbName+";TRACE_LEVEL_FILE=0", "sa", "");
    }
    public void closeConnection() throws SQLException{
        connection.close();
    }
    public ResultSet executeQuery(String query) throws SQLException{
        Statement statement = connection.createStatement();
        statement.execute(query);
        return statement.getResultSet();
    }
}
