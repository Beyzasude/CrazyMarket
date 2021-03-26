import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Random;

class CrazyMarket implements MyQueue<Customer>{
    /**default tekerleme
     * */
    String tekerleme = "O piti piti karamela sepeti "
            + "\nTerazi lastik jimnastik "
            + "\nBiz size geldik bitlendik Hamama gittik temizlendik.";
    double  waitTime;
    double arrive=0;
    double currenTime=0;//son gelen kişinin gelme zamanını tutar.
    int islemGorMusteriSayisi=0;
    int musteriSayisi=0;  //constructorda aldığım parametre değerini başka yerlerde kullanmak için değişken işlem görmesi gerekn müşteeri sayısı

    private Node head=null,tail=null ;
    private int size;
    private class Node{
        Node next;
        Customer data;
        public Node(Customer data) {
            this.data = data;
            this.next = null;
        }
    }

    acceptCustomer x = new acceptCustomer(); //ekleme işlemini eş zamanlı yapmak için extend ettiğim thread sınıfı için nesne
    Cashier z = new Cashier();  //kasa işlemini eş zamanlı yapmak için extend ettiğim thread sınıfı için nesne
    boolean closeMarket = false;
    private static DecimalFormat df2 = new DecimalFormat("#.##"); //long tanımlanan sürenin , den sonra 2 basamak alması

    public void Run() {
        x.start();
        z.start();
    }

    /**
     *  numberOfCustumer ile verilen sayida
     * musteri hizmet gorene kadar calismaya devam eder*/
    public CrazyMarket(int numberOfCustomer) {
        musteriSayisi=numberOfCustomer;
        Run();
    }

    /**
     *  numberOfCustumer ile verilen sayida
     * musteri hizmet gorene kadar calismaya devam eder,
     * calisirken verilen tekerlemeyi kullanir*/
    public CrazyMarket(int numberOfCustomer, String pTekerleme) {
        musteriSayisi=numberOfCustomer;
        tekerleme = pTekerleme;
        Run();
    }



    /** kuyrugun basindaki musteriyi yada tekerleme
     * ile buldugu musteriyi return eder*/
    public Customer chooseCustomer() {
        waitTime=currenTime-head.data.arrivalTime;  //Currenttime total zamanı tutar bu zamandan bştaki kişinin arrivaltime ını çıkartırsak bekleme zamanını buluruz.
        if (waitTime>10){   //eğer bekleme zamanı 10 dan büyükse baştaki elemanı return eder.
            return head.data;
        }
        else{
            return chooseElementPutFront(dequeuWithCounting(tekerleme)); //değilse tekerleme ile birini seçer ve bu kişiyi başa koyar.
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean enqueue(Customer item) {
        Node addBack=new Node(item);
        if (head == null) {
            tail = head = addBack;
        }
        else {
            tail.next=addBack;
            tail = addBack;
        }
        size++;
        return true;
    }

    @Override
    public Customer dequeuNext() {
        if (isEmpty()) {
            return null;
        } else if (head == tail) {
            head = null;
            tail = null;
            size--;
            return null;
        }
        head=head.next;
        size--;
        return head.data;
    }

    public Customer chooseElementPutFront(Customer item){  //eğer tekerleme ile birini seçiyorsak bu seçtiğimiz kişiyi listin başına koyuyorum ki dequeue her iki seçim için silme yapsın hep baştaki seçilsin.
        if (item == head.data){
            return item;
        }

        Node current,oldFront;
        oldFront=head;
        current=head;
        while(current.data!=item){
            current =current.next;
        }
        current.next=current.next.next;
        head=current;
        current.next=oldFront;
        return current.data;
    }
    @Override
    public Customer dequeuWithCounting(String pTekerleme) {
        int indexElemani=0;
        Node choosen;
        tekerleme = pTekerleme;
        char k;
        int sayac = 0,i;
        for(i=0; i<tekerleme.length(); i++) {
            k = tekerleme.charAt(i);
            if (k=='a'|| k=='e'|| k=='ı'|| k=='i'|| k=='o'|| k=='ö'|| k=='u'|| k=='ü' ) sayac++;
        }
        int index=size%sayac;
        choosen=head;
        while(indexElemani!=index){  //seçilen kişinin listin kaçıncı elemanı old bulup bunun return ettiği değeri chooseElementPutFront fonksiyonuna parametre olarak yolluyorum ve bu kişiyi başa koyuyorum.
            choosen=choosen.next;
            indexElemani++;
        }
        return choosen.data;
    }

    @Override
    public Iterator<Customer> iterator() {
        return new QueueIterator();
    }
    private class QueueIterator implements Iterator<Customer>{
        private  Node itr = head;
        @Override
        public boolean hasNext() {
            return itr != null;
        }

        @Override
        public Customer next() {
            Customer data = itr.data;
            itr = itr.next;
            return data;
        }
    }
    class acceptCustomer extends Thread {

        @Override
        public void run() {
            acceptcustomer();

        }
        private int acceptcustomer(){
            if(islemGorMusteriSayisi<musteriSayisi){  //constructor ile yolladığım sayıda müşteri işlem görene kadar sürekli ekleme işlemi olur .
                Random r= new Random();
                arrive=r.nextInt(2000);
                currenTime=currenTime+arrive;  //total zaman hep korunur.
                try {
                    Thread.sleep((long)arrive);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Customer c = new Customer(currenTime);
                enqueue(c);
                System.out.println("eklendi: " + df2.format(c.arrivalTime / 1000) + "sn, size: " + size());
                return acceptcustomer();
            }

            closeMarket = true;  //2 thread arasındaki ilişkiyi sağlar
            return 0;
        }
    }
    class Cashier extends Thread {

        @Override
        public void run() {
            serveCustomer();
        }
        private void serveCustomer()  {
            if(closeMarket){
                QueueIterator i = new QueueIterator();
                while (i.hasNext()){       //iterator
                    Customer c=i.next();   //iterator
                    System.out.println(df2.format((currenTime - c.arrivalTime) / 1000) + " sn");
                }  //kalan kişilerin bekleme zamanı son gelen kişinin arrivelTime ı yani CurrentTime - kendi arrivalTimeları ile bulunur.
                return;
            }
            double islemSuresi=0;
            Random r= new Random();
            islemSuresi=r.nextInt(2000) + 1000;

            try {
                Thread.sleep((long)islemSuresi);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!isEmpty() && islemGorMusteriSayisi < musteriSayisi){
                Customer c = chooseCustomer();  //önce müşteri seçilir 10 dan büyük olsada tekerleme ile seçilsede işlem görecek kişi hep başa gelecek
                dequeuNext(); //seçilen kişiyi siler
                islemGorMusteriSayisi++;
                System.out.println("çıkarıldı: " + df2.format(c.arrivalTime / 1000) + "sn, size: " + size());
            }
            serveCustomer();
        }
    }
}

interface MyQueue<T> extends Iterable<T>{

    /**kuruktaki toplam eleman sayisi*/
    int size();
    boolean isEmpty();
    /**kuyrugun sonuna item ekler*/
    boolean enqueue(T item);

    /** kuyrugun basindan eleman cikarir*/
    T dequeuNext();
    /** tekerleme metnini kullanarak bir sonraki elemani secer*/
    T dequeuWithCounting(String tekerleme);

}


 class Customer {
    Customer (double arrivalTime){
        this.arrivalTime=arrivalTime;
    }
    //datafield tiplerini degistirebilirsiniz
    double arrivalTime; //musteri gelis zamani-
    //bekleme zamanini hesaplamada kullanabilirsiniz
    double removalTime;
}

public class Main {
    public static void main(String[] args) {
    CrazyMarket market = new CrazyMarket(5);
    }
}
