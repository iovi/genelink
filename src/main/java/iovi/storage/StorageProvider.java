package iovi.storage;

import iovi.settings.Settings;
import iovi.settings.SettingsExtractor;
import iovi.storage.db.H2StorageService;
import iovi.storage.memory.MemoryWithDbStorageService;

public class StorageProvider {
    static StorageProvider instance;
    H2StorageService h2Service;
    MemoryWithDbStorageService memoryService;
    private StorageProvider(){
        Settings settings=SettingsExtractor.extractSettings();
        this.h2Service=new H2StorageService(settings.getDbName());
        this.memoryService=new MemoryWithDbStorageService(h2Service);

    }
    public static StorageProvider getInstance(){
        if (instance==null)
            instance=new StorageProvider();
        return instance;
    }

    public StorageService getStorageService(){
        return this.memoryService;
    }

}
