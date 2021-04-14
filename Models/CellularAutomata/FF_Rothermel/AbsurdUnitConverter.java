package CellularAutomata.FF_Rothermel;

public class AbsurdUnitConverter {

    // Convert chains per hour to feet per minute
    public static double ch_h_to_ft_min(double ch_h){
        return ch_h * 1.1;
    }

    // Convert tons per acre to pounds per feet²
    public static double t_ac_to_lb_ftsqrd(double t_ac){return t_ac * 0.0459136823;}
}

