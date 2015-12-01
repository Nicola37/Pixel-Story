/*Pixel Story

This is my term project for my Introduction to Digital Arts and Sciences
class at the University of Florida. It is a pixel art and animation
creator which allows users to make pixel art and use it to create stories.

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

  JPanel buttonPanel;
  FlowLayout rightJustified;
  JButton gridButton;
  JButton brushButton;
  JButton eraserButton;

  JPanel colorPanel;
  JPanel titlePanel;
  JPanel sliderPanel;
  JPanel rgbPanel;
  JSlider redSlider;
  JSlider greenSlider;
  JSlider blueSlider;
  JPanel currentColor;

  JPanel framesPanel;
  JButton previousButton;
  JButton nextButton;
  JLabel frameLabel;

  Line2D.Double gridLines;
  int pixelSize;
  int width;
  int height;
  int[][] pixels;
  boolean grid;
  boolean eraser;
  Color gridColor;
  int red;
  int green;
  int blue;
  int brushSize;

  ArrayList<StoryFrame> frames;
  int textColor;
  int textBackgroundColor;
  int currentFrame;
  int fps;

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

    colorPanel = new JPanel();
    titlePanel = new JPanel();
    sliderPanel = new JPanel();
    rgbPanel = new JPanel();
    redSlider = new JSlider(JSlider.VERTICAL, 0, 255, 0);
    greenSlider = new JSlider(JSlider.VERTICAL, 0, 255, 0);
    blueSlider = new JSlider(JSlider.VERTICAL, 0, 255, 0);
    currentColor = new JPanel();

    framesPanel = new JPanel();
    previousButton = new JButton("Previous Frame");
    nextButton = new JButton("Next Frame");
    frameLabel = new JLabel("1", SwingConstants.CENTER);

    gridLines = new Line2D.Double();
    pixelSize = 0;
    width = 0;
    height = 0;
    pixels = null;
    grid = false;
    eraser = false;
    gridColor = new Color(0, 0, 0, 100);
    red = 0;
    green = 0;
    blue = 0;
    brushSize = 1;

    frames = null;
    textColor = 0xFFFFFFFF;
    textBackgroundColor = 0xFF000000;
    currentFrame = 1;
    fps = 1;
	
	  //add a menu to the frame
	  addMenu();

    buttonPanel.setLayout(rightJustified);
    buttonPanel.add(gridButton);
    buttonPanel.add(brushButton);
    buttonPanel.add(eraserButton);

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
    rgbPanel.add(new Label("Red"));
    rgbPanel.add(new Label("Green"));
    rgbPanel.add(new Label("Blue"));
    colorPanel.add(rgbPanel);
    currentColor.setMaximumSize(new Dimension(1000, 4000));
    currentColor.setBackground(Color.BLACK);
    currentColor.setOpaque(true);
    colorPanel.add(currentColor);

    setArtPane();

    framesPanel.setLayout(new GridLayout(1, 5));
    framesPanel.add(previousButton);
    framesPanel.add(new Label(""));
    framesPanel.add(frameLabel);
    framesPanel.add(new Label(""));
    framesPanel.add(nextButton);

    //Adds a mouseListener to see when the mouse is pressed.
    canvas.addMouseListener(new MouseAdapter(){
      public void mousePressed( MouseEvent event ){
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
        eraser = false;
      }
    });

    eraserButton.addActionListener( new ActionListener(){
      public void actionPerformed( ActionEvent event ){
        eraser = true;
      }
    });

    redSlider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        red = ((JSlider)e.getSource()).getValue();
        currentColor.setBackground(new Color((0xFF000000 | (red << 16) | (green << 8) | blue)));
      }
    });

    greenSlider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        green = ((JSlider)e.getSource()).getValue();
        currentColor.setBackground(new Color((0xFF000000 | (red << 16) | (green << 8) | blue)));
      }
    });

    blueSlider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        blue = ((JSlider)e.getSource()).getValue();
        currentColor.setBackground(new Color((0xFF000000 | (red << 16) | (green << 8) | blue)));
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
        saveArtImage();
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
        
      }
    });

    JMenuItem textItem = new JMenuItem("Set Text");
    textItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        if (art == true){
          wrongButton(art);
        }
        else{
          setCurrentText();
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
        
      }
    });

    JMenuItem loadStoryItem = new JMenuItem("Load Created Story");
    loadStoryItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event){
        
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

    frames.get(currentFrame-1).setBackground(imageInput);

    g2d.drawImage(imageInput, null, 0, 0);
    frames.get(currentFrame-1).setFullImage(image);

    displayBufferedImage(image);
  }

  public void setCurrentText(){
    String newText = "";

    newText = JOptionPane.showInputDialog("What would you like the text on this frame to be?");
    frames.get(currentFrame-1).text = newText;

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
    fps = 0;

    while (fps < 1){
      //Uses the JOptionPane to ask the user what the fps should be.
      result = JOptionPane.showInputDialog("Please enter how many frames per second you would like the story to go at.");
    
      //Tries and catches an exception in case the user enters something other than an int.
      try{
        fps = Integer.parseInt(result);
      }
      catch (NumberFormatException exception){
      }
      
      //Lets the user know that they have to enter a positive integer if they have not. 
      if (fps < 1){
        JOptionPane.showMessageDialog(this, "Please enter an integer that is greater than 0.");
      }
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

    frames.get(currentFrame-1).setFullImage(image);
  }

  public void setFrame(){
    image = frames.get(currentFrame-1).fullImage;
    g2d = (Graphics2D) image.createGraphics();
    displayBufferedImage(image);
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

        //int fg = JOptionPane.showConfirmDialog(this, "Would you like to keep the same foreground objects?", "Foreground", JOptionPane.YES_NO_OPTION);

        currentFrame++;
        frameLabel.setText(Integer.toString(currentFrame));
        initializeFrame();

        int text = JOptionPane.showConfirmDialog(this, "Would you like to keep the same text?", "Text", JOptionPane.YES_NO_OPTION);
        if (text == 1){
          setCurrentText();
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
  ArrayList<int> foregroundObjectSize;
  ArrayList<int> foregroundObjectWidth;
  ArrayList<int> foregroundObjectHeight;
  ArrayList<int> foregroundObjectX;
  ArrayList<int> foregroundObjectY;

  public StoryFrame(BufferedImage back){
    background = back;
    text = "Text appears here.";
    foregroundObjects = new ArrayList<int[][]>(1);
    foregroundObjectNames = new ArrayList<String>(1);
    foregroundObjectSize = new ArrayList<int>(1);
    foregroundObjectWidth = new ArrayList<int>(1);
    foregroundObjectHeight = new ArrayList<int>(1);
    foregroundObjectX = new ArrayList<int>(1);
    foregroundObjectY = new ArrayList<int>(1);
  }

  public void setFullImage(BufferedImage img){
    fullImage = img;
  }

  public void setBackground(BufferedImage back){
    background = back;
  }

  public void setText(String txt){
    text = txt;
  }

  public void addForegroundObject(int[][] object, String name, int size, int width, int height){
    foregroundObjects.add(object);
    foregroundObjectNames.add(name);
    foregroundObjectSize.add(size);
    foregroundObjectWidth.add(width);
    foregroundObjectHeight.add(height);

    foregroundObjectX.add(fullImage.getWidth()/2);
    foregroundObjectY.add(fullImage.getHeight()/2);
  }

  public void removeForegroundObject(String name){
    for (int i = 0; i < foregroundObjectNames.size(); i++){
      if (foregroundObjectNames.get(i).equals(name)){
        foregroundObjects.remove(i);
        foregroundObjectNames.remove(i);
        foregroundObjectSize.remove(i);
        foregroundObjectWidth.remove(i);
        foregroundObjectHeight.remove(i);
        foregroundObjectX.remove(i);
        foregroundObjectY.remove(i);
        break;
      }
    }
  }
}