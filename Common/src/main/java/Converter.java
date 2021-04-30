import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

//Класс для преобразования файла в JSON, а JSON в файл
public class Converter {

    //На вход объект FileInfo с данными о файле (имя и содержимое). На выходе строка JSON
    public String toJSON(FileInfo fileInfo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(fileInfo);
    }

    //На вход строка JSON, на выходе объект FileInfo
    public FileInfo toJavaObject(String info) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(info, FileInfo.class);
    }
}
