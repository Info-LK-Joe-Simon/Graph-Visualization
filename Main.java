public class Main {
    public static void main(String[] args) {
      
        //Create our graph
        int[][] adjMat = new int[10][10];
        for (int i=0; i< adjMat.length; i++){
            for (int j=0; j< adjMat[i].length; j++) {
                adjMat[i][j]=(Math.random()<0.5)?0:(int)(Math.random()*20);
                System.out.println(i+" "+j+" "+adjMat[i][j]);
            }
        }
      
        Display d=new Display(800, 800, AdjMat/*Adjacency matrix*/);
        //d.setFillKnots(true);
        //d.setPrintWeight(false);
    }
}
