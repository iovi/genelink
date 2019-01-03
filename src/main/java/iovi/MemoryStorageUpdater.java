package iovi;

/** Поток, занимающийся периодическим обновлением хранилища в памяти*/
public class MemoryStorageUpdater extends Thread {
    long wakeUpPeriod;
    InMemoryStorageService service;

    /**
     Конструктор. В дальнейшем в ходе работы поток будет каждые wakeUpPeriod мс выполнять
     обновление хранилища
     * @param wakeUpPeriod период удалениея в мс
     * @param service - сервис хранилища в памяти (реализует {@link InMemoryStorageService})
     * */
    public MemoryStorageUpdater(long wakeUpPeriod, InMemoryStorageService service){
        this.wakeUpPeriod=wakeUpPeriod;
        this.service=service;
    }

    @Override
    public void run() {
        for(;;) {
            try {
                service.buildInMemoryStorage();
                Thread.sleep(wakeUpPeriod);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
