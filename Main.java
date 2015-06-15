import java.util.Vector;
import java.util.Scanner;
import java.util.Random;
import java.lang.Math;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class Main{
	final int tamVentana = 8;
	final int maxReloj = 60;
	final int TIMER = 10; // tiempo del timer

	double lma, lfb, la, lb, svt, lacka, reloj;
	public enum EVENTOS {LMA, LFB, LA, LB, SVT, LACKA};

	int secuenciaMensaje;
	Mensaje mensajeEnPreparacion;
	Vector<Mensaje> colaMensaje;

	int secuenciaPrimerSVT;
	Vector<Timer>   colaTimers;

	int nAck;
	int secuenciaFrame;
	int frameQueEspero;
	Vector<Frame>   colaFrames;

	boolean EA, EB;
	Random rn = new Random();
	NumberFormat f; 

	public Main(){
		lma 	= 0;
		reloj 	= 0;
		lfb 	= Integer.MAX_VALUE;
		la 		= Integer.MAX_VALUE;
		lb 		= Integer.MAX_VALUE;
		lacka 	= Integer.MAX_VALUE;
		svt 	= Integer.MAX_VALUE;
		
		colaMensaje = new Vector<Mensaje>();
		colaTimers  = new Vector<Timer>();
		colaFrames  = new Vector<Frame>();

		EA = false;
		EB = false;

		secuenciaMensaje = 0;
		secuenciaFrame = 0;
		frameQueEspero = 0;

		f = new DecimalFormat("#0.00");

		System.out.println("Comienza simulacion.");
		while(reloj < maxReloj){
			
			Scanner scaner = new Scanner(System.in);
			String s = scaner.nextLine();
			
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
		}

		
	}

	public void llegaMensajeA(){
		reloj = lma;
		System.out.println(f.format(reloj) + " Llega mensaje a A. id="+secuenciaMensaje);

		Mensaje nMensaje = new Mensaje(secuenciaMensaje, "");
		colaMensaje.add(nMensaje);
		secuenciaMensaje++;

		if(colaMensaje.size() <= tamVentana){
			// mensaje dentro de la ventana 
			// xq quedo entro los tamVentana (8) elementos

			if(EA == false){
				// proceso desocupado
				la = reloj + estimarTiempoPrepararMensaje() + 1;
				mensajeEnPreparacion = nMensaje;
				EA = true;
			}
		}

		lma = reloj + estimarTiempoNuevoMensaje();
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
		System.out.println(f.format(reloj) + " Se libera A. id="+mensajeEnPreparacion.nSecuencia);
		mensajeEnPreparacion.necesitaEnviarse = false;
		boolean sePierde; 
		boolean vaConError;

		//Calculo de probabilidades
		int r1 = (int)(rn.nextDouble()*100);
		sePierde  = (r1<50)?true:false;

		if(sePierde)
			System.out.println("El frame id=" + mensajeEnPreparacion.nSecuencia + " se va a perder");
		
		if(!sePierde){
			// frame no se pierde

			// calcular la prob de error
			//boolean vaConError = false;
			double r2 = rn.nextDouble()*100;
			vaConError = (r2< 5);

			if(vaConError)
			System.out.println("El frame id=" + mensajeEnPreparacion.nSecuencia + " va con error.");

			Frame nuevoFrame = new Frame(secuenciaFrame, vaConError, mensajeEnPreparacion.data);
			secuenciaFrame++;
			colaFrames.add(nuevoFrame);

			lfb = reloj + 1;
		}

		double timer = reloj + TIMER;
		if(svt == Integer.MAX_VALUE){
			svt = timer;
			secuenciaPrimerSVT = mensajeEnPreparacion.nSecuencia;
		}else{
			colaTimers.add(new Timer(timer, mensajeEnPreparacion.nSecuencia));
		}

		// hay otro que deba enviar
		Mensaje proxMensaje = buscarMensajeParaEnviar();
		if(proxMensaje != null){
			la = reloj + estimarTiempoPrepararMensaje() + 1;
			mensajeEnPreparacion = proxMensaje;
		}else{
			EA = false;
			la = Integer.MAX_VALUE;
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
		System.out.println(f.format(reloj) + " Llega Frame a B. id="+colaFrames.elementAt(0).nSecuencia);
		if(EB == false){
			lb = reloj + estimarTiempoRevisaFrame() + 0.25;
			EB = true;
		}
		lfb = Integer.MAX_VALUE;
	}

	public double estimarTiempoRevisaFrame(){
		// f(x)=2x/5 2<=x<=3
		double x, r;
		r = rn.nextDouble();
		x = Math.sqrt(5*r+4);		//x = (5r+4)ˆ1/2
		return x;
	}

	public void seLiberaB(){
		reloj = lb;
		System.out.println(f.format(reloj) + " Se libera B. id="+colaFrames.elementAt(0).nSecuencia);
		Frame frame = colaFrames.elementAt(0);

		if(frame.estaDanado){
			// viene con error
			// enviar ack de la secuencia de este
			nAck = frame.nSecuencia;
		}else{
			// si es el frame que estoy esperando
			if(frame.nSecuencia == frameQueEspero){
				// enviar ack siguiente
				nAck = frame.nSecuencia + 1;
				frameQueEspero++;

			}else{
				// enviar ack de este
				nAck = frame.nSecuencia;
			}
		}

		double r = rn.nextDouble()*100;
		boolean ackSePierde = (r < 15);

		if(ackSePierde)
			System.out.println("El ack nAck=" + nAck + " se va a perder.");

		if(!ackSePierde){
			lacka = reloj + 1;
		}

		colaFrames.remove(0);
		if(colaFrames.size() > 0){
			lb = reloj + estimarTiempoRevisaFrame() + 0.25;
		}else{
			EB = false;
			lb = Integer.MAX_VALUE;
		}
	}

	public void seVenceTimer(){
		reloj = svt;
		System.out.println(f.format(reloj) + " Se vence timer. id="+secuenciaPrimerSVT);

		for(int i = 0, s = colaMensaje.size(); i < tamVentana && i < s; i++){
			Mensaje m = colaMensaje.elementAt(i);
			if(m.nSecuencia >= secuenciaPrimerSVT){
				m.necesitaEnviarse = true;
			}
		}
		colaTimers.clear();

		Mensaje proxMensaje = buscarMensajeParaEnviar();
		if(proxMensaje != null){
			if(EA == false){
				la = reloj + estimarTiempoPrepararMensaje() + 1;
				mensajeEnPreparacion = proxMensaje;
			}
		}
		svt = Integer.MAX_VALUE;

	}

	public void llegaACKA(){
		reloj = lacka;
		System.out.println(f.format(reloj) + " Llega ACK a A. ack="+nAck);

		boolean fin = false;
		while(!fin && !colaMensaje.isEmpty()){
			Mensaje m = colaMensaje.elementAt(0);
			if(!colaMensaje.isEmpty() && m.nSecuencia < nAck){
				eliminarTimer(m.nSecuencia);
				colaMensaje.remove(0);
			}else{
				fin = true;
			}
		}

		Mensaje proxMensaje = buscarMensajeParaEnviar();
		if(proxMensaje != null){
			if(EA == false){
				la = reloj + estimarTiempoPrepararMensaje() + 1;
				mensajeEnPreparacion = proxMensaje;
			}
		}
		lacka = Integer.MAX_VALUE;
	}

	public void eliminarTimer(int s){
		if(secuenciaPrimerSVT == s){
			if(colaTimers.isEmpty()){
				svt = Integer.MAX_VALUE;
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
	

	public static void main(String args[]){
		Main m = new Main();
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

class Mensaje{
	public Mensaje(int n, String d){
		this.nSecuencia = n;
		this.necesitaEnviarse = true;
		this.data = d;
	}
	public int nSecuencia;
	public boolean necesitaEnviarse;
	public String data;
}

class Frame{
	public Frame(int n, boolean e, String d){
		this.nSecuencia = n;
		this.estaDanado = e;
		this.data = d;
	}
	public int nSecuencia;
	public boolean estaDanado;
	public String data;
}

class Timer{
	public Timer(double h, int n){
		this.hora = h;
		this.nSecuencia = n;
	}
	public int nSecuencia;
	public double hora;
}

