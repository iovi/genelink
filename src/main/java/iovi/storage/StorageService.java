package iovi.storage;


import iovi.Statistics;

/**Интерфейс взаимодействия с хранилищем*/
public interface StorageService {
    /**
     * Сохранение новой ссылки в хранилище
     * @param originalLink новая ссылка
     * @return ключ, по которому можно работать со ссылкой*/
    String storeLink(String originalLink);
    /**Возвращает ключ по ссылке*/
    String getKeyByLink(String originalLink);
    /**Возвращает ссылку по ключу*/
    String getLinkByKey(String key);
    /**Сохранение информации об использовании ключа
     * @return успешность сохранения,
     * true - сохранение выполнено, false - сохранить не удалось (например, если указан несуществующий ключ)*/
    boolean storeHistory(String key);
    /**Получение статистики использования конкретного ключа на основании ранее сохраненной истории
     * @param key ключ, по которому запрошена статистика
     * @return стастика использования данного ключа*/
    Statistics getLinkStatistics(String key);
    /**Постраничное получение статистики использования всех ключей на основании истории
     * @param itemsOnPage количество элементов на странице
     * @param pageNumber номер страницы
     * @return массив статистик (из itemsOnPage элементов) с соответствующей страницы.
     * */
    Statistics[] getAllStatistics(int itemsOnPage,int pageNumber);

    void closeStorage();
}
