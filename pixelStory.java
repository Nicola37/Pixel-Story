/*Pixel Story

This is my term project for my Introduction to Digital Arts and Sciences
class at the University of Florida. It is a pixel art and animation
creator which allows users to make pixel art and use it to create stories.

Thanks to Dave Small who taught us how to do most of this,
as well as anonymous java-gaming.org forum people, for helping me implement animated gifs in java.

by Nicola Frachesen*/

//Necessary imports.
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.Random;
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
  boolean art;

  BufferedImage image;
  BufferedImage imageInput;
  Graphics2D g2d;
  JLabel canvas;
  JPanel canvasPanel;
  //The java file chooser
  private final JFileChooser chooser;
  //The timer for timed animation.
  Timer timer;


  JPanel buttonPanel;
  FlowLayout rightJustified;
  JButton gridButton;
  JButton brushButton;
  JButton eraserButton;
  JButton getColorButton;
  JButton smallButton;
  JButton bigButton;

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

  JPanel framesPanel;
  JButton previousButton;
  JButton nextButton;
  JLabel frameLabel;
  JButton playButton;
  JButton stopButton;

  Line2D.Double gridLines;
  int pixelSize;
  int width;
  int height;
  int[][] pixels;
  boolean grid;
  boolean eraser;
  boolean getColor;
  Color gridColor;
  int red;
  int green;
  int blue;
  int brushSize;

  ArrayList<StoryFrame> frames;
  int textColor;
  int textBackgroundColor;
  int currentFrame;
  double fps;
  int currentObject;

  //constructor
  public ImageFrame(int w, int h){
    //set up the frame's attributes
    this.setTitle("Pixel Story");
    this.setSize(w, h);
    
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

            int gotColor = pixels[(x/pixelSize)][(y/pixelSize)];
            red = (gotColor >>> 16) & 0x000000FF;
            green = (gotColor >>> 8) & 0x000000FF;
            blue = gotColor & 0x000000FF;

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
            
            if (!eraser){
              for (int i = 0; i < brushSize; i++){
                for (int j = 0; j < brushSize; j++){

                  if(((x/pixelSize)+i < width) && ((y/pixelSize)+j < height)){
                    pixels[(x/pixelSize)+i][(y/pixelSize)+j] = (0xFF000000 | (red << 16) | (green << 8) | blue);
                  }

                  //Set the color and draw the cell.
                  g2d.setColor(new Color(pixels[x/pixelSize][y/pixelSize]));
                  g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                }
              }
            }
            else{
              for (int i = 0; i < brushSize; i++){
                for (int j = 0; j < brushSize; j++){

                  if(((x/pixelSize)+i < width) && ((y/pixelSize)+j < height)){
                    pixels[(x/pixelSize)+i][(y/pixelSize)+j] = 0xFFFFFFFF;
                  }

                  //Set the color and draw the cell.
                  g2d.setColor(new Color(pixels[x/pixelSize][y/pixelSize]));
                  g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                }
              }
            }

            if (grid == true){
              drawGrid();
            }

            displayBufferedImage(image);
          }
        }
        else{
            currentObject = objectCheck(event.getX(), event.getY());
        }
      }
    });
    
    //Adds a mouseListener to see when the mouse is pressed.
    canvas.addMouseListener(new MouseAdapter(){
      public void mouseExited( MouseEvent event ){
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
              for (int i = 0; i < brushSize; i++){
                for (int j = 0; j < brushSize; j++){
                  //Set the color and draw the cell.
                  g2d.setColor(new Color(0xFF000000 | (red << 16) | (green << 8) | blue));
                  g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
                }
              }
            }
            else{
              for (int i = 0; i < brushSize; i++){
                for (int j = 0; j < brushSize; j++){
                  //Set the color and draw the cell.
                  g2d.setColor(new Color(0xFFFFFFFF));
                  g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
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
        if (art && !getColor){
          if (grid == true){
            drawGrid();
          }
          //Get the x and y position within an accuracy of 8.
          int x = (event.getPoint().x - (event.getPoint().x % pixelSize));
          int y = (event.getPoint().y - (event.getPoint().y % pixelSize));
          
          if (!eraser){
            for (int i = 0; i < brushSize; i++){
              for (int j = 0; j < brushSize; j++){

                if(((x/pixelSize)+i < width) && ((y/pixelSize)+j < height)){
                  pixels[(x/pixelSize)+i][(y/pixelSize)+j] = (0xFF000000 | (red << 16) | (green << 8) | blue);
                }

                //Set the color and draw the cell.
                g2d.setColor(new Color(pixels[x/pixelSize][y/pixelSize]));
                g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
              }
            }
          }
          else{
            for (int i = 0; i < brushSize; i++){
              for (int j = 0; j < brushSize; j++){

                if(((x/pixelSize)+i < width) && ((y/pixelSize)+j < height)){
                  pixels[(x/pixelSize)+i][(y/pixelSize)+j] = 0xFFFFFFFF;
                }

                //Set the color and draw the cell.
                g2d.setColor(new Color(pixels[x/pixelSize][y/pixelSize]));
                g2d.fillRect(x+(i*pixelSize), y+(j*pixelSize), pixelSize, pixelSize);
              }
            }
          }

          if (grid == true){
            drawGrid();
          }

          displayBufferedImage(image);
        }
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

    timer = new Timer((int)((1.0/fps)*1000.0), new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        timer.stop();
        if (currentFrame == frames.size()){
          currentFrame = 1;
        }
        else{
          currentFrame++;
        }
        frameLabel.setText(Integer.toString(currentFrame));
        setFrame();
        timer.restart();
      }
    }); 

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

    brushButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        getColor = false;
        eraser = false;
      }
    });

    eraserButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        getColor = false;
        eraser = true;
      }
    });

    getColorButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        getColor = true;
      }
    });

    smallButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (pixelSize > 2){
          pixelSize--;
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

    bigButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        pixelSize++;
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

    nextButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (currentFrame == frames.size()){
          frameMessage(false);
          displayBufferedImage(image);
        }
        else{
          currentFrame++;
          frameLabel.setText(Integer.toString(currentFrame));
          setFrame();
        }
      }
    });

    playButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        if (!timer.isRunning()){
          timer.start();
        }
      }
    });

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
          saveStoryMovie();
        }
      }
    });
  
    //Add the menu options.
    storyMenu.add(storyItem);
    storyMenu.add(backgroundItem);
    storyMenu.add(addItem);
    storyMenu.add(removeItem);
    storyMenu.add(textItem);
    storyMenu.add(fpsItem);
    storyMenu.add(saveStoryItem);
    storyMenu.add(loadStoryItem);
    storyMenu.add(saveStoryImageItem);
    storyMenu.add(saveStoryMovieItem);
  
    //Attach the menu to a menu bar.
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(artMenu);
    menuBar.add(storyMenu);
    this.setJMenuBar(menuBar);
  }

  private void setArtPane(){
    this.getContentPane().removeAll();

    getContentPane().add(canvasPanel, BorderLayout.CENTER);

    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    getContentPane().add(colorPanel, BorderLayout.EAST);

    this.repaint();
    validate();
  }

  private void setStoryPane(){
    this.getContentPane().removeAll();

    getContentPane().add(canvasPanel, BorderLayout.CENTER);

    getContentPane().add(framesPanel, BorderLayout.SOUTH);

    this.repaint();
    validate();
  }

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

  public void importImage(){
    setImport();

    open();

    width = imageInput.getWidth()/pixelSize;
    height = imageInput.getHeight()/pixelSize;
    image = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_INT_ARGB);
    g2d = (Graphics2D) image.createGraphics();
    pixels = new int[width][height];

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

  public void saveArt(){
    String fileName = "";

    while (fileName.equals("")){
      //Uses the JOptionPane to ask the user how wide they want the image to be.
      fileName = JOptionPane.showInputDialog("What would you like to name the saved file? (Please do not include the file extension in the name.)");
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (fileName.equals("")){
        JOptionPane.showMessageDialog(this, "Please enter a name for the file.");
      }
    }

    String savePixelSize = Integer.toString(pixelSize);
    String saveWidth = Integer.toString(width);
    String saveHeight = Integer.toString(height);
    
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

  public void loadArt(){
    JOptionPane.showMessageDialog(this, "Please select a Pixel Story art file (.pxsa) to load.");

    File file = getFile();

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

  public void saveArtImage(){
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

  public void newStory(){
    frames = new ArrayList<StoryFrame>(1);

    JOptionPane.showMessageDialog(this, "Please select an image (png or jpeg) to use as the background.");
    open();

    setFPS();

    currentFrame = 1;

    initializeFrame();

    displayBufferedImage(image);
  }

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

  public void addForegroundObject(){
    JOptionPane.showMessageDialog(this, "Please select a Pixel Story art file (.pxsa) to add as a foreground object.");

    File file = getFile();

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
        
        String n = JOptionPane.showInputDialog("What would you like the name of this object to be?");

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

  public void removeForegroundObject(){
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

      question.append("\nWhich one would you like to delete?");

      String remove = JOptionPane.showInputDialog(question.toString());

      boolean done = frames.get(currentFrame-1).removeObject(remove);
      
      if (done){
        g2d.drawImage(frames.get(currentFrame-1).background, null, 0, 0);
        setCurrentText(false);
        drawObjects(currentFrame-1);
        frames.get(currentFrame-1).fullImage = image;
        displayBufferedImage(image);
      }
      else{
        JOptionPane.showMessageDialog(this, "Sorry, but that does not seem to be the name of an object.");
      }
    }
  }

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

  public void setFPS(){
    String result = "";
    fps = 0.0;

    while (fps <= 0.0){
      //Uses the JOptionPane to ask the user what the fps should be.
      result = JOptionPane.showInputDialog("Please enter how many frames per second you would like the story to go at.");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        fps = Double.parseDouble(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (fps < 0.0){
        JOptionPane.showMessageDialog(this, "Please enter a number that is greater than 0.");
      }
    }

    timer.setInitialDelay((int)((1.0/fps)*1000.0));
  }

  public void saveStory(){
    String fileName = "";

    while (fileName.equals("")){
      fileName = JOptionPane.showInputDialog("What would you like to name the saved file? (Please do not include the file extension in the name.)");
      
      if (fileName.equals("")){
        JOptionPane.showMessageDialog(this, "Please enter a name for the file.");
      }
    }
    
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

    public void loadStory(){
    JOptionPane.showMessageDialog(this, "Please select a Pixel Story story file (.pxss) to load.");

    File file = getFile();

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

  public void saveStoryMovie(){
    String fileName;
    fileName = JOptionPane.showInputDialog("What would you like the name of the file to be? (Please do not enter the file extension. e.g. 'apple' not 'apple.gif')");

    ArrayList<GifFrame> images = new ArrayList<GifFrame>();
    for (int i = 0; i < frames.size(); i++){
      images.add(new GifFrame(frames.get(i).fullImage, (long) ((1/fps)*1000)));
    }
    
    try{
      OutputStream oS = new FileOutputStream(fileName+".gif");
    
      ImageUtil gifMaker = new ImageUtil();
    
      gifMaker.saveAnimatedGIF(oS, images, 0);
    }
    catch (Exception e){
    }
  }

  public void initializeFrame(){
    frames.add(new StoryFrame(imageInput));

    image = new BufferedImage(imageInput.getWidth(), imageInput.getHeight()+100, BufferedImage.TYPE_INT_ARGB);
    g2d = (Graphics2D) image.createGraphics();

    g2d.drawImage(imageInput, null, 0, 0);
    g2d.setColor(new Color(textBackgroundColor));
    g2d.fill(new Rectangle(0, imageInput.getHeight()-1, image.getWidth(), 100));
    
    g2d.setFont(new Font("Arial", Font.BOLD, 20));
    FontMetrics fm = g2d.getFontMetrics();
    g2d.setColor(new Color(textColor));
    g2d.drawString(frames.get(currentFrame-1).text, (image.getWidth()/2)-(fm.stringWidth(frames.get(currentFrame-1).text)/2), image.getHeight()-50);

    frames.get(currentFrame-1).fullImage = image;
  }

  public void setFrame(){
    image = frames.get(currentFrame-1).fullImage;
    g2d = (Graphics2D) image.createGraphics();
    displayBufferedImage(image);
  }

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

  public void wrongButton(boolean mode){
    if (mode){
      JOptionPane.showMessageDialog(this, "Please set the program to story mode by selecting 'New Story.'");
    }
    else{
      JOptionPane.showMessageDialog(this, "Please set the program to art mode by selecting 'Blank Canvas,' 'Import Image,' or 'Load Pixel Art.'");
    }
  }

  public void frameMessage(boolean direction){
    if (direction){
      JOptionPane.showMessageDialog(this, "You are already on the first frame.");
    }
    else{
      int choice = JOptionPane.showConfirmDialog(this, "You are currently on the last frame. Would you like to create a new one?", "New Frame", JOptionPane.YES_NO_OPTION);
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

  public void setCanvas(){
    String result = "";
    pixelSize = 0;
    width = 0;
    height = 0;


    while (pixelSize < 2){
      //Uses the JOptionPane to ask the user how wide they want the image to be.
      result = JOptionPane.showInputDialog("How large should each pixel be?");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        pixelSize = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (pixelSize < 2){
        JOptionPane.showMessageDialog(this, "Please enter an integer that is greater than 2.");
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
      //Uses the JOptionPane to ask the user how wide they want the image to be.
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

  public void setBrushSize(){
    String result = "";
    brushSize = 0;
    
    while (brushSize < 1){
      //Uses the JOptionPane to ask the user how wide they want the image to be.
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

  public void setImport(){
    String result = "";
    pixelSize = 0;

    while (pixelSize < 2){
      //Uses the JOptionPane to ask the user how wide they want the image to be.
      result = JOptionPane.showInputDialog("How pixelated should the image become? (For example, entering 2 will average every 2x2 square of pixels in the image.)");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        pixelSize = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (pixelSize < 2){
        JOptionPane.showMessageDialog(this, "Please enter an integer that is greater than 2.");
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

class StoryFrame{
  BufferedImage fullImage;
  BufferedImage background;
  String text;
  ArrayList<int[][]> foregroundObjects;
  ArrayList<String> foregroundObjectNames;
  ArrayList<Integer> foregroundObjectSize;
  ArrayList<Integer> foregroundObjectWidth;
  ArrayList<Integer> foregroundObjectHeight;
  ArrayList<Integer> foregroundObjectX;
  ArrayList<Integer> foregroundObjectY;

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

  public void addObject(int[][] object, String name, int size, int width, int height){
    foregroundObjects.add(object);
    foregroundObjectNames.add(name);
    foregroundObjectSize.add(size);
    foregroundObjectWidth.add(width);
    foregroundObjectHeight.add(height);

    foregroundObjectX.add(fullImage.getWidth()/2);
    foregroundObjectY.add((fullImage.getHeight()-100)/2);
  }

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
    return false;
  }
}

class GifFrame
{
   public static final String NONE                = "none";
   public static final String DO_NOT_DISPOSE      = "doNotDispose";
   public static final String RESTORE_TO_BGCOLOR  = "restoreToBackgroundColor";
   public static final String RESTORE_TO_PREVIOUS = "restoreToPrevious";

   public final BufferedImage img;
   public final long          delay; // in millis
   public final String        disposalMethod;

   public GifFrame(BufferedImage img, long delay)
   {
      this(img, delay, NONE);
   }

   public GifFrame(BufferedImage img, long delay, String disposalMethod)
   {
      this.img = img;
      this.delay = delay;
      this.disposalMethod = disposalMethod;
   }
}

class ImageUtil
{
   public static BufferedImage convertRGBAToGIF(BufferedImage src, int transColor)
   {
      BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
      Graphics g = dst.getGraphics();
      g.setColor(new Color(transColor));
      g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
      {
         IndexColorModel indexedModel = (IndexColorModel) dst.getColorModel();
         WritableRaster raster = dst.getRaster();
         int sample = raster.getSample(0, 0, 0);
         int size = indexedModel.getMapSize();
         byte[] rr = new byte[size];
         byte[] gg = new byte[size];
         byte[] bb = new byte[size];
         indexedModel.getReds(rr);
         indexedModel.getGreens(gg);
         indexedModel.getBlues(bb);
         IndexColorModel newModel = new IndexColorModel(8, size, rr, gg, bb, sample);
         dst = new BufferedImage(newModel, raster, dst.isAlphaPremultiplied(), null);
      }
      dst.createGraphics().drawImage(src, 0, 0, null);
      return dst;
   }

   public static void saveAnimatedGIF(OutputStream out, ArrayList<GifFrame> frames, int loopCount) throws Exception
   {
      ImageWriter iw = ImageIO.getImageWritersByFormatName("gif").next();

      ImageOutputStream ios = ImageIO.createImageOutputStream(out);
      iw.setOutput(ios);
      iw.prepareWriteSequence(null);

      int p = 0;
      for (GifFrame frame : frames)
      {
         ImageWriteParam iwp = iw.getDefaultWriteParam();
         IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(frame.img), iwp);
         ImageUtil.configureGIFFrame(metadata, String.valueOf(frame.delay / 10L), p++, frame.disposalMethod, loopCount);
         IIOImage ii = new IIOImage(frame.img, null, metadata);
         iw.writeToSequence(ii, null);
      }

      iw.endWriteSequence();
      ios.close();
   }

   private static void configureGIFFrame(IIOMetadata meta, String delayTime, int imageIndex, String disposalMethod, int loopCount)
   {
      String metaFormat = meta.getNativeMetadataFormatName();

      if (!"javax_imageio_gif_image_1.0".equals(metaFormat))
      {
         throw new IllegalArgumentException("Unfamiliar gif metadata format: " + metaFormat);
      }

      Node root = meta.getAsTree(metaFormat);

      Node child = root.getFirstChild();
      while (child != null)
      {
         if ("GraphicControlExtension".equals(child.getNodeName()))
            break;
         child = child.getNextSibling();
      }

      IIOMetadataNode gce = (IIOMetadataNode) child;
      gce.setAttribute("userDelay", "FALSE");
      gce.setAttribute("delayTime", delayTime);
      gce.setAttribute("disposalMethod", disposalMethod);

      if (imageIndex == 0)
      {
         IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
         IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
         ae.setAttribute("applicationID", "NETSCAPE");
         ae.setAttribute("authenticationCode", "2.0");
         byte[] uo = new byte[] { 0x1, (byte) (loopCount & 0xFF), (byte) ((loopCount >> 8) & 0xFF) };
         ae.setUserObject(uo);
         aes.appendChild(ae);
         root.appendChild(aes);
      }

      try
      {
         meta.setFromTree(metaFormat, root);
      }
      catch (IIOInvalidTreeException e)
      {
         throw new Error(e);
      }
   }
}