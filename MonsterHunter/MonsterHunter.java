/* MonsterHunter.java
 * Sets the minimum number of traps, finds the maximum steps before the monster's capture, and minimum number of steps to set traps
 * March 27 2018
 * Raymond Wang
 */

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.Scanner;

class MonsterHunter{
  /**
   * readFile
   * Reads the provided map file and converts it into a 2D array
   * @param A string that is the file name of the map file
   * @return The maze in the form of a 2D array
   */
  public static String[][] readFile(String fileName) throws Exception {
    File myFile = new File(fileName);
    Scanner input = new Scanner(myFile);
    
    String[][] array;
    
    String line;
    int lengthArray;
    
    //Reads the first row
    line = input.nextLine();
    lengthArray = line.length();
    array = new String[lengthArray][lengthArray];
    
    for (int i=0; i<lengthArray; i++){
      array[0][i] = line.substring(i,i+1);
    }
    
    //Reads subsequent rows
    for (int j=1; j<lengthArray ;j++){
      line = input.nextLine();
      for (int k=0; k<lengthArray;k++){
        array[j][k] = line.substring(k,k+1);
      }
    }
    
    input.close();
    return array;
  }
  
  
  /**
   * move
   * This method is a recursive function where the monster moves around on a 2D array
   * @param The 2D array to travel on, the starting y-coordinate, the starting x-coordinate, a 2D array of all traps-
   * on path, which mode is being used, and the number of steps taken.
   * Mode 1 finds the traps on the path, mode 2 finds the minimum number of traps, mode 3 finds the number of steps-
   * the monster takes before getting trapped (corresponds to levels 3, 4, 4+ respectively)
   * @return nothing
   */
  public static void move(String [][] array, int yCoordinate, int xCoordinate, String[][] trapsOnPath, int mode, int steps)throws Exception{
    
    //Makes a copy of the array
    String[][]copy=new String[array.length][array.length];
    for (int i=0; i<array.length;i++){
      for (int j=0; j<array.length;j++){
        copy[i][j]=array[i][j];
      }
    }
    
    int y=yCoordinate;
    int x=xCoordinate;
    
    if((mode==3) &&(copy[y][x].equals("T"))){ //In mode 3, the number of monster steps is counted 
      appendListSteps(steps); //Will append the number of steps to a file once a monster reaches a trap
      return;
    }
    
    if((mode!=3) && (copy[y][x].equals("F"))){ //In mode 3, the recursive call ends at a trap, not "F", so this would be skipped
      if (mode!=1){ //Mode 1 does not append to the file with the blocked array, so this would be skipped
        appendBlockCombos("Passed Through"); //Appends to signify the array was not successfully blocked
      }
      
      //Copies all the traps on path to a file for later use; unnecessary traps are left as is
      for (int i=0; i<copy.length;i++){
        for (int j=0; j<copy.length;j++){
          if (copy[i][j].equals("T")){
            trapsOnPath[i][j]="T";
          }
        }
      }
      
      writeSolution(trapsOnPath);
      return;
    }
    
    if (copy[y][x].equals(" ")){ //Leaves breadcrumbs to prevent backtracking
      copy[y][x]=".";
    } else if ((copy[y][x].equals("P")) && (mode!=3)){ //In mode 3, P's are passible and should not be turned into traps
      copy[y][x]="T";
    }
    
    //In mode 3 where checking the maximum number of steps the monster should take before it gets trapped, the monster can move onto a trap
    if (mode==3){
      if (copy[y-1][x].equals("T")){
        move(copy, y-1,x,trapsOnPath, mode, steps+1);
      }
      if (copy[y][x+1].equals("T")){
        move(copy,y,x+1, trapsOnPath,  mode, steps+1);
      }
      if (copy[y+1][x].equals("T")){
        move(copy, y+1,x, trapsOnPath, mode, steps+1);
      }
      if (copy[y][x-1].equals("T")) {
        move(copy, y,x-1, trapsOnPath, mode, steps+1);
      }
    }
    
    //Checks all four directions for positions to move 
    if ((copy[y-1][x].equals(" ")) || (copy[y-1][x].equals("F")) || (copy[y-1][x].equals("P"))){
      move(copy, y-1,x, trapsOnPath, mode, steps+1);
    }
    if ((copy[y][x+1].equals(" ")) || (copy[y][x+1].equals("F")) || (copy[y][x+1].equals("P"))){
      move(copy,y,x+1, trapsOnPath, mode, steps+1);
    }
    if ((copy[y+1][x].equals(" ")) || (copy[y+1][x].equals("F")) || (copy[y+1][x].equals("P"))){
      move(copy, y+1,x, trapsOnPath, mode, steps+1);
    }
    if ((copy[y][x-1].equals(" ")) || (copy[y][x-1].equals("F")) || (copy[y][x-1].equals("P"))) {
      move(copy, y,x-1, trapsOnPath, mode, steps+1);
    }
  }
  
  
  /**
   * moveHunter
   * This method is a recursive function where the hunter moves around on a 2D array, finding the minimum steps to set all traps
   * @param The 2D array to travel on, the starting y-coordinate, the starting x-coordinate,
   * the number of steps, the number of traps left to set, the trapCount required for the readListSteps method, and the mode
   * Mode 1 disables backtracking
   * Mode 2 allows backtracking
   * @return nothing
   */
  public static void moveHunter(String [][] array, int yCoordinate, int xCoordinate,  int steps, int trapsleft, int trapCount, int mode) throws Exception{
    
    //Makes a copy of the array
    String[][]copy=new String[array.length][array.length];
    for (int i=0; i<array.length;i++){
      for (int j=0; j<array.length;j++){
        copy[i][j]=array[i][j];
      }
    }
    
    int y=yCoordinate;
    int x=xCoordinate;
    
    if(copy[y][x].equals("T")){ 
      copy[y][x]=" "; //The trap is removed when it is reached
      trapsleft-=1;
      
      if (mode==2){ 
        if (steps >= (readListSteps(trapCount, "monsterSteps")) ){ //To increase efficiency, when the number of steps with backtracking is greater than the minimum steps without backtracking, the function terminates
          return;
        }
        
        for (int j=0; j<copy.length ;j++){ //Deletes the bread crumbs
          for (int k=0; k<copy.length;k++){
            if (copy[j][k]=="."){
              copy[j][k]=" ";
            }
          }
        }
      }
      
      if(trapsleft==0){
        appendListSteps(steps);//Append the number of steps to set a trap to a file
        return;
      }
    }
    
    if (copy[y][x].equals(" ")){ //Leaves breadcrumbs
      copy[y][x]=".";
    }
    
    //Checks all four directions for positions to move
    if ((copy[y-1][x].equals(" ")) || (copy[y-1][x].equals("T")) || (copy[y-1][x].equals("P"))){
      moveHunter(copy, y-1,x, steps+1,trapsleft,trapCount, mode);
    }
    if ((copy[y][x+1].equals(" ")) || (copy[y][x+1].equals("T")) || (copy[y][x+1].equals("P"))){
      moveHunter(copy,y,x+1,  steps+1,trapsleft,trapCount, mode);
    }
    if ((copy[y+1][x].equals(" ")) || (copy[y+1][x].equals("T")) || (copy[y+1][x].equals("P"))){
      moveHunter(copy, y+1,x,   steps+1,trapsleft,trapCount, mode);
    }
    if ((copy[y][x-1].equals(" ")) || (copy[y][x-1].equals("T")) || (copy[y][x-1].equals("P"))) {
      moveHunter(copy, y,x-1,    steps+1,trapsleft, trapCount, mode);
    }
  }
  
  
  /**
   * writeSolution
   * Takes an array and outputs it into solution.txt
   * @param A 2D array that is the maze
   * @return nothing
   */
  public static void writeSolution(String[][] array) throws Exception{
    File myFile=new File("solution.txt");
    
    PrintWriter output = new PrintWriter(myFile);
    
    for (int f=0; f<array.length;f++){
      for (int g=0; g<array.length;g++){
        output.print(array[f][g]);
      }
      output.println();
    }
    output.close();
  }
  
  
  /**
   * appendSolution
   * Takes a sentence and appends it to solution.txt
   * @param A string that will say the number of monster's steps and the hunter's steps
   * @return nothing
   */
  public static void appendSolution(String sentence) throws Exception{
    PrintWriter output = new PrintWriter(new FileWriter("solution.txt", true));
    output.println(sentence);
    output.close();
  }
  
  
  /**
   * writeBlockCombos
   * Creates a file to contain all the blocking combinations
   * @param A string that will add a title in the file
   * @return nothing
   */
  public static void writeBlockCombos(String line) throws Exception{
    File myFile=new File("BlockCombos.txt");
    
    PrintWriter output = new PrintWriter(myFile);
    output.println(line);
    output.close();
  }
  
  
  /**
   * appendBlockCombos
   * Takes a string and appends it to the blocking combination list
   * @param A string that will either be the binary code, the number of blocked traps, or "Passed Through"
   * @return nothing
   */
  public static void appendBlockCombos(String line) throws Exception{
    PrintWriter output = new PrintWriter(new FileWriter("BlockCombos.txt", true));
    output.println(line);
    output.close();
  }
  
  
  /**
   * readBlockCombos
   * Reads the blocking combination list and determines the binary code with the minimum traps necessary
   * @param A number that will be the total number of traps
   * @return A string with the binary code signifying the which traps are necessary
   */
  public static String readBlockCombos(int totalTrapCount) throws Exception {
    File myFile=new File("BlockCombos.txt");
    Scanner input = new Scanner(myFile);
    
    int trapCount=totalTrapCount;
    int minTrap=totalTrapCount;
    
    String line;
    String binaryCode="0000";
    String optimumCode="";
    boolean pass=false;
    
    line=input.nextLine();
    
    while (input.hasNext()){
      line=input.nextLine();
      
      if (line.equals("-----")){ //Dashes divide each different blocking combination
        pass=false; //Resets the boolean after every combination
      } else if (line.equals("Passed Through")){
        pass=true; //Denotes that the array has not been successfully blocked
      }
      
      if ((!line.equals("Passed Through")) && (!line.equals("-----")) && (!pass)){
        binaryCode=line;
        trapCount=Integer.parseInt(input.nextLine());
        
        if (trapCount<=minTrap){
          minTrap=trapCount; //Takes the minimum trapCount that also blocks all paths
          optimumCode=binaryCode;
        }
      }
    }
    input.close();
    return optimumCode;
  }
  
  
  /**
   * writeListSteps
   * Creates a file to contain the list of step numbers for both the hunter and the monster
   * @param An int of -1 representing a divider (and the title) of the file
   * @return nothing
   */
  public static void writeListSteps(int steps) throws Exception{
    File myFile=new File("NumberStepsList.txt");
    
    PrintWriter output = new PrintWriter(myFile);
    output.println(steps);
    output.close();
  }
  
  
  /**
   * appendListSteps
   * Takes an integer and appends it to the total list of steps
   * @param An int representing the number of steps taken or -1 to signify a divider
   * @return nothing
   */
  public static void appendListSteps(int steps) throws Exception{
    PrintWriter output = new PrintWriter(new FileWriter("NumberStepsList.txt", true));
    output.println(steps);
    output.close();
  }
  
  
  /**
   * readListSteps
   * Reads the number of steps and determines the maximum number of steps before the monster's capture and the-
   * minimum number of steps to set all the traps
   * @param An int representing the total number of traps
   * Mode 1 returns the max monster's steps before capture
   * Mode 2 returns the min hunter's steps to set all traps
   * @return nothing
   */
  public static int readListSteps(int trapCount, String mode) throws Exception {
    File myFile=new File("NumberStepsList.txt");
    Scanner input = new Scanner(myFile);
    
    int line;
    int maxMonsterSteps=0;
    int minHunterSteps=-1;
    
    boolean hunter=true; //Determines if monster's steps or hunter's steps
    
    while (input.hasNext()){
      line=input.nextInt();
      
      if (line==-1){ //Divider -1 will toggle the switch, monster's steps is checked first
        hunter=!hunter;
        line=input.nextInt();
      }
      
      if ((!hunter) && (line>=maxMonsterSteps)){ //Saves the max monster's steps
        maxMonsterSteps=line;
      }
      
      if (hunter){
        if (minHunterSteps==-1){ //Initializes minHunterSteps so it doesn't start at -1
          minHunterSteps=line;
        }
        if( line<=minHunterSteps){ //Saves the min hunter's steps
          minHunterSteps=line;
        }
      }
    }
    
    if (trapCount==1){
      minHunterSteps=0; //If there is only one trap, the minHunterSteps is 0, this overrides everything else
    }
    
    input.close();
    
    if (mode.equals("monsterSteps")){
      return maxMonsterSteps;
      
    }else{
      return minHunterSteps;
    }
  }
  
  
  /**
   * main
   * The main method where the read and write methods and the movement methods are combined
   * @param String[] args
   * @return nothing
   */
  public static void main(String[] args) throws Exception{
    Scanner keyboard=new Scanner(System.in);
    
    String[][] maze;
    String[][] allPathedTrapsArray;
    String[][] blockingArray;
    String [][] minTrapArray;
    String[][] hunterArray;
    
    int [][]trapCoordinateArray;
    int [][]trapCoordinateArray2;
    
    String fileName;
    String binaryTotalBlockCombos;
    String binaryCode;
    
    int lengthMaze;
    int trapCount;
    int decimalTotalBlockCombos;
    int countBTrap;
    
    writeBlockCombos("List of Block Combinations");
    writeListSteps(-1);
    
    //Greets and prompts user for file name
    System.out.println("Welcome to Monster Hunter");
    System.out.println("Enter file name: ");
    fileName=keyboard.nextLine();
    keyboard.close();
    
    allPathedTrapsArray=readFile(fileName);
    blockingArray=readFile(fileName);
    
    //Counts the number of total traps
    maze= readFile(fileName); //Updates the maze
    lengthMaze=maze.length;
    int totalTraps=0;
    for (int i=0; i<maze.length;i++){
      for (int j=0; j<maze.length;j++){
        if (maze[i][j].equals("P")){
          totalTraps+=1;
        }
      }
    }
    
    //Makes an array with all the traps
    trapCoordinateArray= new int[2][totalTraps+1];
    
    //Moves on maze, starts on 1,1, edits an array with on-path traps, mode 1 (determines on-path traps), and step number of 1
    move(maze,1,1, allPathedTrapsArray, 1,1);
    
    allPathedTrapsArray=readFile("solution.txt"); //The file contains all on-path traps
    
    //Recounts the number of traps
    trapCount=0;
    for (int f=0; f<allPathedTrapsArray.length;f++){
      for (int g=0; g<allPathedTrapsArray.length;g++){
        if (allPathedTrapsArray[f][g].equals("T")){
          trapCoordinateArray[0][trapCount]=f;
          trapCoordinateArray[1][trapCount]=g;
          trapCount+=1;
        }
      }
    }
    
    //Makes an integer that is made up of repeated 1's, one for every trap there is
    binaryTotalBlockCombos="";
    for (int i=0;i<trapCount;i++){
      binaryTotalBlockCombos+="1";
    }
    
    //Converts it the number of combinations of blocks to a decimal number
    decimalTotalBlockCombos=Integer.parseInt(binaryTotalBlockCombos,2);
    binaryCode="";
    countBTrap=0;
    
    
    for (int i=1;i<=decimalTotalBlockCombos;i++){ //Converts every number up to and including the total number of combinations to a binary string
      binaryCode=Integer.toBinaryString(i);
      
      while (binaryCode.length()<trapCount){
        binaryCode="0"+binaryCode; //Pads the binaryCode with "0"s
      }
      countBTrap=0;
      
      for (int f=0; f<blockingArray.length;f++){ //Resets the array so all blocked traps become regular potential traps
        for (int g=0; g<blockingArray.length;g++){
          if (blockingArray[f][g].equals("B")){
            blockingArray[f][g]="P";
          }
        }
      }
      
      for (int k=0;k<binaryCode.length();k++){ //Counts the number of "B" traps in the array
        if (binaryCode.substring(k,k+1).equals("1")){
          blockingArray[trapCoordinateArray[0][k]][trapCoordinateArray[1][k]]="B";
          countBTrap+=1;
        }
      }
      
      //Moves on the array with blocked traps, starts on 1,1, mode 2 (finds only the necessary traps), and step number is 1
      move(blockingArray,1,1, allPathedTrapsArray, 2,1);
      
      //Appends the binary code and the number of traps blocked
      appendBlockCombos(binaryCode);
      appendBlockCombos(Integer.toString(countBTrap));
      
      appendBlockCombos("-----"); //Adds a divider before a new blocked array
    }
    
    binaryCode=readBlockCombos(trapCount); //This is the binary code representing only the necessary traps
    
    minTrapArray=readFile(fileName);
    
    //Uses binary strings to get every combination of blocked paths
    for (int k=0;k<binaryCode.length();k++){
      if (binaryCode.substring(k,k+1).equals("1")){ //If the digit is a one, a specific trap is blocked
        minTrapArray[trapCoordinateArray[0][k]][trapCoordinateArray[1][k]]="T";
      }
    }
    
    //Saves the minimum number of traps to a file
    writeSolution(minTrapArray);
    
    //Starts on 1,1, mode 3, and step number of 1
    move(minTrapArray,1,1, allPathedTrapsArray,  3,1);
    
    //Copies an array with the minimum number of traps for the hunter to move in
    hunterArray=readFile("solution.txt");
    
    if (trapCount!=1){ //If there is only one trap, the number of steps to set all traps is zero; this loop is skipped
      //Blocks the entrance ("N") and exit ("F")
      hunterArray[0][1]="+"; 
      hunterArray[hunterArray.length-2][hunterArray.length-1]="+";
      
      //Recalculates the number of traps; this is the minimum number of traps
      trapCount=0;
      for (int f=0; f<hunterArray.length;f++){
        for (int g=0; g<hunterArray.length;g++){
          if (hunterArray[f][g].equals("T")){
            trapCount+=1;
          }
        }
      }
      
      //Makes a second trap array with the minimum traps necessary and recalculates the trap count
      trapCoordinateArray2=new int[2][trapCount];
      trapCount=0;
      for (int f=0; f<hunterArray.length;f++){
        for (int g=0; g<hunterArray.length;g++){
          if (hunterArray[f][g].equals("T")){
            trapCoordinateArray2[0][trapCount]=f;
            trapCoordinateArray2[1][trapCount]=g;
            trapCount+=1;
          }
        }
      }
      
      //Adds -1 to the file to divide the list of monster steps and the list of human steps
      appendListSteps(-1);
      
      //Runs a recursive call for every starting trap position while placing breadcrumbs
      //Moves on hunterArray, starts on every trap coordinate, starts with 0 steps, first trapCount is used for traps remaining-
      //second trap count is a parameter for readListSteps method, mode 1 to disable backtracking
      for (int i=0; i<trapCount;i++){
        moveHunter(hunterArray,trapCoordinateArray2[0][i],trapCoordinateArray2[1][i], 0 ,trapCount, trapCount, 1);
      }
      
      //Repeats the above recursive call but mode 2 to allow backtracking
      for (int i=0; i<trapCount;i++){
        moveHunter(hunterArray,trapCoordinateArray2[0][i],trapCoordinateArray2[1][i], 0 ,trapCount, trapCount, 2);
      }
    }
    
    //Appends number of monster steps and hunter steps to solution.txt
    appendSolution("");
    appendSolution(readListSteps(trapCount, "monsterSteps")+ " steps to capture monster");
    appendSolution(readListSteps(trapCount, "hunterSteps")+ " steps to set traps");
  }
}