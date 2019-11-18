import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, IOException, CyclicReferenceExeption, InstantiationException, InvocationTargetException, ClassNotFoundException, ParserConfigurationException, SAXException {
        ClassB classB = new ClassB();
        classB.setJ(5);
        ClassA classA = new ClassA();
        classA.setField(3);
        classA.setClassB(classB);
        SerealizeClass serealizeClass = new SerealizeClass();
        Object object = serealizeClass.deserialize(serealizeClass.serialize(classA));
        System.out.println(object.getClass().toString());
//        ClassC classC = new ClassC();
//        classC.setField(60);
//        ClassD classD = new ClassD();
//        classD.setClassC(classC);
//        classC.setClassD(classD);
//        serealizeClass.serialize(classC);
    }
}
