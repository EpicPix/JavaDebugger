package ga.epicpix.javadebugger;

public class DescriptorType {

    public static String typeToDescriptor(String type) {
        StringBuilder output = new StringBuilder();
        while(type.endsWith("[]")) {
            output.append("[");
            type = type.substring(0, type.length() - 2);
        }
        if(type.equals("int")) output.append("I");
        else if(type.equals("long")) output.append("J");
        else if(type.equals("boolean")) output.append("Z");
        else if(type.equals("double")) output.append("D");
        else if(type.equals("float")) output.append("F");
        else if(type.equals("short")) output.append("S");
        else if(type.equals("char")) output.append("C");
        else output.append(type.replace('/', '.'));
        return output.toString();
    }

}
