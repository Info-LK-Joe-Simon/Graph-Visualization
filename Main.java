public class Main {
    public static void main(String[] args) {
      
        //Create our graph
        double[][] adjMat = new double[10][10];
        for (int i=0; i< adjMat.length; i++){
            for (int j=0; j< adjMat[i].length; j++) {
                if (i==j)
                    continue;
                adjMat[i][j]=(Math.random()<0.5)?0:(Math.random()*20);
            }
        }
      
        Display d=new Display(800, 800, AdjMat/*Adjacency matrix*/);
        //d.setFillKnots(true);
        //d.setPrintWeight(false);
    }
}
