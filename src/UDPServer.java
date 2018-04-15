
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

class UDPServer{
    static Queue<DatagramPacket> requisicoes = new LinkedList<>();
    static Queue<DatagramPacket> disco = new LinkedList<>();
    static Queue<DatagramPacket> processamento = new LinkedList<>();
    static Map<BigInteger, String> map = new HashMap();
    static DatagramSocket serverSocket;
    static byte[] receiveData = new byte[1400];
    static byte[] sendData = new byte[1400];
    
    public static void main(String args[]) throws SocketException, IOException{
        Properties p = new Properties();
        FileInputStream inputStream = new FileInputStream("src/propriedades.properties");
        p.load(inputStream);
        serverSocket = new DatagramSocket(Integer.parseInt(p.getProperty("porta_server")));
        while(true){
            DatagramPacket receivePacket = receive();
            //Adicionar Thread para colocar em uma fila
            requisicoes.offer(receivePacket);
            processamento();
            //Thread consumindo da da fila de requisições e logando em disco
            setLog(receivePacket);
            //getlog()
            //Thread para processamento
        }
    }
          
    //Recebe pacotes do cliente
    static DatagramPacket receive() throws IOException{
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        return receivePacket;
    }
    
    //Envia pacotes para o cliente
    static void send(DatagramPacket receivePacket, String sentence) throws IOException{
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);
    }

    //Pego os dados da fila de 'processamento' e envio as respostas
    static void processamento() throws IOException{
        DatagramPacket receivePacket = requisicoes.poll();
        String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
        BigInteger bigInteger;
        DatagramPacket datagramPacket;
        switch( sentence ){
          case "insert":
              send(receivePacket, "Digite o indice...");
              datagramPacket = receive();
              bigInteger = new BigInteger(datagramPacket.getData());
              send(datagramPacket, "Digite o dado...");
              datagramPacket = receive();
              String insertData = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
              insert(bigInteger, insertData);
              send(datagramPacket, "Inserido com sucesso!");
              break;
          case "delete":
              send(receivePacket, "Digite o indice...");
              datagramPacket = receive();
              bigInteger = new BigInteger(datagramPacket.getData());
              remove(bigInteger);
              send(datagramPacket, "Removido com sucesso!");
              break;
          case "update":
              send(receivePacket, "Digite o indice...");
              datagramPacket = receive();
              bigInteger = new BigInteger(datagramPacket.getData());
              send(datagramPacket, "Digite o dado...");
              datagramPacket = receive();
              String updateData = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
              update(bigInteger, updateData);
              send(datagramPacket, "Atualizado com sucesso!");
              break;
          case "select":
              send(receivePacket, "Digite o indice...");
              datagramPacket = receive();
              bigInteger = new BigInteger(datagramPacket.getData());
              String dados = select(bigInteger);
              send(datagramPacket, dados);
              break;
          default:
              System.out.println("Comando não reconhecido... ");
              send(receivePacket, "Comando não reconhecido... ");
              break;
          }
    }

    //Salva log no disco
    static void setLog(DatagramPacket receivePacket){
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        String sentence = new String( receivePacket.getData(), 0, receivePacket.getLength() );
        System.out.println("REQUISIÇAO: "+sentence);
        System.out.println("IP: "+IPAddress);
        System.out.println("PORTA: "+port);
        System.out.println("TAMANHO DO MAP: "+map.size());
        System.out.println();
        
        //Salvar log no arquivo
    }
    
    //Recupera log do disco
    static void getLog(){
        //Ler log do arquivo
        List<DatagramPacket> datagrams = null;
        for(DatagramPacket dp: datagrams){
            requisicoes.add(dp);
            }
        }
    
    //Insere objeto no HashMap
    static void insert(BigInteger bigInteger, String s){
        map.put(bigInteger, s);
    }

    //Atualiza objeto no HashMap
    static void update(BigInteger bigInteger, String s){
        map.replace(bigInteger, s);
    }
    
    //Remove objeto no HashMap
    static void remove(BigInteger bigInteger){
        map.remove(bigInteger);
    }

    //Lista objeto no HashMap
    static String select(BigInteger bigInteger){
        return map.get(bigInteger);
    }

}