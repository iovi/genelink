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
    static final long WAKE_UP_PERIOD=10000;
    static final int LOCAL_HISTORY_SIZE=1000;
    Map<String,String> storage;
    List<String> history;
    StorageService dbService;
    Thread updater;

    public MemoryWithDbStorageService(StorageService dbService){

        storage=synchronizedMap(new HashMap<>());
        history=synchronizedList(new ArrayList<String>());
        this.dbService = dbService;
        MemoryUpdatingThread updater=new MemoryUpdatingThread(WAKE_UP_PERIOD,this);
        updater.start();
        this.updater=updater;
    }

    @Override
    public boolean storeHistory(String key) {
        boolean result=true;
        if (history.size()> LOCAL_HISTORY_SIZE)
            result=storeHistory2Db();
        else
            history.add(key);
        return result;
    }

    private boolean storeHistory2Db(){
        boolean result=false;
        for(String key:history){
            result=dbService.storeHistory(key);
        }
        history.clear();
        return result;
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
        Statistics stats[]= dbService.getAllStatistics(LOCAL_HISTORY_SIZE,1);
        storage.clear();
        for (int i=0;i<stats.length && stats[i]!=null;i++){
            if (stats[i].getCount()>settings.getBarrierForMemory()){
                storage.put(stats[i].getKey(),stats[i].getOriginalLink());
                System.out.println(stats[i].getKey()+" Added to storage");
            }
        }
    }

    @Override
    public void closeStorage(){
        updater.interrupt();
        try{
            updater.join();
        }
        catch (InterruptedException e){
            System.err.println(e.getMessage());
        }
        storeHistory2Db();
        dbService.closeStorage();
    }
}
