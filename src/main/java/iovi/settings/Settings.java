package iovi.settings;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Settings")
public class Settings {

    /**Название БД*/
    String dbName;
    /**Период запуска системного обслуживания в мс*/
    long wakeUpPeriod;
    /**Максимальное количество ссылок в памяти*/
    int linksInMemoryCount;
    /**Порог количества переходов по ссылке, после которого она записывается в память*/
    int redirectionsBarrier;
    /**Длина истории в памяти*/
    int localHistorySize;

    public int getBarrierForMemory() {
        return redirectionsBarrier;
    }

    public int getLinksInMemoryCount() {
        return linksInMemoryCount;
    }

    public long getWakeUpPeriod() {
        return wakeUpPeriod;
    }

    public String getDbName() {
        return dbName;
    }

    public int getLocalHistorySize() {
        return localHistorySize;
    }

    public void setBarrierForMemory(int barrierForMemory) {
        this.redirectionsBarrier = barrierForMemory;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setLinksInMemoryCount(int linksInMemoryCount) {
        this.linksInMemoryCount = linksInMemoryCount;
    }

    public void setWakeUpPeriod(long wakeUpPeriod) {
        this.wakeUpPeriod = wakeUpPeriod;
    }

    public void setLocalHistorySize(int localHistorySize) {
        this.localHistorySize = localHistorySize;
    }
}
