/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sraboni, Aadriza, Ritika, Prottasha
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
        byte[] buf, b, rdt;
	Random random = new Random();
        
        try(DatagramSocket server = new DatagramSocket(port))
        {
            System.out.println("---FT Server is Listening on Port - " + port);
            // read file into buffer
            buf = new byte[512];
            String filename, strSend, strEnd, fileack;
            int filesize, remaining, result = 0, consignment = 0, sequence_number = 0; // number of bytes read
            
            DatagramPacket frequest = new DatagramPacket(new byte[100], 100);
            server.receive(frequest);
            
            InetAddress clientHost = frequest.getAddress();
            int clientPort = frequest.getPort();
            System.out.println("\nClient IP Address = " + clientHost);
            System.out.println("Client port = " + clientPort);
               
            filename = new String(frequest.getData()).trim();
            
            if(new File(filename).exists())
            {
                strSend = "FOUND";
                b = strSend.getBytes();
                DatagramPacket reply = new DatagramPacket(b, b.length, clientHost, clientPort);
                server.send(reply);
            }
            else
            {
                strSend = "no";
                b = strSend.getBytes();
                DatagramPacket reply = new DatagramPacket(b, b.length, clientHost, clientPort);
                server.send(reply);
            }
            
            fis = new FileInputStream(filename);
            
            filesize = fis.available();
            remaining = filesize;
            System.out.println("Client Requested for : " + filename);
              
            while(result != -1 && consignment != -1)
            {
                if (random.nextDouble() < LOSS_RATE)
		{
                    System.out.println("Timeout.\nResent CONSIGNMENT " + consignment);
                    continue;
         	}
         	Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
                System.out.println("Sent Consignment #" + consignment);
                if(remaining >= 512)
                {
                    buf = new byte[512];
                    strSend = "\nRDT " + sequence_number + " " + buf.length + CRLF;
                    rdt = strSend.getBytes();
                    DatagramPacket sendRDT = new DatagramPacket(rdt, rdt.length, clientHost, clientPort);
                    server.send(sendRDT);   
                }
                else if(remaining < 512 && remaining > 0)
                {
                    buf = new byte[remaining];
                    strEnd = "\nRDT " + sequence_number + " " + buf.length + " END" + CRLF;
                    rdt = strEnd.getBytes();
                    DatagramPacket sendRDT = new DatagramPacket(rdt, rdt.length, clientHost, clientPort);
                    server.send(sendRDT);
                    consignment = -2;
                }
                result = fis.read(buf);
                
                DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
                server.send(reply);
                
                DatagramPacket ack = new DatagramPacket(new byte[100], 100);
                server.receive(ack);
                fileack = new String(ack.getData());
                System.out.println("Client Response = " + fileack);
                
                remaining = remaining - 512;
                sequence_number++;
                consignment++;
                rdt = null;
            }
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        } 
	catch (Exception ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
	finally{
            try{
                if (fis != null)
                    fis.close();}
            catch (IOException ex){
                System.out.println(ex.getMessage());}
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