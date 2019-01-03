package iovi.storage;


import iovi.Statistics;

public interface StorageService {
    String storeLink(String originalLink);
    String getKeyByLink(String originalLink);
    String getLinkByKey(String key);
    void storeHistory(String key);
    Statistics getLinkStatistics(String key);
    Statistics[] getAllStatistics(int itemsOnPage,int pageNumber);
}
