import processing.video.*;

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

PImage boidImage;
PGraphics pg;
import controlP5.*;

ControlP5 cp5;

boolean toggleFullscreen, toggleGui = false;

float radiusBoid;
float maxforce;    // Maximum steering force
float maxspeed, separationForce, alignmentForce, cohesionForce;    // Maximum speed
float attractionForce, range, opacity;

PVector center;

boolean debug = false;
int XY_XZ_YZ = 1;
RadioButton trackingPlane;

void setup() {
  fullScreen(P3D);
  //size(1280,720,P3D);

  setupKeystone();
  kinect = new Kinect(this);
  tracker = new KinectTracker();

  pg = createGraphics(width, height);
  flock = new Flock();
  img = loadImage("moonwalk.jpg");


  // read all the video filenames in the videos directory
  path = sketchPath() + "/videos/";

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

  maxspeed = 2;
  maxforce = 0.03;
  center = new PVector(0, 0);
  com = new PVector(0, 0);
  ks.load();
  cp5.loadProperties(("default.ser"));
}

void movieEvent(Movie m) {
  m.read();
}

void draw() {

  center.set(com);
  // Convert the mouse coordinate into surface coordinates
  // this will allow you to use mouse events inside the 
  // surface from your screen. 
  PVector surfaceMouse = surface.getTransformedMouse();

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
  if (toggleGui) {
    cp5.draw();
  }
}

// Adjust the maxThreshold with key presses
void keyPressed() {
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
    ks.load();
    cp5.loadProperties(("default.ser"));
    println("loaded preset");
    break;

  case 's':
    // saves the layout
    ks.save();
    cp5.saveProperties("default", "default");
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

void controlEvent(ControlEvent theEvent) {
  if (theEvent.isFrom(trackingPlane)) {
    print("got an event from "+theEvent.getName()+"\t");
    for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
      print(int(theEvent.getGroup().getArrayValue()[i]));
    }
    println("\t "+theEvent.getValue());
    XY_XZ_YZ = int(theEvent.getGroup().getValue());
  }
}
