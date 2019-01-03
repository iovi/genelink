package iovi;

public class StorageCreator {
    static StorageCreator instance;
    H2StorageService h2Service;
    MemoryWithDbStorageService memoryService;
    private StorageCreator(){
        this.h2Service=new H2StorageService("genelink3");
        this.memoryService=new MemoryWithDbStorageService(h2Service);
        MemoryStorageUpdater remover=new MemoryStorageUpdater(10000,memoryService);
        remover.start();
    }
    public static StorageCreator getInstance(){
        if (instance==null)
            instance=new StorageCreator();
        return instance;
    }

    public StorageService getStorageService(){
        return this.memoryService;
    }

}
