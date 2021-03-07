package Assignment2;

//My Student ID is 1906001, hope you enjoy my game!
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Scanner;

//This is the executable class, it sets the frame for the games main menu.
public class CE203_1906001_Ass2 {
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();

        frame.setTitle("SID: 1906001");
        frame.setVisible( true );
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}

//In this class I define all the panels, buttons, labels, etc. that are put on the main menu frame, and also set where
// the buttons will take the player.
class MainFrame extends JFrame {
    JPanel imagePanel = new JPanel();
    JPanel levelPanel = new JPanel(new GridLayout(1, 5));
    JPanel optionPanel = new JPanel(new GridLayout(1, 2));

    public MainFrame(){
        super();

        JButton level1 = new JButton("Level 1");
        JButton level2 = new JButton("Level 2");
        JButton level3 = new JButton("level 3");
        JButton level4 = new JButton("Level 4");
        JButton level5 = new JButton("Level 5");
        JButton helpButton = new JButton("Description");
        JButton exitGame = new JButton("Exit Game");
        JButton scoreBoard = new JButton("Scores");

        URL resource = CE203_1906001_Ass2.class.getResource("titleScreen.png");
        imagePanel.add(new JLabel(new ImageIcon(resource)));
        levelPanel.add(level1);
        levelPanel.add(level2);
        levelPanel.add(level3);
        levelPanel.add(level4);
        levelPanel.add(level5);
        optionPanel.add(helpButton);
        optionPanel.add(scoreBoard);
        optionPanel.add(exitGame);

        add(imagePanel, BorderLayout.NORTH);
        add(levelPanel, BorderLayout.CENTER);
        add(optionPanel, BorderLayout.SOUTH);

        setSize(900, 600);

        level1.addActionListener(new LevelHandler(1));
        level2.addActionListener(new LevelHandler(2));
        level3.addActionListener(new LevelHandler(3));
        level4.addActionListener(new LevelHandler(4));
        level5.addActionListener(new LevelHandler(5));
        helpButton.addActionListener(new HelpHandler());
        exitGame.addActionListener(new ExitHandler(this));
        scoreBoard.addActionListener(new ScoreHandler());
    }
}

//This class sets the result of choosing any level from the menu, it sets up a new frame, and does all the neccessary set up as
// far as the levels frame is concerned.
class LevelHandler extends JFrame implements ActionListener {
    int level;

    LevelHandler(int levelNumber) {
        level = levelNumber;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Upon making the frame it is also checked whether or not the scoreBoard.txt exists and it it does not, creates it.
        fileCheck();
        setTitle("Level " + level);
        setVisible(true);
        setSize(800, 800);
        setResizable(false);
        add(new gameLogic(this, level));
    }

    // This function checks if scoreBoard.txt exists and if it does not then it creates it. The function also sets up the file with all the
    // neccessary phrases so when scores are added then the program knows where to put them.
    private void fileCheck() {
        try {
            File scores = new File("scoreBoard.txt");
            if (scores.createNewFile()) {
                int counting = 1;
                System.out.println("File created: " + scores.getName());
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(scores, true));
                    for (int i = 0; i < 5; i++) {
                        writer.write("Level " + counting + " scores:");
                        writer.newLine();
                        writer.newLine();
                        counting++;
                    }
                    writer.close();
                } catch (IOException a) {
                    a.printStackTrace();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

//This class is where everything concerning the game itself happens. It takes in a level number and according to that draws all the
// needed objects and then controls the objects movement and all the other rules concerning the game.
class gameLogic extends JPanel{
    int level;
    JFrame levelFrame;
    public boolean gotHandWash = false;
    int handWashCount = 0;
    boolean gotMask = false;
    int maskCount = 0;
    boolean gotCovidTest = false;
    int covidTestCount = 0;
    int starter = 1;
    private int totalCells = 0;
    private int cellNumGreen = 0;
    private int cellNumRed = 0;
    private final int BALL_OUT = 780;
    int gameScore = 0;
    int lives = 2;
    private boolean gameInProgress = true;
    private boolean playerWon = false;
    private Timer gameTimer;
    private Bar bar;
    private Ball ball;
    private greenCell[] greenCells;
    private redCell[] redCells;
    String momentText = "Press left mouse button on the game window to begin the game!";

    //This function takes the required details from the levelhandler class and also sets up a mouse listener for the panel.
    //The mouse listener is needed for the starting the actual game, it also lets the play start the game again after losing
    //a body cell(the balls life).
    gameLogic(JFrame levelF, int chosenLevel){
        level = chosenLevel;
        levelFrame = levelF;

        startGame();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1){
                    if(starter == 3 || starter == 5){
                        momentText = "";
                        gameInProgress = true;

                        gameTimer = new Timer(10, new runningGame());
                        gameTimer.start();
                        starter++;
                    }
                    if(starter == 1){
                        momentText = "";
                        logicStart();
                        starter++;
                    }
                }
            }
        });
    }

    //startGame sets up a keyboard listener on the panel for the bar movement for the player, and also draws all the objects required
    //for the level.
    private void startGame(){
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                bar.keyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                bar.keyPressed(e);
            }
        });

        setLayout(null);
        setFocusable(true);

        bar = new Bar();
        ball = new Ball();
        cellLayout();
    }

    //This method starts up the timer, which keeps the game going, once the timer is stopped, the game stops.
    private void logicStart(){
        gameTimer = new Timer(1, new runningGame());
        gameTimer.start();
    }

    //The paint component is overridden to be set to draw higher quality versions of the images required for the virus cells of the game
    // since these use actual images instead of drawing them from scratch. It also sets up the sentences for the game, which are the
    //current score,the ball lives and an extra line in case of special messages, like a power up activation. Then the paint component
    // check if the game is in progress, if it is then it draws all the objects, if not then depending on the player lives it either ends
    // the game or waits for the player to click the window.
    // The idea for using renderingHints I got from here: https://stackoverflow.com/questions/31581374/image-rendering-on-java-jframe
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.drawString("Your score: " + gameScore, 10, 500);
        g2d.drawString("Cures left: " + lives, 10, 520);
        g2d.drawString(momentText, 10, 540);

        if (gameInProgress == true) {
            drawShapes(g2d);
        } else {
            if(lives == -1){
                gameEnd(false);
            } else{
                gameInProgress = true;
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }

    //This method is for drawing all the shapes in a level. It checks if the handwash power is active and according to that changes the
    // drawn bars width. This method also always checks whether cells have been destroyed, if they are destroyed then it won't redraw them.
    private void drawShapes(Graphics2D g2d){
        if(gotHandWash == true){
            g2d.drawRoundRect(bar.getX(), bar.getY(), bar.getShapeWidth()*2 ,bar.getShapeHeight(), bar.getBarArcWidth(), bar.getBarArcHeight());
            g2d.setColor(Color.BLUE);
        }else{
            g2d.drawRoundRect(bar.getX(), bar.getY(), bar.getShapeWidth() ,bar.getShapeHeight(), bar.getBarArcWidth(), bar.getBarArcHeight());
            g2d.setColor(Color.BLUE);
        }
        g2d.fillOval(ball.getX(),ball.getY(),ball.getShapeWidth(),ball.getShapeHeight());
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < cellNumGreen; i++) {
            if(greenCells[i].isDestruct() == false){
                g2d.drawImage(greenCells[i].getImage(), greenCells[i].getX(), greenCells[i].getY(), greenCells[i].getShapeWidth(),
                        greenCells[i].getShapeHeight(), this);
            }
        }
        for (int i = 0; i < cellNumRed; i++) {
            if(redCells[i].isDestruct() == false){
                g2d.drawImage(redCells[i].getImage(), redCells[i].getX(), redCells[i].getY(), redCells[i].getShapeWidth(),
                        redCells[i].getShapeHeight(), this);
            }
        }
    }

    //This where the levels differentiate from each other. Depending on which level was chosen this method sets up all the positions on
    // panel for the cells, so when they are drawn in the drawShapes() function then the function knows where to draw them. The more
    // complex the level design is the more specific the cell placement has to be, as levels 4 and 5 show. Depending on the cell colour
    // this method sets those cells up into their specific arrays with the required coordinates. It also sets up the total number of cells
    // which is required later.
    public void cellLayout(){
        int gCell = 0;
        int rCell = 0;
        if(level == 1){
            totalCells = 80;
            cellNumGreen = 80;
            greenCells = new greenCell[cellNumGreen];

            for(int i = 0; i<8; i++){
                for(int j = 0; j<10; j++){
                    greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                    gCell++;
                }
            }
        }
        if(level == 2){
            totalCells = 40;
            cellNumGreen = 40;
            greenCells = new greenCell[cellNumGreen];

            for(int i = 0; i<8; i++){
                if(i % 2 != 0){
                    for(int j = 1; j<11; j += 2){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                }else{
                    for(int j = 0; j<10; j += 2){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                }
            }
        }
        if(level == 3){
            totalCells = 80;
            cellNumGreen = 48;
            cellNumRed = 32;
            greenCells = new greenCell[cellNumGreen];
            redCells = new redCell[cellNumRed];

            for(int i = 1; i<7; i++){
                for(int j = 1; j<9; j++){
                    greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                    gCell++;
                }
            }
            for(int i = 0; i<8; i++){
                for(int j = 0; j<10; j++){
                    if(i == 0 || i == 7){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    } else if(j == 0 || j == 9){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                }
            }
        }
        if(level == 4){
            totalCells = 28;
            cellNumGreen = 24;
            cellNumRed = 4;
            greenCells = new greenCell[cellNumGreen];
            redCells = new redCell[cellNumRed];

            for(int i = 0; i<8; i++){
                for(int j = 0; j<10; j++){
                    if((i == 0 || i== 7) && (j == 0 || j == 1 || j == 8 || j==9)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if((i == 1 || i== 6) && (j == 0 || j==9)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if((i == 2 || i== 5) && (j > 2 && j < 7)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if((i == 3 || i== 4) && (j == 3 || j == 6)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                }
            }

            for(int i = 0; i<8; i++){
                for(int j = 0; j<10; j++){
                    if((i == 3 || i== 4) && (j == 4 || j == 5)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                }
            }
        }
        if(level == 5){
            totalCells = 37;
            cellNumGreen = 19;
            cellNumRed = 18;
            greenCells = new greenCell[cellNumGreen];
            redCells = new redCell[cellNumRed];

            for(int i = 0; i<8; i++){
                for(int j = 0; j<10; j++){
                    if(i == 0 && (j == 0 || j == 4)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                    if(i == 1 && (j < 2 || j == 3 || j == 4)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                    if(i == 2 && (j == 0 || j == 2 || j == 4)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                    if(i == 3 && j == 5){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                    if((i == 4 || i == 6) && (j == 5 || j == 7)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                    if(i == 5 && (j == 5 || j == 6)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                    if(i == 7 && (j == 5 || j == 8)){
                        redCells[rCell] = new redCell(j * 74 + 30, i * 22 + 50);
                        rCell++;
                    }
                }
            }

            for(int i = 0; i<8; i++){
                for(int j = 0; j<10; j++){
                    if(i == 0 && j == 7){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if(i == 1 && (j == 6 || j == 8)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if(i == 2 && (j == 6 || j == 7 || j == 8)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if(i == 3 && (j == 1 || j == 2 || j == 3 || j == 6 || j == 8)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if(i == 4 && j == 1){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if((i == 5 || i == 7) && (j == 1 || j == 2 || j == 3)){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                    if(i == 6 && j == 3){
                        greenCells[gCell] = new greenCell(j * 74 + 30, i * 22 + 50);
                        gCell++;
                    }
                }
            }
        }
    }

    //This is where the program ends up in once the player loses all of their lives or destroys all the cells. This sets up
    // a new frame which either congratulates or lets the player know they have lost. It also shows the players end score and
    // gives a button to press. If the button is pressed or the small frame is closed from the X in the corner, then both that
    //frame and the level frame close. This also activates the fileUpdate() method which stores the game score into the
    // scoreBoard.txt file.
    private void gameEnd(boolean playerStatus){
        fileUpdate(level, gameScore);
        JFrame endScreen = new JFrame();
        endScreen.setVisible(true);
        endScreen.setSize(250, 150);
        JPanel end = new JPanel(new GridLayout(3, 1));

        if(playerStatus == true){
            JLabel endText = new JLabel("You Won!", SwingConstants.CENTER);
            end.add(endText);
        } else{
            JLabel endText = new JLabel("Game Over!", SwingConstants.CENTER);
            end.add(endText);
        }
        JLabel endScore = new JLabel("Your end score is: " + gameScore, SwingConstants.CENTER);
        JButton ok = new JButton("Ok");

        end.add(endScore);
        end.add(ok);
        endScreen.add(end);

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                endScreen.dispose();
                levelFrame.dispose();
            }
        });
        endScreen.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) { }
            @Override
            public void windowClosing(WindowEvent e) {
                endScreen.dispose();
                levelFrame.dispose();
            }
            @Override
            public void windowClosed(WindowEvent e) { }
            @Override
            public void windowIconified(WindowEvent e) { }
            @Override
            public void windowDeiconified(WindowEvent e) { }
            @Override
            public void windowActivated(WindowEvent e) { }
            @Override
            public void windowDeactivated(WindowEvent e) { }
        });
    }

    // This is where the physics of the game gets calculated throughout the game.
    // This code was a big inspiration for my hitbox calculations:
    // https://stackoverflow.com/questions/28397996/java-programming-breakout-game-ball-physics-not-behaving-as-expected
    void hitBox(){
        //Once the ball goes under the players bar lifeLost() is activated.
        if(ball.checkRect(false).getMaxY() > BALL_OUT){
            lifeLost();
        }

        int j = 0;

        //The next 2 for loops check how many cells have been destroyed overall.
        for(int i = 0; i < cellNumGreen; i++){
            if(greenCells[i].isDestruct() == true){
                j++;
            }
        }

        if(cellNumRed != 0){
            for(int i = 0; i < cellNumRed; i++){
                if(redCells[i].isDestruct() == true) {
                    j++;
                }
            }
        }

        //If all the cells are destroyed the game is stopped  and the gameEnd receives a parameter that lets it know, then the player won.
        if(j == totalCells){
            playerWon = true;
            gameTimer.stop();
            gameEnd(true);
        }

        //This if statement checks if the ball touches the player bar, and redirects the ball depending on where the ball landed on the bar.
        //The if statements within the ball landing checks also check if the mask or Covid Test power up is active. If they are then the balls
        //speed is changed.
        if((ball.checkRect(false)).intersects(bar.checkRect(gotHandWash))){
            int ballPlace = (int) ball.checkRect(false).getMinX();
            int barPlace = (int) bar.checkRect(false).getMinX();
            int place1 = barPlace + 18;
            int place2 = barPlace + 36;
            int place3 = barPlace + 54;
            int place4 = barPlace + 72;

            if(gotHandWash == true){
                place1 = barPlace + 36;
                place2 = barPlace + 72;
                place3 = barPlace + 104;
                place4 = barPlace + 140;
            }

            if(ballPlace < place1){
                if(gotMask == true){
                    ball.putDirectionX(-1);
                    ball.putDirectionY(-1);
                    mask(maskCount);
                } else if(gotCovidTest == true){
                    ball.putDirectionX(-6);
                    ball.putDirectionY(-6);
                    covidTest(covidTestCount);
                } else{
                    ball.putDirectionX(-3);
                    ball.putDirectionY(-3);
                }
            }
            if (ballPlace >= place1 && ballPlace < place2) {
                if(gotMask == true){
                    ball.putDirectionX(-1);
                    ball.putDirectionY(-1 * ball.getDirectionY());
                    mask(maskCount);
                } else if(gotCovidTest == true){
                    ball.putDirectionX(-6);
                    ball.putDirectionY(-6 * ball.getDirectionY());
                    covidTest(covidTestCount);
                } else{
                    ball.putDirectionX(-3);
                    ball.putDirectionY(-3 * ball.getDirectionY());
                }
            }
            if (ballPlace >= place2 && ballPlace < place3) {
                if(gotMask == true){
                    ball.putDirectionX(0);
                    ball.putDirectionY(-1);
                    mask(maskCount);
                } else if(gotCovidTest == true){
                    ball.putDirectionX(0);
                    ball.putDirectionY(-6);
                    covidTest(covidTestCount);
                } else{
                    ball.putDirectionX(0);
                    ball.putDirectionY(-3);
                }
            }
            if (ballPlace >= place3 && ballPlace < place4) {
                if(gotMask == true){
                    ball.putDirectionX(1);
                    ball.putDirectionY(-1 * ball.getDirectionY());
                    mask(maskCount);
                } else if(gotCovidTest == true){
                    ball.putDirectionX(6);
                    ball.putDirectionY(-6 * ball.getDirectionY());
                    covidTest(covidTestCount);
                } else{
                    ball.putDirectionX(3);
                    ball.putDirectionY(-3 * ball.getDirectionY());
                }
            }
            if (ballPlace > place4) {
                if(gotMask == true){
                    ball.putDirectionX(1);
                    ball.putDirectionY(-1);
                    mask(maskCount);
                } else if(gotCovidTest == true){
                    ball.putDirectionX(6);
                    ball.putDirectionY(-6);
                    covidTest(covidTestCount);
                } else{
                    ball.putDirectionX(3);
                    ball.putDirectionY(-3);
                }
            }
        }

        // These next 2 big for loops check if the ball touches any of the cells, green or red, if it does the ball is redirected accordingly
        // and the cell is destroyed. This also cheks for the power ups when choosing the speed. In the end of the loop it also adds score
        // depending on the colour of the cell which was destroyed. After that it also does a power up check to see if a power up should be
        // activated or not.
        for(int i = 0; i<cellNumGreen; i++){
            if((ball.checkRect(false)).intersects(greenCells[i].checkRect(false))) {
                int ballX = (int) ball.checkRect(false).getMinX();
                int ballY = (int) ball.checkRect(false).getMinY();
                int ballHeight = (int) ball.checkRect(false).getHeight();
                int ballWidth = (int) ball.checkRect(false).getWidth();
                Point right = new Point(ballX + ballWidth + 1, ballY);
                Point left = new Point(ballX - 1, ballY);
                Point bottom = new Point(ballX, ballY + ballHeight + 1);
                Point top = new Point(ballX, ballY - 1);

                if (greenCells[i].isDestruct() == false) {
                    if (greenCells[i].checkRect(false).contains(top)) {
                        if(gotMask == true){
                            ball.putDirectionY(1);
                            mask(maskCount);
                        } else if(gotCovidTest == true){
                            ball.putDirectionY(6);
                            covidTest(covidTestCount);
                        } else{
                            ball.putDirectionY(3);
                        }
                    } else if (greenCells[i].checkRect(false).contains(bottom)) {
                        if(gotMask == true){
                            ball.putDirectionY(-1);
                            mask(maskCount);
                        } else if(gotCovidTest == true){
                            ball.putDirectionY(-6);
                            covidTest(covidTestCount);
                        } else{
                            ball.putDirectionY(-3);
                        }
                    }
                    if (greenCells[i].checkRect(false).contains(right)) {
                        if(gotMask == true){
                            ball.putDirectionX(-1);
                            mask(maskCount);
                        } else if(gotCovidTest == true){
                            ball.putDirectionX(-6);
                            covidTest(covidTestCount);
                        } else{
                            ball.putDirectionX(-3);
                        }
                    } else if (greenCells[i].checkRect(false).contains(left)) {
                        if(gotMask == true){
                            ball.putDirectionX(1);
                            mask(maskCount);
                        } else if(gotCovidTest == true){
                            ball.putDirectionX(6);
                            covidTest(covidTestCount);
                        } else{
                            ball.putDirectionX(3);
                        }
                    }
                    greenCells[i].ifDestruct(true);
                    gameScore += 100;
                    powerUpCheck(level, i, 0);
                }
            }
        }
        // This is the same big loop as last one, but for the red cells. The thing that this does differently is that it checks if the cell
        // has been hit twice, since red cells do not get destroyed with only 1 hit.
        for(int i = 0; i<cellNumRed; i++) {
            if (cellNumRed != 0) {
                if ((ball.checkRect(false)).intersects(redCells[i].checkRect(false))) {
                    int ballX = (int) ball.checkRect(false).getMinX();
                    int ballY = (int) ball.checkRect(false).getMinY();
                    int ballHeight = (int) ball.checkRect(false).getHeight();
                    int ballWidth = (int) ball.checkRect(false).getWidth();
                    Point right = new Point(ballX + ballWidth + 1, ballY);
                    Point left = new Point(ballX - 1, ballY);
                    Point bottom = new Point(ballX, ballY + ballHeight + 1);
                    Point top = new Point(ballX, ballY - 1);

                    if (redCells[i].isDestruct() == false) {
                        if (redCells[i].checkRect(false).contains(top)) {
                            if(gotMask == true){
                                ball.putDirectionY(1);
                                mask(maskCount);
                            } else if(gotCovidTest == true){
                                ball.putDirectionY(6);
                                covidTest(covidTestCount);
                            } else{
                                ball.putDirectionY(3);
                            }
                        } else if (redCells[i].checkRect(false).contains(bottom)) {
                            if(gotMask == true){
                                ball.putDirectionY(-1);
                                mask(maskCount);
                            } else if(gotCovidTest == true){
                                ball.putDirectionY(-6);
                                covidTest(covidTestCount);
                            } else{
                                ball.putDirectionY(-3);
                            }
                        }
                        if (redCells[i].checkRect(false).contains(right)) {
                            if(gotMask == true){
                                ball.putDirectionX(-1);
                                mask(maskCount);
                            } else if(gotCovidTest == true){
                                ball.putDirectionX(-6);
                                covidTest(covidTestCount);
                            } else{
                                ball.putDirectionX(-3);
                            }
                        } else if (redCells[i].checkRect(false).contains(left)) {
                            if(gotMask == true){
                                ball.putDirectionX(1);
                                mask(maskCount);
                            } else if(gotCovidTest == true){
                                ball.putDirectionX(6);
                                covidTest(covidTestCount);
                            } else{
                                ball.putDirectionX(3);
                            }
                        }
                        if (redCells[i].count == 1) {
                            redCells[i].count++;
                        } else {
                            redCells[i].ifDestruct(true);
                            gameScore += 200;
                            powerUpCheck(level, i, 1);
                        }
                    }
                }
            }
        }
    }

    // This function takes in the games level, the cell number and also the cells colour, and then checks if the correct cell was
    //destroyed for the power up to be activated.
    private void powerUpCheck(int level, int cellNum, int colour){
        if(level == 1){
            if(cellNum == 70){
                handWash(0);
            }
            if(cellNum == 9){
                mask(0);
            }
            if(cellNum == 22){
               covidTest(0);
            }
        }
        if(level == 2){
            if(cellNum == 16){
                handWash(0);
            }
            if(cellNum == 0){
                mask(0);
            }
            if(cellNum == 27){
                covidTest(0);
            }
        }
        if(level == 3){
            if(colour == 0){
                if(cellNum == 26){
                    handWash(0);
                }
                if(cellNum == 47){
                    mask(0);
                }
            }
            if(colour == 1){
                if(cellNum == 3){
                    covidTest(0);
                }
            }
        }
        if(level == 4){
            if(colour == 0){
                if(cellNum == 5){
                    handWash(0);
                }
                if(cellNum == 8){
                    covidTest(0);
                }
            }
            if(colour == 1){
                if(cellNum == 2){
                   mask(0);
                }
            }
        }
        if(level == 5){
            if(colour == 0){
                if(cellNum == 4){
                    covidTest(0);
                }
            }
            if(colour == 1){
                if(cellNum == 6){
                   mask(0);
                }
                if(cellNum == 12){
                    handWash(0);
                }
            }
        }
    }

    // This function lets the hand wash power up start and keeps it going for 10 seconds, as the counter has been set up. Once the
    // counter ends the hand wash modifications go away as well, so the bar width goes back to normal.
    public void handWash(int count){
        if(count >= 1000){
            gotHandWash = false;
            momentText = "";
        } else{
            gotHandWash = true;
            momentText = "Hand Wash Power active!";
        }
    }

    //This function starts up the mask power up and keeps it going for 10 seconds, as the counter implies. Once the counter ends
    // the balls speed goes back to normal, as it was slowed down before.
    public void mask(int count){
        if(count >= 1000){
            gotMask = false;
            momentText = "";
        } else{
            gotMask = true;
            momentText = "Mask Power active!";
        }
    }

    // This method starts the covid test power up and keeps it going for 10 seconds as well, hence the counter. Once the counter goes over
    // 1000 the power up is ended and the ball speed goes backto normal, since it was twice the speed before.
    public void covidTest(int count){
        if(count >= 1000){
            gotCovidTest = false;
            momentText = "";
        } else{
            gotCovidTest = true;
            momentText = "Covid Test Power active!";
        }
    }

    //This class keeps the game running on every game timer tick. Each time this class is called, then the class calls for the runTheGame()
    //function.
    class runningGame implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            runTheGame();
        }
    }

    //This function is for keeping the game going over all. Each time it moves the bar depending on the new chosen location by player,
    // it moves the ball depending on the next position(also depending on the power ups speed). Every time it is called it also checks with
    // the  hitbox if anything was hit and after all those checks have been done, it repaints all the objects into their new locations or
    // does nto draw any cells that were destroyed. On every call it also raises the counter for the power ups, if they have been called.
    // So since the game timer is with a delay of 10 milliseconds then when the counter reaches 1000 then the power up method cancels the
    // power up.
    void runTheGame(){
        bar.moveBar(gotHandWash);
        if(gotMask == true){
            ball.moveBall(1);
            maskCount++;
        }else if(gotCovidTest == true){
            ball.moveBall(2);
            covidTestCount++;
        } else{
            ball.moveBall(0);
        }
        hitBox();
        repaint();

        if(gotHandWash == true){
            handWashCount++;
            handWash(handWashCount);
        }
    }

    //This method gets called whenever the ball touches the lower border of the frame. It stops the game, removes a body cell(player life),
    // reduces the player score, lets the mouse be used again(starter++) for game continuation, and then resets the ball and bar positions.
    private void lifeLost(){
        gameInProgress = false;
        lives--;
        gameScore -= 500;
        momentText = "Click Left Mouse Button on the game window to continue the game!";
        starter++;
        bar = new Bar();
        ball = new Ball();
        gameTimer.stop();
    }

    //This method checks through the scoreBoard.txt file and then if there are already values there, puts them into the level specific
    //arrays and then sorts the arrays and writes them into their respective places in the file.
    private void fileUpdate(int level, int gameScore) {
        int specCount1 = 0;
        int specCount2 = 0;
        int specCount3 = 0;
        int specCount4 = 0;
        int specCount5 = 0;

        Integer scores1[] = new Integer[6];
        Integer scores2[] = new Integer[6];
        Integer scores3[] = new Integer[6];
        Integer scores4[] = new Integer[6];
        Integer scores5[] = new Integer[6];

        //this try catch block checks through the file to find any previous scores, and puts them into their respective arrays.
        try {
            int counter = 0;
            File scores = new File("scoreBoard.txt");
            Scanner myReader = new Scanner(scores);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.startsWith("1. ")){
                    data = data.replaceFirst("1. ", "");
                }
                if(data.startsWith("2. ")){
                    data = data.replaceFirst("2. ", "");
                }
                if(data.startsWith("3. ")){
                    data = data.replaceFirst("3. ", "");
                }
                if(data.startsWith("4. ")){
                    data = data.replaceFirst("4. ", "");
                }
                if(data.startsWith("5. ")){
                    data = data.replaceFirst("5. ", "");
                }
                try {
                    int realData = Integer.parseInt(data);
                    if (counter == 1) {
                        scores1[specCount1] = realData;
                        specCount1++;
                    }
                    if (counter == 2) {
                        scores2[specCount2] = realData;
                        specCount2++;
                    }
                    if (counter == 3) {
                        scores3[specCount3] = realData;
                        specCount3++;
                    }
                    if (counter == 4) {
                        scores4[specCount4] = realData;
                        specCount4++;
                    }
                    if (counter == 5) {
                        scores5[specCount5] = realData;
                        specCount5++;
                    }
                } catch (NumberFormatException n) {
                    if (data.endsWith(":")) {
                        counter++;
                    }
                    continue;
                }
            }
            myReader.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // These if statements check which level was played so it knows which array gets the new score and then sorts the scores.
        if(level == 1){
            scores1[specCount1] = gameScore;
            for(int i = 0 ; i < scores1.length;i++)
            {
                for(int j = i+1 ; j< scores1.length;j++)
                {
                    try{
                        if(scores1[i] < scores1[j]) {
                            int temp = scores1[i];
                            scores1[i] = scores1[j];
                            scores1[j] = temp;
                        }
                    } catch (NullPointerException e){
                        continue;
                    }
                }
            }
        }
        if(level == 2){
            scores2[specCount2] = gameScore;
            for(int i = 0 ; i < scores2.length;i++)
            {
                for(int j = i+1 ; j< scores2.length;j++)
                {
                    try {
                        if (scores2[i] < scores2[j]) {
                            int temp = scores2[i];
                            scores2[i] = scores2[j];
                            scores2[j] = temp;
                        }
                    } catch(NullPointerException e){
                        continue;
                    }
                }
            }
        }
        if(level == 3){
            scores3[specCount3] = gameScore;
            for(int i = 0 ; i < scores3.length;i++)
            {
                for(int j = i+1 ; j< scores3.length;j++)
                {
                    try {
                        if (scores3[i] < scores3[j]) {
                            int temp = scores3[i];
                            scores3[i] = scores3[j];
                            scores3[j] = temp;
                        }
                    } catch(NullPointerException e){
                        continue;
                    }
                }
            }
        }
        if(level == 4){
            scores4[specCount4] = gameScore;
            for(int i = 0 ; i < scores4.length;i++)
            {
                for(int j = i+1 ; j< scores4.length;j++)
                {
                    try{
                        if(scores4[i] < scores4[j])
                        {
                            int temp = scores4[i];
                            scores4[i] = scores4[j];
                            scores4[j] = temp;
                        }
                    } catch (NullPointerException e){
                        continue;
                    }
                }
            }
        }
        if(level == 5){
            scores5[specCount5] = gameScore;
            for(int i = 0 ; i < scores5.length;i++)
            {
                for(int j = i+1 ; j< scores5.length;j++)
                {
                    try{
                        if(scores5[i] < scores5[j])
                        {
                            int temp = scores5[i];
                            scores5[i] = scores5[j];
                            scores5[j] = temp;
                        }
                    }catch(NullPointerException e){
                        continue;
                    }
                }
            }
        }

        // Here all of the arrays get written back into the file with the correct format.
        BufferedWriter writer = null;
        try {
            int counting = 1;
            int statCounter = 1;
            File scores = new File("scoreBoard.txt");
            writer = new BufferedWriter(new FileWriter(scores, false));
            for (int i = 0; i < 5; i++) {
                writer.write("Level " + counting + " scores:");
                writer.newLine();
                if(counting == 1){
                    for(int j = 0; j < 5; j++){
                        try{
                            int nextUp = scores1[j];
                            writer.write(statCounter + ". " + nextUp);
                            statCounter++;
                        } catch(NullPointerException e){
                            System.out.println("No value to add");
                        }
                        writer.newLine();
                    }
                    statCounter = 1;
                }
                if(counting == 2){
                    for(int j = 0; j < 5; j++){
                        try{
                            int nextUp = scores2[j];
                            writer.write(statCounter + ". " + nextUp);
                            statCounter++;
                        } catch(NullPointerException e){
                            System.out.println("No value to add");
                        }
                        writer.newLine();
                    }
                    statCounter = 1;
                }
                if(counting == 3){
                    for(int j = 0; j < 5; j++){
                        try{
                            int nextUp = scores3[j];
                            writer.write(statCounter + ". " + nextUp);
                            statCounter++;
                        } catch(NullPointerException e){
                            System.out.println("No value to add");
                        }
                        writer.newLine();
                    }
                    statCounter = 1;
                }
                if(counting == 4){
                    for(int j = 0; j < 5; j++){
                        try{
                            int nextUp = scores4[j];
                            writer.write(statCounter + ". " + nextUp);
                            statCounter++;
                        } catch(NullPointerException e){
                            System.out.println("No value to add");
                        }
                        writer.newLine();
                    }
                    statCounter = 1;
                }
                if(counting == 5){
                    for(int j = 0; j < 5; j++){
                        try{
                            int nextUp = scores5[j];
                            writer.write(statCounter + ". " + nextUp);
                            statCounter++;
                        } catch(NullPointerException e){
                            System.out.println("No value to add");
                        }
                        writer.newLine();
                    }
                    statCounter = 1;
                }
                writer.write(" ");
                writer.newLine();
                counting++;
            }
            writer.close();
        } catch (IOException a) {
            a.printStackTrace();
        }
    }
}

// This class has different methods that almost all get used to get information about different objects into the game, all the objects
// extend this class.
abstract class Shape{
    int x;
    int y;
    int height;
    int width;
    ImageIcon cellImg;

    public void assignX(int x){
        this.x = x;
    }

    public void assignY(int y){
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getShapeWidth(){
        return width;
    }

    public int getShapeHeight(){
        return height;
    }

    //This is used for checking the hitboxes in the game, if the objects intersect or not, the boolean statement is here only in case the
    //bars width is doubled thanks to the hand wash power up event.
    Rectangle checkRect(boolean check){
        if(check == true){
            return new Rectangle(x, y, getShapeWidth() * 2, getShapeHeight());
        }else{
            return new Rectangle(x, y, getShapeWidth(), getShapeHeight());
        }
    }

    Image getImage(){
        return cellImg.getImage();
    }
}

//This class has everything needed to handle the bar functionality in the game.
class Bar extends Shape{
    private int changeX;
    private int width = 72;
    private int height = 20;
    private int arcWidth = 5;
    private int arcHeight = 5;

    public Bar(){
        makeBar();
    }

    private void makeBar(){
        resetBar();
    }

    //This function changes the bars location, it is used together with the keyboard listener. It also makes sure that the bar does not
    // move outside of the game window. The check is there in case the hand wash power up is active(the bar is double the width).
    public void moveBar(boolean check){
        x += changeX;

        if (x <= 0) {
            x = 0;
        }

        if(check == true) {
            if (x >= 800 - width * 2) {
                x = 800 - (width * 2);
            }
        } else {
            if (x >= 800 - width) {
                x = 800 - width;
            }
        }
    }

    public int getShapeWidth(){
        return width;
    }

    public int getShapeHeight(){
        return height;
    }

    public int getBarArcWidth(){ return arcWidth; }

    public int getBarArcHeight(){
        return arcHeight;
    }

    //This function puts the bar back to its original position.
    private void resetBar() {
        x = 364;
        y = 730;
    }

    //This function handles the bar movement by player keyboard arrow key presses.
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            changeX = -6;
        }

        if (key == KeyEvent.VK_RIGHT) {
            changeX = 6;
        }
    }

    //This function makes it sure that the bar does not keep moving when the key is released.
    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            changeX = 0;
        }

        if (key == KeyEvent.VK_RIGHT) {
            changeX = 0;
        }
    }
}

//This class has all the functions needed to make the ball behave correctly and also get all the neccessary variables.
class Ball extends Shape{
    private int width = 15;
    private int height = 15;
    private int directionX;
    private int directionY;

    public Ball() {
        createBall();
    }

    //This gives the ball the starting speed and direction, also resets the ball in place.
    private void createBall(){
        directionX = 3;
        directionY = -3;

        resetBall();
    }

    //This puts the ball into the starting position by coordinates.
    private void resetBall(){
        x = 385;
        y = 550;
    }

    //This makes sure that the ball keeps moving in the correct direction, and also makes sure that the ball does not go out of the
    //frame boundaries. The angles value checks if there is any power up active, if there is then upon wall bounces keeps the speed
    // up as well.
    public void moveBall(int angles) {
        x += directionX;
        y += directionY;

        if(angles == 0){
            if(x > 800 - width){
                putDirectionX(-3);
            }
            if(x < 0){
                putDirectionX(3);
            }
            if(y < 0){
                putDirectionY(3);
            }
        } else if(angles == 1){
            if(x > 800 - width){
                putDirectionX(-1);
            }
            if(x < 0){
                putDirectionX(1);
            }
            if(y < 0){
                putDirectionY(1);
            }
        } else if(angles == 2){
            if(x > 800 - width){
                putDirectionX(-6);
            }
            if(x < 0){
                putDirectionX(6);
            }
            if(y < 0){
                putDirectionY(6);
            }
        }
    }

    public int getShapeWidth(){
        return width;
    }

    public int getShapeHeight(){
        return height;
    }

    public void putDirectionX(int x){
        directionX = x;
    }

    public void putDirectionY(int y){
        directionY = y;
    }

    public int getDirectionY(){
        return directionY;
    }
}

//This function has everything concerning the green cell object and also getting all the neccessary info from it.
class greenCell extends Shape{
    private boolean destruct;

    public greenCell(int x, int y){
        makeCell(x, y);
    }

    //This function takes in the x and y value so it knows where the cell will be placed. Then it checks if the image
    // exists and according to that, makes the image the object which will be drawn. It also sets the cells width and
    // height to be the width and height of the image. Sets the destruct value as false so the cell is not counted as
    // destroyed yet.
    private void makeCell(int x, int y){
        this.x = x;
        this.y = y;

        URL resource = CE203_1906001_Ass2.class.getResource("greenCell.png");
        cellImg = new ImageIcon(resource);
        width = cellImg.getIconWidth();
        height = cellImg.getIconHeight();

        destruct = false;
    }

    public int getShapeWidth(){
        return width;
    }

    public int getShapeHeight(){
        return height;
    }

    //Helps gameLogic() functions to check if the cell is destroyed.
    public boolean isDestruct(){
        return destruct;
    }

    //changes the cell to the "been destroyed" state, so it won't be drawn anymore.
    public void ifDestruct(boolean choice){
        destruct = choice;
    }
}

//This has all the red cell functions that are required to make it and get info about it.
class redCell extends Shape{
    //the count is initialized as 1 always so when it reaches 2 in the gameLogic() functions it will be destroyed.
    int count = 1;
    private boolean destruct;

    public redCell(int x, int y){
        makeCell(x, y);
    }

    //Makes the cell the same way as in greenCell, just uses different image.
    private void makeCell(int x, int y){
        this.x = x;
        this.y = y;

        URL resource = CE203_1906001_Ass2.class.getResource("redCell.png");
        cellImg = new ImageIcon(resource);
        width = cellImg.getIconWidth();
        height = cellImg.getIconHeight();

        destruct = false;
    }

    public int getShapeWidth(){
        return width;
    }

    public int getShapeHeight(){
        return height;
    }

    //Helps gameLogic() functions to check if cell is destroyed.
    public boolean isDestruct(){
        return destruct;
    }

    //If the cell is destroyed then this function is called to set the cell as "been destroyed".
    public void ifDestruct(boolean choice){
        destruct = choice;
    }
}

//This class has everything required to make the description frame that the player can access from the main menu. It consists of all the
//required Jlabels, basically this is just a text frame, that is required to describe the game to the player and also the rules. If the
// button is pressed then the frame closes.
class HelpHandler implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame helpFrame = new JFrame();
        helpFrame.setTitle("Description");
        helpFrame.setVisible(true);
        helpFrame.setResizable(false);

        JLabel title = new JLabel("Covid-19 Infection Protection!", SwingConstants.CENTER);
        JLabel descript = new JLabel("<html>   The aim of this game is to raise awareness for COVID-19 and also " +
                "point out some ways you can keep others and yourself safe. Inside different levels you will get a bar which " +
                "you can move around with your left and right arrow keyboard keys and by pressing the left mouse button you will release the ball, " +
                "which is the virus cure, into the game. To win you must destroy all the contaminated cells in the human organism, which" +
                " is where the levels take place. To destroy a cell you must hit the cell with your cure ball. By destroying different" +
                " cells you might gain power ups which are explained in the legend below. In each level you also start off with 3 cure balls," +
                " if you fail to catch the cure on each return and it falls below the bar then you lose a cure. Points will be gained" +
                " depending on which types of cells you break in the level and how many times you lose the cure. If you lose the cure, then" +
                " you lose 500 points. In case you want to see the best scores so far be sure to check the scoreboard in the main menu. " +
                "Hope you enjoy the game!</html>");
        JLabel brickExp = new JLabel("Cell colours:");
        JLabel greenCell = new JLabel("Green cells = These are the weaker virus cells, so these can be broken by 1 hit from the cure. " +
                "Breaking this cell awards you with 100 points.");
        JLabel redCell = new JLabel("<html>Red cells = These virus cells are stronger so take care in curing these. They will not go away with " +
                "only 1 hit of the cure, these cells require 2 hits. There is a higher chance of getting a power up from destroying these. These" +
                " cells grant you 200 points.</html>");
        JLabel powerUps = new JLabel("Power ups:");
        JLabel handWash = new JLabel("Hand wash = You washed your hands and you can feel your immune system getting stronger, it doubles the" +
                " size of your bar.");
        JLabel maskWear = new JLabel("<html>Mask = You get a mask! It slows down the rate of infection thus giving you more time to line up your cure" +
                " hits. This slows down the movement of your ball.</html>");
        JLabel covidTest = new JLabel("<html>Covid-19 test = You passed the test for Covid which greatly enhances the motivation of the immune" +
                " system to fight back! Your ball will move faster.</html>");
        JButton close = new JButton("Close description");

        JPanel main = new JPanel(new GridLayout(2, 1));
        JPanel bricks = new JPanel(new GridLayout(3, 1));
        JPanel powers = new JPanel(new GridLayout(5, 1));
        main.add(title);
        main.add(descript);
        bricks.add(brickExp);
        bricks.add(greenCell);
        bricks.add(redCell);
        powers.add(powerUps);
        powers.add(handWash);
        powers.add(maskWear);
        powers.add(covidTest);
        powers.add(close);

        helpFrame.add(main, BorderLayout.NORTH);
        helpFrame.add(bricks, BorderLayout.CENTER);
        helpFrame.add(powers, BorderLayout.SOUTH);

        helpFrame.setSize(900, 500);

        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpFrame.dispose();
            }
        });
    }
}

//This class has everything required to make a frame which shows all the top scores in all levels in the game. The frame keeps the top
//list to the 5 best scores. The filereader is used to cycle through all the linesvet and then put the lines from the scoreBoard.txt
// file into the frame. If there are no scores yet in the game then the frame prompts the player to play any level to get a score inserted.
// If the button is pressed then the frame closes.
class ScoreHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame scoreFrame = new JFrame();
        scoreFrame.setTitle("Scoreboard");
        JLabel title = new JLabel("Top Scores", SwingConstants.CENTER);
        scoreFrame.setVisible(true);
        JButton returnal = new JButton("Return to main menu");
        scoreFrame.add(title, BorderLayout.NORTH);

        File scores = new File("scoreBoard.txt");
        if(scores.exists()){
            JPanel scoring = new JPanel(new GridLayout(35, 1));

            try {
                FileReader fr = new FileReader(scores);
                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) {
                    String data = br.readLine();
                    JLabel nextLine = new JLabel(data);
                    scoring.add(nextLine);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            scoreFrame.setSize(500, 500);
            scoreFrame.add(scoring);
            scoreFrame.add(returnal, BorderLayout.SOUTH);
        } else {
            JLabel bummer = new JLabel("<html>No scores have been posted yet! Play the game to get scores placed into the score board!</html>", SwingConstants.CENTER);
            scoreFrame.setSize(300, 200);
            scoreFrame.add(bummer, BorderLayout.CENTER);
            scoreFrame.add(returnal, BorderLayout.SOUTH);
        }

        returnal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreFrame.dispose();
            }
        });
    }
}

//This class handles the exit game button in the main menu. Once the button is pressed then a new smaller frame appears which asks the
// player a reassuring question which is are they sure that they want to leave the game? If the player chooses no the the frame closes,
// if the player chooses yes then both that frame and the games main menu close.
class ExitHandler implements ActionListener {
    MainFrame theApp;

    ExitHandler(MainFrame app){
        theApp = app;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame exitFrame = new JFrame();
        exitFrame.setTitle("Exit?");
        exitFrame.setVisible( true );
        exitFrame.setResizable(false);

        JLabel asker = new JLabel("Are you sure you want to leave?", SwingConstants.CENTER);
        asker.setPreferredSize(new Dimension(50, 20));
        JButton positive = new JButton("Yes");
        JButton negative = new JButton("No");

        JPanel butPanel = new JPanel();
        butPanel.add(positive);
        butPanel.add(negative);

        exitFrame.add(asker, BorderLayout.CENTER);
        exitFrame.add(butPanel, BorderLayout.SOUTH);

        exitFrame.setSize(300, 150);

        positive.addActionListener(new ExitListener(exitFrame, 1));
        negative.addActionListener(new ExitListener(exitFrame, 2));
    }
}

//This is the class that handles the button presses from the ExitHandler. If the player chose no then only that frame closes, but if
// the player chose yes then both that frame and the games main menu close.
class ExitListener implements ActionListener {
    JFrame decision;
    int choice;

    ExitListener(JFrame decided, int chosen){
        decision = decided;
        choice = chosen;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(choice == 1){
            System.out.println("Player has chosen to terminate the game!");
            System.exit(0);
        }
        if(choice == 2){
            System.out.println("Player has chosen to keep playing!");
            decision.dispose();
        }
    }
}