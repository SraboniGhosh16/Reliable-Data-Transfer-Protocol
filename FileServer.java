/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sraboni
 */

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class FileServer {
    private static final String CRLF = "\r\n";
    private static final double LOSS_RATE = 0.3;
    private static final int AVERAGE_DELAY = 100;
    private int port;
    public FileServer(int port){
        this.port = port;
    }
    public void execute()
    {
        FileInputStream fis = null;
        
        //String[] secret = {"ZERO", "ONE", "TWO", "THREE", "FOUR", "5", "6", "7"};
        byte[] buf, b, finalbuf = null;
	Random random = new Random();
        try(DatagramSocket server = new DatagramSocket(port))
        {
            System.out.println("---FT Server is Listening on Port - " + port);
            // read file into buffer
            fis = new FileInputStream("demoText.html");
            String strConsignment, strData;
            String strReq, strRec, strSend, strEnd;
            int consignment = 0, sequence_number = 0, result = 0; // number of bytes read
            
            while(true && result != -1)
            {
                buf = new byte[512];
                DatagramPacket request = new DatagramPacket(new byte[100], 100);
                server.receive(request);

		if (random.nextDouble() < LOSS_RATE)
		{
            		System.out.println("Timeout.\nSent CONSIGNMENT "+consignment);
            		continue;
         	}
         	Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
                // get client's consignment request from DatagramPacket
                
                InetAddress clientHost = request.getAddress();
                int clientPort = request.getPort();
                
                System.out.println("\nClient IP Address = " + clientHost);
                System.out.println("Client port = " + clientPort);
                
                strConsignment = new String(request.getData());
                consignment = Integer.valueOf((strConsignment.trim()));
                System.out.println("Client ACK = " + consignment);
                //strReq = strData.substring(0,strData.indexOf(' '));
                
                //strRec = strData.substring(strData.indexOf(' '));
                
                //RDT SequenceNumber Consignment CRLF
                
                
                //System.out.println("Client Requests = " + strReq);
                
                result = fis.read(buf);
                
                strSend = "\nRDT " + sequence_number + " " + buf.length + CRLF;
                strEnd = "\nRDT " + sequence_number + " " + buf.length + " END" + CRLF;
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(buf);
                if (result == -1) { // last consignment
                    b = strEnd.getBytes();
                    outputStream.write(b);
                    consignment = -1; // reset consignment after last SECRET is delivered
                }
                else{
                    b = strSend.getBytes();
                    outputStream.write(b);
                    consignment++;
                }
                finalbuf = outputStream.toByteArray();
                
                DatagramPacket reply = new DatagramPacket(finalbuf, finalbuf.length, clientHost, clientPort);
                server.send(reply);
                System.out.println("Sent Consignment #" + consignment);
                //System.out.println("Client Say : " + strRec);
                request = null;
                buf = null;
                finalbuf = null;
                reply = null;
                sequence_number++;
            }
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        } 
	catch (Exception ex) {
                Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
                }
	finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Syntax : java FileServer <port-number>");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        FileServer server = new FileServer(port);
        server.execute();
    }
}