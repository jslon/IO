import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.lang.Math;
import java.util.Vector;

public class Estadistica{
  
  private NumberFormat f; 
  private double tamPromCola, tiempoPromPerm, tiempoPromTrans, tiempoPromServ, eficiencia;
  private double confianzaA, confianzaB;
  
  public Estadistica(double tamPromCola, double tiempoPromPerm){
    f = new DecimalFormat("#0.00");
    this.tiempoPromPerm = tiempoPromPerm;
    this.tamPromCola = tamPromCola;

    this.confianzaA = 0.0;
    this.confianzaB = 0.0;
  }

  public Estadistica(double tamPromCola, double tiempoPromPerm, double confianzaA, double confianzaB){
    f = new DecimalFormat("#0.00");
    this.tiempoPromPerm = tiempoPromPerm;
    this.tamPromCola = tamPromCola;

    this.confianzaA = confianzaA;
    this.confianzaB = confianzaB;
  }
  
  public void imprimirEstadistica(Archivo a){
    String aux = "Tamano promedio de la cola A: "+ f.format(this.tamPromCola);
    System.out.println(aux);
    a.agregarEstadistica(aux);

    aux = "Tiempo promedio de permanencia de mensajes en el sistema: " + f.format(this.tiempoPromPerm);
    System.out.println(aux);
    a.agregarEstadistica(aux);

    if(this.confianzaA != 0.0){
      aux = "Intervalo de Confianza: ["+ f.format(this.confianzaA) + ", "+ f.format(this.confianzaB) + "]";
      System.out.println(aux);
      a.agregarEstadistica(aux);
    }
    
  }

  public Estadistica calcularPromedioEstadisticas(Vector<Estadistica> v){
    int cantSim = v.size();

    double tamPromCola = 0.0;
    double tiempoPromPerm = 0.0;

    for(int i = 0; i<cantSim;i++){
      Estadistica e = v.elementAt(i);
      tamPromCola     += e.tamPromCola;
      tiempoPromPerm  += e.tiempoPromPerm;
    }
    tamPromCola     /= cantSim;
    tiempoPromPerm  /= cantSim;

    double varMuestral = 0.0;
    for(int i = 0; i<cantSim;i++){
      Estadistica e = v.elementAt(i);
      varMuestral += Math.pow((e.tiempoPromPerm - tiempoPromPerm), 2);
    }
    varMuestral /= (cantSim-1);

    double intervaloConfianzaA = tiempoPromPerm - 2.26*Math.sqrt(varMuestral/cantSim);
    double intervaloConfianzaB = tiempoPromPerm + 2.26*Math.sqrt(varMuestral/cantSim);

    return new Estadistica(tamPromCola, tiempoPromPerm, intervaloConfianzaA, intervaloConfianzaB);
  }

}