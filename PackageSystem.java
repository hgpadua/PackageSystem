package packagesystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Huey Padua
 * CNT 4714 Summer 2018
 * Project 2 - Multi threaded programming in Java
 * June 10, 2018
 * Enterprise Computing
 */

public class PackageSystem {

    private final static String file_in = "config.txt";
    private static ArrayList<Station> stationList;
    private static int stations;
    
    public static void main(String[] args) throws IOException{
        System.out.println("* * * SIMULATION BEGINS * * *\n");
        System.out.println("* * * SIMULATION ENDS * * *\n");
        writeOutput("* * * SIMULATION BEGINS * * *\n");
        writeOutput("* * * SIMULATION ENDS * * *\n");
        stationList = new ArrayList<>();
        readConfig();
        
        //loop thru stationList
        //init each station to begin with work loads
        stationList.forEach((s) -> {
            new Thread(new PackageSystem().new workLoad(s)).start();
        });
    }
    //function to read config.txt file
    private static void readConfig() throws FileNotFoundException, IOException {
        
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(file_in));
            String inputLine;
            stations = Integer.valueOf(reader.readLine());
            int i=0;
            int prevCon;
            while((inputLine = reader.readLine()) != null) {
                if(i==0) {
                    prevCon = stations -1;
                }
                else {
                    prevCon = i -1;
                }
                //init station with constructor 
                //add to array list
                //increment station number
                Station ns = new Station(i, prevCon, Integer.valueOf(inputLine));
                stationList.add(ns);
                i++;
            }
//            System.out.println("Success, file read!!");
            reader.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("Error: Could not read the file!");
        }
    }
    //function that writes to an output file
    //individually writing each system.out.println
    private static void writeOutput(String ns) {
        String file_out = "output.txt";
        try {
            FileWriter fw = new FileWriter(file_out, true);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(ns);
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(PackageSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //thread class that will run 
    //until the packge groups have been finished
    //by each station
    class workLoad implements Runnable {
        
        Station currentS;
        Station prevS;
        
        //workload constructor
        //for stations working on package groups
        public workLoad(Station ns) {
            this.currentS = ns;
            int position = stationList.indexOf(ns);
            if(position != 0) {
                prevS = stationList.get(position-1);  
            }
            else {
                prevS = stationList.get(stationList.size()-1);
            }
        }
        @Override
        public void run() {
            System.out.println("Routing Station "+this.currentS.stationNum+
                    ": In-connection set to conveyor "+this.currentS.con1);
            System.out.println("Routing Station "+this.currentS.stationNum+
                    ": Out-connection set to conveyor "+this.currentS.con2);
            System.out.println("Routing Station "+this.currentS.stationNum+
                    ": Workload set. Station "+this.currentS.stationNum+
                    " has "+this.currentS.workLoad+" package groups to move.");    
            
            //write to file
            writeOutput("Routing Station "+this.currentS.stationNum+
                    ": In-connection set to conveyor "+this.currentS.con1);
            writeOutput("Routing Station "+this.currentS.stationNum+
                    ": Out-connection set to conveyor "+this.currentS.con2);
            writeOutput("Routing Station "+this.currentS.stationNum+
                    ": Workload set. Station "+this.currentS.stationNum+
                    " has "+this.currentS.workLoad+" package groups to move.");
            
            //loop until station workLoad is completed
            while(currentS.workLoad != 0) {
                Random rand = new Random();
                try {
                    // routing station is set to sleep for random period
                    //assuming max 10 stations
                    Thread.sleep(rand.nextInt(10));
                } catch (InterruptedException ex) {
                    Logger.getLogger(PackageSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentS.doWork(prevS);
            }
            //statement to see if station is finished with workload
            if(currentS.workLoad == 0) {
                System.out.println("\n* * Station "+ this.currentS.stationNum+
                        ": workload successfully completed. * *\n");
                //write to file
                writeOutput("\n* * Station "+ this.currentS.stationNum+
                        ": workload successfully completed. * *\n");
            }
        }
    }
}

class Station {
    
    private final Lock stationLock = new ReentrantLock();
    public int stationNum;
    public int workLoad;
    public int con1;
    public int con2;
    
    //station constructor
    //defines station number
    //and the two conveyor belts the station is connected to
    public Station(int stationNum, int con1, int workLoad) {
        this.con1 = con1;
        this.con2 = stationNum;
        this.stationNum = stationNum;
        this.workLoad = workLoad;
    }
    //Function that checks the lock of the current station and
    //the station to see if doWork is possible
    //prevents deadlock from occuring
    public boolean checkLocks(Station nextStation) {
        boolean xLock = false;
        boolean yLock = false;
        
        //check to see if conveyor 1 is free
        if(xLock = this.stationLock.tryLock()) {
            System.out.println("Routing Station "+ stationNum+": Granted access to conveyor "
                    + this.con1);
            //write to file
            writeOutput("Routing Station "+ stationNum+": Granted access to conveyor "
                    + this.con1);
        }
        //check to see if conveyor 2 is free
        if(yLock = nextStation.stationLock.tryLock()) {
            System.out.println("Routing Station "+ stationNum+": Granted access to conveyor "
                    + this.con2);
            //write to file
            writeOutput("Routing Station "+ stationNum+": Granted access to conveyor "
                    + this.con2);
        }
        //statement to release access to conveyors
        //only if station is waiting
        if(!(xLock && yLock)) {
            if(xLock) {
                this.stationLock.unlock();
                System.out.println("Routing Station "+stationNum + ": Released access to conveyor "+
                        this.con1);
                //write to file   
                writeOutput("Routing Station "+stationNum + ": Released access to conveyor "+
                        this.con1);  
            }
            else if (yLock) {
                nextStation.stationLock.unlock();
                System.out.println("Routing Station "+stationNum+": Released access to conveyor "+
                        this.con2);
                //write to file
                writeOutput("Routing Station "+stationNum+": Released access to conveyor "+
                        this.con2);
            }
        }
        return xLock && yLock;
    }
    //function to check the locks with the checkLock method
    //if not locked then station can do work on conveyor that is free
    public void doWork(Station nextStation) {
        if(checkLocks(nextStation)) {
            
            System.out.println("Routing Station "+stationNum+": Successfully moves packages on conveyor "
                    +workLoad);
            //write to file
            writeOutput("Routing Station "+stationNum+": Successfully moves packages on conveyor "
                    +workLoad);
            Random rand = new Random();
            try {
                //after station does workload, sleep for random set
                //assuming max 10 stations
                Thread.sleep(rand.nextInt(10));
            } catch (InterruptedException ex) {
                Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
            }
            //workLoad decreases
            //until station has completed tasks
            //next station is unlocked
            //then station can release access to conveyors
            workLoad = workLoad - 1;
            
            this.stationLock.unlock();
            nextStation.stationLock.unlock();
            System.out.println("Routing Station "+ stationNum+ ": Released access to conveyor "
                + this.con1);
            System.out.println("Routing Station "+ stationNum+": Released access to conveyor "
                + this.con2);
            //write to file
            writeOutput("Routing Station "+ stationNum+ ": Released access to conveyor "
                + this.con1);
            writeOutput("Routing Station "+ stationNum+": Released access to conveyor "
                + this.con2);
        }
        else {
            //conveyor is being used and station needs to wait
        }
    }
    //Function to write to output file
    //individually writing each system.out.println
    private static void writeOutput(String ns) {
        String file_out = "output.txt";
        try {
            FileWriter fw = new FileWriter(file_out, true);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(ns);
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(PackageSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
