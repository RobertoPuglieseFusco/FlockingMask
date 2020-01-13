void setupControlsUI(){
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
