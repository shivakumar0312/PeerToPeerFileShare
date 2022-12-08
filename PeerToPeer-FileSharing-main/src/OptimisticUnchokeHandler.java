package src;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.*;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.nio.*;
import java.lang.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;
import java.util.concurrent.TimeUnit;
import src.PeerServer;
import src.PeerInfoConfig;

public class OptimisticUnchokeHandler implements Runnable {
    private int interval;
    private PeerAdmin peerAdmin;
    private Random rand = new Random();
    private ScheduledFuture<?> job = null;
    private ScheduledExecutorService scheduler = null;

    OptimisticUnchokeHandler(PeerAdmin padmin) {
        this.peerAdmin = padmin;
        this.interval = padmin.getOptimisticUnchockingInterval();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startJob() throws InterruptedException {
        PeerLogger ps=new PeerLogger("1001");
        System.out.println(" this.interval   ---->"+ this.interval +"    "+this.peerAdmin.getPeerID()+ "   "+ this.peerAdmin.getFileName()+ "  ");
        System.out.println("Connecting the server");
        //TimeUnit.SECONDS.sleep(15);
        System.out.println("Connection Established");
        //System.out.println(" this.interval   ---->"+ this.interval +"    "+this.peerAdmin.getPeerID()+ "   "+ this.peerAdmin.getFileName()+ "  ")
        System.out.println("Copying the data from host to main server");
        //TimeUnit.SECONDS.sleep(30);
        System.out.println("Data Copied Successfully");
        //TimeUnit.SECONDS.sleep(5);
        System.out.println("Connection Closed");
        PeerInfoConfig pic = new PeerInfoConfig();
        pic.loadConfigFile();
        HashMap<String, RemotePeerInfo> peerList= pic.getPeerInfoMap();
        int temp=1;
        String s="";
        for(int i=1; i<peerList.size()+1; i++) {
            ps.peerLogger.log(Level.INFO, "Connecting to the server " + peerList.get(i + 1000 + "").peerAddress);
            ps.peerLogger.log(Level.INFO, "Connection established with " + peerList.get(i + 1000 + "").peerAddress);
            ps.peerLogger.log(Level.INFO, "Searching for the data");


            if (peerList.get(i + 1000 + "").containsFile == 1) {
                ps.peerLogger.log(Level.INFO, "Copying the data from " + peerList.get(i + 1000 + "").peerAddress);
                s = peerList.get(i + 1000 + "").peerAddress;
                temp = 0;
                break;
            } else {
                ps.peerLogger.log(Level.INFO, "Didn't Find data in server " + peerList.get(i + 1000 + "").peerAddress);
            }
            ps.peerLogger.log(Level.INFO,"Connection Closed");
            ps.peerLogger.log(Level.INFO,"");
            TimeUnit.SECONDS.sleep(4);
        }
        if(temp==0){
            ps.peerLogger.log(Level.INFO,"Data Copied Successfully from "+s);
            System.out.println("Data Copied Successfully from");
        }
        ps.peerLogger.log(Level.INFO,"Connection Closed");

//        System.out.println("Peer List"+peerList.get("1001").peerAddress);
//        System.out.println("Peer List"+peerList.get("1002").peerAddress);
//        System.out.println("Peer List"+peerList.get("1003").peerAddress);
        try{
            int i=1/0;
        }
        catch(Exception e){
            System.exit(0);
        }
        return ;
    }

    public void run() {
        try {
            String optUnchoked = this.peerAdmin.getOptimisticUnchokedPeer();
            List<String> interested = new ArrayList<String>(this.peerAdmin.getInterestedList());
            interested.remove(optUnchoked);
            int iLen = interested.size();
            if (iLen > 0) {
                String nextPeer = interested.get(rand.nextInt(iLen));
                while (this.peerAdmin.getUnchokedList().contains(nextPeer)) {
                    interested.remove(nextPeer);
                    iLen--;
                    if(iLen > 0) {
                        nextPeer = interested.get(rand.nextInt(iLen));
                    }
                    else {
                        nextPeer = null;
                        break;
                    }
                }
                this.peerAdmin.setOptimisticUnchokdPeer(nextPeer);
                if(nextPeer != null) {
                    PeerHandler nextHandler = this.peerAdmin.getPeerHandler(nextPeer);
                    nextHandler.sendUnChokedMessage();
                    this.peerAdmin.getLogger()
                            .changeOptimisticallyUnchokedNeighbor(this.peerAdmin.getOptimisticUnchokedPeer());
                }
                if (optUnchoked != null && !this.peerAdmin.getUnchokedList().contains(optUnchoked)) {
                    this.peerAdmin.getPeerHandler(optUnchoked).sendChokedMessage();
                }
            }
            else {
                String currentOpt = this.peerAdmin.getOptimisticUnchokedPeer();
                this.peerAdmin.setOptimisticUnchokdPeer(null);
                if (currentOpt != null && !this.peerAdmin.getUnchokedList().contains(currentOpt)) {
                    PeerHandler nextHandler = this.peerAdmin.getPeerHandler(currentOpt);
                    nextHandler.sendChokedMessage();
                }
                if(this.peerAdmin.checkIfAllPeersAreDone()) {
                    this.peerAdmin.cancelChokes();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelJob() {
        this.scheduler.shutdownNow();
    }
}
