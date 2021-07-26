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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class FileClient {
    private static final String CRLF = "\r\n";
    private static final int MAX_TIMEOUT = 3000;
    
    private String hostname;
    private int port;

    public FileClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    public void execute()
    {
        FileOutputStream fos = null;
        try{
            System.out.println("---" + hostname + " is Connected to the FT Server " + port);
            InetAddress server = InetAddress.getByName(hostname);
            DatagramSocket client = new DatagramSocket();
            int sequence_number = 1, count=0;
            byte[] buf;
            String strSend, reply, last, strFile;
            DatagramPacket request;
            boolean end = true;
            
            // write received data into demoText1.html
            fos = new FileOutputStream("demoText1.html");
            System.out.println("Request Sent");
            
            while (end) {
                strSend = "" + sequence_number + CRLF; //RequestFile ACK SequenceNumber CRLF
                buf = strSend.getBytes();
                request = new DatagramPacket(buf, buf.length, server, port);
                client.send(request);
                
                
                // get next consignment
                try {
                    client.setSoTimeout(MAX_TIMEOUT);
                    
                    DatagramPacket response = new DatagramPacket(new byte[600], 600);
                    client.receive(response);
                    
                    reply = new String(response.getData());
                    System.out.println(reply);
                    
                    last = reply.substring(reply.lastIndexOf(' '), reply.length());
                    if ((last.trim()).equals("END")){ // last consignment
                        strFile = reply.substring(0, reply.indexOf("RDT"));
                        Thread.sleep(500);
                        end = false;
                    }
                    else{
                        strFile = reply.substring(0, reply.indexOf("RDT"));
                        Thread.sleep(1500);
                    }
                    fos.write(strFile.getBytes());
                    
                }
                catch (IOException e) {
                    System.out.println("Timeout\n" + sequence_number + e.getMessage());
                }
                catch (Exception ex) {
                Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally {
                    try {
                        if (fos == null)
                            fos.close();
                        if (client == null)
                            client.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
            sequence_number++;
            }
            client.close();
        }
        catch (IOException ex) {
            System.out.println("Client error : " + ex.getMessage());
        } 
    }
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Syntax : java SecretClient <ip-address> <port-number>");
            System.exit(0);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        FileClient client = new FileClient(hostname, port);
        client.execute();
    }
}