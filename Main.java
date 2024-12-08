public class Main {
    public static void main(String[] args) {
      
        //Create our graph
        double[][] adjMat = new double[10][10];
        for (int i=0; i< adjMat.length; i++){
            for (int j=0; j< adjMat[i].length; j++) {
                if (i==j)
                    continue;
                adjMat[i][j]=(Math.random()<0.8)?0:(Math.random()*20);
            }
        }
        Display d=new Display(800, 800, adjMat);
        //Example for renaming
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Examples for renaming and marking knots
        d.changeKnotName(0, "Example");
        d.markKnot(0, true);
        //d.setFillKnots(true);
        //d.setPrintWeight(false);
    }
}
