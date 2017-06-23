import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import java.nio.file.*; 
import java.nio.file.attribute.*; 
import java.util.EnumSet; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class p5generator extends PApplet {

// p5.js project generator by Tim Southorn 2017
// www.timsouthorn.com








PFont pf, cf;

ControlP5 cp5;
String notifications = "";
float satName = 0.0f;  //Red values to warn user of missing entries
float satLoc = 0.0f;

Path p5;

public void setup() {
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
            .setColor(color(0, 200))
              .setColorLabel(color(0))
                .setAutoClear(false)
                  .setCaptionLabel("Project Name")
                    .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER)
                      .setPadding(35, 20)

                        ;

  cp5.addTextfield("loc")
    .setPosition(20, 170)
      .setSize(400, 40)
        .setFont(cf)
          .setFocus(false)
            .setColor(color(0, 200))
              .setAutoClear(false)
                .getCaptionLabel().setVisible(false)
                  ;


  cp5.addTextfield("p5")
    .setPosition(20, 240)
      .setSize(400, 40)
        .setFont(cf)
          .setFocus(false)
            .setColor(color(0, 200))
              .setColorBackground(color(255))
                .setAutoClear(false)
                  .getCaptionLabel().setVisible(false)
                    ;

  cp5.addBang("locButton")
    .setPosition(440, 170)
      .setSize(150, 40)
        .setCaptionLabel("Location")
          .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
            ;  

  cp5.addBang("p5Button")
    .setPosition(440, 240)
      .setSize(150, 40)
        .setCaptionLabel("P5.js Libraries")
          .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
            ;

  cp5.addBang("makeButton")
    .setPosition(20, 310)
      .setSize(120, 40)
        .setCaptionLabel("Make")
          .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
            ;  



  p5 = Paths.get(dataPath("") + "/empty-example/libraries/");
  if (Files.exists(p5) && Files.isDirectory(p5)) {
    cp5.get(Textfield.class, "p5").setText(dataPath("") + "/empty-example/libraries/");
  }
}

public void draw() {
  background(255);
  drawRainbow();

  textFont(pf);
  fill(0, 150);
  text("p5.js PROJECT GENERATOR", 25, 60);
  textFont(cf);

  //Flash empty fields red to warn user
  if (satName > 0) {
    satName -= 2.5f;
  }
  if (satLoc > 0) {
    satLoc -= 2.5f;
  }

  colorMode(HSB);
  cp5.get(Textfield.class, "name").setColorBackground(color(1, satName, 255));
  cp5.get(Textfield.class, "loc").setColorBackground(color(1, satLoc, 255));

  fill(0, 220);
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
public void folderSelected(File selection) {
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
public void p5Selected(File selection) {
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


public void makeButton() throws IOException {
  //Check both fields have been filled
  String name = cp5.get(Textfield.class, "name").getText();
  String loc = cp5.get(Textfield.class, "loc").getText();

  //Check if a name has been entered
  if (name.trim().length() <= 0) {
    satName = 64;
    notifications = "Please specify a project name";
    println("Needs a name!");
  }

  //Check the folder is valid
  if (Files.exists(Paths.get(loc))) {
    satLoc = 64;
    notifications = "Please select a project folder";
    println("Needs a Location!");
  }

  //Copy directory tree of empty P5.js example to specified location
  else {
    final Path source = Paths.get(dataPath("") + "/empty-example");
    final Path target = Paths.get(loc + "/" + name);

    Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
      @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
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
      @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
      {
        Files.copy(file, target.resolve(source.relativize(file)));
        notifications = "New project created!";
        return FileVisitResult.CONTINUE;
      }
    }
    );
  }
}


int r = 550;
int rOff = 60;
float angle = -173;
float a = angle;
float v, b, g, y, o;

public void drawRainbow() {
  PVector mouse = new PVector(mouseX, mouseY);
  PVector offset = new PVector(width + 40, height - 30);
  float dist = offset.dist(mouse);


  pushMatrix();
  pushStyle();
  translate(offset.x, offset.y);
  colorMode(RGB);
  noFill();

  strokeWeight(rOff/1.2f);
  stroke(214);
  if (dist > 260 && dist < 290)
    v = lerp(v, angle + 1, 0.3f);
  else
    v = lerp(v, angle, 0.1f);
  arc(0, 0, r, r, radians(v-3), radians(-90));
  if (dist > 230 && dist < 260)
    b = lerp(b, angle + 1, 0.3f);
  else
    b = lerp(b, angle, 0.1f);
  arc(0, 0, r - rOff, r - rOff, radians(b-7), radians(-90));
  if (dist > 200 && dist < 230)
    g = lerp(g, angle + 1, 0.3f);
  else
    g = lerp(g, angle, 0.1f);
  arc(0, 0, r - rOff * 2, r - rOff * 2, radians(g-3), radians(-90));
  if (dist > 170 && dist < 200)
    y = lerp(y, angle + 1, 0.3f);
  else
    y = lerp(y, angle, 0.1f);
  arc(0, 0, r - rOff * 3, r - rOff * 3, radians(y-5), radians(-90));
  if (dist > 140 && dist < 170)
    o = lerp(o, angle + 1, 0.3f);
  else
    o = lerp(o, angle, 0.1f);
  arc(0, 0, r - rOff * 4, r - rOff * 4, radians(o-1), radians(-90));


  strokeWeight(rOff/1.2f);
  stroke(255, 255, 255);
  if (dist > 260 && dist < 290)
    v = lerp(v, angle + 1, 0.3f);
  else
    v = lerp(v, angle, 0.1f);
  arc(0, 0, r, r, radians(v-1), radians(-90));
  if (dist > 230 && dist < 260)
    b = lerp(b, angle + 1, 0.3f);
  else
    b = lerp(b, angle, 0.1f);
  arc(0, 0, r - rOff, r - rOff, radians(b-5), radians(-90));
  if (dist > 200 && dist < 230)
    g = lerp(g, angle + 1, 0.3f);
  else
    g = lerp(g, angle, 0.1f);
  arc(0, 0, r - rOff * 2, r - rOff * 2, radians(g-1), radians(-90));
  if (dist > 170 && dist < 200)
    y = lerp(y, angle + 1, 0.3f);
  else
    y = lerp(y, angle, 0.1f);
  arc(0, 0, r - rOff * 3, r - rOff * 3, radians(y-2), radians(-90));
  if (dist > 140 && dist < 170)
    o = lerp(o, angle + 1, 0.3f);
  else
    o = lerp(o, angle, 0.1f);
  arc(0, 0, r - rOff * 4, r - rOff * 4, radians(o+1), radians(-90));

  if (dist > 260 && dist < 290)
    v = lerp(v, angle + 1, 0.3f);
  else
    v = lerp(v, angle, 0.1f);
  stroke(150, 89, 167);  
  strokeWeight(rOff/2);
  arc(0, 0, r, r, radians(v-1), radians(-90));

  if (dist > 230 && dist < 260)
    b = lerp(b, angle + 1, 0.3f);
  else
    b = lerp(b, angle, 0.1f);
  stroke(36, 148, 193);
  arc(0, 0, r - rOff, r - rOff, radians(b-3), radians(-90));

  if (dist > 200 && dist < 230)
    g = lerp(g, angle + 1, 0.3f);
  else
    g = lerp(g, angle, 0.1f);
  stroke(73, 187, 108);
  arc(0, 0, r - rOff * 2, r - rOff * 2, radians(g+1), radians(-90));

  if (dist > 170 && dist < 200)
    y = lerp(y, angle + 1, 0.3f);
  else
    y = lerp(y, angle, 0.1f);
  stroke(241, 197, 0);
  arc(0, 0, r - rOff * 3, r - rOff * 3, radians(y-1), radians(-90));

  if (dist > 140 && dist < 170)
    o = lerp(o, angle + 1, 0.3f);
  else
    o = lerp(o, angle, 0.1f);
  stroke(243, 89, 86);
  arc(0, 0, r - rOff * 4, r - rOff * 4, radians(o+2), radians(-90));

  popStyle();
  popMatrix();
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "p5generator" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
