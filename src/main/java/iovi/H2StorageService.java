package iovi;

import com.sun.org.glassfish.external.statistics.Statistic;

import java.sql.*;

public class H2StorageService implements StorageService{

    Connection connection;
    final int LINK_MAX_LENGTH =1000;

    public H2StorageService(String dbName){
        try {
            Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:h2:./"+dbName, "sa", "");
            Statement st = null;
            st = connection.createStatement();
            st.execute("create table IF NOT EXISTS links (link varchar("+ LINK_MAX_LENGTH +") primary key," +
                    "newlink_key bigint auto_increment);");
            st.execute("create table IF NOT EXISTS request_history (link_key bigint," +
                    "request_time timestamp not null," +
                    "foreign key (link_key) references links(newlink_key))");
        }catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }
    @Override
    public String storeLink(String link) {
        String key=null;
        if (link.length()> LINK_MAX_LENGTH)
            return  null;
        try {
            String query = "insert into links(link) values(?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, link);
            preparedStatement.execute();

            ResultSet resultSet;
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT LAST_INSERT_ID()");
            if (resultSet.next())
                key = resultSet.getString(1);


        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
        return key;
    }

    @Override
    public void storeHistory(String key){
        try {
            String query="insert into request_history values (?,CURRENT_TIMESTAMP())";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,key);
            preparedStatement.execute();
        }catch(SQLException e){
            System.err.print(e.getMessage());
        }
    }


    @Override
    public String getKeyByLink(String link){
        String key=null;

        String query = "select newlink_key from links where link=?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, link);
            ResultSet resultSet;
            resultSet=preparedStatement.executeQuery();
            if (resultSet.next()) {
                key = resultSet.getString(1);
            }else{
                System.err.print("No data found in "+Thread.currentThread().getStackTrace());
            }
        }catch (SQLException e){
            System.err.print(e.getMessage());
        }
        return key;

    }
    @Override
    public String getLinkByKey(String key){
        String link=null;

        String query = "select link from links where newlink_key=?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, key);
            ResultSet resultSet;
            resultSet=preparedStatement.executeQuery();
            if (resultSet.next()) {
                link = resultSet.getString(1);
            }else{
                System.err.print("No data found in "+Thread.currentThread().getStackTrace());
            }
        }catch (SQLException e){
            System.err.print(e.getMessage());
        }
        return link;

    }


    @Override
    protected void finalize() throws Throwable {
        if (connection!=null){
            connection.close();
        }
        super.finalize();
    }

    @Override
    public Statistics getLinkStatistics(String key){
        Statistics stat=null;
        String query = "select cnt,rank from " +
                "(select link_key,rownum rank, cnt from " +
                "(select link_key, count(*) cnt from request_history  group by link_key order by cnt desc)) " +
                "where link_key=?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, key);
            ResultSet resultSet;
            resultSet=preparedStatement.executeQuery();
            if (resultSet.next()) {
                stat = new Statistics(resultSet.getInt(1),resultSet.getInt(2));
            }else{
                System.err.print("No data found in "+Thread.currentThread().getStackTrace());
            }
        }catch (Exception e){
            System.err.print(e.getMessage());
        }
        return stat;

    }

    @Override
    public Statistics[] getAllStatistics(int itemsOnPage,int pageNumber){
        Statistics stats[]=null;
        return stats;
    }
}


