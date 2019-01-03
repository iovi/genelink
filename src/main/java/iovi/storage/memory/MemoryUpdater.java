package iovi.storage.memory;

import iovi.settings.Settings;
import iovi.settings.SettingsExtractor;
import iovi.storage.memory.InMemoryStorageService;

/** Поток, занимающийся периодическим обновлением хранилища в памяти*/
public class MemoryUpdater extends Thread {
    long wakeUpPeriod;
    InMemoryStorageService service;

    /**
     Конструктор. В дальнейшем в ходе работы поток будет каждые wakeUpPeriod мс выполнять
     обновление хранилища
     * @param wakeUpPeriod период удалениея в мс
     * @param service - сервис хранилища в памяти (реализует {@link InMemoryStorageService})
     * */
    public MemoryUpdater(long wakeUpPeriod, InMemoryStorageService service){
        this.wakeUpPeriod=wakeUpPeriod;
        this.service=service;
    }

    @Override
    public void run() {
        Settings settings= SettingsExtractor.extractSettings();
        for(;;) {
            try {

                service.buildInMemoryStorage(settings);
                Thread.sleep(wakeUpPeriod);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
