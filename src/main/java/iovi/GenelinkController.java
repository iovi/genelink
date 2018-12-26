package iovi;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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

    /** Метод перенаправления*/
    @RequestMapping(value = "/l/{key}")
    public RedirectView redirect(@PathVariable("key") String key) {
        String link=storageService.getLinkByKey(key);
        storageService.storeHistory(key);
        return new RedirectView(link);
    }


    /** Метод формирования статистики для ссылки*/
    @RequestMapping(value = "/stats/{key}", method = GET)
    public @ResponseBody JSONObject linkStat(@PathVariable("key") String key) {

        JSONObject outputJson=new JSONObject();
        String link=storageService.getLinkByKey(key);
        Statistics statistics=storageService.getLinkStatistics(key);
        outputJson.put("original",link);
        outputJson.put("link",key);
        if (statistics!=null){
            outputJson.put("rank",statistics.getRank());
            outputJson.put("count",statistics.getCount());
        }
        return outputJson;
    }



}
