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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class FileClient {
    private static final String CRLF = "\r\n";
    private static final int MAX_TIMEOUT = 3000;
    
    private String hostname;
    private int port;

    public FileClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }
    public void execute(String filename)
    {
        FileOutputStream fos = null;
        try{
            DatagramSocket client = new DatagramSocket();
            
            System.out.println("---" + hostname + " is Connected to the FT Server " + client.getPort());
            InetAddress server = InetAddress.getByName(hostname);
            
            int sequence_number = 1, filesize, receivesize = 0;
            byte[] buffile, buf;
            String strSend, reply, replyrdt, last;
            boolean end = true;
            
            buffile = filename.getBytes();
            DatagramPacket frequest = new DatagramPacket(buffile, buffile.length, server, port);
            client.send(frequest);
            
            System.out.println("Request Sent");
            
            DatagramPacket fresponse = new DatagramPacket(new byte[100], 100);
            client.receive(fresponse);
            
            reply = new String(fresponse.getData());
            
            if(reply.contains("FOUND")){
                System.out.println("Request Accepted");
                end = true;
            }
            else{
                System.out.println("Request Declined");
                end = false;
            }
            
            // write received data into NEW file
            fos = new FileOutputStream("NEW" + filename);
            
            while(end)
            {
                try{
                    client.setSoTimeout(MAX_TIMEOUT);
                    
                    DatagramPacket rdt = new DatagramPacket(new byte[100], 100);
                    client.receive(rdt);
                    
                    replyrdt = new String(rdt.getData());
                    System.out.println(replyrdt);
                    
                    String[] sentences = replyrdt.split(" ");
                    last = replyrdt.substring(replyrdt.lastIndexOf(' '), replyrdt.length());
                    if((last.trim()).equals("END"))
                    {
                        System.out.println("File received successfully");
                        Thread.sleep(500);
                        end = false;
                    }
                    else
                    {System.out.println("File receiving.......");
                        Thread.sleep(500);
                    }
                    filesize = Integer.valueOf(sentences[2].trim());
                    DatagramPacket response = new DatagramPacket(new byte[filesize], filesize);
                    client.receive(response);
                    
                    fos.write(response.getData());
                    
                    reply = new String(response.getData());
                    System.out.println(reply);
                    
                    receivesize = receivesize + filesize;
                    if(fos.getChannel().position() == receivesize)
                    {
                        strSend = "ACK " + sequence_number + CRLF; //ACK SequenceNumber CRLF
                        buf = strSend.getBytes();
                        DatagramPacket ack = new DatagramPacket(buf, buf.length, server, port);
                        client.send(ack);
                        sequence_number++;
                    }
                }
                catch (IOException e) {
                    System.out.println("Timeout\n" + sequence_number + e.getMessage());
                } catch (InterruptedException ex) {
                    Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            client.close();
        }
        catch (IOException ex) {
            System.out.println("Client error : " + ex.getMessage());
        }
    }
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Syntax : java SecretClient <ip-address> <port-number> <filename>");
            System.exit(0);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String filename = args[2];
        FileClient client = new FileClient(hostname, port);
        client.execute(filename);
    }
}