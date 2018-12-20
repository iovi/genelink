package iovi;

public class StorageCreator {
    public static StorageService createStorage(){
        return new DummyStorageService();
    }
}
