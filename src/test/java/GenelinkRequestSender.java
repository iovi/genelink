import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>Отправщик запросов на веб-сервис genelink</p>
 * <p>Рекомендуемый порядок работы - создать новый объект класса, выполнить
 * <ul>
 *     <li>{@link #registerClient(String, String)}</li>
 *     <li>{@link #newCaptcha(String, String)}</li>
 *     <li>{@link #getImage(String, String)}</li>
 *     <li>{@link #solveCaptcha(String, String)}</li>
 *     <li>{@link #verifyClient(String, String)}</li>
 * </ul>
 * необходимые промежуточные результаты будут сохранены в поля класса
 */
public class GenelinkRequestSender {


    /**Метод для получения JSON-объекта из соединения HttpURLConnection*/
    static JSONObject getResponseJSON(HttpURLConnection connection) throws IOException,ParseException{
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        bufferedReader.close();
        JSONParser parser = new JSONParser();
        return(JSONObject) parser.parse(response.toString());
    }

    /**
     * Выполнет POST-запрос на заданный адрес, ожидает выполнения логики
     * {@link iovi.GenelinkController#generate(HttpServletRequest, HttpServletResponse)} по этому адресу
     */
    public String generate(String address, String link){
        String newLink=null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            JSONObject json=new JSONObject();
            json.put("original",link);
            OutputStream stream = connection.getOutputStream();
            stream.write(json.toJSONString().getBytes());
            stream.close();


            int status = connection.getResponseCode();

            if (status==HttpURLConnection.HTTP_OK) {
                json=getResponseJSON(connection);
                newLink=(String)json.get("link");
            }
            connection.disconnect();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            return newLink;
        }

    }

    /**
     * Выполнет HTTP-запрос на указанный адрес, с указанным методом,
     * ожидает выполнения логики {@link CaptchaController#createCaptcha(String, HttpServletResponse)} по этому адресу
     */

    public String getResponseLocation(String address, String method){
        String response=null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setRequestMethod(method);
            response = connection.getHeaderField("Location");
        }catch (Exception e){
            System.err.println(e.getMessage());

        }finally {
            return response;
        }
    }



    /**
     * Выполнет HTTP-запрос на указанный адрес, с указанным методом,
     * ожидает выполнения логики
     * {@link CaptchaController#getCaptchaImage(String, String, HttpServletRequest, HttpServletResponse, Model)} по этому адресу
     */
    public int getFullStatistics(String address, int page,int count){
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(address+"?page="+page+"&count="+count).openConnection();
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            connection.disconnect();
            if (status==HttpURLConnection.HTTP_OK) {
                JSONObject jsonArray=getResponseJSON(connection);
                System.out.println(jsonArray.toJSONString());

            }
            return status;
        }catch (Exception e){
            System.err.println(e.getMessage());
            return 0;
        }
    }


}
