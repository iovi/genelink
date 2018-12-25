package iovi;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStorageService  {
    Map<String,String> storage;
    H2StorageService h2StorageService;

    public InMemoryStorageService(String dbName){
        storage=new HashMap<String, String>();
    }

    public String getKeyByLink(String link){
        String key=storage.get(link);
        if (key==null)
            key=h2StorageService.getKeyByLink(link);
        return key;
    }


    public String storeLink(String link) {
        String key=h2StorageService.storeLink(link);
        return key;
    }

    public void buildFastStorage(){

    }
}
