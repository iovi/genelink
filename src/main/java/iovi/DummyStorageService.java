package iovi;


public class DummyStorageService implements StorageService {
    @Override
    public String storeLink(String link) {
        return "https://www.google.ru/webhp";
    }
}
