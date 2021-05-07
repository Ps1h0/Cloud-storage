import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

//Класс для преобразования объекта в JSON, а JSON в в объект
//TODO реализовать передачу через JSON/удалить
public class Converter {

    ObjectMapper mapper = new ObjectMapper();

    //На вход объект, на выходе строка JSON
    public String toJSON(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    //На вход строка JSON, на выходе объект FileInfo
    public FileInfo toJavaObject(String info) throws IOException {
        return mapper.readValue(info, FileInfo.class);
    }
}
