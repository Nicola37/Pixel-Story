/*Pixel Story

This is my term project for my Introduction to Digital Arts and Sciences
class at the University of Florida. It is an art and short movie creator
with a focus on pixel art and animation.

Thanks to Dave Small, our professor who taught us how to do much of this.

by Nicola Frachesen*/

//Necessary imports.
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.lang.StringBuilder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.Node;

public class pixelStory{
  //The size of the window.
  private static final int WIDTH = 800;
  private static final int HEIGHT = 800;

  //main
  public static void main(String[] args){
    //Creating the GUI with the EDT
    SwingUtilities.invokeLater(
      new Runnable(){
        public void run(){
          createAndShowGUI();
        }
      }
    );
  }

  //Creating the basic GUI.
  private static void createAndShowGUI(){
    JFrame frame = new ImageFrame(WIDTH,HEIGHT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    frame.validate();
    frame.setVisible(true);
  }
}

class ImageFrame extends JFrame{
  //Whether the program is in art or story mode.
  boolean art;
  
  //Buffered images to store the currently displayed image, and input images.
  BufferedImage image;
  BufferedImage imageInput;
  //The graphics 2D to draw on the buffered image
  Graphics2D g2d;
  //The cavas which will hold the image
  JLabel canvas;
  JPanel canvasPanel;
  //The java file chooser
  private final JFileChooser chooser;
  //The timer for timed animation.
  Timer timer;

  //The panel and buttons appearing at the bottom in art mode.
  JPanel buttonPanel;
  FlowLayout rightJustified;
  JButton gridButton;
  JButton brushButton;
  JButton eraserButton;
  JButton getColorButton;
  JButton smallButton;
  JButton bigButton;
  
  //The panel and sliders appearing on the right side in art mode.
  JPanel colorPanel;
  JPanel titlePanel;
  JPanel sliderPanel;
  JPanel rgbPanel;
  JPanel amountPanel;
  JSlider redSlider;
  JSlider greenSlider;
  JSlider blueSlider;
  JLabel redAmount;
  JLabel greenAmount;
  JLabel blueAmount;
  JPanel currentColor;
  
  //The panel and buttons appearing at the bottom in story mode.
  JPanel framesPanel;
  JButton previousButton;
  JButton nextButton;
  JLabel frameLabel;
  JButton playButton;
  JButton stopButton;
  
  //The line for drawing grid lines.
  Line2D.Double gridLines;
  //The size of each "pixel" in actual pixels.
  int pixelSize;
  //Width of the image in "pixels."
  int width;
  //Height of the image in "pixels."
  int height;
  //Stores the color value of each pixel.
  int[][] pixels;
  //Whether the grid is on or off.
  boolean grid;
  //Whether the eraser or brush is on.
  boolean eraser;
  //Whether art mode is currently set to get color.
  boolean getColor;
  //The color of the grid.
  Color gridColor;
  //The RGB values of the current color being drawn.
  int red;
  int green;
  int blue;
  //The size of the brush.
  int brushSize;
  
  //An arraylist of frames in the story.
  ArrayList<StoryFrame> frames;
  //The color of the text and its background.
  int textColor;
  int textBackgroundColor;
  //The current frame being displayed.
  int currentFrame;
  //The frames per second the story goes at.
  double fps;
  //The current foreground object being selected.
  int currentObject;

  //constructor
  public ImageFrame(int w, int h){
    //set up the frame's attributes
    this.setTitle("Pixel Story");
    this.setSize(w, h);
    
    //Set up all of the variables.
    art = true;

    image = null;
    g2d = null;
    canvas = new JLabel();
    canvasPanel = new JPanel();
    canvasPanel.add(canvas);
    chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File("."));

    buttonPanel = new JPanel();
    rightJustified = new FlowLayout(FlowLayout.RIGHT);
    gridButton = new JButton("Grid On/Off");
    brushButton = new JButton("Brush");
    eraserButton = new JButton("Eraser");
    getColorButton = new JButton("Get Color");
    smallButton = new JButton("-");
    bigButton = new JButton("+");

    colorPanel = new JPanel();
    titlePanel = new JPanel();
    sliderPanel = new JPanel();
    rgbPanel = new JPanel();
    amountPanel = new JPanel();
    redSlider = new JSlider(JSlider.VERTICAL, 0, 255, 0);
    greenSlider = new JSlider(JSlider.VERTICAL, 0, 255, 0);
    blueSlider = new JSlider(JSlider.VERTICAL, 0, 255, 0);
    redAmount = new JLabel("0", SwingConstants.CENTER);
    greenAmount = new JLabel("0", SwingConstants.CENTER);
    blueAmount = new JLabel("0", SwingConstants.CENTER);
    currentColor = new JPanel();

    framesPanel = new JPanel();
    previousButton = new JButton("Previous Frame");
    nextButton = new JButton("Next Frame");
    frameLabel = new JLabel("1", SwingConstants.CENTER);
    playButton = new JButton("Play");
    stopButton = new JButton("Stop");

    gridLines = new Line2D.Double();
    pixelSize = 0;
    width = 0;
    height = 0;
    pixels = null;
    grid = false;
    eraser = false;
    getColor = false;
    gridColor = new Color(0, 0, 0, 100);
    red = 0;
    green = 0;
    blue = 0;
    brushSize = 1;

    frames = null;
    textColor = 0xFFFFFFFF;
    textBackgroundColor = 0xFF000000;
    currentFrame = 1;
    fps = 1.0;
    currentObject = -1;
  
    //add a menu to the frame
    addMenu();
    
    //Adding the buttons, labels, etc. to each panel.

    buttonPanel.setLayout(rightJustified);
    buttonPanel.add(gridButton);
    buttonPanel.add(brushButton);
    buttonPanel.add(eraserButton);
    buttonPanel.add(getColorButton);
    buttonPanel.add(smallButton);
    buttonPanel.add(bigButton);

    colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS));
    Dimension limiter = new Dimension(1000, 100);
    titlePanel.setMaximumSize(limiter);
    titlePanel.add(new Label("Set Color"));
    colorPanel.add(titlePanel);
    sliderPanel.setLayout(new GridLayout(1, 3));
    sliderPanel.add(redSlider);
    sliderPanel.add(greenSlider);
    sliderPanel.add(blueSlider);
    colorPanel.add(sliderPanel);
    rgbPanel.setLayout(new GridLayout(1, 3));
    rgbPanel.setMaximumSize(limiter);
    rgbPanel.add(new Label("Red", Label.CENTER));
    rgbPanel.add(new Label("Green", Label.CENTER));
    rgbPanel.add(new Label("Blue", Label.CENTER));
    colorPanel.add(rgbPanel);
    amountPanel.setLayout(new GridLayout(1, 3));
    amountPanel.setMaximumSize(limiter);
    amountPanel.add(redAmount);
    amountPanel.add(greenAmount);
    amountPanel.add(blueAmount);
    colorPanel.add(amountPanel);
    currentColor.setMaximumSize(new Dimension(1000, 4000));
    currentColor.setBackground(Color.BLACK);
    currentColor.setOpaque(true);
    colorPanel.add(currentColor);

    setArtPane();

    framesPanel.setLayout(new GridLayout(1, 5));
    framesPanel.add(previousButton);
    framesPanel.add(stopButton);
    framesPanel.add(frameLabel);
    framesPanel.add(playButton);
    framesPanel.add(nextButton);

    //Adds a mouseListener to see when the mouse is pressed.
    canvas.addMouseListener(new MouseAdapter(){
      public void mousePressed( MouseEvent event ){
        if (art){
          if (getColor){
            //Get the x and y position.
            int x = (event.getPoint().x - (event.getPoint().x % pixelSize));
            int y = (event.getPoint().y - (event.getPoint().y % pixelSize));
            
            //Get the color at that position.
            int gotColor = pixels[(x/pixelSize)][(y/pixelSize)];
            red = (gotColor >>> 16) & 0x000000FF;
            green = (gotColor >>> 8) & 0x000000FF;
            blue = gotColor & 0x000000FF;
            
            //Set the sliders and current color to reflect the gotten color.
            currentColor.setBackground(new Color(gotColor));
            redSlider.setValue(red);
            greenSlider.setValue(green);
            blueSlider.setValue(blue);
          }
          else{
            if (grid){
              drawGrid();
            }
            //Get the x and y position.
            int x = (event.getPoint().x - (event.getPoint().x % pixelSize));
            int y = (event.getPoint().y - (event.getPoint().y % pixelSize));
            
            //Draw the cell(s) that have been clicked on.
            if (!eraser){
              for (int i = -(brushSize/2); i < (brushSize-(brushSize/2)); i++){
                for (int j = -(brushSize/2); j < (brushSize-(brushSize/2)); j++){
                  
                  //If the cell is in the frame.
                  if(((x/pixelSize)+i < width) && ((x/pixelSize)+i > -1) && ((y/pixelSize)+j < height) && ((y/pixelSize)+j > -1)){
                    pixels[(x/pixelSize)+i][(y/pixelSize)+j] = (0xFF000000 | (red << 16) | (green << 8) | blue);
                    //Set the color and draw the cell.
                    g2d.setColor(new Color(pixels[(x/pixelSize)+i][(y/pixelSize)+j]));
                    g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                  }
                }
              }
            }
            //Erase the cell(s) that have been clicked on.
            else{
              for (int i = -(brushSize/2); i < (brushSize-(brushSize/2)); i++){
                for (int j = -(brushSize/2); j < (brushSize-(brushSize/2)); j++){
                  
                  //If the cell is in the frame.
                  if(((x/pixelSize)+i < width) && ((x/pixelSize)+i > -1) && ((y/pixelSize)+j < height) && ((y/pixelSize)+j > -1)){
                    pixels[(x/pixelSize)+i][(y/pixelSize)+j] = 0xFFFFFFFF;
                    //Set the color and draw the cell.
                    g2d.setColor(new Color(pixels[(x/pixelSize)+i][(y/pixelSize)+j]));
                    g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                  }
                }
              }
            }

            if (grid == true){
              drawGrid();
            }

            displayBufferedImage(image);
          }
        }
        //If we're in story mode, check to see if an object is being clicked on.
        else{
            currentObject = objectCheck(event.getX(), event.getY());
        }
      }
    });
    
    //Adds a mouseListener to see when the mouse exits the screen.
    canvas.addMouseListener(new MouseAdapter(){
      public void mouseExited( MouseEvent event ){
        //If in art mode, redraw the image when the mouse leaves
        //so as to undo any temporary changes.
        if (art == true){
          if (grid == true){
            drawGrid();
          }

          for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
              g2d.setColor(new Color(pixels[i][j]));
              g2d.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
            }
          }

          if (grid == true){
            drawGrid();
          }

          displayBufferedImage(image);
        }
      }
    });

    //Adds a mouseListener to see when the mouse is moved.
    canvas.addMouseMotionListener(new MouseAdapter(){
      public void mouseMoved( MouseEvent event ){
        //If in art mode, shows the user what they will draw if they press the mouse.
        if (art == true){
          if (!getColor){
            if (grid){
              drawGrid();
            }

            for (int i = 0; i < width; i++){
              for (int j = 0; j < height; j++){
                g2d.setColor(new Color(pixels[i][j]));
                g2d.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
              }
            }

            //Get the x and y position within an accuracy of 8.
            int x = (event.getPoint().x - (event.getPoint().x % pixelSize));
            int y = (event.getPoint().y - (event.getPoint().y % pixelSize));
            
            if (!eraser){
              for (int i = -(brushSize/2); i < (brushSize-(brushSize/2)); i++){
                for (int j = -(brushSize/2); j < (brushSize-(brushSize/2)); j++){

                  //If the cell is in the frame.
                  if(((x/pixelSize)+i < width) && ((x/pixelSize)+i > -1) && ((y/pixelSize)+j < height) && ((y/pixelSize)+j > -1)){
                    //Set the color and draw the cell.
                    g2d.setColor(new Color(0xFF000000 | (red << 16) | (green << 8) | blue));
                    g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                  }
                }
              }
            }
            else{
              for (int i = -(brushSize/2); i < (brushSize-(brushSize/2)); i++){
                for (int j = -(brushSize/2); j < (brushSize-(brushSize/2)); j++){
                  //If the cell is in the frame.
                  if(((x/pixelSize)+i < width) && ((x/pixelSize)+i > -1) && ((y/pixelSize)+j < height) && ((y/pixelSize)+j > -1)){
                    //Set the color and draw the cell.
                    g2d.setColor(new Color(0xFFFFFFFF));
                    g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                  }
                }
              }
            }

            if (grid){
              drawGrid();
            }

            displayBufferedImage(image);
          }
        }    
      }
    });

    //Adds a mouseListener to see when the mouse is dragged.
    canvas.addMouseMotionListener(new MouseAdapter(){
      public void mouseDragged( MouseEvent event ){
        //In art mode, dragging the mouse has the same effect as pressing.
        if (art && !getColor){
          if (grid == true){
            drawGrid();
          }
          //Get the x and y position within an accuracy of 8.
          int x = (event.getPoint().x - (event.getPoint().x % pixelSize));
          int y = (event.getPoint().y - (event.getPoint().y % pixelSize));
          
          if (!eraser){
            for (int i = -(brushSize/2); i < (brushSize-(brushSize/2)); i++){
              for (int j = -(brushSize/2); j < (brushSize-(brushSize/2)); j++){

                //If the cell is in the frame.
                if(((x/pixelSize)+i < width) && ((x/pixelSize)+i > -1) && ((y/pixelSize)+j < height) && ((y/pixelSize)+j > -1)){
                  pixels[(x/pixelSize)+i][(y/pixelSize)+j] = (0xFF000000 | (red << 16) | (green << 8) | blue);
                  //Set the color and draw the cell.
                  g2d.setColor(new Color(pixels[(x/pixelSize)+i][(y/pixelSize)+j]));
                  g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                }
              }
            }
          }
          else{
            for (int i = -(brushSize/2); i < (brushSize-(brushSize/2)); i++){
              for (int j = -(brushSize/2); j < (brushSize-(brushSize/2)); j++){

                //If the cell is in the frame.
                  if(((x/pixelSize)+i < width) && ((x/pixelSize)+i > -1) && ((y/pixelSize)+j < height) && ((y/pixelSize)+j > -1)) {
                    pixels[(x/pixelSize)+i][(y/pixelSize)+j] = 0xFFFFFFFF;
                    //Set the color and draw the cell.
                    g2d.setColor(new Color(pixels[(x/pixelSize)+i][(y/pixelSize)+j]));
                    g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                  }
              }
            }
          }

          if (grid == true){
            drawGrid();
          }

          displayBufferedImage(image);
        }
        //In story mode, you can drag around foreground objects.
        else if (art == false && currentObject != -1){
          frames.get(currentFrame-1).foregroundObjectX.set(currentObject, event.getX());
          frames.get(currentFrame-1).foregroundObjectY.set(currentObject, event.getY());
          
          g2d.drawImage(frames.get(currentFrame-1).background, null, 0, 0);
          drawObjects(currentFrame-1);
          setCurrentText(false);
          frames.get(currentFrame-1).fullImage = image;
          displayBufferedImage(image);
        }    
      }
    });
    
    //Sets the timer.
    timer = new Timer((int)((1.0/fps)*1000.0), new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        //Stop the timer.
        timer.stop();

        //Loops animation.
        if (currentFrame == frames.size()){
          currentFrame = 1;
        }
        else{
          currentFrame++;
        }

        //Show the next frame.
        frameLabel.setText(Integer.toString(currentFrame));
        setFrame();

        //Restart the timer.
        timer.restart();
      }
    }); 
    
    //Button that turns the grid on or off.
    gridButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        drawGrid();
        if (grid == false){
          grid = true;
        }
        else{
          grid = false;
        }

        displayBufferedImage(image);
      }
    });

    //Sets the brush to be activated.
    brushButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        getColor = false;
        eraser = false;
      }
    });
    
    //Sets the eraser to be activated.
    eraserButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        getColor = false;
        eraser = true;
      }
    });
 
    //Sets get color to be activated.
    getColorButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        getColor = true;
      }
    });
    
    //Makes the whole image smaller.
    smallButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (pixelSize > 2){
          //Reduces the size of all "pixels" by 1.
          pixelSize--;

          //Redraws the image.
          image = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_INT_ARGB);
          g2d = (Graphics2D) image.createGraphics();

          for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
              g2d.setColor(new Color(pixels[i][j]));
              g2d.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
            }
          }

          if (grid){
            drawGrid();
          }

          displayBufferedImage(image);
        }
      }
    });
    
    //Makes the whole image larger.
    bigButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        //Increases the size of all "pixels" by 1.
        pixelSize++;

        //Redraws the image.
        image = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.createGraphics();

        for (int i = 0; i < width; i++){
          for (int j = 0; j < height; j++){
            g2d.setColor(new Color(pixels[i][j]));
            g2d.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
          }
        }

        if (grid){
          drawGrid();
        }

        displayBufferedImage(image);
      }
    });
    
    //Setting the red, green, and blue values of the current color with sliders.
    redSlider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        red = ((JSlider)e.getSource()).getValue();
        currentColor.setBackground(new Color((0xFF000000 | (red << 16) | (green << 8) | blue)));
        redAmount.setText(Integer.toString(red));
      }
    });

    greenSlider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        green = ((JSlider)e.getSource()).getValue();
        currentColor.setBackground(new Color((0xFF000000 | (red << 16) | (green << 8) | blue)));
        greenAmount.setText(Integer.toString(green));
      }
    });

    blueSlider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        blue = ((JSlider)e.getSource()).getValue();
        currentColor.setBackground(new Color((0xFF000000 | (red << 16) | (green << 8) | blue)));
        blueAmount.setText(Integer.toString(blue));
      }
    });
    
    //Goes to the previous frame.
    previousButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (currentFrame != 1){
          currentFrame--;
          frameLabel.setText(Integer.toString(currentFrame));
          setFrame();
        }
        else{
          frameMessage(true);
        }
      }
    });
    
    //Goes to the next frame.
    nextButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (currentFrame == frames.size()){
          frameMessage(false);
          frames.get(currentFrame-1).fullImage = image;
          displayBufferedImage(image);
        }
        else{
          currentFrame++;
          frameLabel.setText(Integer.toString(currentFrame));
          setFrame();
        }
      }
    });
    
    //Plays the animation.
    playButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (!timer.isRunning()){
          timer.start();
        }
      }
    });

    //Stops the animation.
    stopButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (timer.isRunning()){
          timer.stop();
        }
      }
    });
  }
  
  //set up the frame's menu bar
  private void addMenu(){
    //Art Menu
    JMenu artMenu = new JMenu("Art Creation");

    //Each item that cannot be used to start art mode cannot be used
    //unless you are already in art mode.

    JMenuItem blankItem = new JMenuItem("Blank Canvas");
    blankItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == false){
          setArtPane();
          art = true;
        }
        resetCanvas();
      }
    });

    JMenuItem importItem = new JMenuItem("Import Image");
    importItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == false){
          setArtPane();
          art = true;
        }
        importImage();
      }
    });

    JMenuItem sizeItem = new JMenuItem("Set Brush/Eraser Size");
    sizeItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == false){
          wrongButton(art);
        }
        else{
          setBrushSize();
        }
      }
    });

    JMenuItem saveArtItem = new JMenuItem("Save Pixel Art");
    saveArtItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == false){
          wrongButton(art);
        }
        else{
          saveArt();
        }
      }
    });

    JMenuItem loadArtItem = new JMenuItem("Load Pixel Art");
    loadArtItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == false){
          setArtPane();
          art = true;
        }
        loadArt();
      }
    });

    JMenuItem saveArtImageItem = new JMenuItem("Save Art as an Image");
    saveArtImageItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == false){
          wrongButton(art);
        }
        else{
          saveArtImage();
        }
      }
    });
  
    //Add the menu options.
    artMenu.add(blankItem);
    artMenu.add(importItem);
    artMenu.add(sizeItem);
    artMenu.add(saveArtItem);
    artMenu.add(loadArtItem);
    artMenu.add(saveArtImageItem);

    //Story Menu
    JMenu storyMenu = new JMenu("Story Creation");

    //Each item that cannot be used to start story mode cannot be used
    //unless you are already in story mode.

    JMenuItem storyItem = new JMenuItem("New Story");
    storyItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          setStoryPane();
          art = false;
        }
        newStory();
      }
    });

    JMenuItem backgroundItem = new JMenuItem("Set Background");
    backgroundItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          setCurrentBackground();
        }
      }
    });

    JMenuItem addItem = new JMenuItem("Add Foreground Object");
    addItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          addForegroundObject();
        }
      }
    });

    JMenuItem removeItem = new JMenuItem("Remove Foreground Object");
    removeItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          removeForegroundObject();
        }
      }
    });

    JMenuItem mirrorItem = new JMenuItem("Mirror Foreground Object");
    mirrorItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          mirrorForegroundObject();
        }
      }
    });

    JMenuItem textItem = new JMenuItem("Set Text");
    textItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          setCurrentText(true);
        }
      }
    });

    JMenuItem fpsItem = new JMenuItem("Set Frames per Second");
    fpsItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          setFPS();
        }
      }
    });

    JMenuItem saveStoryItem = new JMenuItem("Save Created Story");
    saveStoryItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          saveStory();
        }
      }
    });

    JMenuItem loadStoryItem = new JMenuItem("Load Created Story");
    loadStoryItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          setStoryPane();
          art = false;
        }
        loadStory();
      }
    });

    JMenuItem saveStoryImageItem = new JMenuItem("Save Current Frame as an Image");
    saveStoryImageItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          saveArtImage();
        }
      }
    });

    JMenuItem saveStoryMovieItem = new JMenuItem("Save Story as a gif");
    saveStoryMovieItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          try{
            saveStoryMovie();
          }
          catch (Exception e){
            gifErrorMessage();
          }
        }
      }
    });
  
    //Add the menu options.
    storyMenu.add(storyItem);
    storyMenu.add(backgroundItem);
    storyMenu.add(addItem);
    storyMenu.add(removeItem);
    storyMenu.add(mirrorItem);
    storyMenu.add(textItem);
    storyMenu.add(fpsItem);
    storyMenu.add(saveStoryItem);
    storyMenu.add(loadStoryItem);
    storyMenu.add(saveStoryImageItem);
    storyMenu.add(saveStoryMovieItem);
  
    //Attach the menus to a menu bar.
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(artMenu);
    menuBar.add(storyMenu);
    this.setJMenuBar(menuBar);
  }
  
  //Sets the GUI for art mode.
  private void setArtPane(){
    this.getContentPane().removeAll();

    getContentPane().add(canvasPanel, BorderLayout.CENTER);

    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    getContentPane().add(colorPanel, BorderLayout.EAST);

    this.repaint();
    validate();
  }
  
  //Sets the GUI for story mode.
  private void setStoryPane(){
    this.getContentPane().removeAll();

    getContentPane().add(canvasPanel, BorderLayout.CENTER);

    getContentPane().add(framesPanel, BorderLayout.SOUTH);

    currentFrame = 1;

    this.repaint();
    validate();
  }
  
  //Creates a user-specified blank canvas.
  public void resetCanvas(){
    setCanvas();
    
    pixels = new int[width][height];
    for (int i = 0; i < width; i++){
      for (int j = 0; j < height; j++){
        pixels[i][j] = 0xFFFFFFFF;
      }
    }

    image = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_INT_ARGB);
    g2d = (Graphics2D) image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fill(new Rectangle(0, 0, width*pixelSize, height*pixelSize));
    if (grid == true){
      drawGrid();
    }
    displayBufferedImage(image);
  }
  
  //Imports and pixelates an image to the user's liking.
  public void importImage(){
    setImport();
    
    //Gets an image from the user.
    open();

    width = imageInput.getWidth()/pixelSize;
    height = imageInput.getHeight()/pixelSize;
    image = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_INT_ARGB);
    g2d = (Graphics2D) image.createGraphics();
    pixels = new int[width][height];
    
    //Averages the color in bocks to pixelate it.
    for (int i = 0; i < width; i++){
      for (int j = 0; j < height; j++){
        pixels[i][j] = colorAverager(i*pixelSize, j*pixelSize);

        g2d.setColor(new Color(pixels[i][j]));
        g2d.fillRect(i*pixelSize, j*pixelSize, pixelSize, pixelSize);
      }
    }

    if (grid == true){
      drawGrid();
    }

    displayBufferedImage(image);
  }
  
  //Save art as a text file that you can come back to and edit later.
  public void saveArt(){
    String fileName = "";

    while (fileName.equals("")){
      //Uses the JOptionPane to ask the user how wide they want the image to be.
      fileName = JOptionPane.showInputDialog("What would you like to name the saved file? (Please do not include the file extension in the name.)");
      
      //Lets the user know that they have to enter a String if they have not. 
      if (fileName.equals("")){
        JOptionPane.showMessageDialog(this, "Please enter a name for the file.");
      }
    }

    String savePixelSize = Integer.toString(pixelSize);
    String saveWidth = Integer.toString(width);
    String saveHeight = Integer.toString(height);
    
    //Write everything about the art into the create dsave file.
    try{
      PrintWriter fileWriter = new PrintWriter(fileName+".pxsa", "UTF-8");

      fileWriter.println("Pixel Story Saved Art File");
      fileWriter.println(savePixelSize);
      fileWriter.println(saveWidth);
      fileWriter.println(saveHeight);
      for (int i = 0; i < width; i++){
        for (int j = 0; j < height; j++){
          fileWriter.println(pixels[i][j]);
        }
      }
      fileWriter.close();
    }
    catch (FileNotFoundException e){
    }
    catch(UnsupportedEncodingException e){
    }
  }
  
  //Load a saved art file.
  public void loadArt(){
    JOptionPane.showMessageDialog(this, "Please select a Pixel Story art file (.pxsa) to load.");

    File file = getFile();
    
    //Read through the art file, and set variables and objects accordingly.
    if (file != null){
      try{
        BufferedReader saveReader = new BufferedReader(new FileReader(file));

        String currentLine = null;

        saveReader.readLine();

        currentLine = saveReader.readLine();
        pixelSize = Integer.parseInt(currentLine);

        currentLine = saveReader.readLine();
        width = Integer.parseInt(currentLine);

        currentLine = saveReader.readLine();
        height = Integer.parseInt(currentLine);

        pixels = new int[width][height];

        image = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.createGraphics();

        for (int i = 0; i < width; i++){
          for (int j = 0; j < height; j++){
            currentLine = saveReader.readLine();
            pixels[i][j] = Integer.parseInt(currentLine);
            
            g2d.setColor(new Color(pixels[i][j]));
            g2d.fill(new Rectangle(i*pixelSize, j*pixelSize, pixelSize, pixelSize));
          }
        }

        if (grid == true){
          drawGrid();
        }
      }
      catch (FileNotFoundException exception){
      }
      catch (IOException exception){
      }

      displayBufferedImage(image);
    }
  }
  
  //Saves the art as a png image.
  public void saveArtImage(){
    //Asks the user for a file name and then writes the image.
    String fileName;
    fileName = JOptionPane.showInputDialog("What would you like the name of the file to be? (Please do not enter the file extension. e.g. 'apple' not 'apple.png')");
    File outputFile = new File(fileName + ".png");

    try{
      javax.imageio.ImageIO.write( image, "png", outputFile );
    }
    catch ( IOException e ){
      JOptionPane.showMessageDialog(ImageFrame.this, "Error saving file","oops!", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  //Creates a new story.
  public void newStory(){
    frames = new ArrayList<StoryFrame>(1);
    currentFrame = 1;
    
    //Asks for an initial background.
    JOptionPane.showMessageDialog(this, "Please select an image (png or jpeg) to use as the background.");
    open();
    
    //Asks for the fps.
    setFPS();
    
    //Sets the current frame to 1.
    currentFrame = 1;
    
    //initializes the first frame.
    initializeFrame();

    displayBufferedImage(image);
  }
  
  //Change the current image's background.
  public void setCurrentBackground(){
    JOptionPane.showMessageDialog(this, "Please select an image (png or jpeg) to use as the background.");
    open();

    frames.get(currentFrame-1).background = imageInput;

    image = new BufferedImage(imageInput.getWidth(), imageInput.getHeight()+100, BufferedImage.TYPE_INT_ARGB);
    g2d = (Graphics2D) image.createGraphics();

    g2d.drawImage(imageInput, null, 0, 0);
    drawObjects(currentFrame-1);
    setCurrentText(false);
    frames.get(currentFrame-1).fullImage = image;

    displayBufferedImage(image);
  }
  
  //Add a pixel art object to the foreground, which can then be manipulated.
  public void addForegroundObject(){
    JOptionPane.showMessageDialog(this, "Please select a Pixel Story art file (.pxsa) to add as a foreground object.");

    File file = getFile();
    
    //Reads the file and adds the pixel art stored in it as a foreground object.
    if (file != null){
      try{
        BufferedReader saveReader = new BufferedReader(new FileReader(file));

        String currentLine = null;

        int listSize = frames.get(currentFrame-1).foregroundObjects.size();

        saveReader.readLine();

        currentLine = saveReader.readLine();
        int s = Integer.parseInt(currentLine);

        currentLine = saveReader.readLine();
        int w = Integer.parseInt(currentLine);

        currentLine = saveReader.readLine();
        int h = Integer.parseInt(currentLine);

        int[][] o = new int[w][h];

        for (int i = 0; i < w; i++){
          for (int j = 0; j < h; j++){
            currentLine = saveReader.readLine();
            o[i][j] = Integer.parseInt(currentLine);
          }
        }
        
        String n = "";
        n += JOptionPane.showInputDialog("What would you like the name of this object to be?");

        if (n.length() == 0 || n.equals("null")){
          n = "object"+Integer.toString(frames.get(currentFrame-1).foregroundObjects.size()+1);
        }

        frames.get(currentFrame-1).addObject(o, n, s, w, h);
        drawObjects(currentFrame-1);
        
        frames.get(currentFrame-1).fullImage = image;
        
        setCurrentText(false);

        displayBufferedImage(image);
      }
      catch (FileNotFoundException exception){
      }
      catch (IOException exception){
      }
    }
  }
  
  //Removes a foreground object present in the current frame.
  public void removeForegroundObject(){
    //If there are no objects to remove, let the user know.
    if (frames.get(currentFrame-1).foregroundObjects.size() == 0){
      JOptionPane.showMessageDialog(this, "Sorry, but there do not seem to be any objects.");
    }
    else{
      //List the objects for the user to see.
      StringBuilder question = new StringBuilder("The foreground objects currently in this frame are:\n");
      for (int i = 0; i < frames.get(currentFrame-1).foregroundObjects.size(); i++){
        question.append(frames.get(currentFrame-1).foregroundObjectNames.get(i));

        if (i != frames.get(currentFrame-1).foregroundObjects.size()-1){
          question.append(", ");
        }
      }

      question.append("\nWhich one would you like to delete?");
      
      //Have the user select which object they want to delete.
      String remove = JOptionPane.showInputDialog(question.toString());
      
      //Have the frame try to remove it.
      boolean done = frames.get(currentFrame-1).removeObject(remove);
      
      //If the object was successfully removed, redraw the image.
      if (done){
        g2d.drawImage(frames.get(currentFrame-1).background, null, 0, 0);
        drawObjects(currentFrame-1);
        setCurrentText(false);
        frames.get(currentFrame-1).fullImage = image;
        displayBufferedImage(image);
      }
      //Else, let the user know that the object name they entered does not exist.
      else{
        JOptionPane.showMessageDialog(this, "Sorry, but that does not seem to be the name of an object.");
      }
    }
  }
  
  //Mirrors a foreground object.
  //Espeecially useful for sprites so that you don't have to make art for facing both directions.  
  public void mirrorForegroundObject(){
    if (frames.get(currentFrame-1).foregroundObjects.size() == 0){
      JOptionPane.showMessageDialog(this, "Sorry, but there do not seem to be any objects.");
    }
    else{
      StringBuilder question = new StringBuilder("The foreground objects currently in this frame are:\n");
      for (int i = 0; i < frames.get(currentFrame-1).foregroundObjects.size(); i++){
        question.append(frames.get(currentFrame-1).foregroundObjectNames.get(i));

        if (i != frames.get(currentFrame-1).foregroundObjects.size()-1){
          question.append(", ");
        }
      }

      question.append("\nWhich one would you like to mirror?");

      String mirror = JOptionPane.showInputDialog(question.toString());

      boolean done = frames.get(currentFrame-1).mirrorObject(mirror);
      
      if (done){
        g2d.drawImage(frames.get(currentFrame-1).background, null, 0, 0);
        drawObjects(currentFrame-1);
        setCurrentText(false);
        frames.get(currentFrame-1).fullImage = image;
        displayBufferedImage(image);
      }
      else{
        JOptionPane.showMessageDialog(this, "Sorry, but that does not seem to be the name of an object.");
      }
    }
  }
  
  //Set the text on the current frame.
  public void setCurrentText(boolean change){
    if (change){
      String newText = "";

      newText = JOptionPane.showInputDialog("What would you like the text on this frame to be?");
      frames.get(currentFrame-1).text = newText;
    }

    g2d.setColor(new Color(textBackgroundColor));
    g2d.fill(new Rectangle(0, frames.get(currentFrame-1).background.getHeight()-1, image.getWidth(), 100));

    g2d.setFont(new Font("Arial", Font.BOLD, 20));
    FontMetrics fm = g2d.getFontMetrics();
    g2d.setColor(new Color(textColor));
    g2d.drawString(frames.get(currentFrame-1).text, (image.getWidth()/2)-(fm.stringWidth(frames.get(currentFrame-1).text)/2), image.getHeight()-50);

    displayBufferedImage(image);
  }
  
  //Set the animation's frames per second.
  public void setFPS(){
    String result = "";
    fps = 0.0;

    while (fps <= 0.0){
      //Uses the JOptionPane to ask the user what the fps should be.
      result = JOptionPane.showInputDialog("Please enter how many frames per second you would like the story to go at.");
    
      //Tries and catches an exception in case the user enters something other than a double.
      try{
        fps = Double.parseDouble(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive double if they have not. 
      if (fps <= 0.0){
        JOptionPane.showMessageDialog(this, "Please enter a number that is greater than 0.");
      }
    }
    
    //Use the fps to set the timer's new delay.
    timer.setInitialDelay((int)((1.0/fps)*1000.0));
  }
  
  //Save the story to a text file so the user can load and continue work later.
  public void saveStory(){
    //Ask the user to name the file.
    String fileName = "";

    while (fileName.equals("")){
      fileName = JOptionPane.showInputDialog("What would you like to name the saved file? (Please do not include the file extension in the name.)");
      
      if (fileName.equals("")){
        JOptionPane.showMessageDialog(this, "Please enter a name for the file.");
      }
    }
    
    //Write the information of each frame onto the file.
    try{
      PrintWriter fileWriter = new PrintWriter(fileName+".pxss", "UTF-8");

      fileWriter.println("Pixel Story Saved Story File");
      
      fileWriter.println(fps);
      fileWriter.println(frames.size());
      for (int i = 0; i < frames.size(); i++){

        fileWriter.println(frames.get(i).fullImage.getWidth());
        fileWriter.println(frames.get(i).fullImage.getHeight());
        for (int fIX = 0; fIX < frames.get(i).fullImage.getWidth(); fIX++){
          for (int fIY = 0; fIY < frames.get(i).fullImage.getHeight(); fIY++){
            fileWriter.println(frames.get(i).fullImage.getRGB(fIX, fIY));
          }
        }

        for (int bX = 0; bX < frames.get(i).background.getWidth(); bX++){
          for (int bY = 0; bY < frames.get(i).background.getHeight(); bY++){
            fileWriter.println(frames.get(i).background.getRGB(bX, bY));
          }
        }

        fileWriter.println(frames.get(i).text);
        
        fileWriter.println(frames.get(i).foregroundObjects.size());
        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjects.get(o).length);
          fileWriter.println(frames.get(i).foregroundObjects.get(o)[0].length);
          for (int oX = 0; oX < frames.get(i).foregroundObjects.get(o).length; oX++){
            for (int oY = 0; oY < frames.get(i).foregroundObjects.get(o)[0].length; oY++){
              fileWriter.println(frames.get(i).foregroundObjects.get(o)[oX][oY]);
            }
          }
        }

        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjectNames.get(o));
        }

        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjectSize.get(o));
        }

        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjectWidth.get(o));
        }

        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjectHeight.get(o));
        }

        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjectX.get(o));
        }

        for (int o = 0; o < frames.get(i).foregroundObjects.size(); o++){
          fileWriter.println(frames.get(i).foregroundObjectY.get(o));
        }

      }

      fileWriter.close();
    }
    catch (FileNotFoundException e){
    }
    catch(UnsupportedEncodingException e){
    }
  }
  
  //Load a saved story file to continue work on it.
  public void loadStory(){
    JOptionPane.showMessageDialog(this, "Please select a Pixel Story story file (.pxss) to load.");

    File file = getFile();
    
    //Read the file as it was written and apply its settings.
    if (file != null){
      try{
        BufferedReader saveReader = new BufferedReader(new FileReader(file));

        String currentLine = null;
        int numOfFrames;
        int x;
        int y;
        int o;

        saveReader.readLine();

        currentLine = saveReader.readLine();
        fps = Double.parseDouble(currentLine);
        timer.setInitialDelay((int)((1.0/fps)*1000.0));

        currentLine = saveReader.readLine();
        numOfFrames = Integer.parseInt(currentLine);
        frames = new ArrayList<StoryFrame>(numOfFrames);

        for (int i = 0; i < numOfFrames; i++){
          StoryFrame newFrame = new StoryFrame();

          currentLine = saveReader.readLine();
          x = Integer.parseInt(currentLine);
          currentLine = saveReader.readLine();
          y = Integer.parseInt(currentLine);
          newFrame.fullImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
          for (int fIX = 0; fIX < x; fIX++){
            for (int fIY = 0; fIY < y; fIY++){
              currentLine = saveReader.readLine();
              newFrame.fullImage.setRGB(fIX, fIY, Integer.parseInt(currentLine));
            }
          }

          newFrame.background = new BufferedImage(x, y-100, BufferedImage.TYPE_INT_ARGB);
          for (int bX = 0; bX < x; bX++){
            for (int bY = 0; bY < (y-100); bY++){
              currentLine = saveReader.readLine();
              newFrame.background.setRGB(bX, bY, Integer.parseInt(currentLine));
            }
          }

          currentLine = saveReader.readLine();
          newFrame.text = new String(currentLine);

          currentLine = saveReader.readLine();
          o = Integer.parseInt(currentLine);
          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            x = Integer.parseInt(currentLine); 
            currentLine = saveReader.readLine();
            y = Integer.parseInt(currentLine);
            int[][] currO = new int[x][y];

            for (int oX = 0; oX < x; oX++){
              for (int oY = 0; oY < y; oY++){
                currentLine = saveReader.readLine();
                currO[oX][oY] = Integer.parseInt(currentLine);
              }
            }

            newFrame.foregroundObjects.add(oo, currO);
          }

          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            newFrame.foregroundObjectNames.add(oo, new String(currentLine));
          }

          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            newFrame.foregroundObjectSize.add(oo, Integer.parseInt(currentLine));
          }

          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            newFrame.foregroundObjectWidth.add(oo, Integer.parseInt(currentLine));
          }

          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            newFrame.foregroundObjectHeight.add(oo, Integer.parseInt(currentLine));
          }

          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            newFrame.foregroundObjectX.add(oo, Integer.parseInt(currentLine));
          }

          for (int oo = 0; oo < o; oo++){
            currentLine = saveReader.readLine();
            newFrame.foregroundObjectY.add(oo, Integer.parseInt(currentLine));
          }

          frames.add(i, newFrame);
        }

      }
      catch (FileNotFoundException exception){
      }
      catch (IOException exception){
      }
      
      currentFrame = 1;
      frameLabel.setText("1");
      image = frames.get(0).fullImage;
      g2d = (Graphics2D) image.createGraphics();

      displayBufferedImage(image);
    }
  }
  
  //Save the story as an animated gif.
  public void saveStoryMovie() throws Exception{
    //Asks the user to name the file.
    String fileName;
    fileName = JOptionPane.showInputDialog("What would you like the name of the file to be? (Please do not enter the file extension. e.g. 'apple' not 'apple.gif')");
    
    //The gif needs the fps in the form of a delay between each frame in long.
    long delay = ((long) ((1/fps)*1000))/10L;

    //Set up the image writer, and stream to take the images
    ImageWriter iw = ImageIO.getImageWritersByFormatName("gif").next();
    OutputStream out = new FileOutputStream(fileName+".gif");
    ImageOutputStream ios = ImageIO.createImageOutputStream(out);
    iw.setOutput(ios);
    iw.prepareWriteSequence(null);
    
    //Writes each given frame to the gif.
    for (int i = 0; i < frames.size(); i++){
       ImageWriteParam iwp = iw.getDefaultWriteParam();
       IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(frames.get(i).fullImage), iwp);
       configureGIFFrame(metadata, String.valueOf(delay), i, "none", 0);
       IIOImage ii = new IIOImage(frames.get(i).fullImage, null, metadata);
       iw.writeToSequence(ii, null);
    }
    
    //Stop writing the gif and close the stream
    iw.endWriteSequence();
    ios.close();
  }
  
  //Creates the first frame of a story, and sets the necessary initial settings.
  public void initializeFrame(){
    //Creates the first frame from the given background image.
    frames.add(new StoryFrame(imageInput));
    
    //Sets the image.
    image = new BufferedImage(imageInput.getWidth(), imageInput.getHeight()+100, BufferedImage.TYPE_INT_ARGB);
    g2d = (Graphics2D) image.createGraphics();
    
    //Draws the text box.
    g2d.drawImage(imageInput, null, 0, 0);
    g2d.setColor(new Color(textBackgroundColor));
    g2d.fill(new Rectangle(0, imageInput.getHeight()-1, image.getWidth(), 100));
    
    //Sets the font.
    g2d.setFont(new Font("Arial", Font.BOLD, 20));
    //This FontMetrics is used to get the pixel length of the string for centering.
    FontMetrics fm = g2d.getFontMetrics();
    //Draws the text.
    g2d.setColor(new Color(textColor));
    g2d.drawString(frames.get(currentFrame-1).text, (image.getWidth()/2)-(fm.stringWidth(frames.get(currentFrame-1).text)/2), image.getHeight()-50);
    
    //Sets the first frame's full image.
    frames.get(currentFrame-1).fullImage = image;
  }
  
  //Redraw the image of the current frame.
  public void setFrame(){
    image = frames.get(currentFrame-1).fullImage;
    g2d = (Graphics2D) image.createGraphics();
    displayBufferedImage(image);
  }
  
  //Checks if the given x and y location is home to a foreground object,
  //returning which one it is (or -1 if there isn't one).
  public int objectCheck(int x, int y){
    for (int i = 0; i < frames.get(currentFrame-1).foregroundObjects.size(); i++){
      if (x > frames.get(currentFrame-1).foregroundObjectX.get(i) && x < (frames.get(currentFrame-1).foregroundObjectX.get(i)+(frames.get(currentFrame-1).foregroundObjectSize.get(i)*frames.get(currentFrame-1).foregroundObjectWidth.get(i)))){
        if (y > frames.get(currentFrame-1).foregroundObjectY.get(i) && y < (frames.get(currentFrame-1).foregroundObjectY.get(i)+(frames.get(currentFrame-1).foregroundObjectSize.get(i)*frames.get(currentFrame-1).foregroundObjectHeight.get(i)))){
          return i;
        }
      }
    }

    return -1;
  }
  
  //Redraw all of the foreground objects on a given frame.
  public void drawObjects(int whichFrame){
    for (int i = (frames.get(whichFrame).foregroundObjects.size()-1); i >= 0; i--){
      int x = frames.get(whichFrame).foregroundObjectX.get(i);
      int y = frames.get(whichFrame).foregroundObjectY.get(i);
      int s = frames.get(whichFrame).foregroundObjectSize.get(i);

      for (int j = 0; j < frames.get(whichFrame).foregroundObjectWidth.get(i); j++){
        for (int k = 0; k < frames.get(whichFrame).foregroundObjectHeight.get(i); k++){
          if (frames.get(whichFrame).foregroundObjects.get(i)[j][k] != -1){
            g2d.setColor(new Color(frames.get(whichFrame).foregroundObjects.get(i)[j][k]));
            g2d.fill(new Rectangle(x+(j*s), y+(k*s), s, s));
          }
        }
      }
    }
  }
  
  //Draw the grid using xor mode, so it can easily be reversed at any time
  //by calling this same method again.
  public void drawGrid(){
    g2d.setColor(Color.WHITE);
    g2d.setXORMode(gridColor);
    for (int i = pixelSize-1; i < width*pixelSize-1; i += pixelSize){
      gridLines.setLine(i, 0, i, (height*pixelSize)-1);
      g2d.draw(gridLines);
    }
    for (int i = pixelSize-1; i < height*pixelSize-1; i += pixelSize){
      gridLines.setLine(0, i, (width*pixelSize)-1, i);
      g2d.draw(gridLines);
    }
    g2d.setPaintMode();
  }

  //Finds the average color of the given pixels.
  public int colorAverager(int startingX, int startingY){
    //Creates the initial red, green, blue, and color values.
    int redA = 0;
    int greenA = 0;
    int blueA = 0;
    int currentColorA = 0;
    int area = pixelSize*pixelSize;
    
    //These two for loops go through all of the pixels in the given area.
    for (int i = startingX; i < (startingX+pixelSize); i++){
      for (int j = startingY; j < (startingY+pixelSize); j++){
        //Gets the color of the current pixel.
        currentColorA = imageInput.getRGB(i, j);

        //Extracts the red, green, and blue values from the color,
        //and adds it to the current total for each channel.
        redA += (currentColorA >>> 16) & 0x000000FF;
        greenA += (currentColorA >>> 8) & 0x000000FF;
        blueA += currentColorA & 0x000000FF;  
      }
    }
    
    //Finds the average of reds, greens, and blues for all the pixels.
    redA = redA/area;
    greenA = greenA/area;
    blueA = blueA/area;
    
    //Returns the average color.
    return (redA << 16 | greenA << 8 | blueA);
  }

  //open() - choose a file, load, and display the image
  private void open(){
    File file = getFile();
    if (file != null){
      displayFile(file);
    }
  }
  
  //open a file selected by the user
  private File getFile(){
    File file = null;
  
    if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
      file = chooser.getSelectedFile();
    }
  
    return file;
  }
  
  //display specified file in the frame 
  private void displayFile(File file){
    try{
      imageInput = ImageIO.read(file);
    }
    catch (IOException exception){
      JOptionPane.showMessageDialog(this, exception);
    }
  }
  
  //If the user selects a menu item that is not for this mode, let them know.
  public void wrongButton(boolean mode){
    if (mode){
      JOptionPane.showMessageDialog(this, "Please set the program to story mode by selecting 'New Story' or 'Load Story.'");
    }
    else{
      JOptionPane.showMessageDialog(this, "Please set the program to art mode by selecting 'Blank Canvas,' 'Import Image,' or 'Load Pixel Art.'");
    }
  }
  
  //Handles what happens when moving to a nonexistant frame.
  public void frameMessage(boolean direction){
    //If the user is going back on the first frame, let them know they can't do that.
    if (direction){
      JOptionPane.showMessageDialog(this, "You are already on the first frame.");
    }
    //If the user is going forward on the last frame, asks them if they want to make a new one.
    else{
      int choice = JOptionPane.showConfirmDialog(this, "You are currently on the last frame. Would you like to create a new one?", "New Frame", JOptionPane.YES_NO_OPTION);
      //If the user does want to make a new frame, asks if they want to keep the same background, foreground, and text.
      if (choice == 0){
        int bg = JOptionPane.showConfirmDialog(this, "Would you like to keep the same background image?", "Background", JOptionPane.YES_NO_OPTION);
        
        if (bg == 1){
          open();
        }
        else{
          imageInput = frames.get(currentFrame-1).background;
        }

        currentFrame++;
        frameLabel.setText(Integer.toString(currentFrame));
        initializeFrame();

        int fg = JOptionPane.showConfirmDialog(this, "Would you like to keep the same foreground objects?", "Foreground", JOptionPane.YES_NO_OPTION);
        if (fg == 0){
          for (int i = 0; i < frames.get(currentFrame-2).foregroundObjects.size(); i++){
            frames.get(currentFrame-1).foregroundObjects.add(frames.get(currentFrame-2).foregroundObjects.get(i));
            frames.get(currentFrame-1).foregroundObjectNames.add(frames.get(currentFrame-2).foregroundObjectNames.get(i));
            frames.get(currentFrame-1).foregroundObjectSize.add(frames.get(currentFrame-2).foregroundObjectSize.get(i));
            frames.get(currentFrame-1).foregroundObjectWidth.add(frames.get(currentFrame-2).foregroundObjectWidth.get(i));
            frames.get(currentFrame-1).foregroundObjectHeight.add(frames.get(currentFrame-2).foregroundObjectHeight.get(i));
            frames.get(currentFrame-1).foregroundObjectX.add(frames.get(currentFrame-2).foregroundObjectX.get(i));
            frames.get(currentFrame-1).foregroundObjectY.add(frames.get(currentFrame-2).foregroundObjectY.get(i));
          }
        }

        drawObjects(currentFrame-1);

        int text = JOptionPane.showConfirmDialog(this, "Would you like to keep the same text?", "Text", JOptionPane.YES_NO_OPTION);
        if (text == 1){
          setCurrentText(true);
        }
        else{
          frames.get(currentFrame-1).text = frames.get(currentFrame-2).text;
          setCurrentText(false);
        }
      }
    }
  }
  
  //The error message that displays if there is an error with making a gif.
  public void gifErrorMessage(){
    JOptionPane.showMessageDialog(this, "There was an error in saving the gif.");
  }
   
  //Configures each frame of the gif.
  private static void configureGIFFrame(IIOMetadata meta, String delayTime, int imageIndex, String disposalMethod, int loopCount){  
    //Get the metadata info for gifs.
    String metaFormat = meta.getNativeMetadataFormatName();

    if (!"javax_imageio_gif_image_1.0".equals(metaFormat)){
       throw new IllegalArgumentException("Unfamiliar gif metadata format: " + metaFormat);
    }

    Node root = meta.getAsTree(metaFormat);

    Node child = root.getFirstChild();
    while (child != null){
      if ("GraphicControlExtension".equals(child.getNodeName())){
        break;
      }

      child = child.getNextSibling();
    }

    IIOMetadataNode gce = (IIOMetadataNode) child;
    gce.setAttribute("userDelay", "FALSE");
    gce.setAttribute("delayTime", delayTime);
    gce.setAttribute("disposalMethod", disposalMethod);
    
    //Sets the initial information the gif needs to work.
    if (imageIndex == 0){
      IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
      IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
      ae.setAttribute("applicationID", "NETSCAPE");
      ae.setAttribute("authenticationCode", "2.0");
      byte[] uo = new byte[] { 0x1, (byte) (loopCount & 0xFF), (byte) ((loopCount >> 8) & 0xFF) };
      ae.setUserObject(uo);
      aes.appendChild(ae);
      root.appendChild(aes);
    }

    try{
       meta.setFromTree(metaFormat, root);
    }
    catch (IIOInvalidTreeException e){
      throw new Error(e);
    }
   }
  
  //Asks the user how they want the art canvas to be initialized.
  public void setCanvas(){
    String result = "";
    pixelSize = 0;
    width = 0;
    height = 0;


    while (pixelSize < 2){
      //Uses the JOptionPane to ask the user how big each pixel should be.
      result = JOptionPane.showInputDialog("How large should each pixel be?");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        pixelSize = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter an integer greater than or equal to 2 if they have not. 
      if (pixelSize < 2){
        JOptionPane.showMessageDialog(this, "Please enter an integer that is greater than or equal to 2.");
      }
    }

    while (width <= 0){
      //Uses the JOptionPane to ask the user how wide they want the image to be.
      result = JOptionPane.showInputDialog("How many pixels wide should the image be?");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        width = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (width <= 0){
        JOptionPane.showMessageDialog(this, "Please enter a positive integer.");
      }
    }

    while (height <= 0){
      //Uses the JOptionPane to ask the user how tall they want the image to be.
      result = JOptionPane.showInputDialog("How many pixels tall should the image be?");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        height = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (height <= 0){
        JOptionPane.showMessageDialog(this, "Please enter a positive integer.");
      }
    }
  }
  
  //Asks the user what they want the brush size to be.
  public void setBrushSize(){
    String result = "";
    brushSize = 0;
    
    while (brushSize < 1){
      //Uses the JOptionPane to ask the user what they want the brush size to be.
      result = JOptionPane.showInputDialog("What should the brush size be?");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        brushSize = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (brushSize < 1){
        JOptionPane.showMessageDialog(this, "Please enter an integer that is greater than or equal to 1.");
      }
    }
  }

  //Asks the user hpw pixelated an imported image should become.
  public void setImport(){
    String result = "";
    pixelSize = 0;

    while (pixelSize < 2){
      //Uses the JOptionPane to ask the user how pixelated they want the image to be.
      result = JOptionPane.showInputDialog("How pixelated should the image become? (For example, entering 2 will average every 2x2 square of pixels in the image.)");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        pixelSize = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter an integer greater than or equal to 2 if they have not. 
      if (pixelSize < 2){
        JOptionPane.showMessageDialog(this, "Please enter an integer that is greater than or equal to 2.");
      }
    }
  }

  //Displaying the BufferedImage.
  public void displayBufferedImage(BufferedImage image){
    ImageIcon icon = new ImageIcon();
    canvas.setIcon(icon);
    icon.setImage(image);
    canvas.repaint();
    this.pack();
    validate();
  }
}

//Holds all of the information for a frame in the story.
class StoryFrame{
  //The full image.
  BufferedImage fullImage;
  //the background image.
  BufferedImage background;
  //The displayed text.
  String text;
  //The foreground objects and corresponding names, "pixel" sizes,
  //widths, heights, and x and y locations on the image.
  ArrayList<int[][]> foregroundObjects;
  ArrayList<String> foregroundObjectNames;
  ArrayList<Integer> foregroundObjectSize;
  ArrayList<Integer> foregroundObjectWidth;
  ArrayList<Integer> foregroundObjectHeight;
  ArrayList<Integer> foregroundObjectX;
  ArrayList<Integer> foregroundObjectY;
  
  //Constructors.
  public StoryFrame(){
    text = "Text appears here.";
    foregroundObjects = new ArrayList<int[][]>(1);
    foregroundObjectNames = new ArrayList<String>(1);
    foregroundObjectSize = new ArrayList<Integer>(1);
    foregroundObjectWidth = new ArrayList<Integer>(1);
    foregroundObjectHeight = new ArrayList<Integer>(1);
    foregroundObjectX = new ArrayList<Integer>(1);
    foregroundObjectY = new ArrayList<Integer>(1);
  }

  public StoryFrame(BufferedImage back){
    background = back;
    text = "Text appears here.";
    foregroundObjects = new ArrayList<int[][]>(1);
    foregroundObjectNames = new ArrayList<String>(1);
    foregroundObjectSize = new ArrayList<Integer>(1);
    foregroundObjectWidth = new ArrayList<Integer>(1);
    foregroundObjectHeight = new ArrayList<Integer>(1);
    foregroundObjectX = new ArrayList<Integer>(1);
    foregroundObjectY = new ArrayList<Integer>(1);
  }
  
  //Adds a new foreground object to the frame.
  public void addObject(int[][] object, String name, int size, int width, int height){
    //Adds all of the necessary information.
    foregroundObjects.add(object);
    foregroundObjectNames.add(name);
    foregroundObjectSize.add(size);
    foregroundObjectWidth.add(width);
    foregroundObjectHeight.add(height);
    
    //Sets its top-right corner to appear in the middle of the image.
    foregroundObjectX.add(fullImage.getWidth()/2);
    foregroundObjectY.add((fullImage.getHeight()-100)/2);
  }
  
  //Removes a foreground object with the given name.
  public boolean removeObject(String name){
    for (int i = 0; i < foregroundObjectNames.size(); i++){
      if (foregroundObjectNames.get(i).equals(name)){
        foregroundObjects.remove(i);
        foregroundObjectNames.remove(i);
        foregroundObjectSize.remove(i);
        foregroundObjectWidth.remove(i);
        foregroundObjectHeight.remove(i);
        foregroundObjectX.remove(i);
        foregroundObjectY.remove(i);
        return true;
      }
    }
    
    //If there is no object by that name, return false.
    return false;
  }
  
  //Mirrors a foreground object.
  public boolean mirrorObject(String name){
    for (int i = 0; i < foregroundObjectNames.size(); i++){
      if (foregroundObjectNames.get(i).equals(name)){
        int[][] newObject = new int[foregroundObjects.get(i).length][foregroundObjects.get(i)[0].length];
        for (int j = 0; j < foregroundObjects.get(i).length; j++){
          for (int k = 0; k < foregroundObjects.get(i)[0].length; k++){
            newObject[j][k] = foregroundObjects.get(i)[foregroundObjects.get(i).length-1-j][k];
          }
        }
        foregroundObjects.set(i, newObject);
        return true;
      }
    }

    //If there is no object by that name, return false.
    return false;
  }
}