package iovi;


public interface StorageService {
    String storeLink(String originalLink);
    String getKeyByLink(String originalLink);
    String getLinkByKey(String key);
    int getLinkRank(String key);
    int getLinkTotalCount (String key);
}
