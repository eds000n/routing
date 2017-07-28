package sinalgo.models.EnergyModel.simple;

public class Config {
        
        public final static Float ENERG_SLEEP = 0f;
        public final static Float ENERG_SENSOR = 0f;
//        public final static Float ENERG_TRANSMISSAO = 0.48375f;
//        public final static Float ENERG_TRANSMISSAO = 0.0801f;   //0.0801 joules per second  //  80.1 mw
        public final static Float ENERG_TRANSMISSAO = 0.0165f;	//0.0495W = 0.0165A*3V
//        public final static Float ENERG_RECEPCAO = 0.0222f;//joules per second;
        public final static Float ENERG_RECEPCAO = 0.0096f;		//0.0288W = 0.0096A*3V
        public final static Float ENERG_ESCUTA = 0f;
        public final static Float ENERG_IDS = 0f;
}
