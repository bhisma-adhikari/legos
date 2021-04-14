import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;
import java.lang.ArrayIndexOutOfBoundsException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class Main {
    public static void main(String[] args) {
        // make sure args are as expected.
        String inputStatechartFilepath = null;
        String outputJavaFilepath = "CSE621.java";

        try {
            inputStatechartFilepath = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(
                    "*** ERROR: Missing 'statechart filepath'!\nPlease provide statechart path as the first argument.\nExiting...");
            System.exit(1);
        }

        new CodeGenerator(inputStatechartFilepath, outputJavaFilepath).generateCode();
    }
}

class CodeGenerator {
    private String inputStatechartFilepath;
    private String outputJavaFilepath;
    private List<Vertex> vertices; // all vertices
    private List<Transition> transitions; // all transitions

    public CodeGenerator(String statechartFilepath, String outputJavaFilePath) {
        this.inputStatechartFilepath = statechartFilepath;
        this.outputJavaFilepath = outputJavaFilePath;
        this.setVertices();
        this.setTransitions();
    }

    public void generateCode() {
        String code = this.generateImports();
        code += "\n\n" + this.generateClassCSE621();
        code += "\n\n" + this.generateOtherClasses();
        code += "\n\n" + this.generateEnumState();
        
        this.writeGeneratedCodeToFile(code);
        System.out.println("\nOutput file '" + this.outputJavaFilepath + "' generated successfully in current directory.");
    }

    private String generateImports() {
        String code = null;
        // read entire file as a single string
        try {
            code = new Scanner(new File("code-imports.txt")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            System.out.println("*** ERROR: Cannot find the file 'code-imports.txt' in current directory.\nExiting..."); 
            System.exit(1);
        }
        return code;

    }

    private String generateClassCSE621() {
        String code = "";

        Vertex initialVertex = this.getInitialVertex();
        if (initialVertex != null) {
            code += "\npublic class CSE621 {";
            code += "\n\tpublic static void main(String[] args) {";
            code += "\n\t\tSystem.out.println(\"Starting...\");";
            code += "\n\t\tRobot robot = Robot.getInstance(State." + initialVertex.getNameAndIdForEnum() + ");";
            code += "\n\t\twhile (true) {";
            code += "\n\t\t\tswitch (robot.getState()) {";
            for (Vertex v : this.vertices) {
                if (v.getType().equals("UMLState")) {
                    code += "\n\n\t\t\t\tcase " + v.getNameAndIdForEnum() + ":";
                    for (Transition tran : this.getTransitionsBySourceVertex(v)) {
                        code += this.transitionToStmt(tran);
                    }
                    code += "\n\t\t\t\t\tbreak;";
                }
            }
            code += "\n\t\t\t}\n\t\t}\n\t}\n}";

        } else {
            System.err.println("*** ERROR: Initial State is missing in the statechart. \nExiting...");
            System.exit(1);
        }
        return code;

    }

    private String generateOtherClasses() {
        String code = null;
        // read entire file as a single string
        try {
            code = new Scanner(new File("code-other-classes.txt")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            System.out.println("*** ERROR: Cannot find the file 'code-other-classes.txt' in current directory.\nExiting..."); 
            System.exit(1);
        }
        return code;
    }

    private String generateEnumState() {
        String code = "enum State {\n";
        
        int count = 0;
        for (Vertex v:this.vertices){
            count ++; 
            if (v.getName() != null) {

                String elt = Vertex.createNameAndIDForEnum(v.getName(), v.getId()); 

                if (count == 1){
                    code +=  "\t" + elt + "(\"" + elt + "\")"; 
                } else {
                    code +=  ",\n\t" + elt + "(\"" + elt + "\")"; 
                } 
            }
        }

        code += ";\n";

        code += "\n\tprivate String str;";
        code += "\n\tprivate State(String str) {"; 
        code += "\n\t\tthis.str = str;"; 
        code += "\n\t}"; 
        code += "\n\t@Override"; 
        code += "\n\tpublic String toString() {"; 
        code += "\n\t\treturn str;"; 
        code += "\n\t}"; 
        code += "\n}"; 
        
        return code;
    }

    

    private void writeGeneratedCodeToFile(String code) {

        File file = new File(this.outputJavaFilepath);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(code);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String transitionToStmt(Transition tran) {
        String stmt = "";
        Vertex nextVertex = this.getVertexById(tran.getTargetVertexId());

        if (tran.getTriggers().size() > 0) {
            Trigger trig = tran.getTriggers().get(0);
            String guard = tran.getGuard();

            switch (trig.getName()) {
                case "buttonPress":
                    if (guard != null) {
                        String button = guard.split(" ")[2]; // ENTER / ESCAPE / UP / DOWN / LEFT / RIGHT
                        stmt += "\n\t\t\t\t\tif (Button.readButtons() == Button.ID_" + button + ") {";

                    } else { // buttonPress trigger with no guard
                        stmt += "\n\t\t\t\t\tif (Button.readButtons() != 0) {"; // 0 corresponds to no-button-pressed
                    }
                    break;

                case "distance":
                    if (guard != null) {
                        String[] tokens = guard.split(" ");
                        String sign = tokens[1];

                        // this change is made for the sake of robustness
                        if (sign.equals("==")) {
                            String sourceVertexName = this.getSourceVertexByTransition(tran).getName(); 
                            if (sourceVertexName.equals("FORWARD")){
                                sign = "<";
                            } else if (sourceVertexName.equals("BACKWARD")){
                                sign = ">";
                            }
                        }
                        
                        String distanceValue = tokens[2];
                        stmt += "\n\t\t\t\t\tif (robot.getDistanceMetersFromObstacle() " + sign + " " + distanceValue
                                + ") {";
                    } else { // distance trigger with no guard always fires
                        stmt += "\n\t\t\t\t\tif (true) {";
                    }
                    break;

                case "timer":
                    if (guard != null) {
                        String timeLapse = guard.split(" ")[2];
                        stmt += "\n\t\t\t\t\tif (System.currentTimeMillis() - robot.getStartTimeOfCurrentStateMillis() > "
                                + timeLapse + ") {";
                    } else { // timer trigger with no guard always fires
                        stmt += "\n\t\t\t\t\tif (true) {";
                    }
                    break;

                case "color":
                    if (guard != null) {
                        String color = guard.split(" ")[2];
                        stmt += "\n\t\t\t\t\tif (robot.getColor() == " + color + ") {";
                    } else { // color trigger with no guard always fires
                        stmt += "\n\t\t\t\t\tif (true) {";
                    }
                    break;
            }
            stmt += "\n\t\t\t\t\t\trobot.setState(State." + nextVertex.getNameAndIdForEnum() + ");";
            stmt += "\n\t\t\t\t\t}";

        } else { // transition with no trigger            
            stmt += "\n\t\t\t\t\trobot.setState(State." + nextVertex.getNameAndIdForEnum() + ");";
        }
        return stmt;
    }

    /**
     * Initial vertex is not the one with kind="initial", but rather the one which
     * is pointed by the transition from the node with kind="initial"
     * 
     * @return
     */
    private Vertex getInitialVertex() {
        for (Vertex v : this.vertices) {
            if (v.getKind() != null && v.getKind().equals("initial")) {
                Transition transitionFromStartNodeToInitialVertex = this.getTransitionsBySourceVertex(v).get(0);
                return this.getVertexById(transitionFromStartNodeToInitialVertex.getTargetVertexId());
            }
        }
        return null;
    }

    private void setVertices() {
        if (this.vertices == null) {
            this.vertices = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<Object, Object> jsonMap = mapper.readValue(new File(this.inputStatechartFilepath),
                        new TypeReference<Map<Object, Object>>() {
                        });
                ArrayList<HashMap> verticesMap = (ArrayList) Util.getValueByKey(jsonMap, "vertices");
                for (HashMap vm : verticesMap) {
                    Vertex v = new Vertex((String) vm.get("_type"), (String) vm.get("_id"),
                            (String) ((HashMap) vm.get("_parent")).get("$ref"), (String) vm.get("name"),
                            (String) vm.get("kind"));
                    this.vertices.add(v);
                }
            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }
        }
    }

    private Vertex getVertexById(String id) {
        for (Vertex v : this.vertices) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    private void setTransitions() {
        if (this.transitions == null) {
            this.transitions = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<Object, Object> jsonMap = mapper.readValue(new File(this.inputStatechartFilepath),
                        new TypeReference<Map<Object, Object>>() {
                        });
                ArrayList<HashMap> transitionsMap = (ArrayList) Util.getValueByKey(jsonMap, "transitions");
                for (HashMap<Object, Object> transitionMap : transitionsMap) {
                    List<Trigger> triggers = new ArrayList<>();
                    ArrayList<HashMap> triggersMap = (ArrayList) transitionMap.get("triggers");
                    if (triggersMap != null) {
                        for (HashMap<Object, Object> triggerMap : triggersMap) {
                            Trigger trigger = new Trigger((String) triggerMap.get("_type"),
                                    (String) triggerMap.get("_id"),
                                    (String) ((HashMap) triggerMap.get("_parent")).get("$ref"),
                                    (String) triggerMap.get("name"));
                            triggers.add(trigger);
                        }
                    }
                    Transition transition = new Transition((String) transitionMap.get("_type"),
                            (String) transitionMap.get("_id"),
                            (String) ((HashMap) transitionMap.get("_parent")).get("$ref"),
                            (String) ((HashMap) transitionMap.get("source")).get("$ref"),
                            (String) ((HashMap) transitionMap.get("target")).get("$ref"), triggers,
                            (String) transitionMap.get("guard"));
                    this.transitions.add(transition);
                }
            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }
        }
    }

    private List<Transition> getTransitionsBySourceVertex(Vertex vertex) {
        List<Transition> transitions = new ArrayList();
        for (Transition t : this.transitions) {
            if (t.getSourceVertexId().equals(vertex.getId())) {
                transitions.add(t);
            }
        }
        return transitions;
    }

    private Vertex getSourceVertexByTransition(Transition transition){
        return this.getVertexById(transition.getSourceVertexId()); 
    }

}

class Util {
    /**
     * Extract the value of given key, if exists. If the key does not exist, null is
     * returned. If the key occurs multiple times, the first occurence is returned.
     * 
     * @param jsonMap
     * @param key
     * @return
     */
    public static Object getValueByKey(Map<Object, Object> jsonMap, String key) {
        Set<Object> keys = jsonMap.keySet();
        if (keys.contains(key)) {
            return jsonMap.get(key);
        }

        for (Object k : keys) {
            Object v = jsonMap.get(k);
            Object ans = null;

            if (v instanceof HashMap) {
                ans = getValueByKey((HashMap) v, key);
                if (ans != null) {
                    return ans;
                }
            }
            if (v instanceof ArrayList) {
                for (Object o : (ArrayList) v) {
                    HashMap<Object, Object> hm = (HashMap) o;
                    ans = getValueByKey(hm, key);
                    if (ans != null) {
                        return ans;
                    }
                }
            }
        }
        return null;
    }
}

class Vertex {
    private String type;
    private String id;
    private String parentId;
    private String name;
    private String kind;
    private String nameAndIdForEnum; 

    public Vertex(String type, String id, String parentId, String name, String kind) {

        this.type = type;
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.kind = kind;
        this.nameAndIdForEnum = Vertex.createNameAndIDForEnum(name, id); 

    }

    public String toString() {
        return "\nVertex: type:" + this.type + ", id:" + this.id + ", parentId:" + this.parentId + ", name:" + this.name
                + ", kind:" + this.kind;
    }


    public static String createNameAndIDForEnum(String name, String id){
        return name + "___" + id.replace("+", "")
                                .replace("-", "")
                                .replace("*", "")
                                .replace("/", "")
                                .replace("=", "")
                                .replace("_", ""); 
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getNameAndIdForEnum(){
        return nameAndIdForEnum; 
    }
}

class Trigger {
    private String type;
    private String id;
    private String transitionId;
    private String name;

    public Trigger(String type, String id, String transitionId, String name) {
        this.type = type;
        this.id = id;
        this.transitionId = transitionId;
        this.name = name;
    }

    public String toString() {
        return "Trigger: type:" + this.type + ", id:" + this.id + ", transitionId:" + this.transitionId + ", name:"
                + this.name;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public String getName() {
        return name;
    }

}

class Transition {
    private String type;
    private String id;
    private String parentId;
    private String sourceVertexId;
    private String targetVertexId;
    private List<Trigger> triggers;
    private String guard;

    public Transition(String type, String id, String parentId, String sourceVertexId, String targetVertexId,
            List<Trigger> triggers, String guard) {
        this.type = type;
        this.id = id;
        this.parentId = parentId;
        this.sourceVertexId = sourceVertexId;
        this.targetVertexId = targetVertexId;
        this.triggers = triggers;
        this.guard = guard;
    }

    public String toString() {
        return "\nTransition: type:" + this.type + ", id:" + this.id + ", parentId:" + this.parentId
                + ", sourceVertexId:" + this.sourceVertexId + ", targetVertexId:" + this.targetVertexId + ", triggers: "
                + this.triggers + ", guard:" + this.guard;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getSourceVertexId() {
        return sourceVertexId;
    }

    public String getTargetVertexId() {
        return targetVertexId;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public String getGuard() {
        return this.guard;
    }
}