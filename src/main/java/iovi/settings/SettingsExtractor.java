package iovi.settings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;

/**Класс для извлечения настроек приложения*/
public class SettingsExtractor {

    static String configFile="app_config.xml";


    /**
     * Возвращает заведенные настройки в виде объекта заданного конфигурационного класса.
     * @return объект, содержащий необходимые настройки.
     */
    public static Settings extractSettings(){
        try {

            File file = new File(configFile);
            JAXBContext jaxbContext = JAXBContext.newInstance(Settings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Settings) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

}
