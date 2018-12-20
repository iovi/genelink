package iovi;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;


import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Controller
public class GenelinkController {

    StorageService storageService=StorageCreator.createStorage();

    /** Метод генерации короткой ссылки для передаваемой*/
    @RequestMapping(value = "/generate", method = POST)
    public @ResponseBody JSONObject generate(HttpServletRequest request, HttpServletResponse response) {

        JSONObject outputJson=new JSONObject();
        try{

            StringBuffer builder = new StringBuffer();
            BufferedReader reader = request.getReader();
            JSONParser parser = new JSONParser();
            try{
                JSONObject inputJson = (JSONObject) parser.parse(reader);
                String originalLink=inputJson.get("original").toString();
                outputJson.put("link",storageService.storeLink(originalLink));

            } catch (Exception e) {
                System.err.print("JSONException"+e.getMessage());
            }
        } catch (IOException e){
            System.err.print(e.getMessage());
        }
        return outputJson;
    }


}
