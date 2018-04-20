import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

class UDPServer {
    static Arquivo arquivo = new Arquivo();
    static Queue<DatagramPacket> requisicoes = new LinkedList<>();
    static Queue<DatagramPacket> disco = new LinkedList<>();
    static Queue<DatagramPacket> processamento = new LinkedList<>();
    static Map<BigInteger, String> map = new HashMap();
    static DatagramSocket serverSocket;
    static byte[] receiveData = new byte[1400];
    static byte[] sendData = new byte[1400];
    /*
        Quando salvar o log? Todas as requisições ou só as não atendidas?
        Execução das Threads? Como fazer?
        Manipular BigInteger? Converter?
        Quando e como processar as requisições?
        Como processar as respostas no cliente?
    */
    public static void main(String args[]) throws SocketException, IOException{
        Properties p = new Properties();
        FileInputStream inputStream = new FileInputStream("src/propriedades.properties");
        p.load(inputStream);
        serverSocket = new DatagramSocket(Integer.parseInt(p.getProperty("porta_server")));
        
        //Recuperar Requisições
        //getLog();
        System.out.println("Numero de requisições recuperadas>> "+requisicoes.size());
        
        while(true){
            //Recebe os pacotes
            DatagramPacket receivePacket = receive();
            
            //Adicionar Thread para colocar em uma fila
            Thread t = new Thread();
            
            requisicoes.offer(receivePacket);
            System.out.println("Requisição adicionada, quantidade atual >> "+requisicoes.size());
                        
            //Thread consumindo da fila de requisições e logando em disco
            disco.add(requisicoes.peek());
            //setLog(receivePacket);

            //Thread para processamento
            processamento.add(requisicoes.poll());
            processamento();
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
        while(processamento.size() > 0){
            System.out.println("Processamento>> "+processamento.size());
            DatagramPacket receivePacket = processamento.poll();
        
            System.out.println("Requisição atendida, quantidade atual>> "+requisicoes.size());
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
                  //String dados = select(bigInteger);
                  send(datagramPacket, "ERRO");
                  break;
               case "list":
                  send(receivePacket, list());
                  break;
              default:
                  send(receivePacket, "Comando não reconhecido... ");
                  break;
              }
        }
    }

    //Salva log no disco
    static void setLog(DatagramPacket receivePacket) throws IOException{
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        String sentence = new String( receivePacket.getData(), 0, receivePacket.getLength() );
        arquivo.escrever(IPAddress+" "+port+" "+sentence);
    }
    
    //Recupera log do disco
    static void getLog() throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new FileReader("src/log.txt"));
        while(true){
            String linha = bufferedReader.readLine();
            if(linha != null){
                String dados[] = linha.split(" ");
                InetAddress IPAddress = InetAddress.getByName(dados[0].replace("/", ""));
                int port = Integer.parseInt(dados[1]);
                String sentence = dados[2];
                sendData = sentence.getBytes();
                DatagramPacket dp = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                requisicoes.offer(dp);
            }else{
                break;
            }
        }
        bufferedReader.close();
    }
    
    //Insere objeto no HashMap
    static void insert(BigInteger bigInteger, String s){
        map.put(bigInteger, s);
        System.out.println("Valor inserido no HashMap>> "+map.get(bigInteger));
        System.out.println("Tamanho do HashMap>> "+map.size());
    }

    //Atualiza objeto no HashMap
    static void update(BigInteger bigInteger, String s){
        System.out.println("Valor atualizado no HashMap>> "+map.get(bigInteger));
        map.replace(bigInteger, s);
    }
    
    //Remove objeto no HashMap
    static void remove(BigInteger bigInteger){
        System.out.println("Valor removido no HashMap>> "+map.get(bigInteger));
        map.remove(bigInteger);
    }

    //Seleciona objeto do HashMap
    static String select(BigInteger bigInteger){
        System.out.println("Valor pesquisado no HashMap>> "+map.get(bigInteger));
        return map.get(bigInteger);
    }

    //Lista objetos do HashMap
    static String list(){
        Set<BigInteger> set = map.keySet();
        String retorno = "";
        for(BigInteger chave: set){
            if(chave != null){
                retorno += "- "+map.get(chave) +"\n";
            }
        }
        System.out.println("Lista de valores do HashMap>>\n"+retorno);
        return retorno;
    }
}