import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Klasse Knoten.
 * für die Darstellungen eines Graphen mittels Adjazenzmatrix
 * auf der Basis des Programms von O.Zimmermann (Benötigt mindestens die Klasse Knoten)
 *
 * @author F.Paul & J.S.Dschungelskog
 * @version 1.0.3-beta
 *
 * @source https://github.com/Info-LK-Joe-Simon/Graph-Visualization
 */

public class Display extends Thread {
    private JFrame frame;
    private Input input;
    private Graphics graphics, g;
    private BufferedImage image;

    private String versionInfo="";

    // Create a menu bar
    JMenuBar menuBar = new JMenuBar();
    JMenu knotMenuBar = new JMenu("Knot");
    JMenuItem knotMarkItem = new JMenuItem("MarkKnot");
    JMenuItem knotUnmarkItem = new JMenuItem("UnmarkKnot");
    JMenu aboutMenuBar = new JMenu("About");
    JMenuItem aboutCreditsItem = new JMenuItem("Credits");
    JMenuItem aboutGithubItem = new JMenuItem("Source Code");

    private int width, height;
    private int[] oldMousePos={-1,-1};

    private double[] pos={0.0,0.5};

    private double zoom=1.0;
    private double[][] adjazenzmatrix;
    private double[][] laplacematrix;

    private int[] listByLeastConncection;

    private Knot[] knots;
    private Knoten[] knoten=null;

    private boolean undecorated=false;
    private boolean darkmode=false;

    private Color c_white=Color.WHITE;
    private Color c_black=Color.BLACK;
    private Color c_orange=Color.ORANGE;
    private Color c_green=Color.GREEN;
    private Color c_magenta=Color.MAGENTA;

    private float knot_radius=25;
    private float selectedKnotRadius=2;
    private Knot currentDraggedKnot=null;
    private ArrayList<Integer> listOfSelectedKnots = new ArrayList<Integer>();;

    private boolean printWeight=true;

    private boolean fillKnots=true;

    private int decimalPlaces = 2;

    public Display(int w, int h, boolean autostart){
        initilize(w, h, null, autostart);
    }
    public Display(int w, int h){
        initilize(w, h, null,true);
    }
    public Display(){
        initilize(800, 600, null,true);
    }

    public Display(int w, int h, double[][] adjazenzmatrix, boolean autostart){
        initilize(w, h, adjazenzmatrix, autostart);
    }
    public Display(int w, int h, double[][] adjazenzmatrix){
        initilize(w, h, adjazenzmatrix,true);
    }
    public Display(double[][] adjazenzmatrix){
        initilize(800, 600, adjazenzmatrix,true);
    }
    public void initilize(int w, int h, double[][] adjazenzmatrix, boolean autostart){
        versionInfo=VersionChecker.check_version("./src/Display.java", "https://github.com/Info-LK-Joe-Simon/Graph-Visualization/blob/main/Display.java");
        width=w;
        height=h;
        frame= new JFrame();

        //Interesting to play with you might set undecorated to true like:
        //undecorated=true;
        frame.setUndecorated(undecorated);

        frame.setSize(new Dimension(width+16, height+30));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.setVisible(true);

        frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        Image icon = Toolkit.getDefaultToolkit().getImage("./src/DisplayingGraphLogo.png");
        frame.setIconImage(icon);

        frame.setTitle("Graph - Visualisation");
        frame.getContentPane().setBackground(Color.BLACK);

        input=new Input();
        frame.addKeyListener(input);
        frame.addMouseListener(input);
        frame.addMouseMotionListener(input);
        frame.addMouseWheelListener(input);
        frame.addFocusListener(input);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        g= frame.getGraphics();
        g.setColor(darkmode?c_white:c_black);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        image=new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        graphics= image.getGraphics();
        graphics.setColor(darkmode?c_white:c_black);
        graphics.fillRect(0,0,image.getWidth(), image.getHeight());
        graphics.setFont(new Font("Arial", Font.PLAIN, 32));

        this.adjazenzmatrix=adjazenzmatrix;

        //addMenuBar();

        if (autostart)
            this.start();
        //credtis();
        openMenuWindow();
    }

    public Input getInput(){
        return input;
    }


    private void addMenuBar(JFrame f) {
        knotMenuBar.add(knotMarkItem);
        knotMenuBar.add(knotUnmarkItem);
        knotMarkItem.addActionListener(e -> markKnots(listOfSelectedKnots, true));
        knotUnmarkItem.addActionListener(e -> markKnots(listOfSelectedKnots, false));
        menuBar.add(knotMenuBar);

        aboutCreditsItem.addActionListener(e -> credtis());
        aboutGithubItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Info-LK-Joe-Simon/Graph-Visualization/blob/main/"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        aboutMenuBar.add(aboutCreditsItem);
        aboutMenuBar.add(aboutGithubItem);
        menuBar.add(aboutMenuBar);

        f.setJMenuBar(menuBar);
        f.revalidate();
        f.repaint();
    }

    public void switchToDarkMde() {
        darkmode = !darkmode;
        frame.getContentPane().setBackground(darkmode ? c_black : c_white);
        graphics.setColor(darkmode ? c_white : c_black);
        g.setColor(darkmode ? c_white : c_black);
        frame.repaint();
    }

    public void setPrintWeight(boolean printWeight) {
        this.printWeight = printWeight;
    }

    public boolean getPrintWeigth(){
        return printWeight;
    }

    public void setFillKnots(boolean f){fillKnots=f;}

    public boolean getFillKnots(){return fillKnots;}

    private double[][] getLaplaceMatrix(double[][] a){
        if(laplacematrix!=null)
            return laplacematrix;
        laplacematrix=new double[a.length][a[0].length];
        for(int i=0; i<a.length; i++) {
            int numOfConnections=0;
            for (int j=0; j < a.length; j++) {
                if (j != i && a[i][j] != 0)
                    numOfConnections++;
                laplacematrix[i][j]=a[i][j];
            }
            laplacematrix[i][i]=numOfConnections;
        }
        return laplacematrix;
    }

    private void prepareKnots(double[][] a){
        laplacematrix=getLaplaceMatrix(a);
        if(knots==null)
            knots = new Knot[a.length];
        if(a.length!= knots.length)
            knots = new Knot[a.length];
        for(int i=0; i<a.length; i++){
            if(knots[i]==null)
                prepareKnot(a, Integer.toString(i), false, i);
        }
    }

    private void prepareKnot(double[][] a, String name, boolean marked, int i){
        float[] pos = getKnotPos(a, i);
        if(knoten==null)
            knots[i]=new Knot(pos, name, marked);
        else if(knoten[i]==null)
            knots[i]=new Knot(pos, name, marked);
        else
            knots[i]=new Knot(pos, knoten[i].getBezeichnung(), knoten[i].getMarkierung());
    }

    private float[] getKnotPos(double[][] a, int i){
        float[] pos=new float[2];
        int length=a.length;
        if(length%2==0){
            if(i==0)
                return new float[]{width / 2, height / 2};
            i-=1;
            length-=1;
        }
        if(i%2!=0)
            i=length-(i+1)/2;
        else
            i=i/2;
        if(true) {
            pos[0] = (float) (Math.sin(2 * Math.PI / length * i)) * width / 3 + width / 2;
            pos[1] = (float) (Math.cos(2 * Math.PI / length * i)) * height / 3 + height / 2;
        }
        return pos;
        //return new float[]{(float) (Math.random() * width), (float) (Math.random() * height)};
    }

    private void draw(){
        update();
        if (adjazenzmatrix!=null) {
            prepareKnots(adjazenzmatrix);
            drawGraph(adjazenzmatrix);
        }
        swapBuffers();
    }

    private void drawGraph(double[][] a){
        for(int i=0; i < knots.length; i++)
            for (int j = 0; j < adjazenzmatrix.length; j++)
                if (adjazenzmatrix[i][j] != 0 || adjazenzmatrix[j][i] != 0)
                    drawLineBetweenKnots(i, j);
        for(int i=0; i < knots.length; i++){
            drawKnot(i);
        }
    }

    private void drawKnot(int i){
        if(listOfSelectedKnots.contains(i)) {
            graphics.setColor(c_magenta);
            graphics.fillOval((int) (knots[i].getX() - (knot_radius + selectedKnotRadius) + pos[0]), (int) (knots[i].getY() - (knot_radius + selectedKnotRadius) + pos[1]), (int) ((knot_radius + selectedKnotRadius) * 2), (int) ((knot_radius + selectedKnotRadius) * 2));
        }
        graphics.setColor(knots[i].marked?(darkmode ? c_green : c_orange):darkmode?c_white:c_black);
        graphics.fillOval((int)(knots[i].getX()-knot_radius+pos[0]),(int)(knots[i].getY()-knot_radius+pos[1]),(int)(knot_radius*2),(int)(knot_radius*2));
        graphics.setColor(fillKnots?knots[i].marked?(darkmode ? c_green : c_orange):darkmode?c_white:c_black:!darkmode?c_white:c_black);
        graphics.fillOval((int)(knots[i].getX()-(knot_radius-1)+pos[0]),(int)(knots[i].getY()-(knot_radius-1)+pos[1]),(int)((knot_radius-1)*2),(int)((knot_radius-1)*2));

        graphics.setColor((fillKnots?!darkmode:darkmode)?c_white:c_black);
        graphics.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int nameWidth = fontMetrics.stringWidth(knots[i].getName());
        int nameHeight = fontMetrics.getHeight();
        graphics.drawString(
                knots[i].getName(),
                (int)(knots[i].getX() - nameWidth / 2 + pos[0]),
                (int)(knots[i].getY() + nameHeight / 4 + pos[1])
        );
    }

    private void drawLineBetweenKnots(int i, int j) {
        if(adjazenzmatrix[i][j]==0&&adjazenzmatrix[j][i]==0)
            return;
        if(adjazenzmatrix[i][j] >= 0 && adjazenzmatrix[j][i] >= 0)
            graphics.setColor(darkmode ? c_white : c_black);
        else if(adjazenzmatrix[i][j] < 0 || adjazenzmatrix[j][i] < 0)
            graphics.setColor(darkmode ? c_green : c_orange);
        graphics.drawLine((int) (knots[i].getX()+pos[0]), (int) (knots[i].getY()+pos[1]), (int) (knots[j].getX()+pos[0]), (int) (knots[j].getY()+pos[1]));

        if(!printWeight)
            return;

        int midX = (int) ((knots[i].getX() + knots[j].getX()) / 2 + pos[0]);
        int midY = (int) ((knots[i].getY() + knots[j].getY()) / 2 + pos[1]);

        graphics.setColor(darkmode ? c_white : c_black);
        graphics.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fontMetrics = graphics.getFontMetrics();

        if(i<=j)
            if(Math.abs(adjazenzmatrix[i][j])==Math.abs(adjazenzmatrix[j][i])) {
                String weight = Double.toString(Math.abs(roundToDecimalPlaces(adjazenzmatrix[i][j], decimalPlaces)));
                int weightWidth = fontMetrics.stringWidth(weight);
                int weightHeight = fontMetrics.getHeight();
                graphics.drawString(weight, midX, midY - weightHeight/2);
            }
            else{
                String weight = (adjazenzmatrix[i][j] == 0 ? "" : (knots[i].getX()>knots[j].getX()?"<- ":"-> ") + Math.abs(roundToDecimalPlaces(adjazenzmatrix[i][j], decimalPlaces))) + ((adjazenzmatrix[i][j] == 0 || adjazenzmatrix[j][i] == 0) ? "" : " | ") + (adjazenzmatrix[j][i] == 0 ? "" : (knots[i].getX()>knots[j].getX()?"-> ":"<- ") + Math.abs(roundToDecimalPlaces(adjazenzmatrix[j][i], decimalPlaces)));
                int weightWidth = fontMetrics.stringWidth(weight);
                int weightHeight = fontMetrics.getHeight();
                graphics.drawString(weight, midX, midY - weightHeight / 2);
            }
    }
    private void update(){
        updateSize();
        updatePos();
        image=new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        graphics= image.getGraphics();
        graphics.setColor(darkmode?c_black:c_white);
        graphics.fillRect(0,0,image.getWidth(), image.getHeight());
    }

    private void updateSize(){
        height=frame.getHeight();
        width=frame.getWidth();
    }
    private void swapBuffers(){
        g.drawImage(image, 0, 0, frame);
    }

    private void updatePos(){
        if(input.inFocus){
            if(input.getMouse(1)) {
                mouseMoved();
            }
            else {
                frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                currentDraggedKnot=null;
            }
            zoom+= input.getMouseWheel()/-50.0;
            if(zoom<=0){
                zoom=0;
            }
        }
        oldMousePos[0] = input.getMouseX();
        oldMousePos[1] = input.getMouseY();
    }

    private void mouseMoved(){
        int i=0;
        for(Knot knot : knots){
            float[] knotPos = knot.getPos();
            float distance = (float) Math.sqrt(Math.pow(oldMousePos[0] - (knotPos[0] + pos[0]), 2) + Math.pow(oldMousePos[1] - (knotPos[1] + pos[1]), 2));

            if ((distance < (listOfSelectedKnots.contains(i)?knot_radius + selectedKnotRadius: knot_radius) && currentDraggedKnot == null) || currentDraggedKnot == knot) {
                if (listOfSelectedKnots.isEmpty())
                    listOfSelectedKnots.add(i);
                if (input.getKey(KeyEvent.VK_SHIFT) || input.getKey(KeyEvent.VK_CONTROL)) {
                    if (!listOfSelectedKnots.contains(i))
                        listOfSelectedKnots.add(i);
                } else {
                    listOfSelectedKnots.clear();
                    listOfSelectedKnots.add(i);
                }
                currentDraggedKnot=knot;
                knot.setX((float) (input.getMouseX() - pos[0]));
                knot.setY((float) (input.getMouseY() - pos[1]));
                frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
            i++;
        }
        listOfSelectedKnots.clear();
        currentDraggedKnot = null;
        pos[0] += (input.getMouseX() - oldMousePos[0]);
        pos[1] += (input.getMouseY() - oldMousePos[1]);
        frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setDecimalPlaces (int dp){
        decimalPlaces=dp;
    }

    public int getDecimalPlaces (){
        return decimalPlaces;
    }

    public void setKnoten(Knoten[] k){
        knoten=k;
    }

    public Knoten[] getKnoten(){
        return knoten;
    }

    public void renameKnot(int index, String name){
        if(knots==null) {
            System.out.println("Warning: Knots array is null. If this is unexpected, try waiting for the thread to initialize. :)");
            return;
        }
        if(index<knots.length)
            knots[index].setName(name);
        else
            System.out.printf("Error: Attempted to access index %d, but it is out of bounds of Knots. Valid indices are between 0 and %d.%n", index, knots.length - 1);

    }

    public void renameKnots(ArrayList<Integer> indices, String name){
        for (int index : indices) {
            renameKnot(index, name);
        }

    }

    public void renameKnots(int[] indices, String name){
        for (int index : indices) {
            renameKnot(index, name);
        }

    }

    public void markKnot(int index, boolean marked){
        if(knoten!=null&&knoten[index]!=null&&marked) //Unfortunately, there is no option to demark a knot in O.Zimmermanns Knoten.
            knoten[index].setMarkierung();
        if(knots==null) {
            System.out.println("Warning: Knots array is null. If this is unexpected, try waiting for the thread to initialize. :)");
            return;
        }
        if(index<knots.length)
            knots[index].setMarked(marked);
        else
            System.out.printf("Error: Attempted to access index %d, but it is out of bounds of Knots. Valid indices are between 0 and %d.%n", index, knots.length - 1);

    }

    public void markKnots(ArrayList<Integer> indices, boolean marked){
        for (int index : indices) {
            markKnot(index, marked);
        }

    }

    public void markKnots(int[] indices, boolean marked){
        for (int index : indices) {
            markKnot(index, marked);
        }

    }

    public void printAdjazenzMatrix() {
        if (adjazenzmatrix == null || adjazenzmatrix.length == 0) {
            System.out.println("The adjacency matrix is empty.");
            return;
        }

        System.out.println("Adjacency Matrix:");
        for (int i = 0; i < adjazenzmatrix.length; i++) {
            for (int j = 0; j < adjazenzmatrix[i].length; j++) {
                System.out.print(adjazenzmatrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public void run(){
        while (true) {
            draw();
            if(input.getKey(KeyEvent.VK_ESCAPE))
                executeOrder66();
        }
    }

    private void executeOrder66() {System.exit(0);}

    public void setAdjazenzmatrix(double[][] a){adjazenzmatrix=a;}
    public double[][] getAdjazenzmatrix(){return adjazenzmatrix;}
    private static double roundToDecimalPlaces(double value, int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return Math.round(value * factor) / factor;
    }

    public class Knot{
        private float[] pos = new float[2];
        private String name="";
        private boolean marked=false;
        public Knot(float[] pos, String name, boolean marked){
            this.pos=pos;
            this.name=name;
            this.marked=marked;
        }

        public void setPos(float[] pos) {
            this.pos = pos;
        }

        public void setX(float x) {
            this.pos[0] = x;
        }
        public void setY(float y) {
            this.pos[1] = y;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        public float[] getPos() {
            return pos;
        }

        public float getX(){
            return pos[0];
        }
        public float getY(){
            return pos[1];
        }

        public String getName() {
            return name;
        }

        public boolean getMarked() {
            return marked;
        }
    }


    public void credtis() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Credits");
            Image icon = Toolkit.getDefaultToolkit().getImage("./src/DisplayingGraphLogo.png");
            frame.setIconImage(icon);
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create an instance of the CreditsPanel class
            CreditsPanel creditsPanel = new CreditsPanel(frame);
            frame.add(creditsPanel);

            // Set frame properties
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Start the scrolling animation
            creditsPanel.startAnimation();
        });
    }

    class CreditsPanel extends JPanel {
        private class Star{
            private double[] velocity = new double[2];
            private double[] pos = new double[2];
            public Star(double speed_factor){
                velocity[0]=(Math.random()-0.5)*speed_factor;
                velocity[1]=(Math.random()-0.5)*speed_factor;
                pos[0]=0.5;
                pos[1]=0.5;
            }
            public void update(){
                pos[0]+=velocity[0];
                pos[1]+=velocity[1];
                if(pos[0]<0||pos[0]>1||pos[1]<0||pos[1]>1){
                    pos[0]=0.5;
                    pos[1]=0.5;
                }
            }
        }
        private int num_of_stars=100;
        private Star[] stars = new Star[num_of_stars];
        private String monologue = "GRAPH\n" +
                "WARS\n" +
                "\n" +
                "A long time ago in a galaxy far, far away...\n" +
                "An Info LK from Zimmermann fought an endless battle\n" +
                "against the \"Zentralabitur\" and its brutal regulations\n" +
                "and operators.\n" +
                "\n" +
                "But a group of students did not stop resisting the intruders.\n" +
                "Among them were the most brilliant minds of the Galaxy,\n" +
                "a team of cunning and resourceful beings who mastered all forms of logic and code.\n" +
                "\n" +
                "They were led by the fearless Joe Simon Dschungelskog,\n" +
                "a student with the wisdom of the ancients and the will of a thousand warriors.\n" +
                "\n" +
                "Together, they built their resistance, using algorithms as their weapons,\n" +
                "and data structures as their shields.\n" +
                "Their knowledge of sorting and searching algorithms was unmatched,\n" +
                "and they fought bravely to crack the complex system of the Zentralabitur.\n" +
                "\n" +
                "The battle raged on, with firewalls and encryption schemes falling one by one.\n" +
                "They planted viruses of rebellion deep within the Ministry's database,\n" +
                "disrupting their cruel plans and rewriting the very laws of assessment.\n" +
                "\n" +
                "In the final hours of the conflict, the forces of the Zentralabitur\n" +
                "faced an unexpected defeat. The students had forged an unbreakable\n" +
                "alliance under the leadership of Joe.\n" +
                "\n" +
                "The battle was won, and the brutal system of the Zentralabitur was no more.\n" +
                "They had triumphed through their intellect, their unity, and their unwavering resolve.\n" +
                "Now, the galaxy would be free to learn, create,\n" +
                "and explore without the shackles of standardized testing.\n" +
                "\n" +
                "The rebellion had succeeded.\n" +
                "And thus, a new era began.\n";

        private float scrollSpeed = 1.5F;
        private Timer timer;
        private float yOffset;

        private JFrame frame;

        public CreditsPanel(JFrame f) {
            setBackground(Color.BLACK);
            setForeground(Color.YELLOW);
            frame=f;
            setFont(new Font("Arial", Font.PLAIN, 20));
            this.grabFocus();
            for(int i=0; i<num_of_stars; i++)
                stars[i]=new Star(0.01);
        }

        public void startAnimation() {
            timer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    yOffset -= scrollSpeed;
                    for(int i=0; i<num_of_stars; i++)
                        stars[i].update();
                    repaint();
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(Color.WHITE);
            for(int i=0; i<num_of_stars; i++)
                g2d.drawLine((int)(stars[i].pos[0]*getWidth()), (int)(stars[i].pos[1]*getHeight()), (int)(stars[i].pos[0]*getWidth()), (int)(stars[i].pos[1]*getHeight()));

            g2d.setColor(getForeground());

            String[] monologue_array=monologue.split("\n");

            for (int i=0; i<monologue_array.length; i++) {
                String m=monologue_array[i];

                // Set the font for the monologue text
                FontMetrics fontMetrics = g2d.getFontMetrics();
                int textWidth = fontMetrics.stringWidth(m);
                int textHeight = fontMetrics.getHeight();

                // Draw the monologue text at the calculated position
                int x = (getWidth() - textWidth) / 2;
                int y = (int)(getHeight() + yOffset + textHeight*i);
                g2d.drawString(m, x, y);

                // Draw the text again to handle the case when it goes off the screen
                if (getHeight() + yOffset + textHeight * monologue_array.length < 0) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        }
    }

    public class MenuWindow extends JFrame {
        public MenuWindow(Display display) {
            Image icon = Toolkit.getDefaultToolkit().getImage("./src/DisplayingGraphLogo.png");
            setIconImage(icon);
            setTitle("Menu");
            setSize(400, 200);
            setDefaultCloseOperation(undecorated?WindowConstants.EXIT_ON_CLOSE:WindowConstants.DO_NOTHING_ON_CLOSE);

            setLayout(new BorderLayout());
            addMenuBar(this);

            JLabel versionLabel = new JLabel(VersionChecker.getVersionInfo());
            versionLabel.setHorizontalAlignment(SwingConstants.LEFT);
            JPanel versionPanel = new JPanel(new BorderLayout());
            versionPanel.add(versionLabel, BorderLayout.WEST);
            add(versionPanel, BorderLayout.NORTH);

            JCheckBox darkModeCheckbox = new JCheckBox("Enable Dark Mode");
            darkModeCheckbox.setSelected(display.darkmode);
            darkModeCheckbox.addActionListener(e -> display.switchToDarkMde());

            JCheckBox fillKnotsCheckbox = new JCheckBox("Fill Knots");
            fillKnotsCheckbox.setSelected(display.fillKnots);
            fillKnotsCheckbox.addActionListener(e -> fillKnots=!fillKnots);

            JPanel settingsPanel = new JPanel();
            settingsPanel.add(darkModeCheckbox);
            settingsPanel.add(fillKnotsCheckbox);

            add(settingsPanel, BorderLayout.CENTER);

            JLabel footerLabel = new JLabel("<html>This service is brought to you by<br>Joe Simon D. & Fabian P.</html>");
            footerLabel.setHorizontalAlignment(SwingConstants.LEFT);
            JPanel footerPanel = new JPanel(new BorderLayout());
            footerPanel.add(footerLabel, BorderLayout.WEST);

            add(footerPanel, BorderLayout.SOUTH);
        }
    }
    private void openMenuWindow() {
        SwingUtilities.invokeLater(() -> {
            MenuWindow settingsWindow = new MenuWindow(this);
            settingsWindow.setVisible(true);
        });
    }

    //Check for latest version
    public class VersionChecker {
        private static String versionInfo="Version is not checked yet";
        public static String check_version(String localFilePath, String githubFileUrl) {
            versionInfo ="";
            try {
                String localVersion = extractVersionFromLocalFile(localFilePath);
                String githubVersion = fetchFileVersionFromGithub(githubFileUrl);

                //Compare versions
                if (localVersion == null) {
                    versionInfo ="Could not find @version in the local file.";
                    System.out.println(versionInfo);
                    return versionInfo;
                }
                if (githubVersion == null) {
                    versionInfo ="Could not find @version in the header.";
                } else if (localVersion.equals(githubVersion)) {
                    versionInfo ="Version is up-to-date: " + localVersion;
                } else {
                    versionInfo ="Version mismatch! Local: " + localVersion + ", GitHub: " + githubVersion +" \nPLEASE UPDATE";
                }
            } catch (Exception e) {
                e.printStackTrace();
                versionInfo ="Failed to check version.";
            }
            System.out.println(versionInfo);
            return versionInfo;
        }
        // Extract the @version field from a file on the local system
        private static String extractVersionFromLocalFile(String filePath) throws IOException {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null)
                    content.append(line).append("\n");
            }
            return extractVersionFromHeader(content.toString());
        }

        // Extract the @version field from a file on github
        private static String fetchFileVersionFromGithub(String fileUrl) throws Exception {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != 200)
                throw new IOException("Failed to fetch file. HTTP error code: " + connection.getResponseCode());

            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    content.append(inputLine);
            }

            return extractVersionFromHeader(content.toString()); // Return file content
        }
        // Extract the @version field from the header comment
        private static String extractVersionFromHeader(String content) {
            // Regex to find @version followed by a version number
            Pattern versionPattern = Pattern.compile("@version\\s+([\\d.]+(?:-\\w+)?)");
            Matcher matcher = versionPattern.matcher(content);
            return matcher.find()?matcher.group(1):null;
        }

        public static String getVersionInfo(){
            return versionInfo;
        }
    }
}
