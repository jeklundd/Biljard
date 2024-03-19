package com.biljardspel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;

public class Biljard {

    final static int UPDATE_FREQUENCY = 100;    // Global constant: fps, ie times per second to simulate

    public static void main(String[] args) {

        JFrame frame = new JFrame("Biljard!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Table table = new Table();
        frame.add(table);

        ResetButton resetButton = new ResetButton(table);
        frame.add(resetButton, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
}

/**
 * *****************************************************************************************
 * Coord
 * <p>
 * A coordinate is a pair (x,y) of doubles. Also used to represent vectors. Here
 * are various utility methods to compute with vectors.
 */
class Coord {

    double x, y;

    Coord(double xCoord, double yCoord) {
        x = xCoord;
        y = yCoord;
    }

    Coord(MouseEvent event) {
        x = event.getX();
        y = event.getY();
    }

    static Coord zero() {
        return new Coord(0,0);
    }

    double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    Coord norm() {                              // norm: a normalised vector at the same direction
        return new Coord(x / magnitude(), y / magnitude());
    }

    void increase(Coord c) {
        x += c.x;
        y += c.y;
    }

    void decrease(Coord c) {
        x -= c.x;
        y -= c.y;
    }

    static double scal(Coord a, Coord b) {      // scalar product
        return a.x * b.x + a.y * b.y;
    }

    static Coord sub(Coord a, Coord b) {
        return new Coord(a.x - b.x, a.y - b.y);
    }

    static Coord mul(double k, Coord c) {       // multiplication by a constant
        return new Coord(k * c.x, k * c.y);
    }

    static double distance(Coord a, Coord b) {
        return Coord.sub(a, b).magnitude();
    }

    static void paintLine(Graphics2D graph2D, Coord a, Coord b) {  // paint line between points
        graph2D.setColor(Color.black);
        graph2D.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
    }
}

/**
 * ****************************************************************************************
 * Table
 * <p>
 * The table has some constants and instance variables relating to the graphics and
 * the balls. When simulating the balls it starts a timer
 * which fires UPDATE_FREQUENCY times per second. Each time the timer is
 * activated one step of the simulation is performed. The table reacts to
 * events to accomplish repaints and to stop or start the timer.
 */
class Table extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

        static final int TABLE_WIDTH = 400;
        static final int TABLE_HEIGHT = 580;
        static final int WALL_THICKNESS = 25;
        private final Color COLOR = new Color(0, 152, 0);
        private final Color WALL_COLOR = new Color(102, 50, 0).brighter();
        static Ball[] balls = new Ball[16];
        static Hole[] holes = new Hole[6];

        double ballSize = Ball.DIAMETER;
        double centerX = (TABLE_WIDTH + 2 * WALL_THICKNESS) / 2;
        double centerY = TABLE_HEIGHT / 2;
        double firstRow = centerY / 3;

        static boolean redPlayer = true;
        static boolean redBall;
        static int whiteBall;
        static boolean ballShoot = false;

        private final Timer simulationTimer;

    Table() {

        setPreferredSize(new Dimension(TABLE_WIDTH + 2 * WALL_THICKNESS,
                TABLE_HEIGHT + 2 * WALL_THICKNESS));

        createInitialBalls();
        createInitialHoles();

        addMouseListener(this);
        addMouseMotionListener(this);

        simulationTimer = new Timer((int) (1000.0 / Biljard.UPDATE_FREQUENCY), this);
    }

    public void reset() {
        createInitialBalls();
        Ball.redBallNumber = 0;
        Ball.blueBallNumber = 0;
        redPlayer = true;
    }

     void createInitialHoles() {
        final Coord firstHolePosition = new Coord(WALL_THICKNESS - Hole.HOLE_RADIUS, WALL_THICKNESS - Hole.HOLE_RADIUS);
        final Coord secondHolePosition = new Coord(TABLE_WIDTH + WALL_THICKNESS - Hole.HOLE_RADIUS, WALL_THICKNESS - Hole.HOLE_RADIUS);
        final Coord thirdHolePosition = new Coord(WALL_THICKNESS - Hole.HOLE_RADIUS, TABLE_HEIGHT + WALL_THICKNESS - Hole.HOLE_RADIUS);
        final Coord fourthHolePosition = new Coord(TABLE_WIDTH + WALL_THICKNESS - Hole.HOLE_RADIUS, TABLE_HEIGHT + WALL_THICKNESS - Hole.HOLE_RADIUS);

        final Coord fifthHolePosition = new Coord(WALL_THICKNESS - Hole.HOLE_RADIUS, centerY);
        final Coord sixthHolePosition = new Coord(WALL_THICKNESS + TABLE_WIDTH - Hole.HOLE_RADIUS, centerY);

        holes[0] = new Hole(firstHolePosition);
        holes[1] = new Hole(secondHolePosition);
        holes[2] = new Hole(thirdHolePosition);
        holes[3] = new Hole(fourthHolePosition);
        holes[4] = new Hole(fifthHolePosition);
        holes[5] = new Hole(sixthHolePosition);
    }

    void createInitialBalls() {

        final Coord firstInitialPosition = new Coord(centerX, (centerY / 2) * 3);

        final Coord secondInitialPosition = new Coord(centerX - 2 * ballSize, firstRow);
        final Coord thirdInitialPosition = new Coord(centerX - 1 * ballSize, firstRow);
        final Coord fourthInitialPosition = new Coord(centerX, firstRow);
        final Coord fifthInitialPosition = new Coord(centerX + 1 * ballSize, firstRow);
        final Coord sixthInitialPosition = new Coord(centerX + 2 * ballSize, firstRow);

        final Coord seventhInitialPosition = new Coord(centerX - 1.5 * ballSize, firstRow + ballSize - 4);
        final Coord eigthInitialPosition = new Coord(centerX - 0.5 * ballSize, firstRow + ballSize - 4);
        final Coord ninethInitialPosition = new Coord(centerX + 0.5 * ballSize, firstRow + ballSize - 4);
        final Coord tenthInitialPosition = new Coord(centerX + 1.5 * ballSize, firstRow + ballSize - 4);

        final Coord eleventhInitialPosition = new Coord(centerX - ballSize, firstRow + 2 * ballSize - 8);
        final Coord twelvethInitialPosition = new Coord(centerX, firstRow + 2 * ballSize - 8); // BLACK
        final Coord thirteenthInitialPosition = new Coord(centerX + ballSize, firstRow + 2 * ballSize - 8);

        final Coord fourteenthInitialPosition = new Coord(centerX - 0.5 * ballSize, firstRow + 3 * ballSize - 12);
        final Coord fifteenthInitialPosition = new Coord(centerX + 0.5 * ballSize, firstRow + 3 * ballSize - 12);

        final Coord sixteenthInitialPosition = new Coord(centerX, firstRow + 4 * ballSize - 16);

        balls[0] = new Ball(firstInitialPosition);
        balls[1] = new Ball(secondInitialPosition);
        balls[2] = new Ball(thirdInitialPosition);
        balls[3] = new Ball(fourthInitialPosition);
        balls[4] = new Ball(fifthInitialPosition);
        balls[5] = new Ball(sixthInitialPosition);
        balls[6] = new Ball(seventhInitialPosition);
        balls[7] = new Ball(eigthInitialPosition);
        balls[8] = new Ball(ninethInitialPosition);
        balls[9] = new Ball(tenthInitialPosition);
        balls[10] = new Ball(eleventhInitialPosition);
        balls[11] = new Ball(twelvethInitialPosition);
        balls[12] = new Ball(thirteenthInitialPosition);
        balls[13] = new Ball(fourteenthInitialPosition);
        balls[14] = new Ball(fifteenthInitialPosition);
        balls[15] = new Ball(sixteenthInitialPosition);

        Ball.setColorBall(balls);
        
    }

    static boolean placeWhiteBall(double xPos, double yPos){
        Coord coordClick= new Coord(xPos,yPos);
        if(xPos < TABLE_WIDTH + WALL_THICKNESS - Ball.RADIUS && xPos > WALL_THICKNESS + Ball.RADIUS){

            if(yPos < TABLE_HEIGHT + WALL_THICKNESS && yPos >  WALL_THICKNESS){

                for(Ball ball: Table.balls){
                    if(Coord.distance(coordClick, ball.position) < 2*Ball.RADIUS + Ball.BORDER_THICKNESS){

                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    static boolean ballsInHoleRules() {

        if (redPlayer && redBall) {
            redBall = false;
            redPlayer = false;
            return redPlayer;
        }

        if (!redPlayer && !redBall) {
            redBall = true;
            redPlayer = true;
            return redPlayer;
        }

        if (!redPlayer && redBall) {
            redBall = false;
            redPlayer = false;
            return redPlayer;
        }

        if (redPlayer && !redBall) {
            redBall = true;
            redPlayer = true;
            return redPlayer;
        }

        if (redPlayer && whiteBall == 1){
            redPlayer = false;
            whiteBall = 0;
            return redPlayer;
        }

        if (!redPlayer && whiteBall == 1){
            redPlayer = true;
            whiteBall = 0;
            return redPlayer;
        }

        return redPlayer;
    }

    static boolean noBallsInHole() {

        if (redPlayer) {
            redPlayer = false;
            return redPlayer;
        }

        if (!redPlayer) {
            redPlayer = true;
            return redPlayer;
        }

        return redPlayer;
    }

    static void blackBall() {

        if (redPlayer && Ball.redBallNumber == 7) {
            JOptionPane.showMessageDialog(null, "Winner is Player 1 (red), CONGRATULATIONS ", "WINNER!", JOptionPane.INFORMATION_MESSAGE);
        }

        if (redPlayer && Ball.redBallNumber != 7) {
            JOptionPane.showMessageDialog(null, "Player 1 (red) lose ", "LOSER!", JOptionPane.INFORMATION_MESSAGE);
        }

        if (!redPlayer && Ball.blueBallNumber == 7) {
            JOptionPane.showMessageDialog(null, "Winner is Player 2 (blue), CONGRATULATIONS ", "WINNER!", JOptionPane.INFORMATION_MESSAGE);
        }

        if (!redPlayer && Ball.blueBallNumber != 7) {
            JOptionPane.showMessageDialog(null, "Player 2 (blue) lose ", "LOSER!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < balls.length; i++) {
            balls[i].move();
        }
        repaint();

        if (allBallStopped()) {
            simulationTimer.stop();

            if (!Ball.isBallInHoleRight() && !Ball.isBallInHoleLeft() && !Ball.isBallInHoleRight() && ballShoot) {
                ballShoot = false;
                Table.noBallsInHole();
            }
        }
    }

    static boolean allBallStopped() {
        int ballsStopped = 0;
        for (Ball ball : Table.balls) {

            if (!ball.isMoving()) {
                ballsStopped++;
            }
        }

        if (ballsStopped == balls.length) {
            return true;
        }

        return false;
    }

    public void mousePressed(MouseEvent event) {
            Coord mousePosition = new Coord(event);
            balls[0].setAimPosition(mousePosition);
            repaint();
    }

    public void mouseReleased(MouseEvent e) {
        balls[0].shoot();

        if (!simulationTimer.isRunning()) {
            simulationTimer.start();
        }
    }

    public void mouseDragged(MouseEvent event) {
        Coord mousePosition = new Coord(event);
        balls[0].updateAimPosition(mousePosition);

        repaint();
    }

    public void mouseClicked(MouseEvent e) {
        if(balls[0].position.x == 1000){

            if(balls[0].position.y == 1000){
                double xPos = e.getX();
                double yPos = e.getY();

                if(placeWhiteBall(xPos,yPos) == true){
                    balls[0].position.x = xPos;
                    balls[0].position.y = yPos;
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseMoved(MouseEvent e) { }

    public void playerStringPaint(Graphics2D graphics) {
        Graphics2D g2D = graphics;
        Color firstPlayer = Color.RED;
        Font player1 = new Font("Serif", Font.BOLD, 20);
        g2D.setFont(player1);
        g2D.setColor(firstPlayer);
        g2D.drawString("Player 1", (float) (WALL_THICKNESS + Hole.HOLE_RADIUS), WALL_THICKNESS - 5);

        Color secondPlayer = Color.BLUE;
        Font player2 = new Font("Serif", Font.BOLD, 20);
        g2D.setFont(player2);
        g2D.setColor(secondPlayer);
        g2D.drawString("Player 2", (float) (WALL_THICKNESS + Hole.HOLE_RADIUS), TABLE_HEIGHT + 2 * WALL_THICKNESS - 5);
    }

    public void paintRedBalls(Graphics2D graphics) {
        int scoreBalls = 7;
        double xPos = Table.WALL_THICKNESS + 5 * Hole.HOLE_RADIUS + Ball.RADIUS;
        Graphics2D g2D = graphics;

        for (int i = 0; i < scoreBalls; i++) {
            g2D.setColor(Color.RED);
            g2D.drawOval(
                    (int) (xPos),
                    (int) (Ball.RADIUS / 2),
                    (int) (Ball.RADIUS),
                    (int) (Ball.RADIUS));
            xPos += Ball.DIAMETER;
        }

        Font redBalls = new Font("Times", Font.BOLD, 15);
        g2D.setFont(redBalls);
        g2D.setColor(Color.BLACK);
        g2D.drawString("1", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS +     Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
        g2D.drawString("2", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 2 * Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
        g2D.drawString("3", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 3 * Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
        g2D.drawString("4", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 4 * Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
        g2D.drawString("5", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 5 * Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
        g2D.drawString("6", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 6 * Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
        g2D.drawString("7", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 7 * Ball.DIAMETER -11), (float) Ball.RADIUS + 4);
    }

    public void paintBlueBalls(Graphics2D graphics) {
        int scoreBalls = 7;
        double xPos = Table.WALL_THICKNESS + 5 * Hole.HOLE_RADIUS + Ball.RADIUS;
        Graphics2D g2D = graphics;
        for (int i = 0; i < scoreBalls; i++) {
            g2D.setColor(Color.BLUE);
            g2D.drawOval(
                    (int) (xPos),
                    (int) (Table.TABLE_HEIGHT + WALL_THICKNESS + Ball.RADIUS / 2),
                    (int) (Ball.RADIUS),
                    (int) (Ball.RADIUS));
            xPos += Ball.DIAMETER;
        }

        Font blueBalls = new Font("Serif", Font.BOLD, 15);
        g2D.setFont(blueBalls);
        g2D.setColor(Color.BLACK);
        g2D.drawString("1", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS +     Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
        g2D.drawString("2", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 2 * Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
        g2D.drawString("3", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 3 * Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
        g2D.drawString("4", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 4 * Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
        g2D.drawString("5", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 5 * Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
        g2D.drawString("6", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 6 * Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
        g2D.drawString("7", (float) (Table.WALL_THICKNESS + 4.95 * Hole.HOLE_RADIUS + 7 * Ball.DIAMETER-11), TABLE_HEIGHT + 2 * WALL_THICKNESS - 6);
    }

    public static void paintFillScoreBallRed(Graphics2D graphics) {
        double redBall = Ball.redBallNumber;
            for(int i=0; i<redBall; i++) {
                double xPos = Table.WALL_THICKNESS + 5 * Hole.HOLE_RADIUS + Ball.RADIUS + i * Ball.DIAMETER;
                graphics.setColor(Color.RED);
                graphics.fillOval(
                        (int) (xPos),
                        (int) (Ball.RADIUS / 2),
                        (int) (Ball.RADIUS),
                        (int) (Ball.RADIUS));
            }
    }

    static void paintFillScoreBallBlue(Graphics2D graphics) {
            double blueBall = Ball.blueBallNumber;
        for(int i=0; i<blueBall; i++) {
            double xPos = Table.WALL_THICKNESS + 5 * Hole.HOLE_RADIUS + Ball.RADIUS + i * Ball.DIAMETER;
            graphics.setColor(Color.BLUE);
            graphics.fillOval(
                    (int) (xPos),
                    (int) (Table.TABLE_HEIGHT + WALL_THICKNESS + Ball.RADIUS / 2),
                    (int) (Ball.RADIUS),
                    (int) (Ball.RADIUS));
        }
    }

    static void player2Paint(Graphics2D graphics) {
            double xPos = Table.WALL_THICKNESS + 4.6 * Hole.HOLE_RADIUS;
            graphics.setColor(Color.WHITE);
            graphics.fillOval(
                    (int) (xPos),
                    (int) (Table.TABLE_HEIGHT + WALL_THICKNESS + Ball.RADIUS / 2),
                    (int) (Ball.RADIUS),
                    (int) (Ball.RADIUS));
    }

    static void player1Paint(Graphics2D graphics){
            double xPos = Table.WALL_THICKNESS + 4.6 * Hole.HOLE_RADIUS;
            graphics.setColor(Color.WHITE);
            graphics.fillOval(
                    (int) (xPos),
                    (int) (Ball.RADIUS/2),
                    (int) (Ball.RADIUS),
                    (int) (Ball.RADIUS));
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // This makes the graphics smoother
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2D.setColor(WALL_COLOR);
        g2D.fillRect(0, 0, TABLE_WIDTH + 2 * WALL_THICKNESS, TABLE_HEIGHT + 2 * WALL_THICKNESS);

        g2D.setColor(COLOR);
        g2D.fillRect(WALL_THICKNESS, WALL_THICKNESS, TABLE_WIDTH, TABLE_HEIGHT);

        for (int i = 0; i < balls.length; i++) {
            balls[i].paintBall(g2D);
        }

        for (int i = 0; i < holes.length - 2; i++) {
            holes[i].paintCornerHoles(g2D);
        }

        holes[4].paintLeftSideHole(g2D);
        holes[5].paintRightSideHole(g2D);

        if(!simulationTimer.isRunning()) {

            if (!redPlayer) {
                player2Paint(g2D);
            }

            if (redPlayer) {
                player1Paint(g2D);
            }
        }

        paintRedBalls(g2D);
        paintBlueBalls(g2D);

        paintFillScoreBallRed(g2D);
        paintFillScoreBallBlue(g2D);

        playerStringPaint(g2D);
    }
}

class Hole extends JPanel {
        static final double HOLE_DIAMETER = 50;
        static final double HOLE_RADIUS = 25;
        Color COLOR_HOLES = Color.BLACK;
        Coord holePosition;
        double holeCenterX;
        double holeCenterY;
        Coord centerHole;

    Hole(Coord initialPosition) {
        holePosition = initialPosition;
        holeCenterX = holePosition.x + HOLE_RADIUS;
        holeCenterY = holePosition.y + HOLE_RADIUS;
        centerHole = new Coord(holeCenterX, holeCenterY);
    }

    public void paintLeftSideHole(Graphics2D graphics) {
        Graphics2D g2D = graphics;
        g2D.setColor(COLOR_HOLES);
        g2D.fillArc(
                (int) (holePosition.x),
                (int) (holePosition.y),
                (int) (HOLE_DIAMETER),
                (int) (HOLE_DIAMETER),
                (int) (90),
                (int) (180));
    }

    public void paintRightSideHole(Graphics2D graphics) {
        Graphics2D g2D = graphics;
        g2D.setColor(COLOR_HOLES);
        g2D.fillArc(
                (int) (holePosition.x),
                (int) (holePosition.y),
                (int) (HOLE_DIAMETER),
                (int) (HOLE_DIAMETER),
                (int) (270),
                (int) (180));
    }

    public void paintCornerHoles(Graphics2D graphics) {
        Graphics2D g2D = graphics;
        g2D.setColor(COLOR_HOLES);
        g2D.fillOval(
                (int) (holePosition.x),
                (int) (holePosition.y),
                (int) (HOLE_DIAMETER),
                (int) (HOLE_DIAMETER));
    }
}

class ResetButton extends JButton implements ActionListener{
    Table myTable;
    String label = "Reset Game";

    ResetButton(Table table) {
        myTable = table;
        addActionListener(this);
        this.setText(label);
    }

    public void actionPerformed(ActionEvent e) {
        myTable.reset();
        myTable.repaint();
    }
}

 /**
 * ****************************************************************************************
 * Ball:
 * <p>
 * The ball has instance variables relating to its graphics and game state:
 * position, velocity, and the position from which a shot is aimed (if any).
 */
class Ball {

     private Color COLOR;
     static final int BORDER_THICKNESS = 2;
     static final double RADIUS = 15;
     static final double DIAMETER = 2 * RADIUS;
     private final double FRICTION = 0.015;                          // its friction constant (normed for 100 updates/second)
     private final double FRICTION_PER_UPDATE =                                 // friction applied each simulation step
             1.0 - Math.pow(1.0 - FRICTION,                       // don't ask - I no longer remember how I got to this
                     100.0 / Biljard.UPDATE_FREQUENCY);
     Coord position;
     private Coord velocity;
     private Coord aimPosition;

     static double redBallNumber = 0;
     static double blueBallNumber = 0;

    Ball(Coord initialPosition) {
         position = initialPosition;
         velocity = Coord.zero();
    }

    static void setColorBall(Ball[] myballs) {
         myballs[0].COLOR = Color.WHITE;

         for (int i = 1; i < Table.balls.length; i += 2) {
             myballs[i].COLOR = Color.RED;
         }

         for (int i = 2; i < Table.balls.length - 1; i += 2) {
             myballs[i].COLOR = Color.BLUE;
         }

         myballs[11].COLOR = Color.BLACK;
    }

   private boolean isAiming() {
         return aimPosition != null;
   }

   boolean isMoving() {
         return velocity.magnitude() > FRICTION_PER_UPDATE;
   }

   String isHitWall() {
         if (position.x >= Table.TABLE_WIDTH + Table.WALL_THICKNESS - RADIUS && velocity.x >= 0) {
             return "West";
         }

         if (position.x <= Table.WALL_THICKNESS + RADIUS && velocity.x <= 0) {
             return "East";
         }

         if (position.y >= Table.TABLE_HEIGHT + Table.WALL_THICKNESS - RADIUS && velocity.y >= 0) {
             return "South";
         }

         if (position.y <= Table.WALL_THICKNESS + RADIUS && velocity.y <= 0) {
             return "North";
         }

         return "No bounce";
   }

   static boolean isBallInCornerHole(){
        for (Ball ball : Table.balls) {
            Coord posBall = ball.position;

            for (int i = 0; i < Table.holes.length - 2; i++) {
                Coord posHole = Table.holes[i].centerHole;

                if (Coord.distance(posBall, posHole) <= (RADIUS + Hole.HOLE_RADIUS)) {
                    return true;
                }
            }
        }
         return false;
   }

   static boolean isBallInHoleLeft() {
        for (Ball ball : Table.balls) {
            Coord leftHole = Table.holes[4].centerHole;

            if ((ball.position.x - leftHole.x) <= Ball.RADIUS + Ball.BORDER_THICKNESS) {

                if (ball.position.y - leftHole.y <= Hole.HOLE_RADIUS && leftHole.y - ball.position.y <= Hole.HOLE_RADIUS) {
                    return true;
                }
            }
        }
        return false;
   }

   static boolean isBallInHoleRight(){
        for (Ball ball : Table.balls) {
            Coord rightHole = Table.holes[5].centerHole;

            if ((rightHole.x - ball.position.x) <= Ball.RADIUS + Ball.BORDER_THICKNESS) {

                if ((ball.position.y - rightHole.y) <= Hole.HOLE_RADIUS && rightHole.y - ball.position.y <= Hole.HOLE_RADIUS) {
                    return true;
                }
            }
        }
        return false;
   }

   void ballInCornerHole() {
         for (Ball ball : Table.balls) {
             Coord posBall = ball.position;

             for (int i = 0; i < Table.holes.length - 2; i++) {
                 Coord posHole = Table.holes[i].centerHole;

                 if (Coord.distance(posBall, posHole) <= (RADIUS + Hole.HOLE_RADIUS)) {

                     if (ball.COLOR == Color.RED) {
                         ball.position = new Coord(1000, 1000);
                         ball.velocity = new Coord(0, 0);
                         redBallNumber++;
                         Table.redBall = true;
                         Table.ballsInHoleRules();
                     }

                     if (ball.COLOR == Color.BLUE) {
                         ball.position = new Coord(920, 920);
                         ball.velocity = new Coord(0, 0);
                         blueBallNumber++;
                         Table.redBall = false;
                         Table.ballsInHoleRules();
                     }

                     if(ball.COLOR == Color.WHITE){
                         Table.whiteBall = 1;
                         ball.position = new Coord(1000, 1000);
                         ball.velocity = new Coord (0, 0);
                         Table.ballsInHoleRules();
                     }

                     if (ball.COLOR == Color.BLACK) {
                         ball.position = new Coord(900, 900);
                         ball.velocity = new Coord(0, 0);
                         Table.blackBall();
                     }
                 }
             }
         }
   }

   void ballInHoleLeft() {
         for (Ball ball : Table.balls) {
             Coord leftHole = Table.holes[4].centerHole;

             if ((ball.position.x - leftHole.x) <= Ball.RADIUS + Ball.BORDER_THICKNESS) {

                 if (ball.position.y - leftHole.y <= Hole.HOLE_RADIUS && leftHole.y - ball.position.y <= Hole.HOLE_RADIUS) {

                     if (ball.COLOR == Color.RED) {
                         ball.position = new Coord(1000, 1000);
                         ball.velocity = new Coord(0, 0);
                         redBallNumber++;
                         Table.redBall = true;
                         Table.ballsInHoleRules();
                     }

                     if (ball.COLOR == Color.BLUE) {
                         ball.position = new Coord(920, 920);
                         ball.velocity = new Coord(0, 0);
                         blueBallNumber++;
                         Table.redBall = false;
                         Table.ballsInHoleRules();
                     }

                     if(ball.COLOR == Color.WHITE) {
                         Table.whiteBall = 1;
                         ball.position = new Coord(1000, 1000);
                         ball.velocity = new Coord (0, 0);
                         Table.ballsInHoleRules();
                     }

                     if (ball.COLOR == Color.BLACK) {
                         ball.position = new Coord(900, 900);
                         ball.velocity = new Coord(0, 0);
                         Table.blackBall();
                     }
                 }
             }
         }
   }

   void ballInHoleRight() {
         for (Ball ball : Table.balls) {
             Coord rightHole = Table.holes[5].centerHole;

             if ((rightHole.x - ball.position.x) <= Ball.RADIUS + Ball.BORDER_THICKNESS) {

                 if ((ball.position.y - rightHole.y) <= Hole.HOLE_RADIUS && rightHole.y - ball.position.y <= Hole.HOLE_RADIUS) {

                     if (ball.COLOR == Color.RED) {
                         ball.position = new Coord(1000, 1000);
                         ball.velocity = new Coord(0, 0);
                         redBallNumber++;
                         Table.redBall = true;
                         Table.ballsInHoleRules();
                     }

                     if (ball.COLOR == Color.BLUE) {
                         ball.position = new Coord(920, 920);
                         ball.velocity = new Coord(0, 0);
                         blueBallNumber++;
                         Table.redBall = false;
                         Table.ballsInHoleRules();
                     }

                     if(ball.COLOR == Color.WHITE) {
                         Table.whiteBall = 1;
                         ball.position = new Coord(1000, 1000);
                         ball.velocity = new Coord (0, 0);
                         Table.ballsInHoleRules();
                     }

                     if (ball.COLOR == Color.BLACK) {
                         ball.position = new Coord(900, 900);
                         ball.velocity = new Coord(0, 0);
                         Table.blackBall();
                     }
                 }
             }
         }
   }

   void isHitBalls(Ball otherBall) {
         if (otherBall != this) {

             if (Coord.distance(this.position, otherBall.position) <= 2 * RADIUS) {
                 ballsHit(otherBall);
             }
         }
   }

   void ballsHit(Ball otherBall) {
         double dx = (this.position.x - otherBall.position.x) / Math.sqrt((Math.pow((this.position.x - otherBall.position.x), 2) + Math.pow((this.position.y - otherBall.position.y), 2)));
         double dy = (this.position.y - otherBall.position.y) / Math.sqrt((Math.pow((this.position.x - otherBall.position.x), 2) + Math.pow((this.position.y - otherBall.position.y), 2)));
         double J = otherBall.velocity.x * dx + otherBall.velocity.y * dy - (this.velocity.x * dx + this.velocity.y * dy);

         double pThisAfterX = this.velocity.x + J * dx;
         double pThisAfterY = this.velocity.y + J * dy;

         double pOtherAfterX = otherBall.velocity.x - J * dx;
         double pOtherAfterY = otherBall.velocity.y - J * dy;

         double distanceBefore = Coord.distance(this.position, otherBall.position);
         double distanceAfterX = (this.position.x - otherBall.position.x) + (this.velocity.x - otherBall.velocity.x);
         double distanceAfterY = (this.position.y - otherBall.position.y) + (this.velocity.y - otherBall.velocity.y);
         double distanceAfter = Math.sqrt(Math.pow(distanceAfterX, 2) + Math.pow(distanceAfterY, 2));

         if (distanceBefore > distanceAfter) {
             this.velocity.x = pThisAfterX;
             this.velocity.y = pThisAfterY;
             otherBall.velocity.x = pOtherAfterX;
             otherBall.velocity.y = pOtherAfterY;
         }
   }

   void setAimPosition(Coord grabPosition) {
         if (Table.allBallStopped()) {

             if (Coord.distance(position, grabPosition) <= RADIUS) {
                 aimPosition = grabPosition;
             }
         }
   }

   void updateAimPosition(Coord newPosition) {
         if (Table.allBallStopped()) {

             if (isAiming()) {
                 aimPosition = newPosition;
             }
         }
   }

   void shoot() {
         if (Table.allBallStopped()) {

             if (isAiming()) {
                 Coord aimingVector = Coord.sub(position, aimPosition);
                 velocity = Coord.mul(Math.sqrt(35.0 * aimingVector.magnitude() / Biljard.UPDATE_FREQUENCY),
                         aimingVector.norm());
                 aimPosition = null;
                 Table.ballShoot = true;
             }
         }
   }

   void move() {

         if (isMoving()) {
             position.increase(velocity);
             velocity.decrease(Coord.mul(FRICTION_PER_UPDATE, velocity.norm()));

             if (isHitWall() == "West" | isHitWall() == "East") {
                 velocity.x = -velocity.x;
             }

             if (isHitWall() == "North" | isHitWall() == "South") {
                 velocity.y = -velocity.y;
             }

             for (Ball ball : Table.balls) {
                 isHitBalls(ball);
             }

             if (isBallInCornerHole()) {
                 ballInCornerHole();
             }

             if (isBallInHoleLeft()) {
                 ballInHoleLeft();
             }

             if (isBallInHoleRight()) {
                 ballInHoleRight();
             }
         }
   }

   void paintBall(Graphics2D g2D) {
        g2D.setColor(Color.BLACK);
        g2D.fillOval(
                (int) (position.x - RADIUS + 0.5),
                (int) (position.y - RADIUS + 0.5),
                (int) (DIAMETER),
                (int) (DIAMETER));
        g2D.setColor(COLOR);
        g2D.fillOval(
                (int) (position.x - RADIUS + 0.5 + BORDER_THICKNESS),
                (int) (position.y - RADIUS + 0.5 + BORDER_THICKNESS),
                (int) (DIAMETER - 2 * BORDER_THICKNESS),
                (int) (DIAMETER - 2 * BORDER_THICKNESS));

        if (isAiming()) {
            paintAimingLine(g2D);
        }
   }

    private void paintAimingLine(Graphics2D graph2D) {
        Coord.paintLine(
                graph2D,
                aimPosition,
                Coord.sub(Coord.mul(2, position), aimPosition)
        );
    }
}
