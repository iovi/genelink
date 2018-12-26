package iovi;

public class StorageCreator {
    public static StorageService createStorage(){
        return new H2StorageService("genelink3");
    }
}
