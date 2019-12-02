// This function returns all the files in a directory as an array of Strings  
String[] listFileNames(String dir) {
  File file = new File(dir);
  if (file.isDirectory()) {
    String names[] = file.list();
    return names;
  } else {
    // If it's not a directory
    return null;
  }
}

String[] fileNames;
String path;

// let's set a filter (which returns true if file's extension is .jpg)
java.io.FilenameFilter mp4Filter = new java.io.FilenameFilter() {
  boolean accept(File dir, String name) {
    return name.toLowerCase().endsWith(".mp4");
  }
};

// This function returns all the files in a directory as an array of Strings  
String[] listFileNames(String dir,java.io.FilenameFilter extension) {
  
  File file = new File(dir);
  if (file.isDirectory()) {
    String names[] = file.list(extension);
    return names;
  } else {
    // If it's not a directory
    return null;
  }
}
