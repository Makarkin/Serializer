import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

public interface SuperEncoder {
    byte[] serialize(Object javaBean) throws CyclicReferenceExeption, IllegalAccessException, IOException;
    Object deserialize(byte[] data) throws CyclicReferenceExeption, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException, ParserConfigurationException, IOException, SAXException;
}
