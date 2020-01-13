import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import controlP5.*; 
import org.openkinect.freenect.*; 
import org.openkinect.processing.*; 
import deadpixel.keystone.*; 
import oscP5.*; 
import netP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class FlockingMaskOSC extends PApplet {



/**
 * Flocking 
 * by Daniel Shiffman.  
 * 
 * An implementation of Craig Reynold's Boids program to simulate
 * the flocking behavior of birds. Each boid steers itself based on 
 * rules of avoidance, alignment, and coherence.
 */

Flock flock;
PImage img;
Movie movie;

Movie topLayerVideo;

PImage boidImage;
PGraphics pg;


ControlP5 cp5;

boolean toggleFullscreen = false;
boolean toggleGui = false;

float radiusBoid;
float maxforce;    // Maximum steering force
float maxspeed, separationForce, alignmentForce, cohesionForce;    // Maximum speed
float attractionForce, range, opacity;

PVector center;

boolean debug = false;
int XY_XZ_YZ = 1;
RadioButton trackingPlane;

int topLayerVideoOpacity;

public void setup() {
  //fullScreen(P3D);
  

  setupKeystone();
  kinect = new Kinect(this);
  tracker = new KinectTracker();

  pg = createGraphics(width, height);
  flock = new Flock();

  topLayerVideo = new Movie(this, "topLayer.mp4");
  topLayerVideo.loop();
  // read all the video filenames in the videos directory
  path = dataPath("") + "/videos/";

  println("Listing all filenames in a directory: " + path);
  fileNames = listFileNames(path, mp4Filter);
  printArray(fileNames);
  // Load and play the video in a loop
  movie = new Movie(this, path + fileNames[0]);
  movie.loop();

  boidImage = loadImage("texture.png");
  // Add an initial set of boids into the system
  for (int i = 0; i < 250; i++) {
    flock.addBoid(new Boid(width/2, height/2));
  }

  cp5 = new ControlP5(this);
  setupControlsUI();

  maxspeed = 3;
  maxforce = 0.3f;
  center = new PVector(0, 0);
  com = new PVector(0, 0);
  ks.load(dataPath("") + "/keystone.xml");
  cp5.loadProperties(dataPath("") + "/default.ser");

  setupOSC();
}

public void movieEvent(Movie m) {
  m.read();
}

public void draw() {

  tint(255, 255);
  center.set(com);
  // Convert the mouse coordinate into surface coordinates
  // this will allow you to use mouse events inside the 
  // surface from your screen. 

  // Draw the scene, offscreen
  offscreen.beginDraw();
  offscreen.background(0);
  offscreen.blendMode(NORMAL);
  offscreen.image(movie, 0, 0, width, height);
  offscreen.blendMode(MULTIPLY); 

  // prepare layer with flock
  pg.beginDraw(); 
  pg.fill(0, 40);
  pg.rect(0, 0, width, height);
  flock.run(pg);  
  pg.endDraw();

  offscreen.image(pg, 0, 0);
  offscreen.blendMode(NORMAL); 
  offscreen.endDraw();

  background(0);
  // render the scene, transformed using the corner pin surface
  surface.render(offscreen);

  drawKinect();

  tint(255, topLayerVideoOpacity);
  image(topLayerVideo, 0, 0, width, height);

  if (toggleGui) {
    cp5.draw();
  }
}

// Adjust the maxThreshold with key presses
public void keyPressed() {
  int t = tracker.getmaxThreshold();
  if (key == CODED) {
    if (keyCode == UP) {
      t+=5;
      tracker.setmaxThreshold(t);
    } else if (keyCode == DOWN) {
      t-=5;
      tracker.setmaxThreshold(t);
    }
  }
  switch(key) {
  case 'g':
    toggleGui=!toggleGui;
    break;
  case 'd':

    debug=!debug;
    break;
  case 'c':
    // enter/leave calibration mode, where surfaces can be warped 
    // and moved
    ks.toggleCalibration();
    break;

  case 'l':
    // loads the saved layout
    ks.load(dataPath("") + "/keystone.xml");
    cp5.loadProperties(dataPath("") + "/default.ser");
    println("loaded preset");
    break;

  case 's':
    // saves the layout
    ks.save(dataPath("") + "/keystone.xml");
    cp5.saveProperties(dataPath("") + "/default", "default");
    println("saved preset");
    break;
  }

  int number = key - 49;

  if (number>=0 && number < fileNames.length) {
    println("number " + number);
    movie.pause();
    movie = new Movie(this, path + fileNames[number]);
    movie.loop();
  } else if (key == 'f') {  
    toggleFullscreen=!toggleFullscreen;
    //if (toggleFullscreen) {
    //  surface.setSize(displayWidth, displayHeight);
    //  surface.setAlwaysOnTop(true);
    //} else {
    //  surface.setSize(1280, 720);
    //  surface.setAlwaysOnTop(false);
    //}
  }
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isFrom(trackingPlane)) {
    print("got an event from "+theEvent.getName()+"\t");
    for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
      print(PApplet.parseInt(theEvent.getGroup().getArrayValue()[i]));
    }
    println("\t "+theEvent.getValue());
    XY_XZ_YZ = PApplet.parseInt(theEvent.getGroup().getValue());
  }
}
// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan

// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/




// The kinect stuff is happening in another class
KinectTracker tracker;
Kinect kinect;

PVector v2;
PVector com;

public void drawKinect() {

  // Run the tracking analysis
  tracker.track();
  // Show the image
  if (toggleGui) {
    tracker.display();
  }

  // Let's draw the raw location
  PVector v1 = tracker.getPos();
  fill(50, 100, 250, 200);
  noStroke();
  //ellipse(v1.x, v1.y, 20, 20);

  // Let's draw the "lerped" location
  v2 = tracker.getLerpedPos();
  fill(255, 0, 50, 200);
  noStroke();
  if (toggleGui) {
    ellipse(v2.x, v2.y, 10, 10);
  }
  if (debug) {
    println("v2 " + v2);
  }

  if (XY_XZ_YZ == 1) { 
    com.set(map(v2.x, 0, 640, 0, width), map(v2.y, 0, 480, 0, height), 0);
    if (debug) {
      println("tracking: XY");
    }
  } else if (XY_XZ_YZ == 2) {
    com.set(map(v2.x, 0, 640, 0, width), map(v2.z, minThreshold, maxThreshold, 0, height), 0);
    if (debug) {
      println("tracking: XZ");
    }
  } else if (XY_XZ_YZ == 3) {
    com.set( map(v2.y, 0, 480, 0, height), map(v2.z, minThreshold, maxThreshold, 0, height), 0);
    if (debug) {
      println("tracking: YZ");
    }
  }

  fill(100, 250, 50, 200);
  noStroke();
  if (toggleGui) {
    ellipse(com.x, com.y, 20, 20);

    int t = tracker.getmaxThreshold();
    fill(255);
    text("framerate: " + PApplet.parseInt(frameRate), 10, 500);
  }
  if (debug) {
    println("com " + com);
  }
}
// The Boid class

class Boid {

  PVector position;
  PVector velocity;
  PVector acceleration;

  float sizePersonal;

  Boid(float x, float y) {
    acceleration = new PVector(0, 0);

    // This is a new PVector method not yet implemented in JS
    // velocity = PVector.random2D();

    // Leaving the code temporarily this way so that this example runs in JS
    float angle = random(TWO_PI);
    velocity = new PVector(cos(angle), sin(angle));

    position = new PVector(x, y);

    sizePersonal = random(1, 3);
  }

  public void run(ArrayList<Boid> boids, PGraphics p) {
    flock(boids);
    update();
    borders();
    render(p);
  }

  public void applyForce(PVector force) {
    // We could add mass here if we want A = F / M
    acceleration.add(force);
  }

  // We accumulate a new acceleration each time based on three rules
  public void flock(ArrayList<Boid> boids) {
    PVector sep = separate(boids);   // Separation
    PVector ali = align(boids);      // Alignment
    PVector coh = cohesion(boids);   // Cohesion
    PVector attractor = attraction(center);   // Cohesion
    // Arbitrarily weight these forces
    sep.mult(separationForce);
    ali.mult(alignmentForce);
    coh.mult(cohesionForce);
    attractor.mult(attractionForce);
    // Add the force vectors to acceleration
    applyForce(sep);
    applyForce(ali);
    applyForce(coh);
    applyForce(attractor);
  }

  // Method to update position
  public void update() {
    // Update velocity
    velocity.add(acceleration);
    // Limit speed
    velocity.limit(maxspeed);
    velocity.mult(1 + mid);
    position.add(velocity);
    // Reset accelertion to 0 each cycle
    acceleration.mult(0);
  }

  // A method that calculates and applies a steering force towards a target
  // STEER = DESIRED MINUS VELOCITY
  public PVector seek(PVector target) {
    PVector desired = PVector.sub(target, position);  // A vector pointing from the position to the target
    // Scale to maximum speed
    desired.normalize();
    desired.mult(maxspeed);

    // Above two lines of code below could be condensed with new PVector setMag() method
    // Not using this method until Processing.js catches up
    // desired.setMag(maxspeed);

    // Steering = Desired minus Velocity
    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce);  // Limit to maximum steering force
    return steer;
  }

  public void render(PGraphics pg) {
    // Draw a triangle rotated in the direction of velocity
    float theta = velocity.heading2D() + radians(90);
    // heading2D() above is now heading() but leaving old syntax until Processing.js catches up


    pg.imageMode(CENTER);
    // pg.fill(255);
    //pg.stroke(255);
    pg.pushMatrix();
    pg.translate(position.x, position.y);
    pg.rotate(theta);
    pg.tint(255, opacity);
    pg.image(boidImage, 0, 0, radiusBoid*sizePersonal*(1+high), radiusBoid*sizePersonal*(1+low));

    //pg.beginShape(TRIANGLES);
    //pg.vertex(0, -r*2);
    //pg.vertex(-r, r*2);
    //pg.vertex(r, r*2);
    //pg.endShape();
    pg.popMatrix();
  }

  // Wraparound
  public void borders() {
    if (position.x < -radiusBoid) position.x = width+radiusBoid;
    if (position.y < -radiusBoid) position.y = height+radiusBoid;
    if (position.x > width+radiusBoid) position.x = -radiusBoid;
    if (position.y > height+radiusBoid) position.y = -radiusBoid;
  }

  // Separation
  // Method checks for nearby boids and steers away
  public PVector separate (ArrayList<Boid> boids) {
    float desiredseparation = 25.0f;
    PVector steer = new PVector(0, 0, 0);
    int count = 0;
    // For every boid in the system, check if it's too close
    for (Boid other : boids) {
      float d = PVector.dist(position, other.position);
      // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
      if ((d > 0) && (d < desiredseparation)) {
        // Calculate vector pointing away from neighbor
        PVector diff = PVector.sub(position, other.position);
        diff.normalize();
        diff.div(d);        // Weight by distance
        steer.add(diff);
        count++;            // Keep track of how many
      }
    }
    // Average -- divide by how many
    if (count > 0) {
      steer.div((float)count);
    }

    // As long as the vector is greater than 0
    if (steer.mag() > 0) {
      // First two lines of code below could be condensed with new PVector setMag() method
      // Not using this method until Processing.js catches up
      // steer.setMag(maxspeed);

      // Implement Reynolds: Steering = Desired - Velocity
      steer.normalize();
      steer.mult(maxspeed);
      steer.sub(velocity);
      steer.limit(maxforce);
    }
    return steer;
  }

  // Alignment
  // For every nearby boid in the system, calculate the average velocity
  public PVector align (ArrayList<Boid> boids) {
    float neighbordist = 150;
    PVector sum = new PVector(0, 0);
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(position, other.position);
      if ((d > 0) && (d < neighbordist)) {
        sum.add(other.velocity);
        count++;
      }
    }
    if (count > 0) {
      sum.div((float)count);
      // First two lines of code below could be condensed with new PVector setMag() method
      // Not using this method until Processing.js catches up
      // sum.setMag(maxspeed);

      // Implement Reynolds: Steering = Desired - Velocity
      sum.normalize();
      sum.mult(maxspeed);
      PVector steer = PVector.sub(sum, velocity);
      steer.limit(maxforce);
      return steer;
    } else {
      return new PVector(0, 0);
    }
  }


  public PVector attraction(PVector _center) {

    PVector attractor = new PVector(0, 0);

    float d = PVector.dist(position, _center);
    if (d < range) {

      attractor = PVector.sub(position, _center);
      attractor.normalize();
      attractor.mult(attractionForce);
    }


    return attractor;
  }

  // Cohesion
  // For the average position (i.e. center) of all nearby boids, calculate steering vector towards that position
  public PVector cohesion (ArrayList<Boid> boids) {
    float neighbordist = 350;
    PVector sum = new PVector(0, 0);   // Start with empty vector to accumulate all positions
    int count = 0;
    for (Boid other : boids) {
      float d = PVector.dist(position, other.position);
      if ((d > 0) && (d < neighbordist)) {
        sum.add(other.position); // Add position
        count++;
      }
    }
    if (count > 0) {
      sum.div(count);
      return seek(sum);  // Steer towards the position
    } else {
      return new PVector(0, 0);
    }
  }
}
public void setupControlsUI(){
 trackingPlane = cp5.addRadioButton("radioButton")
    .setPosition(30, 10)
    .setSize(50, 30)
    .setColorForeground(color(120))
    .setColorActive(color(255))
    .setColorLabel(color(255))
    .setItemsPerRow(5)
    .setSpacingColumn(50)
    .addItem("XY", 1)
    .addItem("XZ", 2)
    .addItem("YZ", 3);
  // add a horizontal sliders, the value of this slider will be linked
  // to variable 'sliderValue' 
  int y = 50;
  cp5.addSlider("radiusBoid").setPosition(30, y+=20).setRange(0, 30).setValue(3).setSize(400, 15);
  cp5.addSlider("separationForce").setPosition(30, y+=20).setRange(-5, 5).setSize(400, 15);
  cp5.addSlider("alignmentForce").setPosition(30, y+=20).setRange(-5, 5).setSize(400, 15);
  cp5.addSlider("cohesionForce").setPosition(30, y+=20).setRange(-5, 5).setSize(400, 15);
  cp5.addSlider("attractionForce").setPosition(30, y+=20).setRange(-5, 5).setSize(400, 15);
  cp5.addSlider("range").setPosition(30, y+=20).setRange(10, 500).setValue(300).setSize(400, 15);

  cp5.addSlider("opacity").setPosition(30, y+=20).setRange(0, 255).setValue(200).setSize(400, 15);

  cp5.addSlider("trackingThreshold").setPosition(30, y+=20).setRange(1, 1000).setValue(30).setSize(400, 15);

  cp5.addSlider("minThreshold").setPosition(30, y+=20).setRange(10, 1000).setValue(930).setSize(400, 15);
  cp5.addSlider("maxThreshold").setPosition(30, y+=20).setRange(10, 2000).setValue(994).setSize(400, 15);

  cp5.addSlider("topCrop").setPosition(30, y+=20).setRange(0, 500).setValue(0).setSize(400, 15);
  cp5.addSlider("bottomCrop").setPosition(30, y+=20).setRange(0, 500).setValue(0).setSize(400, 15);
  
  
   cp5.addSlider("topLayerVideoOpacity").setPosition(30, y+=20).setRange(0, 255).setValue(200).setSize(400, 15);

  
cp5.getProperties().setFormat(ControlP5.SERIALIZED);
  cp5.setAutoDraw(false);
}
// This function returns all the files in a directory as an array of Strings  
public String[] listFileNames(String dir) {
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
  public boolean accept(File dir, String name) {
    return name.toLowerCase().endsWith(".mp4");
  }
};

// This function returns all the files in a directory as an array of Strings  
public String[] listFileNames(String dir,java.io.FilenameFilter extension) {
  
  File file = new File(dir);
  if (file.isDirectory()) {
    String names[] = file.list(extension);
    return names;
  } else {
    // If it's not a directory
    return null;
  }
}
// The Flock (a list of Boid objects)

class Flock {
  ArrayList<Boid> boids; // An ArrayList for all the boids

      

    
  Flock() {
    boids = new ArrayList<Boid>(); // Initialize the ArrayList

  }

  public void run(PGraphics pg) {
    for (Boid b : boids) {
      b.run(boids, pg);  // Passing the entire list of boids to each boid individually
    }
  }

  public void addBoid(Boid b) {
    boids.add(b);
  }

}
/**
 * This is a simple example of how to use the Keystone library.
 *
 * To use this example in the real world, you need a projector
 * and a surface you want to project your Processing sketch onto.
 *
 * Simply drag the corners of the CornerPinSurface so that they
 * match the physical surface's corners. The result will be an
 * undistorted projection, regardless of projector position or 
 * orientation.
 *
 * You can also create more than one Surface object, and project
 * onto multiple flat surfaces using a single projector.
 *
 * This extra flexbility can comes at the sacrifice of more or 
 * less pixel resolution, depending on your projector and how
 * many surfaces you want to map. 
 */



Keystone ks;
CornerPinSurface surface;

PGraphics offscreen;

public void setupKeystone() {
  // Keystone will only work with P3D or OPENGL renderers, 
  // since it relies on texture mapping to deform


  ks = new Keystone(this);
  surface = ks.createCornerPinSurface(width, height, 20);
  
  // We need an offscreen buffer to draw the surface we
  // want projected
  // note that we're matching the resolution of the
  // CornerPinSurface.
  // (The offscreen buffer can be P2D or P3D)
  offscreen = createGraphics(width, height, P2D);
}
// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan

// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/

// Depth threshold
int maxThreshold ;
int minThreshold ;
int topCrop ;
int bottomCrop ;
int trackingThreshold;

class KinectTracker {

  // Raw location
  PVector loc;

  // Interpolated location
  PVector lerpedLoc;

  // Depth data
  int[] depth;

  // What we'll show the user
  PImage display;

  KinectTracker() {
    // This is an awkard use of a global variable here
    // But doing it this way for simplicity
    kinect.initDepth();
    kinect.enableMirror(true);
    // Make a blank image
    display = createImage(kinect.width, kinect.height, RGB);
    // Set up the vectors
    loc = new PVector(0, 0);
    lerpedLoc = new PVector(0, 0);
  }

  public void track() {
    // Get the raw depth as array of integers
    depth = kinect.getRawDepth();

    // Being overly cautious here
    if (depth == null) return;

    float sumX = 0;
    float sumY = 0;
    float sumZ = 0;
    float count = 0;

    for (int x = 0; x < kinect.width; x++) {
      for (int y = 0 + topCrop; y < kinect.height - bottomCrop; y++) {

        int offset =  x + y*kinect.width;
        // Grabbing the raw depth
        int rawDepth = depth[offset];

        // Testing against threshold
        if (minThreshold < rawDepth && rawDepth < maxThreshold) {
          sumX += x;
          sumY += y;
          sumZ += rawDepth;
          count++;
        }
      }
    }
    // As long as we found something
    if (count >= trackingThreshold) {
      loc = new PVector(sumX/count, sumY/count, sumZ/count);
    } else {

      loc = new PVector(0, 0, 0);
    }

    // Interpolating the location, doing it arbitrarily for now
    lerpedLoc.x = PApplet.lerp(lerpedLoc.x, loc.x, 0.3f);
    lerpedLoc.y = PApplet.lerp(lerpedLoc.y, loc.y, 0.3f);
    lerpedLoc.z = PApplet.lerp(lerpedLoc.z, loc.z, 0.3f);
  }

  public PVector getLerpedPos() {
    return lerpedLoc;
  }

  public PVector getPos() {
    return loc;
  }

  public void display() {
    PImage img = kinect.getDepthImage();

    // Being overly cautious here
    if (depth == null || img == null) return;

    // Going to rewrite the depth image to show which pixels are in threshold
    // A lot of this is redundant, but this is just for demonstration purposes
    display.loadPixels();
    for (int x = 0; x < kinect.width; x++) {
      for (int y = 0; y < kinect.height; y++) {

        int offset = x + y * kinect.width;
        // Raw depth
        int rawDepth = depth[offset];
        int pix = x + y * display.width;
        if (minThreshold < rawDepth && rawDepth < maxThreshold &&
          y > topCrop && y < (kinect.height - bottomCrop)) {
          // A red color instead
          display.pixels[pix] = color(150, 50, 50);
        } else {
          display.pixels[pix] = img.pixels[offset];
        }
      }
    }
    display.updatePixels();

    // Draw the image
    image(display, 0, 0);
  }

  public int getmaxThreshold() {
    return maxThreshold;
  }

  public int getminThreshold() {
    return minThreshold;
  }

  public void setmaxThreshold(int t) {
    maxThreshold =  t;
  }
}





OscP5 oscP5;
NetAddress myRemoteLocation;

float low, mid, high;

public void setupOSC() {
 
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  

 // myRemoteLocation = new NetAddress("127.0.0.1",12000);
}




public void oscEvent(OscMessage theOscMessage) {
  /* check if theOscMessage has the address pattern we are looking for. */
  
  if(theOscMessage.checkAddrPattern("/TopLayerVideoOpacity")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      topLayerVideoOpacity = PApplet.parseInt(theOscMessage.get(0).floatValue()*255); 
      //println("received mid value " + mid);
      return;   
  } 
  if(theOscMessage.checkAddrPattern("/SeparationForce")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      separationForce = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  }
  if(theOscMessage.checkAddrPattern("/AlignmentForce")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      alignmentForce = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  }
   if(theOscMessage.checkAddrPattern("/CohesionForce")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      cohesionForce = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  }

  
    if(theOscMessage.checkAddrPattern("/Low")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      low = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  }
  if(theOscMessage.checkAddrPattern("/Mid")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      mid = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  } 
  if(theOscMessage.checkAddrPattern("/High")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      high = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  } 
 // println("### received an osc message. with address pattern "+theOscMessage.addrPattern());
}
  public void settings() {  size(1280, 720, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "FlockingMaskOSC" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
