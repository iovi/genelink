package iovi;


import iovi.storage.StorageProvider;
import iovi.storage.StorageService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;


import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Controller
public class GenelinkController {

    StorageService storageService= StorageProvider.getInstance().getStorageService();

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
        Statistics statistics=storageService.getLinkStatistics(key);
        if (statistics!=null){
            outputJson.put("original",statistics.getOriginalLink());
            outputJson.put("link",statistics.getKey());
            outputJson.put("rank",statistics.getRank());
            outputJson.put("count",statistics.getCount());
        }
        return outputJson;
    }

    /** Метод формирования полной статистики*/
    @RequestMapping(value = "/stats", method = GET)
    public @ResponseBody JSONArray fullStat(@RequestParam("page") String pageParam,
                                            @RequestParam("count") String countParam,
                                            HttpServletResponse response) {

        Statistics[] stats;
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        try{
            int pageNumber=Integer.parseInt(pageParam),
                    count=Integer.parseInt(countParam);

            if (pageNumber<1 || count<1 || count>100){
                response.setStatus(400);
                return null;
            }
            stats=storageService.getAllStatistics(count,pageNumber);
            for (int i=0;i<count && stats[i]!=null;i++){
                jsonObject=new JSONObject();
                jsonObject.put("original",stats[i].getOriginalLink());
                jsonObject.put("link",stats[i].getKey());
                jsonObject.put("rank",stats[i].getRank());
                jsonObject.put("count",stats[i].getCount());
                jsonArray.add(jsonObject);
            }
        } catch (Exception e){
            System.err.print(e.getMessage());
        }
        return jsonArray;
    }





}
