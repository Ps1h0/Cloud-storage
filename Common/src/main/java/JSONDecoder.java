import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

//TODO реализовать передачу всех сообщений с помощью JSON/удалить
public class JSONDecoder extends MessageToMessageDecoder<String> {


    //TODO реализовать преобразование любых сообщений в объект любых типов
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        System.out.println("JSONDecoder делает объект");
        Converter converter = new Converter();
        System.out.println(channelHandlerContext);
        System.out.println(s);
        AbstractMessage abstractMessage = converter.toJavaObject(s);
        list.add(abstractMessage);
    }
}
