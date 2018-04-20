import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;

class UDPClient
{
   public static void main(String args[]) throws Exception{
        Properties p = new Properties();
        FileInputStream in = new FileInputStream("src/propriedades.properties");
        p.load(in);
        //teste
        Scanner s = new Scanner(System.in);
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(p.getProperty("ip_server"));
        Integer porta = Integer.parseInt(p.getProperty("porta_server"));
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        while(true){
            String request = s.nextLine();
            Thread threadRequest = new Thread(request);
            threadRequest.start();
            threadRequest.join();
            sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);
            clientSocket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            Thread threadResponse = new Thread(response);
            threadResponse.start();
            threadRequest.join();
            System.out.println(response);
        }
        //clientSocket.close();
   }
}