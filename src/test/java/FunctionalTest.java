import iovi.Main;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FunctionalTest {
    static final String ADDRESS="http://localhost:8080";



    /**Запуск приложения*/
    @BeforeClass
    public static void startMain(){
        String[] args = new String[]{};
        Main.main(args);
        System.setProperty("production","false");
    }
    boolean deleteDbFile(){
        File file = new File("genelink3.mv.db");
        return file.delete();
    }

    @Test
    public void generateAndRedirectTest(){
        String link="https://"+(new Random()).toString()+".ru";

        GenelinkRequestSender sender=new GenelinkRequestSender();
        String newLink=sender.generate(ADDRESS+"/generate",link);
        System.out.println("new="+newLink);
        if (newLink==null)
            fail("new link is null");

        String locationAfterRedirect=sender.getResponseLocation(ADDRESS+newLink,"GET");
        assertEquals(link,locationAfterRedirect);

    }

    /**Проверка одновременной работы приложения с несколькими отправщиками запросов*/
    @Test
    public void manySendersWork() {

        Runnable senderActivity = () -> {
            Random random=new Random();
            String link = "https://"+random.toString()+".ru";
            System.out.println((new Date())+"Thread "+link+" starts");
            GenelinkRequestSender sender = new GenelinkRequestSender();
            String newLink = sender.generate(ADDRESS + "/generate", link);
            if (newLink == null)
                fail("new link is null");

            for (int i = 0; i < 10; i++) {
                String locationAfterRedirect = sender.getResponseLocation(ADDRESS + newLink, "GET");
                assertEquals(link, locationAfterRedirect);
            }
            System.out.println((new Date())+"Thread "+link+" ends");
        };

        Thread threads[] = new Thread[100];
        for (int i = 0; i < 100; i++) {
            threads[i] = new Thread(senderActivity);
            threads[i].start();
        }

        try {
            for (Thread thread : threads)
                thread.join();
        } catch (InterruptedException e) {
            fail();
        }

    }
    /**Проверка одновременной работы приложения с несколькими отправщиками запросов*/
    @Test
    public void getFullStatTest() {
        GenelinkRequestSender sender = new GenelinkRequestSender();
        sender.getFullStatistics(ADDRESS + "/stats",1,10);
    }

}
