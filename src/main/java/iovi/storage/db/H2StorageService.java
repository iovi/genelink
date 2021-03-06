package iovi.storage.db;

import iovi.Statistics;
import iovi.storage.StorageService;

import java.sql.*;

/**StorageService, использующий для хранение файл БД h2*/
public class H2StorageService implements StorageService {

    Connection connection;
    final int LINK_MAX_LENGTH =1000;

    /**В конструкторе создается подключение к БД в файле с указанным именем, создаются таблицы (при их отсутствии):
     * <ul>
     * <li>links (link varchar({@link #LINK_MAX_LENGTH}) primary key, newlink_key bigint auto_increment) </li>
     * <li>request_history (link_key bigint, request_time timestamp,<br>
     * foreign key (link_key) references links(newlink_key))</li>
     * </ul>
     * Подключение рекомендуется закрыть с помощью {@link #closeStorage}
     * @param dbName имя файла БД. Указание пути в имени необязательно
     * */
    public H2StorageService(String dbName){
        try {
            Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:h2:./"+dbName+";LOCK_MODE=3", "sa", "");
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

    /**
     * Сохранение ссылки в таблице links
     * @return номер ключа для ссылки, который генерируется БД автоматически (автоинкрементальное поле newlink_key).
     *  При попытке сохранить уже существующую ссылку в таблице вернется null и выведется текст в потоке ошибок
     *  */
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


    /**
     * Сохранение истории об использовании ключа в таблице request_history<br>
     * При передаче несуществующего в базе ключа выведется текст в потоке ошибок
     *  */
    @Override
    public boolean storeHistory(String key){
        try {
            String query="insert into request_history values (?,CURRENT_TIMESTAMP())";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,key);
            preparedStatement.execute();
            return true;
        }catch(SQLException e){
            System.err.print(e.getMessage());
        }
        return false;
    }

    /**
     * Возвращает ссылку по ключу
     * @return оригинальная ссылка или null,
     * если такого ключа нет или произошла какая-то ошибка при работе с БД
     *  */
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

    /**
     * Возвращает ключ по ссылке
     * @return ключ для указанной ссылки или null,
     * если такого ключа нет или произошла какая-то ошибка при работе с БД
     *  */
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
    /**Постраничное получение статистики использования всех ключей из БД H2 на основании истории
     * @return массив статистик (из itemsOnPage элементов) с соответствующей страницы.
     * Элементы с порядковым номером больше, чем общее количество ссылок в хранилище будут нулевыми.
     * Пример - в хранилище 7 разных ссылок, переданы itemsOnPage=5, pageNumber=2.
     * Будет возвращен массив с нулевыми последними тремя элементами.
    */
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

    /**Закрытие соединения с БД*/
    @Override
    public void closeStorage(){
        try{
            connection.close();
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }
}


