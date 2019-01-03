package iovi.storage.memory;

import iovi.Statistics;
import iovi.settings.Settings;
import iovi.settings.SettingsExtractor;
import iovi.storage.StorageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Collections.*;

public class MemoryWithDbStorageService implements StorageService,InMemoryStorageService {
    Map<String,String> storage;
    List<String> history;

    StorageService dbService;

    public MemoryWithDbStorageService(StorageService dbService){

        storage=synchronizedMap(new HashMap<>());
        history=synchronizedList(new ArrayList<String>());
        this.dbService = dbService;
    }

    @Override
    public void storeHistory(String key) {
        int localHistorySize=SettingsExtractor.extractSettings().getLocalHistorySize();
        if (history.size()> localHistorySize)
            storeHistory2Db();
        else
            history.add(key);
    }

    private void storeHistory2Db(){
        for(String key:history){
            dbService.storeHistory(key);
        }
        history.clear();
        System.out.println("history stored");
    }
    @Override
    public String getKeyByLink(String link){
        return dbService.getKeyByLink(link);
    }


    @Override
    public String storeLink(String link) {
        String key= dbService.storeLink(link);
        return key;
    }

    @Override
    public String getLinkByKey(String key) {
        String link=storage.get(key);
        if (link==null){
            link= dbService.getLinkByKey(key);
            System.out.println("from DB");
        } else{
            System.out.println("from storage");
        }
        return link;
    }

    @Override
    public Statistics getLinkStatistics(String key) {
        storeHistory2Db();
        return dbService.getLinkStatistics(key);
    }

    @Override
    public Statistics[] getAllStatistics(int itemsOnPage, int pageNumber) {
        storeHistory2Db();
        return dbService.getAllStatistics(itemsOnPage,pageNumber);
    }

    @Override
    public void buildInMemoryStorage(Settings settings){
        Statistics stats[]= dbService.getAllStatistics(settings.getLinksInMemoryCount(),1);
        storage.clear();
        System.out.println("\nStorage cleared");
        for (int i=0;i<stats.length && stats[i]!=null;i++){
            if (stats[i].getCount()>settings.getBarrierForMemory()){
                storage.put(stats[i].getKey(),stats[i].getOriginalLink());
                System.out.println(stats[i].getKey()+" Added to storage");
            }
        }
    }
}
