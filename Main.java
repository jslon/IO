import java.util.Vector;
import java.util.Random;
import java.lang.Math;

public class Main{
	final int tamVentana = 8;
	final int maxReloj = 300;
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

		System.out.println("Comienza simulacion.");
		while(reloj < maxReloj){
			EVENTOS e = evento_minimo();
			switch(e){
				case LMA:
				llegaMensajeA();
				break;
				case LFB:
				break;
				case LA:
				break;
				case LB:
				break;
				case SVT:
				break;
				case LACKA:
				break;
				default:
				break;
			}
		}

		
	}

	public void llegaMensajeA(){
		reloj = lma;
		System.out.println(reloj + " Llega mensaje a A.");

		Mensaje nMensaje = new Mensaje(secuenciaMensaje, "");
		colaMensaje.add(nMensaje);
		secuenciaMensaje++;

		if(colaMensaje.size() <= tamVentana){
			// mensaje dentro de la ventana 
			// xq quedo entro los tamVentana (8) elementos

			if(EA == false){
				// proceso desocupado
				//la = reloj + estimarTiempoPrepararMensaje() + 1;
				//mensajeEnPreparacion = nMensaje;
				//EA = true;
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
		mensajeEnPreparacion.necesitaEnviarse = false;

		boolean sePierde = false; // prob que frame se pierde

		if(!sePierde){
			// frame no se pierde

			// calcular la prob de error
			boolean vaConError = false;

			Frame nuevoFrame = new Frame(secuenciaFrame, vaConError, mensajeEnPreparacion.data);
			secuenciaFrame++;
			colaFrames.add(nuevoFrame);

			lfb = reloj+1;
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
		if(EB == false){
			lb = reloj + estimarTiempoRevisaFrame() + 0.25;
			EB = true;
		}
	}

	public int estimarTiempoRevisaFrame(){
		// f(x)=2x/5 2<=x<=3
		return 0;
	}

	public void seLiberaB(){
		reloj = lb;
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

		boolean ackSePierde = false; // calcular prob

		if(!ackSePierde){
			lacka = reloj + 1;
		}

		if(colaFrames.size() > 0){
			lb = reloj + estimarTiempoRevisaFrame() + 0.25;
		}else{
			EB = false;
			lb = Integer.MAX_VALUE;
		}
	}

	public void seVenceTimer(){

		for(int i = 0, s = colaMensaje.size(); i < tamVentana && i < s; i++){
			Mensaje m = colaMensaje.elementAt(i);
			if(m.nSecuencia >= secuenciaPrimerSVT){
				m.necesitaEnviarse = true;
			}
		}
		colaTimers.clear();

		if(EA == false){
			la = reloj + estimarTiempoPrepararMensaje() + 1;
			EA = true;
		}

	}

	public void llegaACKA(){
		reloj = lacka;
		boolean fin = false;
		while(!fin){
			Mensaje m = colaMensaje.elementAt(0);
			if(!colaMensaje.isEmpty() && m.nSecuencia < nAck){
				eliminarTimer(m.nSecuencia);
				colaMensaje.remove(0);
			}else{
				fin = true;
			}
		}
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

