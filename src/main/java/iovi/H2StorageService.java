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
            Statement statement = connection.createStatement();
            statement.execute("create table IF NOT EXISTS links (link varchar("+ LINK_MAX_LENGTH +") primary key," +
                    "newlink_key bigint auto_increment);");
            statement.execute("create table IF NOT EXISTS request_history (link_key bigint," +
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
        String query = "select * from (select *,rownum rank from (select l.link,l.newlink_key, nvl(h.cnt,0) from " +
                "(select link_key, count(*) cnt from request_history group by link_key) h " +
                "right join links l on h.link_key=l.newlink_key order by cnt desc)) where newlink_key=?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, key);
            ResultSet resultSet;
            resultSet=preparedStatement.executeQuery();
            if (resultSet.next()) {
                stat = new Statistics(resultSet.getString(1),resultSet.getString(2),resultSet.getInt(3),resultSet.getInt(4));
            }
        }catch (Exception e){
            System.err.print(e.getMessage());
        }
        return stat;

    }

    @Override
    public Statistics[] getAllStatistics(int itemsOnPage,int pageNumber){
        Statistics stats[]=new Statistics[itemsOnPage];
        String query = "select * from (select *,rownum rank from (select l.link,l.newlink_key, nvl(h.cnt,0) from " +
                "(select link_key, count(*) cnt from request_history group by link_key) h " +
                "right join links l on h.link_key=l.newlink_key order by cnt desc)) where rank between ? and ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, (pageNumber-1)*itemsOnPage+1);
            preparedStatement.setInt(2, pageNumber*itemsOnPage);
            ResultSet resultSet;
            resultSet=preparedStatement.executeQuery();
            for(int i=0;resultSet.next();i++) {
                stats[i] = new Statistics(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4));
            }
        }catch (Exception e){
            System.err.print("error: "+e.getMessage());
        }
        return stats;
    }
}


