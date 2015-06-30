import java.util.Vector;
import java.util.Scanner;
import java.util.Random;
import java.lang.Math;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class Simulacion{

  // variables determinadas por constructor
  int     maxReloj;
  int     timer;
  boolean modoLento;

  // constantes
  final int INFINITO = Integer.MAX_VALUE;

  // tiempos de eventos y enumeraciones para identificarlas
  double lma, lfb, la, lb, reloj;
  enum EVENTOS {LMA, LFB, LA, LB};

  // estados de los procesos que preparan frames
  // y revisan frames
  boolean EA, EB;

  int             secuenciaMensaje;
  Mensaje         mensajeEnPreparacion;
  Vector<Mensaje> colaMensaje;

  int             frameQueEspero;
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
  Archivo      archivoRegistros;

  public Simulacion(int timer, int relojMax, boolean modoLento, Archivo archivoRegistros){
    this.timer = timer;
    this.maxReloj = relojMax;
    this.modoLento = modoLento;
    this.archivoRegistros = archivoRegistros;

    rn = new Random();
    f = new DecimalFormat("#0.00");
    scanner = new Scanner(System.in);

    colaMensaje     = new Vector<Mensaje>();
    colaFrames      = new Vector<Frame>();
    framesRecibidos = new Vector<Integer>();
    colaTamanos     = new Vector<Integer>();
    colaNacimientos = new Vector<Double>();
    colaMuertes     = new Vector<Double>();

  }

  public Estadistica correrSimulacion(){
    lma  = 0;
    lfb  = INFINITO;
    la   = INFINITO;
    lb   = INFINITO;

    EA = false;
    EB = false;

    secuenciaMensaje    = 0;
    frameQueEspero      = 0;

    mensajesEnviados = 0;
    tiemposTransmision = 0;

    this.imprimir("Comienza simulacion.\nTiempo\tInfo");
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
        default:
          break;
      }

      // imprimir mas cosas aqui
      this.imprimir("");

      this.imprimir("\tLongitud de la cola de A: "+colaMensaje.size());
      String aux = "\tCola de A: ";
      for(int i = 0, s=colaMensaje.size(); i<s && i<20;i++){
        aux += colaMensaje.elementAt(i).nSecuencia + " ";
      }
      this.imprimir(aux);

      this.imprimir("\tFrames correctamente recibidos por B: "+framesRecibidos.size());

      aux = "\tUltimos frames recibidos por B: ";
      for(int i = 0, j = framesRecibidos.size()-1; i<20&&j>0;i++){
        aux += framesRecibidos.elementAt(j) + " ";
        j--;
      }

      this.imprimir(aux);
      this.imprimir("\n");

      if(modoLento){
        try {
          Thread.sleep(1000);
        } catch(InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }

    }while(reloj < maxReloj);

    double tamanoPromedioColaA = 0.0;
    for(int i=0, s=colaTamanos.size(); i<s; i++){
      tamanoPromedioColaA += colaTamanos.elementAt(i);
    }
    tamanoPromedioColaA = tamanoPromedioColaA/colaTamanos.size();

    double tiempoPromedioMsj = 0.0;
    //Tiempo promedio de permanencia de mensajes en el sistema
    for(int i = 0, s=colaMuertes.size(); i<s; i++){
      tiempoPromedioMsj += (colaMuertes.elementAt(i) - colaNacimientos.elementAt(i));
    }
    tiempoPromedioMsj = tiempoPromedioMsj/ mensajesEnviados;
    double tiempoPromTrans = tiemposTransmision/mensajesEnviados;
    double tiempoPromServicio = tiempoPromedioMsj - tiempoPromTrans;
    double eficiencia = tiempoPromTrans/tiempoPromServicio;

    Estadistica e = new Estadistica(tamanoPromedioColaA, tiempoPromedioMsj, tiempoPromTrans, tiempoPromServicio, eficiencia);

    colaMensaje.clear();
    colaFrames.clear();
    colaMuertes.clear();
    colaNacimientos.clear();
    framesRecibidos.clear();

    return e;

  }

  public void llegaMensajeA(){
    reloj = lma;
    this.imprimir(f.format(reloj) + "\tLlega mensaje a A.");

    Mensaje nMensaje = new Mensaje(secuenciaMensaje, "");
    colaMensaje.add(nMensaje);
    secuenciaMensaje++;
    colaTamanos.add(colaMensaje.size());
    colaNacimientos.add(reloj);

    if(EA == false){
      // proceso desocupado
      double tiempoTrans = estimarTiempoPrepararMensaje();
      la = reloj + tiempoTrans + 1;
      mensajeEnPreparacion = nMensaje;
      EA = true;

      tiemposTransmision += tiempoTrans+1;
    }


    lma = reloj + estimarTiempoNuevoMensaje(); // aqui esta el tiempo de tranferencia
  }

  public double estimarTiempoNuevoMensaje(){
    // normal con media 25 y var 1
    double x, z, r1, r2;
    r1 = rn.nextDouble() ;
    r2 = rn.nextDouble() ;
    z=(Math.sqrt(-2*Math.log(r1)))*Math.sin(2*Math.PI*r2);
    x=0.001*z+2; //x = varianza*z+media
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
    this.imprimir(f.format(reloj) + "\tSe libera A.");
    mensajeEnPreparacion.necesitaEnviarse = false;

    Frame nuevoFrame = new Frame(mensajeEnPreparacion.nSecuencia, mensajeEnPreparacion.data);

    colaFrames.add(nuevoFrame);

    lfb = reloj + 1;

    mensajesEnviados++;
    tiemposTransmision++; // segundo de propagacion

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
    for(int i = 0, s = colaMensaje.size(); i<s && m == null; i++){
      if(colaMensaje.elementAt(i).necesitaEnviarse)
        m = colaMensaje.elementAt(i);
    }
    return m;
  }

  public void llegaFrameB(){
    reloj = lfb;
    this.imprimir(f.format(reloj) + "\tLlega Frame a B.");
    if(EB == false){
      lb = reloj + estimarTiempoRevisaFrame();
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
    this.imprimir(f.format(reloj) + "\tSe libera B.");
    Frame frame = colaFrames.elementAt(0);

    colaFrames.remove(0);
    if(colaFrames.size() > 0){
      lb = reloj + estimarTiempoRevisaFrame();
    }else{
      EB = false;
      lb = INFINITO;
    }
  }

  public void imprimir(String s){
    System.out.println(s);
    this.archivoRegistros.agregarSimulacion(s);
  }

  public EVENTOS evento_minimo(){
    if(lma < lfb && lma < la && lma < lb){
      return EVENTOS.LMA;
    }
    if(lfb < lma && lfb < la && lfb < lb){
      return EVENTOS.LFB;
    }
    if(la < lfb && la < lma && la < lb){
      return EVENTOS.LA;
    }
    if(lb < lfb && lb < la && lb < lma){
      return EVENTOS.LB;
    }
    return EVENTOS.LMA;
  }

}
