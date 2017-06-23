import controlP5.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.EnumSet;

PFont pf, cf;

ControlP5 cp5;
String notifications = "";
float rName = 0.0;  //Red values to warn user of missing entries
float rLoc = 0.0;

Path p5;

void setup() {
  size(700, 400);

  cp5 = new ControlP5(this);

  pf = createFont("Verdana Bold", 30);
  cf = createFont("Verdana", 15); 
  cp5.setControlFont(cf);

  cp5.addTextfield("name")
    .setPosition(20, 100)
      .setSize(400, 40)
        .setFont(cf)
          .setFocus(true)
            .setColor(color(255))
              .setAutoClear(false)
                .setCaptionLabel("Project Name")
                  .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPadding(20, 20)
                    ;

  cp5.addTextfield("loc")
    .setPosition(20, 170)
      .setSize(400, 40)
        .setFont(cf)
          .setFocus(false)
            .setColor(color(255))
              .setAutoClear(false)
                .getCaptionLabel().setVisible(false)
                  ;

  cp5.addBang("locButton")
    .setPosition(440, 170)
      .setSize(150, 40)
        .setCaptionLabel("Location")
          .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
            ;  

  cp5.addTextfield("p5")
    .setPosition(20, 240)
      .setSize(400, 40)
        .setFont(cf)
          .setFocus(false)
            .setColor(color(255))
              .setAutoClear(false)
                .getCaptionLabel().setVisible(false)
                  ;

  cp5.addBang("p5Button")
    .setPosition(440, 240)
      .setSize(150, 40)
        .setCaptionLabel("P5.js Libraries")
          .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
            ;

  cp5.addBang("makeButton")
    .setPosition(20, 310)
      .setSize(80, 40)
        .setCaptionLabel("Make")
          .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
            ;  

  textFont(cf);


  p5 = Paths.get(dataPath("") + "/empty-example/libraries/");
  if (Files.exists(p5) && Files.isDirectory(p5)) {
    cp5.get(Textfield.class, "p5").setText(dataPath("") + "/empty-example/libraries/");
  }
}

void draw() {
  background(0);
  drawRainbow();

  textFont(pf);
  text("p5.js PROJECT GENERATOR", 25, 60);

  textFont(cf);

  //Flash empty fields red to warn user
  if (rName > 0) {
    rName -= 5;
  }
  if (rLoc > 0) {
    rLoc -= 5;
  }
  cp5.get(Textfield.class, "name").setColorBackground(color(rName, 0, 0));
  cp5.get(Textfield.class, "loc").setColorBackground(color(rLoc, 0, 0));

  text(notifications, 20, 380);
}


//Open a file dialogue
public void locButton() {
  selectFolder("Select a folder for new project:", "folderSelected");
}

public void p5Button() {
  selectFolder("Select your p5.js libraries folder:", "p5Selected");
}

//Handle folder selection
void folderSelected(File selection) {
  if (selection != null && selection.isDirectory() == true) {
    //The user has selected a folder
    cp5.get(Textfield.class, "loc").setText(selection.getAbsolutePath());
  } else {
    //The user has selected a file, use parent directory
    cp5.get(Textfield.class, "loc").setText(selection.getParent());
    println("Needs to be a folder");
  }
}

//Handle P5.js libraries selection
void p5Selected(File selection) {
  Path p = Paths.get("");
  if (selection != null && selection.isDirectory() == false) {
    //The user has selected a file, select parent directory
    p = Paths.get(selection.getParent());
  } else if (selection != null && selection.isDirectory() == true) {
    //The user has selected a folder 
    p = Paths.get(selection.getAbsolutePath());
  }

  //Check if p5.js is there
  if (Files.exists(Paths.get(selection.getAbsolutePath() + "/p5.js"))) {
    notifications = "";
    cp5.get(Textfield.class, "p5").setText(p.toString());
  } else {
    notifications = "p5.js file not found, using default";
    cp5.get(Textfield.class, "p5").setText(dataPath("") + "/empty-example/libraries/");
  }
}


void makeButton() throws IOException {
  //Check both fields have been filled
  String name = cp5.get(Textfield.class, "name").getText();
  String loc = cp5.get(Textfield.class, "loc").getText();
  if (name.trim().length() <= 0 || loc.trim().length() <= 0) {
    if (name.trim().length() <= 0) {
      rName = 255;
      notifications = "Please specify a project name";
      println("Needs a name!");
    }
    if (loc.trim().length() <= 0) {
      rLoc = 255;
      notifications = "Please select a project folder";
      println("Needs a Location!");
    }
  }

  //Copy directory tree of empty P5.js example to specified location
  else {
    final Path source = Paths.get(dataPath("") + "/empty-example");
    final Path target = Paths.get(loc + "/" + name);

    Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
      @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path targetdir = target.resolve(source.relativize(dir));
        try {
          Files.copy(dir, targetdir);
        } 
        catch (FileAlreadyExistsException e) {
          if (!Files.isDirectory(targetdir))
            throw e;
        }
        return FileVisitResult.CONTINUE;
      }
      @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
      {
        Files.copy(file, target.resolve(source.relativize(file)));
        return FileVisitResult.CONTINUE;
      }
    }
    );
    notifications = "New project created!";
  }
}

void drawRainbow() {
  int r = 550;
  int rOff = 60;
  pushMatrix();
  pushStyle();
  translate(width, height);
  strokeWeight(rOff/2);
  noFill();
  stroke(150, 89, 167);
  arc(0, 0, r, r, radians(-171), radians(-90));
  stroke(36, 148, 193);
  arc(0, 0, r - rOff, r - rOff, radians(-173), radians(-90));
  stroke(73, 187, 108);
  arc(0, 0, r - rOff * 2, r - rOff * 2, radians(-169), radians(-90));
  stroke(241, 197, 0);
  arc(0, 0, r - rOff * 3, r - rOff * 3, radians(-171), radians(-90));
  stroke(243, 89, 86);
  arc(0, 0, r - rOff * 4, r - rOff * 4, radians(-168), radians(-90));

  popStyle();
  popMatrix();
}

