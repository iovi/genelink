import iovi.Statistics;
import iovi.storage.db.H2StorageService;
import org.junit.Test;

import java.io.File;
import java.sql.ResultSet;

import static org.junit.Assert.*;

/**Тест класса {@link H2StorageService}*/
public class H2StorageServiceTest {

    String dbName="testDB";

    boolean deleteDbFile(){
        File file = new File(dbName+".mv.db");
        return file.delete();
    }

    /**Тест конструктора. Проверяется:
     * <ul>
     *     <li>создание файла БД с указанным именем</li>
     *     <li>создание в этой БД пустых таблиц link и request_history, содержащих колонки с правильным названием </li>
     * </ul>
     * */
    @Test
    public void createH2Storage(){
        deleteDbFile();
        try {
            H2StorageService service = new H2StorageService(dbName);

            H2QueryExecutor h2QueryExecutor=new H2QueryExecutor(dbName);
            ResultSet resultSet=h2QueryExecutor.executeQuery("select link,newlink_key from links");
            assertEquals(resultSet.next(),false);

            resultSet=h2QueryExecutor.executeQuery("select link_key,request_time from request_history");
            assertEquals(resultSet.next(),false);

            h2QueryExecutor.closeConnection();
            service.closeStorage();
            assertEquals(deleteDbFile(),true);
        }catch (Exception e){
            System.err.println(e.getMessage());
            fail("CreateDb exception");
        }

    }
    /**Тест {@link H2StorageService#storeLink(String)}. Проверяется
     * <ul>
     *     <li>Возврат ненулевого key для первого сохранение ссылки</li>
     *     <li>Наличие в таблице links сохраненной связки ссылка-ключ после сохранения</li>
     *     <li>Возврат нулевого key для второго сохранения ссылки (контроль уникальности ссылок)</li>
     * </ul>
     * Вывод сообщений в потоке ошибок считается нормальным
     * */
    @Test
    public void storeLinkTest(){
        deleteDbFile();
        String link="http://url.ru";
        H2StorageService service = new H2StorageService(dbName);

        String key=service.storeLink(link);
        assertNotEquals(key,null);


        try {
            H2QueryExecutor h2QueryExecutor=new H2QueryExecutor(dbName);
            ResultSet resultSet = h2QueryExecutor.executeQuery(
                    "select link,newlink_key from links where link='" + link + "'");
            if (resultSet.next()) {
                String actualLink=resultSet.getString(1),
                        actualKey=resultSet.getString(2);
                assertEquals(link,actualLink);
                assertEquals(key,actualKey);
            } else
                fail("Empty result set");
        }catch (Exception e){
            System.err.println("Test error: "+e.getMessage());
            fail("storeLinkTest exception");
        }
        //возвращает null при попытке сохранения уже имеющейся ссылки
        key=service.storeLink("http://url.ru");
        assertEquals(key,null);
        deleteDbFile();
    }

    /**Тест {@link H2StorageService#storeHistory(String)} . Проверяется
     * <ul>
     *     <li>Неуспешное сохранение несущестующего в БД ключа</li>
     *     <li>Успешное сохранение существующего ключа</li>
     *     <li>Наличие сохраненной записи в таблице request_history после сохранения</li>
     * </ul>
     * Вывод сообщений в потоке ошибок считается нормальным
     * */
    @Test
    public void storeHistoryTest(){
        deleteDbFile();
        String link="http://url2.ru";
        H2StorageService service = new H2StorageService(dbName);

        assertEquals(service.storeHistory("111"),false);

        String key=service.storeLink(link);
        assertEquals(service.storeHistory(key),true);
        try {
            H2QueryExecutor h2QueryExecutor=new H2QueryExecutor(dbName);
            ResultSet resultSet = h2QueryExecutor.executeQuery(
                    "select count(*) from request_history where link_key='" + key + "'");
            if (resultSet.next()) {
                int count=resultSet.getInt(1);
                assertEquals(count,1);
            } else
                fail("Empty result set for storeHistory");
        }catch (Exception e){
            System.err.println("Test error: "+e.getMessage());
            fail("storeHistoryTest exception");
        }
    }

    /**Тест {@link H2StorageService#getKeyByLink(String)} и {@link H2StorageService#getLinkByKey(String)}.
     * Выполняет запись новой ссылки в БД, получает новый ключ.
     * Выполняет поиск ключа и ссылки, сравнивает с ранее сохранными.
     * */
    @Test
    public void getTest(){
        deleteDbFile();
        String link="http://url.zw";
        H2StorageService service = new H2StorageService(dbName);
        String key=service.storeLink(link);

        String gotLink=service.getLinkByKey(key),
                gotKey=service.getKeyByLink(link);
        assertEquals(gotKey,key);
        assertEquals(gotLink,link);
    }

    /**Тест {@link H2StorageService#getLinkStatistics(String)}.
     * Записывает новые ссылки в БД, получает новые ключи.
     * Сохраняет историю использования ключей. Получает статистику и сравнивает с количеством сохранений истории.
     * */
    @Test
    public void getLinkStatisticsTest(){
        deleteDbFile();
        H2StorageService service = new H2StorageService(dbName);
        String link1="http://url.rw";
        String link2="http://url.zw";
        String key1=service.storeLink(link1);
        String key2=service.storeLink(link2);
        service.storeHistory(key1);
        service.storeHistory(key2);
        service.storeHistory(key2);
        service.storeHistory(key2);

        Statistics stat1=service.getLinkStatistics(key1);
        assertEquals(stat1.getOriginalLink(),link1);
        assertEquals(stat1.getKey(),key1);
        assertEquals(stat1.getCount(),1);
        assertEquals(stat1.getRank(),2);

        Statistics stat2=service.getLinkStatistics(key2);
        assertEquals(stat2.getOriginalLink(),link2);
        assertEquals(stat2.getKey(),key2);
        assertEquals(stat2.getCount(),3);
        assertEquals(stat2.getRank(),1);
    }

    /**Тест {@link H2StorageService#getAllStatistics(int, int)}.
     * Проверка аналогичная {@link #getLinkStatisticsTest()},
     * кроме того, проверяется, что для больших номеров страницы возвращаются нулевые элементы массива статистик.
     * */
    @Test
    public void getAllStatisticsTest(){
        deleteDbFile();
        H2StorageService service = new H2StorageService(dbName);
        String link1="http://url.ru";
        String link2="http://url.rw";
        String link3="http://url.zw";
        String key1=service.storeLink(link1);
        String key2=service.storeLink(link2);
        String key3=service.storeLink(link3);

        service.storeHistory(key1);
        service.storeHistory(key1);
        service.storeHistory(key2);
        service.storeHistory(key2);
        service.storeHistory(key2);
        service.storeHistory(key3);

        Statistics stats[]=service.getAllStatistics(2,1);
        assertEquals(stats.length,2);
        //link2 statistics
        assertEquals(stats[0].getOriginalLink(),link2);
        assertEquals(stats[0].getKey(),key2);
        assertEquals(stats[0].getCount(),3);
        assertEquals(stats[0].getRank(),1);

        //link1 statistics
        assertEquals(stats[1].getOriginalLink(),link1);
        assertEquals(stats[1].getKey(),key1);
        assertEquals(stats[1].getCount(),2);
        assertEquals(stats[1].getRank(),2);

        //link3 statistics
        stats=service.getAllStatistics(2,2);
        assertEquals(stats[0].getOriginalLink(),link3);
        assertEquals(stats[0].getKey(),key3);
        assertEquals(stats[0].getCount(),1);
        assertEquals(stats[0].getRank(),3);

        //absent item in statistics
        stats=service.getAllStatistics(3,10);
        assertEquals(stats.length,3);
        assertNull(stats[0]);
        assertNull(stats[1]);
        assertNull(stats[2]);

    }
}
