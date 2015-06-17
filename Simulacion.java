import java.util.Vector;
import java.util.Scanner;
import java.util.Random;
import java.lang.Math;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class Simulacion{

 // variables determinadas por constructor
 int     maxReloj;
 int     timer; // tiempo del timer
 boolean modoLento;

 // constantes
 final int INFINITO                = Integer.MAX_VALUE;
 final int tamVentana              = 8;
 final int rangoFrameVieneConError = 10;
 final int rangoFrameSePierda      = 5;
 final int rangoACKSePierde        = 15;

 // tiempos de eventos y enumeraciones para identificarlas
 double lma, lfb, la, lb, svt, lacka, reloj;
 enum EVENTOS {LMA, LFB, LA, LB, SVT, LACKA};

 // estados de los procesos que preparan frames
 // y revisan frames
 boolean EA, EB;

 int             secuenciaMensaje;
 Mensaje         mensajeEnPreparacion;
 Vector<Mensaje> colaMensaje;

 int             secuenciaPrimerSVT;
 Vector<Timer>   colaTimers;

 int             nAck, ultimoAckEnviado, ultimoAckRecibido;
 int             frameQueEspero;
 double          tamanoPromedioColaA;
 double          tiempoPromedioMsj;
 Vector<Integer> framesRecibidos;  //control de frames recibidos
 Vector<Frame>   colaFrames;
 
 double          tiemposTransmision;
 int             mensajesEnviados;

 Vector<Integer> colaTamanos;      //Almacena los tamanos de la cola en cada iteracion para poder calcular los promedios
 Vector<Double>  colaNacimientos;  //Almacena los tiempos de nacimiento de cada mensaje en el sistema
 Vector<Double>  colaMuertes;      //Almacena los tiempos de muerte de cada mensaje en el sistema
 
 // utilidades
 Random       rn;
 NumberFormat f; 
 Scanner      scanner;

 public Simulacion(int timer, int relojMax, boolean modoLento){
  this.timer = timer;
  this.maxReloj = relojMax;
  this.modoLento = modoLento;

  rn = new Random();
  f = new DecimalFormat("#0.00");
  scanner = new Scanner(System.in);

  colaMensaje     = new Vector<Mensaje>();
  colaTimers      = new Vector<Timer>();
  colaFrames      = new Vector<Frame>();
  framesRecibidos = new Vector<Integer>();
  colaTamanos     = new Vector<Integer>();
  colaNacimientos = new Vector<Double>();
  colaMuertes     = new Vector<Double>(); 

 }

 public void correrSimulacion(){
  lma  = 0;
  lfb  = INFINITO;
  la   = INFINITO;
  lb   = INFINITO;
  lacka  = INFINITO;
  svt  = INFINITO;

  EA = false;
  EB = false;

  secuenciaMensaje    = 0;
  frameQueEspero      = 0;
  tamanoPromedioColaA = 0;
  tiempoPromedioMsj   = 0;

  ultimoAckRecibido = -1;
  ultimoAckEnviado  = -1;

  mensajesEnviados = 0;
  tiemposTransmision = 0;

  System.out.println("Comienza simulacion.\nTiempo\tInfo");
  do{
    
   EVENTOS e = evento_minimo();
   switch(e){
    case LMA:
    llegaMensajeA();
    break;
    case LFB:
    llegaFrameB();
    break;
    case LA:
    seLiberaA();
    break;
    case LB:
    seLiberaB();
    break;
    case SVT:
    seVenceTimer();
    break;
    case LACKA:
    llegaACKA();
    break;
    default:
    break;
   }

   // imprimir mas cosas aqui
   System.out.println("");

   System.out.println("\tLongitud de la cola de A: "+colaMensaje.size());
   System.out.print("\tCola de A: ");
   for(int i = 0, s=colaMensaje.size(); i<s && i<20;i++){
    System.out.print(colaMensaje.elementAt(i).nSecuencia + " ");
   }
   System.out.println("");

   System.out.println("\tFrames correctamente recibidos por B: "+framesRecibidos.size());
   System.out.print("\tUltimos frames recibidos por B: ");
   for(int i = 0, j = framesRecibidos.size()-1; i<20&&j>0;i++){
    System.out.print(framesRecibidos.elementAt(j) + " ");
    j--;
   }

   System.out.println("");

   if(ultimoAckEnviado>-1)
    System.out.println("\tUltimo ACK enviado: "+ultimoAckEnviado);

  if(ultimoAckRecibido>-1)
    System.out.println("\tUltimo ACK recibido: "+ultimoAckRecibido);

   System.out.println("\n");

   if(modoLento){
    try {
     Thread.sleep(1000);
    } catch(InterruptedException ex) {
    Thread.currentThread().interrupt();
    }
   }

  }while(reloj < maxReloj);
  
  for(int i=0, s=colaTamanos.size(); i<s; i++){
    tamanoPromedioColaA += colaTamanos.elementAt(i);
  }

  tamanoPromedioColaA = tamanoPromedioColaA/colaTamanos.size();
  System.out.println("Tamano promedio de la cola A: "+ f.format(tamanoPromedioColaA));

  //Tiempo promedio de permanencia de mensajes en el sistema
   for(int i = 0, s=colaMuertes.size(); i<s; i++){
     tiempoPromedioMsj += (colaMuertes.elementAt(i) - colaNacimientos.elementAt(i));
   }
   tiempoPromedioMsj = tiempoPromedioMsj/ mensajesEnviados;

   System.out.println("Tiempo promedio de permanencia de mensajes en el sistema: " + f.format(tiempoPromedioMsj));

   double tiempoPromTrans = tiemposTransmision/mensajesEnviados;
   System.out.println("Tiempo promedio de transmision: "+f.format(tiempoPromTrans));

   double tiempoPromServicio = tiempoPromedioMsj - tiempoPromTrans;
   System.out.println("Tiempo promedio de servicio: "+f.format(tiempoPromServicio));

   double eficiencia = tiempoPromTrans/tiempoPromServicio;
   System.out.println("Eficiencia: "+ f.format(eficiencia));
   
  colaMensaje.clear();
  colaFrames.clear();
  colaTimers.clear();
  framesRecibidos.clear();

 }

 public void llegaMensajeA(){
  reloj = lma;
  System.out.println(f.format(reloj) + "\tLlega mensaje a A.");

  Mensaje nMensaje = new Mensaje(secuenciaMensaje, "");
  colaMensaje.add(nMensaje);
  secuenciaMensaje++;
  colaTamanos.add(colaMensaje.size());
  colaNacimientos.add(reloj);
  

  if(colaMensaje.size() <= tamVentana){
   // mensaje dentro de la ventana 
   // xq quedo entro los tamVentana (8) elementos
    

   if(EA == false){
    // proceso desocupado
    double tiempoTrans = estimarTiempoPrepararMensaje();
    la = reloj + tiempoTrans + 1;
    mensajeEnPreparacion = nMensaje;
    EA = true;

    tiemposTransmision += tiempoTrans+1;
   }
  }

  lma = reloj + estimarTiempoNuevoMensaje(); // aqui esta el tiempo de tranferencia
 }

 public double estimarTiempoNuevoMensaje(){
  // normal con media 25 y var 1
  double x, z, r1, r2;  
  r1 = rn.nextDouble() ;
  r2 = rn.nextDouble() ;
  z=(Math.sqrt(-2*Math.log(r1)))*Math.sin(2*Math.PI*r2);
  x=1*z+25; //x = varianza*z+media
  return x;
 }

 public double estimarTiempoPrepararMensaje(){
  // exponenencial con media 2
  int lambda = 2;
  double x, r;
  r = rn.nextDouble();
  x = ((-Math.log(1-r))/lambda);
  return x;
 }

 public void seLiberaA(){
  reloj = la;
  System.out.println(f.format(reloj) + "\tSe libera A.");
  mensajeEnPreparacion.necesitaEnviarse = false;
  boolean sePierde; 
  boolean vaConError;

  //Calculo de probabilidades
  int r1 = (int)(rn.nextDouble()*100);
  sePierde  = (r1<rangoFrameSePierda)?true:false;
  
  if(!sePierde){
   // frame no se pierde
   double r2 = rn.nextDouble()*100;
   vaConError = (r2< rangoFrameVieneConError);

   if(vaConError)
   System.out.println("El frame id=" + mensajeEnPreparacion.nSecuencia + " va con error.");

   Frame nuevoFrame = new Frame(mensajeEnPreparacion.nSecuencia, vaConError, mensajeEnPreparacion.data);

   colaFrames.add(nuevoFrame);

   lfb = reloj + 1; 

   mensajesEnviados++;
   tiemposTransmision++; // segundo de propagacion

  }else{
   System.out.println("\tEl frame id=" + mensajeEnPreparacion.nSecuencia + " se va a perder");
  }

  double timer = reloj + this.timer;
  if(svt == INFINITO){
   svt = timer;
   secuenciaPrimerSVT = mensajeEnPreparacion.nSecuencia;
  }else{
   colaTimers.add(new Timer(timer, mensajeEnPreparacion.nSecuencia));
  }

  // hay otro que deba enviar
  Mensaje proxMensaje = buscarMensajeParaEnviar();
  if(proxMensaje != null){
   double tiempoTrans = estimarTiempoPrepararMensaje();
   la = reloj + tiempoTrans + 1;
   mensajeEnPreparacion = proxMensaje;

   tiemposTransmision += tiempoTrans+1;
  }else{
   EA = false;
   la = INFINITO;
  }

 }

 public Mensaje buscarMensajeParaEnviar(){
  Mensaje m = null;
  for(int i = 0, s = colaMensaje.size(); i<s && i<tamVentana && m == null; i++){
   if(colaMensaje.elementAt(i).necesitaEnviarse)
    m = colaMensaje.elementAt(i);
  }
  return m;
 }

 public void llegaFrameB(){
  reloj = lfb;
  System.out.println(f.format(reloj) + "\tLlega Frame a B.");
  if(EB == false){
   lb = reloj + estimarTiempoRevisaFrame() + 0.25;
   EB = true;
  }
  lfb = INFINITO;
 }

 public double estimarTiempoRevisaFrame(){
  // f(x)=2x/5 2<=x<=3
  double x, r;
  r = rn.nextDouble();
  x = Math.sqrt(5*r+4);  //x = (5r+4)ˆ1/2
  return x;
 }

 public void seLiberaB(){
  reloj = lb;
  System.out.println(f.format(reloj) + "\tSe libera B.");
  Frame frame = colaFrames.elementAt(0);

  if(frame.estaDanado){
   // viene con error
   // enviar ack de la secuencia de este
   System.out.println("\tEl frame "+frame.nSecuencia+" viene danado.");
   nAck = frame.nSecuencia;
  }else{
   // si es el frame que estoy esperando
   if(frame.nSecuencia == frameQueEspero){
    // enviar ack siguiente
    System.out.println("\tEl frame "+frame.nSecuencia+" es el que espero.");
    framesRecibidos.add(frame.nSecuencia);
    nAck = frame.nSecuencia + 1;
    frameQueEspero++;

   }else{
    // enviar ack de este
    System.out.println("\tEl frame "+frame.nSecuencia+" no es el que espero.");
    nAck = frameQueEspero;
   }
  }

  double r = rn.nextDouble()*100;
  boolean ackSePierde = (r < rangoACKSePierde);

  if(!ackSePierde){
   ultimoAckEnviado = nAck;
   lacka = reloj + 1;
  }else{
   System.out.println("\tEl ack nAck=" + nAck + " se va a perder.");
  }

  colaFrames.remove(0);
  if(colaFrames.size() > 0){
   lb = reloj + estimarTiempoRevisaFrame() + 0.25;
  }else{
   EB = false;
   lb = INFINITO;
  }
 }

 public void seVenceTimer(){
  reloj = svt;
  System.out.println(f.format(reloj) + "\tSe vence timer.");

  for(int i = 0, s = colaMensaje.size(); i < tamVentana && i < s; i++){
   Mensaje m = colaMensaje.elementAt(i);
   if(m.nSecuencia >= secuenciaPrimerSVT){
    System.out.println("\tDebo volver a enviar el id="+m.nSecuencia);
    m.necesitaEnviarse = true;
   }
  }
  colaTimers.clear();

  Mensaje proxMensaje = buscarMensajeParaEnviar();
  if(proxMensaje != null){
   if(EA == false){
    System.out.println("\tPreparo el reenvio de id="+proxMensaje.nSecuencia);
    double tiempoTrans = estimarTiempoPrepararMensaje();
    la = reloj + tiempoTrans + 1;
    mensajeEnPreparacion = proxMensaje;

    tiemposTransmision += tiempoTrans+1;
   }
  }
  svt = INFINITO;

 }

 public void llegaACKA(){
  reloj = lacka;
  System.out.println(f.format(reloj) + "\tLlega ACK a A.");
  ultimoAckRecibido = nAck;

  boolean fin = false;
  while(!fin && !colaMensaje.isEmpty()){
   Mensaje m = colaMensaje.elementAt(0);
   if(!colaMensaje.isEmpty() && m.nSecuencia < nAck){
    System.out.println("\tElimino el timer y saco de la cola al id="+m.nSecuencia);
    eliminarTimer(m.nSecuencia);
    colaMensaje.remove(0);
    colaTamanos.add(colaMensaje.size());
    colaMuertes.add(reloj);
   }else{
    fin = true;
   }
  }

  for(int i = 0, s=colaMensaje.size(); i<s; i++){
   colaMensaje.elementAt(i).necesitaEnviarse = true;
  }

  Mensaje proxMensaje = buscarMensajeParaEnviar();
  if(proxMensaje != null){
   if(EA == false){
    double tiempoTrans = estimarTiempoPrepararMensaje();
    la = reloj + tiempoTrans + 1;
    mensajeEnPreparacion = proxMensaje;

    tiemposTransmision += tiempoTrans+1;
   }
  }
  lacka = INFINITO;
 }

 public void eliminarTimer(int s){
  if(secuenciaPrimerSVT == s){
   if(colaTimers.isEmpty()){
    svt = INFINITO;
   }else{
    svt = colaTimers.elementAt(0).hora;
    secuenciaPrimerSVT = colaTimers.elementAt(0).nSecuencia;
   }
  }else{
   for(int i=0, size=colaTimers.size(); i<size; i++){
    if(colaTimers.elementAt(i).nSecuencia == s){
     colaTimers.remove(i);
    }
   }
  }
 }

 public EVENTOS evento_minimo(){
  if(lma < lfb && lma < la && lma < lb && lma < lacka && lma < svt){
   return EVENTOS.LMA;
  }
  if(lfb < lma && lfb < la && lfb < lb && lfb < lacka && lfb < svt){
   return EVENTOS.LFB;
  }
  if(la < lfb && la < lma && la < lb && la < lacka && la < svt){
   return EVENTOS.LA;
  }
  if(lb < lfb && lb < la && lb < lma && lb < lacka && lb < svt){
   return EVENTOS.LB;
  }
  if(lacka < lfb && lacka < la && lacka < lb && lacka < lma && lacka < svt){
   return EVENTOS.LACKA;
  }
  if(svt < lfb && svt < la && svt < lb && svt < lacka && svt < lma){
   return EVENTOS.SVT;
  }
  return EVENTOS.LMA;
 }

}
