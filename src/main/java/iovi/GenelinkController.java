package iovi;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class GenelinkController {

    /** Метод генерации короткой ссылки для передаваемой*/
    @RequestMapping(value = "/generate", method = POST)
    public @ResponseBody JSONObject generate() throws IOException {
        JSONObject json  = new JSONObject();
        json.put("link", "https://www.google.com/");
        return json;
    }


}
