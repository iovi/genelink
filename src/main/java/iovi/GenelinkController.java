package iovi;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;


import static org.springframework.web.bind.annotation.RequestMethod.GET;
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
                String key=storageService.getKeyByLink(originalLink);
                if (key==null){
                    key=storageService.storeLink(originalLink);
                }

                outputJson.put("link","/l/"+key);

            } catch (Exception e) {
                System.err.print("JSONException"+e.getMessage());
            }
        } catch (IOException e){
            System.err.print(e.getMessage());
        }
        return outputJson;
    }

    /** Метод формирования статистики для ссылки*/
    @RequestMapping(value = "/stats/{key}", method = GET)
    public @ResponseBody JSONObject linkStat(@PathVariable("key") String key) {

        JSONObject outputJson=new JSONObject();
        String link=storageService.getLinkByKey(key);
        int count=storageService.getLinkTotalCount(key);
        int rank=storageService.getLinkRank(key);
        outputJson.put("original",link);
        outputJson.put("link",key);
        outputJson.put("rank",rank);
        outputJson.put("count",count);
        return outputJson;
    }



}
