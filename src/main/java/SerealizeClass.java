import com.sun.org.apache.bcel.internal.util.ClassLoaderRepository;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;

public class SerealizeClass implements SuperEncoder {

    private LinkedHashSet<String> classesFields = new LinkedHashSet<>();

    public byte[] serialize(Object javaBean) throws CyclicReferenceExeption, IllegalAccessException, IOException {
        classesFields.clear();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("firstClass").addAttribute("name", javaBean.getClass().getName());
        fieldXMLWriter(root, javaBean);
        OutputFormat format = OutputFormat.createCompactFormat();
        FileOutputStream fos = new FileOutputStream("xmlclass.xml");
        XMLWriter writer = new XMLWriter(fos, format);
        writer.write(document);
        writer.flush();

        File file = new File("xmlclass.xml");
        final FileInputStream fis = new FileInputStream("xmlclass.xml");
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        fis.close();
        return bytes;
    }

    private void fieldXMLWriter(Element element, Object javaBean) throws IllegalAccessException, CyclicReferenceExeption {
        classesFields.add(javaBean.getClass().getTypeName());
        Element element1;
        String firstClassName;
        Field[] javaBeanFields = javaBean.getClass().getDeclaredFields();
        for (Field field : javaBeanFields) {
            field.setAccessible(true);
            if (String.valueOf(field.getType().getSuperclass()) == "null") {
                element.addElement(field.getName()).addAttribute("class", "primitive").addAttribute("type", field.getType().toString()).addAttribute("value", field.get(javaBean).toString());
                firstClassName = classesFields.iterator().next();
                this.classesFields.clear();
                this.classesFields.add(firstClassName);
            } else {
                if (!classesFields.contains(field.getType().toString().split(" ")[1])) {
                    classesFields.add(field.getType().toString().split(" ")[1]);
                    element1 = element.addElement(field.getName()).addAttribute("class", "object").addAttribute("type", field.getType().toString());
                    fieldXMLWriter(element1, field.get(javaBean));
                } else {
                    throw new CyclicReferenceExeption();
                }
            }
        }
    }

    public Object deserialize(byte[] bytes) throws IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException, ParserConfigurationException, IOException, SAXException, CyclicReferenceExeption, NoSuchFieldException {
        classesFields.clear();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document document = builder.parse(new ByteArrayInputStream(bytes));
        String firstClassName = document.getFirstChild().
                getAttributes().item(0).toString().
                replace("name=\"", "").
                replace("\"", "");
        Class clazz = Class.forName(firstClassName);
        Object javaBean = clazz.getConstructor().newInstance();
        fieldXMLReader(document.getFirstChild(), javaBean);
        return javaBean;
    }

    private String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";//или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private String getWrapperClassName(String word){
        if (word == null || word.isEmpty()) return "";
        if ("int".equals(word)) word = "Integer";
        return "java.lang." + word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private void fieldXMLReader(Node node, Object javaBean) throws IllegalAccessException, CyclicReferenceExeption, NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        classesFields.add(javaBean.getClass().getTypeName());
        Node element1;
        Node element2;
        Node element3;
        String firstClassName;
        String methodName;
        String objectName;
        String wrapperClassName;
        Class classInReader;
        Class primitive;
        Object objectInReader;
        NamedNodeMap attributes;
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            attributes = n.getAttributes();
            element1 = attributes.getNamedItem("class");
            element2 = attributes.getNamedItem("type");
            element3 = attributes.getNamedItem("value");
            methodName = "set" + firstUpperCase(n.getNodeName());
            if ("primitive".equals(element1.getNodeValue())) {
                primitive = Class.forName(getWrapperClassName(element2.getNodeValue()));
                javaBean.getClass().
                        getDeclaredMethod(methodName, (Class) primitive.getField("TYPE").get(null)).invoke(javaBean, Integer.valueOf(element3.getNodeValue()));
                firstClassName = classesFields.iterator().next();
                this.classesFields.clear();
                this.classesFields.add(firstClassName);
            } else {
                objectName = element2.getNodeValue().split(" ")[1];
                if (!classesFields.contains(objectName)) {
                    classesFields.add(objectName);
                    classInReader = Class.forName(objectName);
                    objectInReader = classInReader.getConstructor().newInstance();
                    fieldXMLReader(n, objectInReader);
                } else {
                    throw new CyclicReferenceExeption();
                }

            }
        }
    }
}
