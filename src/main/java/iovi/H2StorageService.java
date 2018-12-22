package iovi;

import java.sql.*;

public class H2StorageService implements StorageService{

    Connection connection;
    final int MAX_LENGTH=1000;

    public H2StorageService(String dbName){
        try {
            Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:h2:./"+dbName, "sa", "");
            Statement st = null;
            st = connection.createStatement();
            st.execute("create table IF NOT EXISTS links (id bigint auto_increment primary key, url varchar("+MAX_LENGTH+"));");
        }catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }
    public String storeLink(String link) {
        String id=null;
        if (link.length()>MAX_LENGTH)
            return  null;
        try {
            String query = "insert into links(url) values(?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, link);
            preparedStatement.execute();

            ResultSet result;
            Statement statement = connection.createStatement();
            result = statement.executeQuery("SELECT LAST_INSERT_ID()");
            while (result.next()) {
                id = result.getString(1);
                System.out.println("id="+id);
            }
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
        return id;
    }

    @Override
    protected void finalize() throws Throwable {
        if (connection!=null){
            connection.close();
        }
        super.finalize();
    }
}


