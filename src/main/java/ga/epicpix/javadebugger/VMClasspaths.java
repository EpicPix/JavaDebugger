package ga.epicpix.javadebugger;

import java.util.ArrayList;

public record VMClasspaths(String baseDir, ArrayList<String> classpaths, ArrayList<String> bootClasspaths) {}
